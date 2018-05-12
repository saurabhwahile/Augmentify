package com.augmentify.explorer.GraphicsModule;

import android.content.Context;
import android.graphics.PixelFormat;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;

public class GLSurface extends GLSurfaceView
{
	private GLRenderer GLRenderer;

	public GLSurface(Context context)
	{
		super(context);
		GLRenderer = new GLRenderer();
		// To see the camera preview, the OpenGL surface has to be created
		// translucently.
		// See link above.
		// glView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
		// glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
		this.setEGLConfigChooser(8, 8, 8, 8, 0, 0);
		this.getHolder().setFormat(PixelFormat.RGBA_8888);
		this.setRenderer(GLRenderer);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event != null)
		{
			com.augmentify.explorer.GraphicsModule.GLRenderer.touchX = event.getX();
			com.augmentify.explorer.GraphicsModule.GLRenderer.touchY = event.getY();
			com.augmentify.explorer.GraphicsModule.GLRenderer.toProcessTouch = true;
			
			return true;
		}
		else
		{
			return super.onTouchEvent(event);
		}
	}
}
