package com.augmentify.explorer.GraphicsModule.IntersectionTests;

import javax.microedition.khronos.opengles.GL10;

import android.opengl.GLU;
import android.opengl.Matrix;

public class RayAndTriangleBuilder
{
	static MatrixGrabber matrixGrabber = new MatrixGrabber();
	
	public static float[] nearCoOrds = new float[3];
	public static float[] farCoOrds = new float[3];
	
	public static void setGL(GL10 gl)
	{
		matrixGrabber.getCurrentState(gl);
	}
	
	public static void buildRay(float xTouch, float yTouch)
	{
		int[] viewport = com.augmentify.explorer.GraphicsModule.GLRenderer.viewPort;

		// get the near and far CoOrds for the click
		float winx = xTouch, winy = (float) viewport[3] - yTouch;
		
		float[] temp = new float[4];
		float[] temp2 = new float[4];
		
		int result = GLU.gluUnProject(winx, winy, 1.0f,
				matrixGrabber.mModelView, 0, matrixGrabber.mProjection, 0,
				viewport, 0, temp, 0);

		Matrix.multiplyMV(temp2, 0, matrixGrabber.mModelView, 0, temp, 0);
		if (result == GL10.GL_TRUE)
		{
			nearCoOrds[0] = temp2[0] / temp2[3];
			nearCoOrds[1] = temp2[1] / temp2[3];
			nearCoOrds[2] = temp2[2] / temp2[3];
		}

		result = GLU.gluUnProject(winx, winy, 0, matrixGrabber.mModelView, 0,
				matrixGrabber.mProjection, 0, viewport, 0, temp, 0);
		Matrix.multiplyMV(temp2, 0, matrixGrabber.mModelView, 0, temp, 0);
		if (result == GL10.GL_TRUE)
		{
			farCoOrds[0] = temp2[0] / temp2[3];
			farCoOrds[1] = temp2[1] / temp2[3];
			farCoOrds[2] = temp2[2] / temp2[3];
		}
	}
	
	final static Triangle leftTriangleStore = new Triangle(
			new float[]{1.0f, 1.0f, 0.0f, 1.0f}, 
			new float[]{-1.0f, 1.0f, 0.0f, 1.0f}, 
			new float[]{-1.0f, -1.0f, 0.0f, 1.0f}
			);
	final static Triangle rightTriangleStore = new Triangle(
			new float[]{1.0f, 1.0f, 0.0f, 1.0f}, 
			new float[]{-1.0f, -1.0f, 0.0f, 1.0f},
			new float[]{1.0f, -1.0f, 0.0f, 1.0f}
			);
	
	public static Triangle leftTriangle;
	public static Triangle rightTriangle;
	
	static float result[] = new float[4];
	
	public static void buildTriangle()
	{
		leftTriangle = leftTriangleStore.newInstance(leftTriangleStore);
		
		Matrix.multiplyMV(result, 0, matrixGrabber.mModelView, 0, leftTriangle.V0,0);
		result[0] = result[0]/result[3];
		result[1] = result[1]/result[3];
		result[2] = result[2]/result[3];
		leftTriangle.V0 = new float[]{result[0], result[1], result[2], 1.0f};
		//Log.d("Array",Arrays.toString(result));
		Matrix.multiplyMV(result, 0, matrixGrabber.mModelView, 0, leftTriangle.V1,0);
		result[0] = result[0]/result[3];
		result[1] = result[1]/result[3];
		result[2] = result[2]/result[3];
		leftTriangle.V1 = new float[]{result[0], result[1], result[2], 1.0f};
		//Log.d("Array",Arrays.toString(result));
		Matrix.multiplyMV(result, 0, matrixGrabber.mModelView, 0, leftTriangle.V2,0);
		result[0] = result[0]/result[3];
		result[1] = result[1]/result[3];
		result[2] = result[2]/result[3];
		leftTriangle.V2 = new float[]{result[0], result[1], result[2], 1.0f};
		//Log.d("Array",Arrays.toString(result));
		
		rightTriangle = rightTriangleStore.newInstance(rightTriangleStore);
		
		Matrix.multiplyMV(result, 0, matrixGrabber.mModelView, 0, rightTriangle.V0,0);
		result[0] = result[0]/result[3];
		result[1] = result[1]/result[3];
		result[2] = result[2]/result[3];
		rightTriangle.V0 = new float[]{result[0], result[1], result[2], 1.0f};
		//Log.d("Array",Arrays.toString(result));
		Matrix.multiplyMV(result, 0, matrixGrabber.mModelView, 0, rightTriangle.V1,0);	
		result[0] = result[0]/result[3];
		result[1] = result[1]/result[3];
		result[2] = result[2]/result[3];
		rightTriangle.V1 = new float[]{result[0], result[1], result[2], 1.0f};
		//Log.d("Array",Arrays.toString(result));
		Matrix.multiplyMV(result, 0, matrixGrabber.mModelView, 0, rightTriangle.V2,0);
		result[0] = result[0]/result[3];
		result[1] = result[1]/result[3];
		result[2] = result[2]/result[3];
		rightTriangle.V2 = new float[]{result[0], result[1], result[2], 1.0f};
		//Log.d("Array",Arrays.toString(result));
	}
}
