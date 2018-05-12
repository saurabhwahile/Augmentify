package com.augmentify.explorer.GraphicsModule;

import com.augmentify.explorer.Explorer;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class GLWidgetView extends WebView
{
	Canvas canvas;
	private int WIDGET_RES_WIDTH = 64;
	private int WIDGET_RES_HEIGHT = 64;
	private int widgetNumber;

	public GLWidgetView(Context context)
	{
		super(context);
	}

	public GLWidgetView(Context context, int widgetNumber)
	{
		super(context);
		this.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
		this.widgetNumber = widgetNumber;

		WebSettings webSettings = this.getSettings();
		webSettings.setJavaScriptEnabled(true);
		//this.enableHTML5AppCache();

		this.setBackgroundColor(Color.WHITE);
		//this.setBackgroundColor(Color.TRANSPARENT);
		this.layout(0, 0, WIDGET_RES_WIDTH, WIDGET_RES_HEIGHT);
		this.loadUrl("file:///android_asset/page.html");
		// LoadHTML();
		// this.setBackgroundColor(Color.TRANSPARENT);
	}

	@Override
	public void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		this.canvas = canvas;
	}

	public Canvas getCanvas()
	{
		return canvas;
	}
	
	public int getWidgetNumber()
	{
		return this.widgetNumber;
	}

	public void LoadHTML()
	{
		String summary = null;
		if (widgetNumber % 2 == 0)
		{
			summary = "<html><body><script>document.write('hello');</script>You scored <b>192</b> points.</body></html>";
		}
		else
		{
			summary = Explorer.getContext().getCacheDir().getAbsolutePath();
		}
		this.loadData(summary, "text/html", null);
	}

	@Override
	public void onMeasure(int wms, int hms)
	{
		this.setMeasuredDimension(MeasureSpec.getSize(wms),
				MeasureSpec.getSize(hms));
	}

	private void enableHTML5AppCache()
	{
		this.getSettings().setDomStorageEnabled(true);

		// Set cache size to 8 mb by default. should be more than enough
		this.getSettings().setAppCacheMaxSize(1024 * 1024 * 8);

		// This next one is crazy. It's the DEFAULT location for your app's
		// cache
		// But it didn't work for me without this line

		this.getSettings().setAppCachePath(
				Explorer.getContext().getCacheDir().getAbsolutePath()
						+ "/webCache");
		this.getSettings().setAllowFileAccess(true);
		this.getSettings().setAppCacheEnabled(true);
		this.getSettings().setCacheMode(WebSettings.LOAD_DEFAULT);
	}
}
