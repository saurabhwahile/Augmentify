package com.augmentify.explorer.GraphicsModule;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import javax.microedition.khronos.opengles.GL10;

import com.augmentify.explorer.Explorer;
import com.augmentify.explorer.DataModule.RuntimeData;

import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.view.Surface;
import android.view.Surface.OutOfResourcesException;

public class GLFrameBuffer
{
	GLFrameBuffer()
	{
		setVertices();
		setIndices();
		setTextureCoordinates();
		initWidgets();
	}

	// Our vertex buffer.
	private FloatBuffer vertexBuffer;

	void setVertices()
	{
		// Our vertices.
		float vertices[] = { -1.0f, 1.0f, 0.0f, // 0, Top Left
				-1.0f, -1.0f, 0.0f, // 1, Bottom Left
				1.0f, -1.0f, 0.0f, // 2, Bottom Right
				1.0f, 1.0f, 0.0f, // 3, Top Right
		};

		// a float is 4 bytes, therefore we multiply the number if
		// vertices with 4.
		ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
		vbb.order(ByteOrder.nativeOrder());
		vertexBuffer = vbb.asFloatBuffer();
		vertexBuffer.put(vertices);
		vertexBuffer.position(0);
	}

	// Our index buffer.
	private ShortBuffer indexBuffer;

	void setIndices()
	{
		// The order we like to connect them.
		// short[] indices = { 0, 1, 2, 0, 2, 3 };

		short[] indices = { 0, 1, 2, 2, 3, 0 };

		// short is 2 bytes, therefore we multiply the number if
		// vertices with 2.
		ByteBuffer ibb = ByteBuffer.allocateDirect(indices.length * 2);
		ibb.order(ByteOrder.nativeOrder());
		indexBuffer = ibb.asShortBuffer();
		indexBuffer.put(indices);
		indexBuffer.position(0);
	}

	// Our UV texture buffer.
	private FloatBuffer textureBuffer;

	protected void setTextureCoordinates()
	{
		float textureCoordinates[] = { 0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f,
				1.0f, 0.0f, };

		// float is 4 bytes, therefore we multiply the number if
		// vertices with 4.
		ByteBuffer byteBuf = ByteBuffer
				.allocateDirect(textureCoordinates.length * 4);
		byteBuf.order(ByteOrder.nativeOrder());
		textureBuffer = byteBuf.asFloatBuffer();
		textureBuffer.put(textureCoordinates);
		textureBuffer.position(0);
	}

	private GLWidgetView widget[];

	public void initWidgets()
	{
		widget = new GLWidgetView[RuntimeData.numberOfWidgets];
		for (int widgetNumber = 0; widgetNumber < RuntimeData.numberOfWidgets; widgetNumber++)
		{
			widget[widgetNumber] = new GLWidgetView(Explorer.getContext(),
					widgetNumber);
			//widget[widgetNumber].addJavascriptInterface(widget[widgetNumber], "Widget");
		}

	}

	// Generate texture pointer...
	private int[] textureIds;

	private Surface surface[];
	private SurfaceTexture surfaceTexture[];
	private int TEXTURE_RES_WIDTH = 128;
	private int TEXTURE_RES_HEIGHT = 128;

	private final int GL_TEXTURE_EXTERNAL_OES = 0x8D65;

	public void loadGLTextures(GL10 gl)
	{
		surface = new Surface[RuntimeData.numberOfWidgets];
		surfaceTexture = new SurfaceTexture[RuntimeData.numberOfWidgets];
		textureIds = new int[RuntimeData.numberOfWidgets];

		gl.glGenTextures(RuntimeData.numberOfWidgets, textureIds, 0);

		for (int textureNumber = 0; textureNumber < RuntimeData.numberOfWidgets; textureNumber++)
		{
			// ...and bind it to our array
			gl.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureIds[textureNumber]);

			// Create Nearest Filtered Texture
			gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
					GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
			gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
					GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

			// Different possible texture parameters, e.g. GL10.GL_CLAMP_TO_EDGE
			/*
			 * gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
			 * GL10.GL_TEXTURE_WRAP_S, GL10.GL_REPEAT);
			 * gl.glTexParameterf(GL_TEXTURE_EXTERNAL_OES,
			 * GL10.GL_TEXTURE_WRAP_T, GL10.GL_REPEAT);
			 */

			surfaceTexture[textureNumber] = new SurfaceTexture(
					textureIds[textureNumber]);
			surfaceTexture[textureNumber].setDefaultBufferSize(
					TEXTURE_RES_WIDTH, TEXTURE_RES_HEIGHT);
			surface[textureNumber] = new Surface(surfaceTexture[textureNumber]);
		}
	}

	public void updateTexture(int widgetNumber)
	{
		Canvas canvas = null;
		try
		{
			canvas = surface[widgetNumber].lockCanvas(null);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (OutOfResourcesException e)
		{
			e.printStackTrace();
		}

		widget[widgetNumber].draw(canvas);
		surface[widgetNumber].unlockCanvasAndPost(widget[widgetNumber]
				.getCanvas());
		surfaceTexture[widgetNumber].updateTexImage();
	}
	
	// This function draws our square on screen.
	public void draw(GL10 gl, int widgetNumber)
	{
		// Counter-clockwise winding.
		gl.glFrontFace(GL10.GL_CCW);
		// Enable face culling.
		gl.glEnable(GL10.GL_CULL_FACE);
		// What faces to remove with the face culling.
		gl.glCullFace(GL10.GL_BACK);

		// Enabled the vertices buffer for writing and to be used during
		// rendering.
		gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
		// Specifies the location and data format of an array of vertex
		// coordinates to use when rendering.
		gl.glVertexPointer(3, GL10.GL_FLOAT, 0, vertexBuffer);

		if (textureIds[0] != -1 && textureBuffer != null)
		{
			updateTexture(widgetNumber);
			
			gl.glEnable(GL_TEXTURE_EXTERNAL_OES);
			// Enable the texture state
			gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

			// Point to our buffers
			gl.glTexCoordPointer(2, GL10.GL_FLOAT, 0, textureBuffer);
			gl.glBindTexture(GL_TEXTURE_EXTERNAL_OES, textureIds[widgetNumber]);
		}

		gl.glDrawElements(GL10.GL_TRIANGLES, 6, GL10.GL_UNSIGNED_SHORT,
				indexBuffer);

		if (textureIds[0] != -1 && textureBuffer != null)
		{
			gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
		}

		// Disable the vertices buffer.
		gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);
		// Disable face culling.
		gl.glDisable(GL10.GL_CULL_FACE);
	}
}
