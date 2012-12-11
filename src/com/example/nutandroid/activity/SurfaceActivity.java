package com.example.nutandroid.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.SweepGradient;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.example.nutandroid.R;

public class SurfaceActivity extends Activity implements Callback, Runnable, OnTouchListener
{
	private static final Logger logger = LoggerFactory.getLogger(SurfaceActivity.class);

	private SurfaceView mSurface;
	private SurfaceHolder mHolder;
	private Thread mThread;
	private boolean mThreadRunning = false;
	private boolean mThreadFinish = false;
	private Paint mPaintBack = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mPaintFont = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mPaintPie = new Paint(Paint.ANTI_ALIAS_FLAG);
	private int[] mColors = new int[] { Color.BLACK, Color.BLUE, Color.GREEN, Color.RED };
	private int mCurrentColor = 0;
	private int mduration = 3000;
	private long mStartTime = 0;
	private long count = 0;
	private RectF arcRect = new RectF();
	private int mHeight = 0, mWidth = 0;
	private boolean mNewPie = true;

	private float currentDegree;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_surface);
		mSurface = (SurfaceView) findViewById(R.id.surface);
		mSurface.setOnTouchListener(this);
		mHolder = mSurface.getHolder();
		mHolder.addCallback(this);
	}

	@Override
	protected void onPause()
	{
		logger.debug("onpause");
		mThreadRunning = false;
		super.onPause();
	}

	private void initPaints()
	{
		mPaintBack.setColor(Color.YELLOW);
		mPaintBack.setStyle(Style.FILL);
		mPaintFont.setColor(Color.RED);
		mPaintFont.setStyle(Style.STROKE);
		mPaintFont.setTextSize(24);
		mPaintPie.setColor(Color.GREEN);
		mPaintPie.setStyle(Style.FILL);
		SweepGradient gradient = new SweepGradient(mSurface.getMeasuredWidth() / 2, mSurface.getMeasuredHeight() / 2, Color.WHITE, Color.GREEN);
		mPaintPie.setShader(gradient);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event)
	{
		logger.debug("ontouch,action:{},{}", event.getAction() == MotionEvent.ACTION_UP, event);
		if (event.getAction() != MotionEvent.ACTION_UP)
		{
			return true;
		}
		synchronized (this)
		{
			logger.debug("mThreadRunning:{}", mThreadRunning);
			mThreadRunning = !mThreadRunning;
			logger.debug("mThreadRunning2:{}", mThreadRunning);
			if (mThreadRunning)
			{
				mStartTime = System.currentTimeMillis();
				mNewPie = true;
				notify();
			}

		}
		return true;
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		logger.debug("surfaceChanged formart:{},width:{},height:{}", new Integer[] { format, width, height });
		mWidth = width;
		mHeight = height;
		arcRect.set(0, 0, width, height);
		SweepGradient gradient = new SweepGradient(width / 2, height / 2, Color.WHITE, Color.GREEN);
		mPaintPie.setShader(gradient);
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		initPaints();
		mThread = new Thread(this);
		mThread.setDaemon(true);
		mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		logger.debug("surface destroyed.");
		mThreadFinish = true;
	}

	@Override
	public void run()
	{
		mStartTime = System.currentTimeMillis();
		while (!mThreadFinish)
		{
			count++;
			synchronized (this)
			{

				if (!mThreadRunning)
				{
					logger.debug("[{}]mThreadRunning is false", count);
					logger.debug("[{}]draw text tap to start", count);
					Canvas canvas = mHolder.lockCanvas();
					canvas.drawColor(Color.WHITE);
					drawSomeText(canvas, "点一下开始");
					mHolder.unlockCanvasAndPost(canvas);
					try
					{
						logger.debug("[{}]beging to wait;", count);
						wait();
						canvas = mHolder.lockCanvas();
						canvas.drawColor(Color.WHITE);
						mHolder.unlockCanvasAndPost(canvas);
						logger.debug("[{}]after wait", count);

					}
					catch (InterruptedException e)
					{
						logger.error("wait interrupted", e);
						return;
					}
				}
				else
				{
					// logger.trace("[{}]begin to draw pie.", count);
					Canvas canvas = mHolder.lockCanvas();
					drawPie(canvas);
					mHolder.unlockCanvasAndPost(canvas);
					// try
					// {
					// Thread.sleep(10);
					// }
					// catch (InterruptedException e)
					// {
					// logger.error("sleep interupted", e);
					// }
				}
			}
		}

	}

	private void drawSomeText(Canvas canvas, String text)
	{
		float textWidth = mPaintFont.measureText(text);
		FontMetrics fontMetrics = mPaintFont.getFontMetrics();
		float textHeight = fontMetrics.descent - fontMetrics.ascent;
		// logger.debug("surface width:{},surface height:{}text width:{},textHeight:{}",
		// new Object[] { mSurface.getWidth(), mSurface.getHeight(), textWidth,
		// textHeight });
		canvas.drawText(text, (mWidth - textWidth) / 2, (mHeight - textHeight) / 2, mPaintFont);
	}

	private void drawPie(Canvas canvas)
	{
		long currentTimeMillis = System.currentTimeMillis();
		float totalDegree = 360;
		long duration = currentTimeMillis - mStartTime;
		if (currentDegree < 360 && duration >= mduration)
		{
			logger.debug("start a new pie.last degree:{},duration:{}", currentDegree, duration);
			canvas.drawColor(Color.WHITE);
			mStartTime = currentTimeMillis;
			mNewPie = true;
			return;
		}
		if (mNewPie)
		{
			canvas.drawColor(Color.WHITE);
			mNewPie = false;
		}
		float percent = (float) duration / mduration;
		currentDegree = totalDegree * percent;
		canvas.drawArc(arcRect, 0, currentDegree, true, mPaintPie);
		drawSomeText(canvas, "点一下暂停");
	}

	// @Override
	// public void run()
	// {
	// while (mThreadLive)
	// {
	// Canvas canvas = mHolder.lockCanvas();
	// int currentColor = mColors[mCurrentColor];
	// canvas.drawColor(currentColor);
	// mHolder.unlockCanvasAndPost(canvas);
	// mCurrentColor = (mCurrentColor + 1) % mColors.length;
	// try
	// {
	// Thread.sleep(500);
	// }
	// catch (InterruptedException e)
	// {
	// e.printStackTrace();
	// }
	// }
	// }
}
