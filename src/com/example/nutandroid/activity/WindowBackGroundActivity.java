package com.example.nutandroid.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.Menu;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.example.nutandroid.R;

public class WindowBackGroundActivity extends Activity implements Callback, Runnable
{

	private SurfaceView surface;
	private Thread mThread;
	private boolean mDrawingThreadIsRunning;
	private SurfaceHolder mHolder;
	private int mWidth;
	private int mHeight;
	private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		overridePendingTransition(0, 0);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_window_back_ground);
		surface = (SurfaceView) findViewById(R.id.timeSurface);
		surface.getHolder().addCallback(this);
//		surface.setZOrderOnTop(true);
		mTextPaint.setColor(Color.RED);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		mWidth = width;
		mHeight = height;
		
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mHolder = holder;
		Canvas canvas = holder.lockCanvas();
		canvas.drawColor(Color.WHITE);
		holder.unlockCanvasAndPost(canvas);
		mThread = new Thread(this, "drawThread");
		mThread.setDaemon(true);
		mDrawingThreadIsRunning = true;
		mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mDrawingThreadIsRunning = false;
	}

	@Override
	public void run()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
		String msg = "";
		while(mDrawingThreadIsRunning)
		{
			msg = sdf.format(new Date());
			Canvas canvas = mHolder.lockCanvas();
			canvas.drawColor(Color.WHITE);
			canvas.drawText(msg, 0, mHeight/2, mTextPaint);
			mHolder.unlockCanvasAndPost(canvas);
		}
	}
}
