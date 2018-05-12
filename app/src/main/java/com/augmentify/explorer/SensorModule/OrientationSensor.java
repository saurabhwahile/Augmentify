package com.augmentify.explorer.SensorModule;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.augmentify.explorer.Explorer;

public class OrientationSensor implements SensorEventListener
{
	private SensorManager mSensorManager = null;

	// angular speeds from gyro
	public float[] gyro = new float[3];

	// rotation matrix from gyro data
	private float[] gyroMatrix = new float[9];

	// orientation angles from gyro matrix
	private float[] gyroOrientation = new float[3];

	// magnetic field vector
	private float[] magnet = new float[3];

	// accelerometer vector
	private float[] accel = new float[3];

	// orientation vector
	private float[] orient = new float[3];

	// orientation angles from accel and magnet
	private float[] accMagOrientation = new float[3];

	// final orientation angles from sensor fusion
	private float[] fusedOrientation = new float[3];

	// accelerometer and magnetometer based rotation matrix
	private float[] rotationMatrix = new float[16];

	// Return Angles
	private float[] returnAngles = new float[3];

	public static final float EPSILON = 0.000000001f;
	private static final float NS2S = 1.0f / 1000000000.0f;
	private float timestamp;
	private boolean initState = true;

	public static final int TIME_CONSTANT = 50;
	public static final float FILTER_COEFFICIENT = 0.98f;//0.98f;
	private Timer fuseTimer = new Timer();

	DecimalFormat d = new DecimalFormat("#.##");

	Sensor accelerometer, gyroscoop, magnetometer, orientation;

	public OrientationSensor()
	{

		gyroOrientation[0] = 0.0f;
		gyroOrientation[1] = 0.0f;
		gyroOrientation[2] = 0.0f;

		// initialise gyroMatrix with identity matrix
		gyroMatrix[0] = 1.0f;
		gyroMatrix[1] = 0.0f;
		gyroMatrix[2] = 0.0f;
		gyroMatrix[3] = 0.0f;
		gyroMatrix[4] = 1.0f;
		gyroMatrix[5] = 0.0f;
		gyroMatrix[6] = 0.0f;
		gyroMatrix[7] = 0.0f;
		gyroMatrix[8] = 1.0f;

		// get sensorManager and sensors
		mSensorManager = (SensorManager) Explorer.getContext()
				.getSystemService(Context.SENSOR_SERVICE);

		List<Sensor> mySensors = mSensorManager
				.getSensorList(Sensor.TYPE_ORIENTATION);
		if (mySensors.size() > 0)
			orientation = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ORIENTATION);

		mySensors = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
		if (mySensors.size() > 0)
			accelerometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

		mySensors = mSensorManager.getSensorList(Sensor.TYPE_GYROSCOPE);
		if (mySensors.size() > 0)
			gyroscoop = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

