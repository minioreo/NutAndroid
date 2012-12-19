package com.example.nutandroid.view;

import java.util.concurrent.TimeUnit;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

import com.example.nutandroid.R;
import com.example.nutandroid.util.NutLogger;

public class GaugeView extends View
{
	private static final NutLogger logger = NutLogger.getLogger(GaugeView.class);

	private float mDialWidth = 80, mDialHeight = 80;

	private Drawable mBackGround;

	private Drawable mBitmapDialer;

	private Drawable mBitmapPointer1;
	private Drawable mBitmapPointer2;
	private int mTotalWidth;
	private int mTotalHeight;
	private int mAngle = 0, mCurrentAngle = 0;
	private final Paint mPaintDialerBox = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintPointerBox1 = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint mPaintPointerBox2 = new Paint(Paint.ANTI_ALIAS_FLAG);

	private long mAnimationStartTime;

	private static final float sAnglePerNanoSecond = 360f / TimeUnit.NANOSECONDS.convert(2, TimeUnit.SECONDS);

	public GaugeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		mBackGround = getResources().getDrawable(R.drawable.bg);
		mBitmapDialer = getResources().getDrawable(R.drawable.dialer);
		mBitmapPointer1 = getResources().getDrawable(R.drawable.pointer1);
		mBitmapPointer2 = getResources().getDrawable(R.drawable.pointer2);
		mDialWidth = mBitmapDialer.getIntrinsicWidth();
		mDialHeight = mBitmapDialer.getIntrinsicHeight();
		mPaintDialerBox.setColor(Color.RED);
		mPaintDialerBox.setStyle(Paint.Style.STROKE);
		mPaintPointerBox1.setColor(Color.YELLOW);
		mPaintPointerBox1.setStyle(Paint.Style.STROKE);
		mPaintPointerBox2.setColor(Color.WHITE);
		mPaintPointerBox2.setStyle(Paint.Style.STROKE);

	}

	public GaugeView(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	public GaugeView(Context context)
	{
		this(context, null);

	}

	public void setPointerAngle(int angle)
	{
		if (angle >= 0 && angle < 360)
		{
			if (mAngle != angle)
			{
				mAngle = angle;
				mAnimationStartTime = System.nanoTime();
				invalidate();
			}
		}
		else
		{
			throw new IllegalArgumentException("angel range : 0 <= angle < 360");
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

		float hScale = 1.0f;
		float vScale = 1.0f;

		if (widthMode != MeasureSpec.UNSPECIFIED && widthSize < mDialWidth)
		{
			hScale = (float) widthSize / (float) mDialWidth;
		}

		if (heightMode != MeasureSpec.UNSPECIFIED && heightSize < mDialHeight)
		{
			vScale = (float) heightSize / (float) mDialHeight;
		}

		float scale = Math.min(hScale, vScale);

		setMeasuredDimension(resolveSize((int) (mDialWidth * scale), widthMeasureSpec), resolveSize((int) (mDialHeight * scale), heightMeasureSpec));
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mTotalWidth = w;
		mTotalHeight = h;
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		int x = mTotalWidth / 2;
		int y = mTotalHeight / 2;

		boolean animationFinished = true;
		if (mCurrentAngle != mAngle)
		{
			boolean reverse = mAngle < mCurrentAngle;
			long duration = System.nanoTime() - mAnimationStartTime;
			int delta = (int) FloatMath.ceil((sAnglePerNanoSecond * duration));
			if (reverse)
			{
				delta = -delta;
			}
			mCurrentAngle += delta;
			if ((reverse && mCurrentAngle < mAngle) || (!reverse && mCurrentAngle > mAngle))
			{
				mCurrentAngle = mAngle;
				animationFinished = true;
			}
			else
			{
				animationFinished = false;
			}

		}
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
		canvas.rotate(mCurrentAngle, x, y);
		final Drawable pointer1 = mBitmapPointer1, pointer2 = mBitmapPointer2;
		w = pointer1.getIntrinsicWidth();
		h = pointer1.getIntrinsicHeight();
		pointer1.setBounds(x - (w / 2), y - h, x + (w / 2), y);
		// canvas.drawRect(pointer1.getBounds(), mPaintPointerBox1);
		// logger.debug("pointer1 bounds:{}", pointer1.getBounds());
		pointer1.draw(canvas);
		w = pointer2.getIntrinsicWidth();
		h = pointer2.getIntrinsicHeight();
		pointer2.setBounds(x - w / 2, y, x + w / 2, y + h);
		// logger.debug("pointer2 bounds:{}", pointer2.getBounds());
		// canvas.drawRect(pointer2.getBounds(), mPaintPointerBox2);
		pointer2.draw(canvas);
		canvas.restore();

		if (scaled)
		{
			canvas.restore();
		}
		if (!animationFinished)
		{
			invalidate();
		}
	}
}
