package com.augmentify.explorer.GraphicsModule;

import javax.microedition.khronos.opengles.GL10;


import android.util.Log;

import com.augmentify.explorer.GraphicsModule.IntersectionTests.RayAndTriangleBuilder;
import com.augmentify.explorer.GraphicsModule.IntersectionTests.Triangle;

public class GLRendererUtils
{
	public static void alignToNorth(GL10 gl, float Angles[])
	{
		gl.glRotatef(Angles[0] + 90.0f, 0, 1, 0);
		gl.glRotatef(Angles[2] + 90.0f, 1, 0, 0);
	}

	public static void alignToUserView(GL10 gl, float Angles[])
	{
		gl.glRotatef(Angles[2] + 90.0f, -1, 0, 0);
		gl.glRotatef(Angles[0] + 90.0f, 0, -1, 0);
	}
	
	
	static float intersectionPoint[] = new float[3];
	public static int processTouch(GL10 gl, float X, float Y)
	{
		RayAndTriangleBuilder.setGL(gl);
		RayAndTriangleBuilder.buildRay(X, Y);
		RayAndTriangleBuilder.buildTriangle();
		int status =  Triangle.intersectRayAndTriangle(RayAndTriangleBuilder.nearCoOrds, 
				RayAndTriangleBuilder.farCoOrds, 
				RayAndTriangleBuilder.leftTriangle, 
				intersectionPoint);
		if(status == 1)
		{
			Log.d("Status",Float.toString(intersectionPoint[0])+Float.toString(intersectionPoint[1])+Float.toString(intersectionPoint[2]));
		}
		return status;
		
	}
}
