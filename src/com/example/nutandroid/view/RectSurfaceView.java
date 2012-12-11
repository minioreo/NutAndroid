package com.example.nutandroid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class RectSurfaceView extends SurfaceView implements Callback, Runnable, OnGestureListener
{

	private final static Logger logger = LoggerFactory.getLogger("RectSurfaceView");

	private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private SurfaceHolder mHolder;
	private int mTotalWidth;
	private Thread mThread;
	private boolean mThreadRunning;
	private RectF mRect = new RectF();
	private int mRectWidth = 40;
	
	private long mCanvasOffset = 0;

	private GestureDetector mGestureDetector;

	public RectSurfaceView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public RectSurfaceView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public RectSurfaceView(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		mPaint.setStyle(Paint.Style.FILL);
		LinearGradient gradient = new LinearGradient(0, 0, mRectWidth, 0, new int[] { Color.rgb(11, 207, 255), Color.rgb(61, 243, 255), Color.rgb(11, 207, 255) }, new float[] { 0, 0.5f, 1 },
				TileMode.REPEAT);
		mPaint.setShader(gradient);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mGestureDetector = new GestureDetector(this);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return mGestureDetector.onTouchEvent(event);
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		logger.debug("surfaceChanged,width:{},height:{}", width, height);
		tempRect.set(width - 20, 0, width + 20, height / 2);
		synchronized (this)
		{
			this.mTotalWidth = width;
			mRect.set(0, height / 2, mRectWidth, height);
			mRect.offset((width - mRectWidth) / 2.0f, 0);
			notify();
		}

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mThread = new Thread(this, "Drawing Thread");
		mThread.setDaemon(true);
		mThreadRunning = true;
		mThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		mThreadRunning = false;
		synchronized (this)
		{
			notify();
		}

	}

	private RectF tempRect = new RectF();

	@Override
	public void run()
	{
		while (mThreadRunning)
		{
			try
			{
				Canvas canvas = mHolder.lockCanvas();
				canvas.drawColor(Color.WHITE);
				canvas.translate(mCanvasOffset, 0);
				synchronized (mRect)
				{
					canvas.drawRect(mRect, mPaint);
				}

				canvas.drawRect(tempRect, mPaint);

				mHolder.unlockCanvasAndPost(canvas);
				synchronized (this)
				{
					wait();
				}

			}
			catch (InterruptedException e)
			{
				logger.error("interrupted", e);
			}
		}
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		// logger.debug("onFling:velocityX:{},velocityY:{},x1:{},x2:{}", new
		// Float[] { velocityX, velocityY, e1.getX(), e2.getX() });
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{

	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		logger.debug("onScroll,distanceX:{},distanceY:{} ", distanceX, distanceY);
		mCanvasOffset -= distanceX;
//		synchronized (mRect)
//		{
//			float newLeft = mRect.left - distanceX;
//			if (newLeft < 0)
//			{
//				mRect.offset(-mRect.left, 0);
//			}
//			else if (newLeft + mRectWidth > mTotalWidth)
//			{
//				mRect.offset(mTotalWidth - mRect.right, 0);
//			}
//			else
//			{
//				mRect.offset(-distanceX, 0);
//			}
//		}

		synchronized (this)
		{
			notify();
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		return false;
	}

}
