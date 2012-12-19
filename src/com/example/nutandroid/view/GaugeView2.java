package com.example.nutandroid.view;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

import com.example.nutandroid.R;
import com.example.nutandroid.util.NutLogger;

public class GaugeView2 extends SurfaceView implements Callback, Runnable
{
	private static final NutLogger logger = NutLogger.getLogger(GaugeView2.class);

	private Drawable mBackGround;

	private Drawable mBitmapDialer;

	private Drawable mBitmapPointer1;
	private Drawable mBitmapPointer2;
	private int mTotalWidth;
	private int mTotalHeight;
	private int mAngle = 0, mLastAngle = 0;
	private final Paint mPaintDialerBox = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintPointerBox1 = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintPointerBox2 = new Paint(Paint.ANTI_ALIAS_FLAG);

	private boolean mRunning = false;
	private final static long sAnimationDuration = TimeUnit.NANOSECONDS.convert(1, TimeUnit.SECONDS);
	private long mAnimationStartTime;

	private final Object drawSignal = new Object();

	private SurfaceHolder mHolder;

	public GaugeView2(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public GaugeView2(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public GaugeView2(Context context)
	{
		super(context);
		init();

	}

	private void init()
	{
		mBackGround = getResources().getDrawable(R.drawable.bg);
		mBitmapDialer = getResources().getDrawable(R.drawable.dialer);
		mBitmapPointer1 = getResources().getDrawable(R.drawable.pointer1);
		mBitmapPointer2 = getResources().getDrawable(R.drawable.pointer2);
		mPaintDialerBox.setColor(Color.RED);
		mPaintDialerBox.setStyle(Paint.Style.STROKE);
		mPaintPointerBox1.setColor(Color.YELLOW);
		mPaintPointerBox1.setStyle(Paint.Style.STROKE);
		mPaintPointerBox2.setColor(Color.WHITE);
		mPaintPointerBox2.setStyle(Paint.Style.STROKE);
		getHolder().addCallback(this);
	}

	public void reset()
	{
		mAngle = 0;
		setPointerOffsetAngle(0);
	}

	public void setPointerOffsetAngle(int angle)
	{
		if (angle == mAngle)
		{
			return;
		}
		if (angle >= 0 && angle < 360)
		{
			if (mAngle != angle)
			{
				mLastAngle = mAngle;
				mAngle = angle;
			}
		}
		else
		{
			throw new IllegalArgumentException("angel range : 0 <= angle < 360 current:" + angle);
		}
		mAnimationStartTime = System.nanoTime();
		synchronized (drawSignal)
		{
			drawSignal.notify();
		}
	}

	public int getPointerAngle()
	{
		return mAngle;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		int widthMode = MeasureSpec.getMode(widthMeasureSpec);
		int widthSize = MeasureSpec.getSize(widthMeasureSpec);
		int heightMode = MeasureSpec.getMode(heightMeasureSpec);
		int heightSize = MeasureSpec.getSize(heightMeasureSpec);
		logger.debug("onMeasure widthMode:{},width:{},heightMode:{},height:{}", widthMode, widthSize, heightMode, heightSize);
		// float hScale = 1.0f;
		// float vScale = 1.0f;
		//
		// if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth)
		// {
		// hScale = (float) widthSize / (float) mDialWidth;
		// }
		//
		// if (heightMode != MeasureSpec.UNSPECIFIED && heightSize <
		// mDialHeight)
		// {
		// vScale = (float) heightSize / (float) mDialHeight;
		// }
		//
		// float scale = Math.min(hScale, vScale);
		// logger.debug("hscale:{},vscale:{},mDialerWidth:{},mDialerHeight:{}",
		// hScale, vScale, mDialWidth, mDialHeight);
		// int resolvedWidth = resolveSize((int) (mDialWidth * scale),
		// widthMeasureSpec);
		// int resolvedHeight = resolveSize((int) (mDialHeight * scale),
		// heightMeasureSpec);
		// setMeasuredDimension(resolvedWidth, resolvedHeight);
		setMeasuredDimension(widthSize, heightSize);
	}

	protected void drawGauge(Canvas canvas, int angle)
	{
		int x = mTotalWidth / 2;
		int y = mTotalHeight / 2;

		final Drawable bg = mBackGround;
		bg.setBounds(0, 0, mTotalWidth, mTotalHeight);
		bg.draw(canvas);

		final Drawable dial = mBitmapDialer;
		int w = dial.getIntrinsicWidth();
		int h = dial.getIntrinsicHeight();
		// logger.debug("dialer width:{},height:{}", w, h);

		boolean scaled = false;

		if (mTotalWidth < w || mTotalHeight < h)
		{
			scaled = true;
			float scale = Math.min((float) mTotalWidth / (float) w, (float) mTotalHeight / (float) h);
			// logger.debug("canvas scale {} for dialer", scale);
			canvas.save();
			canvas.scale(scale, scale, x, y);
			dial.setBounds(x - (w / 2), y - (h / 2), x + (w / 2), y + (h / 2));
			// canvas.drawRect(dial.getBounds(), mPaintDialerBox);
			// logger.debug("dialer bounds:{}", dial.getBounds());
		}

		dial.draw(canvas);

		canvas.save();
		canvas.rotate(angle, x, y);
		final Drawable pointer1 = mBitmapPointer1, pointer2 = mBitmapPointer2;
		w = pointer1.getIntrinsicWidth();
		h = pointer1.getIntrinsicHeight();
		pointer1.setBounds(x - (w / 2), y, x + (w / 2), y + h);
		// canvas.drawRect(pointer1.getBounds(), mPaintPointerBox1);
		// logger.debug("pointer1 bounds:{}", pointer1.getBounds());
		pointer1.draw(canvas);
		w = pointer2.getIntrinsicWidth();
		h = pointer2.getIntrinsicHeight();
		pointer2.setBounds(x - w / 2, y - h, x + w / 2, y);
		// logger.debug("pointer2 bounds:{}", pointer2.getBounds());
		// canvas.drawRect(pointer2.getBounds(), mPaintPointerBox2);
		pointer2.draw(canvas);
		canvas.restore();

		if (scaled)
		{
			canvas.restore();
		}
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		mHolder = holder;
		Thread drawThread = new Thread(this, "GaugeViewDrawThread");
		drawThread.setDaemon(true);
		mRunning = true;
		drawThread.start();
		logger.debug("surface created");
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		logger.debug("surface changed,width:{},height:{}", width, height);
		mTotalWidth = width;
		mTotalHeight = height;
		synchronized (drawSignal)
		{
			drawSignal.notify();
		}
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		logger.debug("surface destroyed");
		mRunning = false;
		synchronized (drawSignal)
		{
			drawSignal.notify();
		}
	}

	@Override
	public void run()
	{
		logger.debug("drawThread run,width:{},height:{}", mTotalWidth, mTotalHeight);
		Canvas canvas = mHolder.lockCanvas();
		drawGauge(canvas, 0);
		mHolder.unlockCanvasAndPost(canvas);

		while (mRunning)
		{
			if (mLastAngle == mAngle) // draw and wait
			{
				canvas = mHolder.lockCanvas();
				drawGauge(canvas, mAngle);
				mHolder.unlockCanvasAndPost(canvas);
				synchronized (drawSignal)
				{
					logger.debug("begin to wait");
					try
					{
						drawSignal.wait();
					}
					catch (InterruptedException e)
					{
						logger.error("wait interrupted", e);
					}
					logger.debug("after wait");
				}
			}
			else
			{
				long currentTime = System.nanoTime();
				long duration = currentTime - mAnimationStartTime;
				if (duration >= sAnimationDuration)
				{
					mLastAngle = mAngle;
					continue;
				}
				else
				{
					double percent = duration / (double) sAnimationDuration;
					int delta = (int) Math.ceil((mAngle - mLastAngle) * percent);
					canvas = mHolder.lockCanvas();
					drawGauge(canvas, mLastAngle + delta);
					mHolder.unlockCanvasAndPost(canvas);
				}
			}

		}
		logger.debug("draw thread finish");
	}
}
