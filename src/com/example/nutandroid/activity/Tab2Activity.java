package com.example.nutandroid.activity;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.example.nutandroid.R;

public class Tab2Activity extends Activity implements Callback, Runnable
{

	private boolean mIsRunning;
	private SurfaceHolder mHolder;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab2);
		SurfaceView sv = (SurfaceView) findViewById(R.id.tab2Surface);
		sv.getHolder().addCallback(this);
//		overridePendingTransition(enterAnim, exitAnim);
	}


	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3)
	{

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mHolder = holder;
		Canvas canvas = mHolder.lockCanvas();
		canvas.drawColor(Color.RED);
		mHolder.unlockCanvasAndPost(canvas);
		Thread t = new Thread(this);
		mIsRunning = true;
		t.start();

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder arg0)
	{
		mIsRunning = false;
	}

	@Override
	public void run()
	{
		while (mIsRunning)
		{
			Canvas canvas = mHolder.lockCanvas();
			canvas.drawColor(Color.RED);
			mHolder.unlockCanvasAndPost(canvas);
		}
	}
}
