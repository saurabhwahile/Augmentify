package com.augmentify.explorer.DataModule;

import com.augmentify.explorer.Explorer;
import com.augmentify.explorer.SensorModule.GPSSensor;

public class RuntimeData
{
	public static int numberOfWidgets = 10;
	public static GPSSensor GPS;
	
	public RuntimeData()
	{
		GPS = new GPSSensor(Explorer.getContext());
	}
	
	public static void onResume()
	{
		GPS = new GPSSensor(Explorer.getContext());
	}
	
	public static void onPause()
	{
		GPS.stopUsingGPS();
	}
	
	public static void onDestroy()
	{
		GPS.stopUsingGPS();
	}
}