		mySensors = mSensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD);
		if (mySensors.size() > 0)
			magnetometer = mSensorManager
					.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

		// Registering Listeners
		onResume();

		// wait for one second until gyroscope and magnetometer/accelerometer
		// data is initialised then schedule the complementary filter task
		
		//fuseTimer.scheduleAtFixedRate(new calculateFusedOrientationTask(),
		//		1000, TIME_CONSTANT);

		// d.setRoundingMode(RoundingMode.HALF_UP);
		d.setMaximumFractionDigits(2);
		d.setMinimumFractionDigits(2);
	}

	protected void onStop()
	{
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		mSensorManager.unregisterListener(this);
		// The TimerTask is still running
	}

	public void onDestroy()
	{
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		mSensorManager.unregisterListener(this);
		// Remove the TimerTask from the list
		fuseTimer.cancel();
	}

	public void onPause()
	{
		// unregister sensor listeners to prevent the activity from draining the
		// device's battery.
		mSensorManager.unregisterListener(this);
	}

	public void onResume()
	{
		// start the sensor listeners
		if (accelerometer != null)
			mSensorManager.registerListener(this, accelerometer,
					SensorManager.SENSOR_DELAY_UI);
		if (gyroscoop != null)
			mSensorManager.registerListener(this, gyroscoop,
					SensorManager.SENSOR_DELAY_UI);
		if (magnetometer != null)
			mSensorManager.registerListener(this, magnetometer,
					SensorManager.SENSOR_DELAY_UI);
		if (orientation != null)
			mSensorManager.registerListener(this, orientation,
					SensorManager.SENSOR_DELAY_UI);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy)
	{
	}

	@Override
	public void onSensorChanged(SensorEvent event)
	{
		synchronized (gyroOrientation)
		{
			switch (event.sensor.getType())
			{
			case Sensor.TYPE_ACCELEROMETER:
				// copy new accelerometer data into accel array and calculate
				// orientation
				// Log.v("SensorFusion", "TYPE_ACCELEROMETER");
				System.arraycopy(event.values, 0, accel, 0, 3);
				calculateAccMagOrientation();
				break;

			case Sensor.TYPE_GYROSCOPE:
				// process gyro data
				// Log.v("SensorFusion", "TYPE_GYROSCOPE");
				gyroFunction(event);
				break;

			case Sensor.TYPE_MAGNETIC_FIELD:
				// copy new magnetometer data into magnet array
				// Log.v("SensorFusion", "TYPE_MAGNETIC_FIELD");
				System.arraycopy(event.values, 0, magnet, 0, 3);
				break;

			case Sensor.TYPE_ORIENTATION:
				// copy new orientation data into orient array
				// Log.v("SensorFusion", "TYPE_ORIENTATION");
				orient[0] = (float) clamp180(event.values[0]);
				orient[1] = (float) clamp180(event.values[1]);
				orient[2] = (float) clamp180(event.values[2]);
				break;
			}
			
		}
	}

	private double clamp180(double angle)
	{
		if (Double.isNaN(angle))
		{
			angle = 0.0;
		}
		else if (Double.isInfinite(angle))
		{
			angle = 0.0;
		}
		if (angle > 180.0)
		{
			angle = angle - 360;
			if (angle > 180.0)
			{
				angle = angle - 360.0;
			}
		}
		else if (angle < -180.0)
		{
			angle = angle + 360.0;
			if (angle < -180.0)
			{
				angle = angle + 360.0;
			}
		}
		return angle;
	}

	// calculates orientation angles from accelerometer and magnetometer output
	private void calculateAccMagOrientation()
	{
		if (SensorManager
				.getRotationMatrix(rotationMatrix, null, accel, magnet))
		{
			SensorManager.getOrientation(rotationMatrix, accMagOrientation);
			SensorManager.remapCoordinateSystem(rotationMatrix,
					SensorManager.AXIS_Y, SensorManager.AXIS_MINUS_X,
					rotationMatrix);
		}
	}

	// This function is borrowed from the Android reference at
	// http://developer.android.com/reference/android/hardware/SensorEvent.html#values
	// It calculates a rotation vector from the gyroscope angular speed values.
	private void getRotationVectorFromGyro(float[] gyroValues,
			float[] deltaRotationVector, float timeFactor)
	{
		float[] normValues = new float[3];

		// Calculate the angular speed of the sample
		float omegaMagnitude = (float) Math
				.sqrt(gyroValues[0] * gyroValues[0] + gyroValues[1]
						* gyroValues[1] + gyroValues[2] * gyroValues[2]);

		// Normalize the rotation vector if it's big enough to get the axis
		if (omegaMagnitude > EPSILON)
		{
			normValues[0] = gyroValues[0] / omegaMagnitude;
			normValues[1] = gyroValues[1] / omegaMagnitude;
			normValues[2] = gyroValues[2] / omegaMagnitude;
		}

		// Integrate around this axis with the angular speed by the timestep
		// in order to get a delta rotation from this sample over the timestep
		// We will convert this axis-angle representation of the delta rotation
		// into a quaternion before turning it into the rotation matrix.
		float thetaOverTwo = omegaMagnitude * timeFactor;
		float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
		float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
		deltaRotationVector[0] = sinThetaOverTwo * normValues[0];
		deltaRotationVector[1] = sinThetaOverTwo * normValues[1];
		deltaRotationVector[2] = sinThetaOverTwo * normValues[2];
		deltaRotationVector[3] = cosThetaOverTwo;
	}

	// This function performs the integration of the gyroscope data.
	// It writes the gyroscope based orientation into gyroOrientation.
	public void gyroFunction(SensorEvent event)
	{
		// don't start until first accelerometer/magnetometer orientation has
		// been acquired
		if (accMagOrientation == null)
			return;

		// initialisation of the gyroscope based rotation matrix
		if (initState)
		{
			float[] initMatrix = new float[9];
			initMatrix = getRotationMatrixFromOrientation(accMagOrientation);
			float[] test = new float[3];
			SensorManager.getOrientation(initMatrix, test);
			gyroMatrix = matrixMultiplication(gyroMatrix, initMatrix);
			initState = false;
		}

		// copy the new gyro values into the gyro array
		// convert the raw gyro data into a rotation vector
		float[] deltaVector = new float[4];
		if (timestamp != 0)
		{
			final float dT = (event.timestamp - timestamp) * NS2S;
			System.arraycopy(event.values, 0, gyro, 0, 3);
			getRotationVectorFromGyro(gyro, deltaVector, dT / 2.0f);
		}

		// measurement done, save current time for next interval
		timestamp = event.timestamp;

		// convert rotation vector into rotation matrix
		float[] deltaMatrix = new float[9];
		// SensorManager.getRotationMatrixFromVector(deltaMatrix, deltaVector);
		getRotationMatrixFromVector(deltaMatrix, deltaVector);

		// apply the new rotation interval on the gyroscope based rotation
		// matrix
		gyroMatrix = matrixMultiplication(gyroMatrix, deltaMatrix);

		// get the gyroscope based orientation from the rotation matrix
		SensorManager.getOrientation(gyroMatrix, gyroOrientation);
	}

	//
	private void getRotationMatrixFromVector(float[] R, float[] rotationVector)
	{

		float q0;
		float q1 = rotationVector[0];
		float q2 = rotationVector[1];
		float q3 = rotationVector[2];

		if (rotationVector.length == 4)
		{
			q0 = rotationVector[3];
		}
		else
		{
			q0 = 1 - q1 * q1 - q2 * q2 - q3 * q3;
			q0 = (q0 > 0) ? (float) Math.sqrt(q0) : 0;
		}

		float sq_q1 = 2 * q1 * q1;
		float sq_q2 = 2 * q2 * q2;
		float sq_q3 = 2 * q3 * q3;
		float q1_q2 = 2 * q1 * q2;
		float q3_q0 = 2 * q3 * q0;
		float q1_q3 = 2 * q1 * q3;
		float q2_q0 = 2 * q2 * q0;
		float q2_q3 = 2 * q2 * q3;
		float q1_q0 = 2 * q1 * q0;

		if (R.length == 9)
		{
			R[0] = 1 - sq_q2 - sq_q3;
			R[1] = q1_q2 - q3_q0;
			R[2] = q1_q3 + q2_q0;

			R[3] = q1_q2 + q3_q0;
			R[4] = 1 - sq_q1 - sq_q3;
			R[5] = q2_q3 - q1_q0;

			R[6] = q1_q3 - q2_q0;
			R[7] = q2_q3 + q1_q0;
			R[8] = 1 - sq_q1 - sq_q2;
		}
		else if (R.length == 16)
		{
			R[0] = 1 - sq_q2 - sq_q3;
			R[1] = q1_q2 - q3_q0;
			R[2] = q1_q3 + q2_q0;
			R[3] = 0.0f;

			R[4] = q1_q2 + q3_q0;
			R[5] = 1 - sq_q1 - sq_q3;
			R[6] = q2_q3 - q1_q0;
			R[7] = 0.0f;

			R[8] = q1_q3 - q2_q0;
			R[9] = q2_q3 + q1_q0;
			R[10] = 1 - sq_q1 - sq_q2;
			R[11] = 0.0f;

			R[12] = R[13] = R[14] = 0.0f;
			R[15] = 1.0f;
		}
	}

	private float[] getRotationMatrixFromOrientation(float[] o)
	{
		float[] xM = new float[9];
		float[] yM = new float[9];
		float[] zM = new float[9];

		float sinX = (float) Math.sin(o[1]);
		float cosX = (float) Math.cos(o[1]);
		float sinY = (float) Math.sin(o[2]);
		float cosY = (float) Math.cos(o[2]);
		float sinZ = (float) Math.sin(o[0]);
		float cosZ = (float) Math.cos(o[0]);

		// rotation about x-axis (pitch)
		xM[0] = 1.0f;
		xM[1] = 0.0f;
		xM[2] = 0.0f;
		xM[3] = 0.0f;
		xM[4] = cosX;
		xM[5] = sinX;
		xM[6] = 0.0f;
		xM[7] = -sinX;
		xM[8] = cosX;

		// rotation about y-axis (roll)
		yM[0] = cosY;
		yM[1] = 0.0f;
		yM[2] = sinY;
		yM[3] = 0.0f;
		yM[4] = 1.0f;
		yM[5] = 0.0f;
		yM[6] = -sinY;
		yM[7] = 0.0f;
		yM[8] = cosY;

		// rotation about z-axis (azimuth)
		zM[0] = cosZ;
		zM[1] = sinZ;
		zM[2] = 0.0f;
		zM[3] = -sinZ;
		zM[4] = cosZ;
		zM[5] = 0.0f;
		zM[6] = 0.0f;
		zM[7] = 0.0f;
		zM[8] = 1.0f;

		// rotation order is y, x, z (roll, pitch, azimuth)
		float[] resultMatrix = matrixMultiplication(xM, yM);
		resultMatrix = matrixMultiplication(zM, resultMatrix);
		return resultMatrix;
	}

	private float[] matrixMultiplication(float[] A, float[] B)
	{
		float[] result = new float[9];

		result[0] = A[0] * B[0] + A[1] * B[3] + A[2] * B[6];
		result[1] = A[0] * B[1] + A[1] * B[4] + A[2] * B[7];
		result[2] = A[0] * B[2] + A[1] * B[5] + A[2] * B[8];

		result[3] = A[3] * B[0] + A[4] * B[3] + A[5] * B[6];
		result[4] = A[3] * B[1] + A[4] * B[4] + A[5] * B[7];
		result[5] = A[3] * B[2] + A[4] * B[5] + A[5] * B[8];

		result[6] = A[6] * B[0] + A[7] * B[3] + A[8] * B[6];
		result[7] = A[6] * B[1] + A[7] * B[4] + A[8] * B[7];
		result[8] = A[6] * B[2] + A[7] * B[5] + A[8] * B[8];

		return result;
	}

	// //////////////////////////////////

	class calculateFusedOrientationTask extends TimerTask
	{
		public void run()
		{
			List<Sensor> mySensors = mSensorManager
					.getSensorList(Sensor.TYPE_ACCELEROMETER);
			if (mySensors.size() > 0)
				accelerometer = mSensorManager
						.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

			final float oneMinusCoeff = 1.0f - FILTER_COEFFICIENT;
			synchronized (gyroOrientation)
			{
				if (gyroscoop != null && magnetometer != null
						&& accelerometer != null)
				{
					/*
					 * Fix for 179 <--> -179 transition problem: Check whether
					 * one of the two orientation angles (gyro or accMag) is
					 * negative while the other one is positive. If so, add 360
					 * (2 * math.PI) to the negative value, perform the sensor
					 * fusion, and remove the 360 from the result if it is
					 * greater than 180. This stabilizes the output in
					 * positive-to-negative-transition cases.
					 */
					// azimuth
					if (gyroOrientation[0] < -0.5 * Math.PI
							&& accMagOrientation[0] > 0.0)
					{
						fusedOrientation[0] = (float) (FILTER_COEFFICIENT
								* (gyroOrientation[0] + 2.0 * Math.PI) + oneMinusCoeff
								* accMagOrientation[0]);
						fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
								: 0;
					}
					else if (accMagOrientation[0] < -0.5 * Math.PI
							&& gyroOrientation[0] > 0.0)
					{
						fusedOrientation[0] = (float) (FILTER_COEFFICIENT
								* gyroOrientation[0] + oneMinusCoeff
								* (accMagOrientation[0] + 2.0 * Math.PI));
						fusedOrientation[0] -= (fusedOrientation[0] > Math.PI) ? 2.0 * Math.PI
								: 0;
					}
					else
					{
						fusedOrientation[0] = FILTER_COEFFICIENT
								* gyroOrientation[0] + oneMinusCoeff
								* accMagOrientation[0];
					}

					// pitch
					if (gyroOrientation[1] < -0.5 * Math.PI
							&& accMagOrientation[1] > 0.0)
					{
						fusedOrientation[1] = (float) (FILTER_COEFFICIENT
								* (gyroOrientation[1] + 2.0 * Math.PI) + oneMinusCoeff
								* accMagOrientation[1]);
						fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
								: 0;
					}
					else if (accMagOrientation[1] < -0.5 * Math.PI
							&& gyroOrientation[1] > 0.0)
					{
						fusedOrientation[1] = (float) (FILTER_COEFFICIENT
								* gyroOrientation[1] + oneMinusCoeff
								* (accMagOrientation[1] + 2.0 * Math.PI));
						fusedOrientation[1] -= (fusedOrientation[1] > Math.PI) ? 2.0 * Math.PI
								: 0;
					}
					else
					{
						fusedOrientation[1] = FILTER_COEFFICIENT
								* gyroOrientation[1] + oneMinusCoeff
								* accMagOrientation[1];
					}

					// roll
					if (gyroOrientation[2] < -0.5 * Math.PI
							&& accMagOrientation[2] > 0.0)
					{
						fusedOrientation[2] = (float) (FILTER_COEFFICIENT
								* (gyroOrientation[2] + 2.0 * Math.PI) + oneMinusCoeff
								* accMagOrientation[2]);
						fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
								: 0;
					}
					else if (accMagOrientation[2] < -0.5 * Math.PI
							&& gyroOrientation[2] > 0.0)
					{
						fusedOrientation[2] = (float) (FILTER_COEFFICIENT
								* gyroOrientation[2] + oneMinusCoeff
								* (accMagOrientation[2] + 2.0 * Math.PI));
						fusedOrientation[2] -= (fusedOrientation[2] > Math.PI) ? 2.0 * Math.PI
								: 0;
					}
					else
					{
						fusedOrientation[2] = FILTER_COEFFICIENT
								* gyroOrientation[2] + oneMinusCoeff
								* accMagOrientation[2];
					}

					// overwrite gyro matrix and orientation with fused
					// orientation
					// to compensate gyro drift
					gyroMatrix = getRotationMatrixFromOrientation(fusedOrientation);
					System.arraycopy(fusedOrientation, 0, gyroOrientation, 0, 3);
					// Log.v("SensorFusion", "mHandler.post");
					// update sensor output in GUI
				}
			}
		}
	}

	
	float previousStateAngles[] = new float[3];
	private float[] lowPass(float[] input, float[] output)
	{
		if (output == null)
			return input;

		float ALPHA = 0.8f;
		for (int i = 0; i < input.length; i++)
		{
			//Fix For Jitter When +179 <--> -179 Transition
			/*
			 * Fix for 179 <--> -179 transition problem: Check whether
			 * one of the two orientation angles (gyro or accMag) is
			 * negative while the other one is positive. If so, add 360
			 * (2 * math.PI) to the negative value, perform the sensor
			 * fusion, and remove the 360 from the result if it is
			 * greater than 180. This stabilizes the output in
			 * positive-to-negative-transition cases.
			 */
			if((input[i]<0)&&(output[i]>0))
			{
				input[i] += 360.0f;
			}
			else
			if((input[i]>0)&&(output[i]<0))
			{
				output[i] += 360.0f;
			}
			
			//Low Pass Filtering
			output[i] += ALPHA * (input[i] - output[i]);
			
			if(output[i]>180.0f)
			{
				output[i] -= 360.0f;
			}
			if(output[i]<-180.0f)
			{
				output[i] += 360.0f;
			}
		}
		return output;
	}

	public final int NO_SENSOR_FOUND = 0;
	public final int GYRO_MAGNETO_ACCLERO = 1;
	public final int MAGNETO_ACCLERO = 2;
	public final int GYRO = 3;
	public final int ORIEN = 4;

	public int decideSensor()
	{
		/*if (gyroscoop != null && magnetometer != null && accelerometer != null)
		{
			returnAngles[0] = (float) Math.toDegrees(fusedOrientation[0]);
			returnAngles[1] = (float) Math.toDegrees(fusedOrientation[1]);
			returnAngles[2] = (float) Math.toDegrees(fusedOrientation[2]);
			
			return GYRO_MAGNETO_ACCLERO;
		}*/
		
		if (magnetometer != null && accelerometer != null)
		{
			System.arraycopy(accMagOrientation, 0, returnAngles, 0, returnAngles.length);
			returnAngles[0] = (float) Math.toDegrees(returnAngles[0]);
			returnAngles[1] = (float) Math.toDegrees(returnAngles[1]);
			returnAngles[2] = (float) Math.toDegrees(returnAngles[2]);

			//Applying Low Pass Filter
			returnAngles = lowPass(previousStateAngles, returnAngles);
			System.arraycopy(returnAngles, 0, previousStateAngles, 0, previousStateAngles.length);
			return MAGNETO_ACCLERO;
		}

		/*if (gyroscoop != null)
		{
			returnAngles[0] = (float) Math.toDegrees(gyroOrientation[0]);
			returnAngles[1] = (float) Math.toDegrees(gyroOrientation[1]);
			returnAngles[2] = (float) Math.toDegrees(gyroOrientation[2]);
			return GYRO;
		}*/

		if (orientation != null)
		{
			returnAngles[0] = orient[0];
			returnAngles[1] = orient[1];
			returnAngles[2] = orient[2];
			return ORIEN;
		}

		return NO_SENSOR_FOUND;
	}

	public float[] getAngles()
	{
		decideSensor();
		return returnAngles;
	}
	
	public float[] getRotationMatrix()
	{
		return rotationMatrix;
	}
}
