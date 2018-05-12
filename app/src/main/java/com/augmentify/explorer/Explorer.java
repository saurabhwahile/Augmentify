package com.augmentify.explorer;

import javax.microedition.khronos.opengles.GL;

import com.augmentify.explorer.DataModule.RuntimeData;
import com.augmentify.explorer.GraphicsModule.CameraView;
import com.augmentify.explorer.GraphicsModule.GLSurface;
import com.augmentify.explorer.GraphicsModule.IntersectionTests.MatrixTrackingGL;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;

public class Explorer extends FragmentActivity
{
	// Context That Can Be Used Externally
	static Context context;
	static Activity activity;

	// Surface For 3D Rendering
	GLSurface GLView;

	// Background Camera View
	CameraView cameraView;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		activity = this;

		//new RuntimeData();

		setWindowParameters();
		addRendererAsView();
		//addCameraView();

	}

	public static Context getContext()
	{
		return context;
	}

	public static Activity getActivity()
	{
		return activity;
	}

	private void setWindowParameters()
	{
		// FullScreen, Horizontal Orientation And No Title
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
	}

	private void addRendererAsView()
	{
		GLView = new GLSurface(this);
		GLView.setGLWrapper(new GLSurface.GLWrapper()
		{
			public GL wrap(GL gl)
			{
				return new MatrixTrackingGL(gl);
			}
		});
		this.addContentView(GLView, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
		// setContentView(GLView);
	}

	private void addCameraView()
	{
		cameraView = new CameraView(this);
		this.addContentView(cameraView, new LayoutParams(
				LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.explorer, menu);
		return true;
	}

	@Override
	protected void onResume()
	{
		// The activity must call the GL surface view's onResume() on activity
		// onResume().
		super.onResume();
		RuntimeData.onResume();
		GLView.onResume();
	}

	@Override
	protected void onPause()
	{
		// The activity must call the GL surface view's onPause() on activity
		// onPause().
		super.onPause();
		GLView.onPause();
		RuntimeData.onPause();
	}

	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		RuntimeData.onDestroy();
	}
}