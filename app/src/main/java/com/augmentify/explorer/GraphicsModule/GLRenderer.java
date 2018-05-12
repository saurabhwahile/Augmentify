package com.augmentify.explorer.GraphicsModule;


import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import com.augmentify.explorer.DataModule.RuntimeData;
import com.augmentify.explorer.SensorModule.OrientationSensor;


import android.opengl.GLU;
import android.opengl.GLSurfaceView.Renderer;
import android.util.Log;

public class GLRenderer implements Renderer
{
	//Buffer Object
	public GLFrameBuffer object;
	
	//Sensor Object
	OrientationSensor sensor;
	
	public GLRenderer()
	{
		sensor = new OrientationSensor();
		object = new GLFrameBuffer();
	}
	

	public void onSurfaceCreated(GL10 gl, EGLConfig config)
	{
		// Set the background colour to black ( rgba ).
		gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		// Enable Smooth Shading, default not really needed.
		gl.glShadeModel(GL10.GL_SMOOTH);
		// Depth buffer setup.
		gl.glClearDepthf(1.0f);
		// Enables depth testing.
		gl.glEnable(GL10.GL_DEPTH_TEST);
		// The type of depth testing to do.
		gl.glDepthFunc(GL10.GL_LEQUAL);
		// Really nice perspective calculations.
		gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_NICEST);
		//Enable Alpha Blending For Textures
		gl.glEnable(GL10.GL_BLEND);
		gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA); 
		
		object.loadGLTextures(gl);
	}
	
	static float touchX = 0.0f;
	static float touchY = 0.0f;
	static boolean toProcessTouch = false;
	static int touchesProcessed = 0;
	public static int viewPort[];
	
	public void onDrawFrame(GL10 gl)
	{
		// Rendering
		// Clears the screen and depth buffer.
		gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);
		
		// Replace the current matrix with the identity matrix
		gl.glLoadIdentity();
		
		//Get Angles From SensorModule
		float recievedAngles[] = sensor.getAngles();
		
		gl.glLoadIdentity();
		GLRendererUtils.alignToNorth(gl, recievedAngles);
		gl.glTranslatef(0.0f, 0.0f, -8.0f);
		GLRendererUtils.alignToUserView(gl, recievedAngles);  		
		object.draw(gl, 0);
		
		//Log.d("GPS",RuntimeData.GPS.getLatitude()+" "+" "+RuntimeData.GPS.getLongitude());
		
		for(int widgetNumber=0; widgetNumber<RuntimeData.numberOfWidgets; widgetNumber++)
		{
			gl.glLoadIdentity();
			GLRendererUtils.alignToNorth(gl, recievedAngles);
			gl.glTranslatef(widgetNumber*-8.0f, 0.0f, -8.0f);
			GLRendererUtils.alignToUserView(gl, recievedAngles);
			
			object.draw(gl, widgetNumber);
			/*if(recievedAngles[0]>-45.0&&recievedAngles[0]<140.0)
			{
				GLRendererUtils.processTouch(gl, touchX, touchY);
				//Log.d("processedTouch", Integer.toString(widgetNumber));
			}*/
			if(toProcessTouch == true)
			{
				if(GLRendererUtils.processTouch(gl, touchX, touchY)==2)
				{
					Log.d("Status", Integer.toString(touchesProcessed));
				}
				touchesProcessed++;
			}
			if(touchesProcessed == RuntimeData.numberOfWidgets)
			{
				touchesProcessed = 0;
				toProcessTouch = false;
			}
		}
	}

	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		// This is called whenever the dimensions of the surface have changed.
		// We need to adapt this change for the GL viewport.
		// Sets the current view port to the new size.
		gl.glViewport(0, 0, width, height);
		//Save ViewPort;
		viewPort = new int[]{0, 0, width, height};
		// Select the projection matrix
		gl.glMatrixMode(GL10.GL_PROJECTION);
		// Reset the projection matrix
		gl.glLoadIdentity();
		// Calculate the aspect ratio of the window
		GLU.gluPerspective(gl, 45.0f, (float) width / (float) height, 0.1f,
				200.0f);
		// Select the modelview matrix
		gl.glMatrixMode(GL10.GL_MODELVIEW);
		// Reset the modelview matrix
		gl.glLoadIdentity();
	}
}
