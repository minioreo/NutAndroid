package com.example.nutandroid.view;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.nutandroid.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.View;

public class PieView extends View
{
	private static final int mColorInnerCircle = Color.rgb(224, 220, 205);

	private static final int mColorUnUsed = Color.rgb(42, 157, 240);

	private static final int mColorUsed = Color.rgb(241, 219, 38);

	private static final int mColorNew = Color.rgb(189, 251, 57);

	private static final int mColorOutCircle = Color.rgb(200, 199, 197);

	private static final Logger logger = LoggerFactory.getLogger(PieView.class);

	private Paint mStrokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mGradientFillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private RectF mOuterCircleRoundRect = new RectF();
	private float mCenterX, mCenterY, d, d1, d2, d3 = 14, offsetPixels = 2;
	private final float mIconWidth = 35, mIconTextSpace = 3, mIconPaddingBottom = 5;
	private float mStart1, mSweep1, mStart2, mSweep2, mStart3, mSweep3;
	private RectF mPieRect1 = new RectF(), mPieRect2 = new RectF(), mPieRect3 = new RectF();
	private RectF mIconRect1 = new RectF(), mIconRect2 = new RectF(), mIconRect3 = new RectF();
	private static final float floatPI2 = (float) Math.PI * 2;

	private float mTotalWidth, mTotalHeight;

	private boolean mHaveData;

	private float mTextHeight;

	private String mStringUsed;

	private String mStringUnUsed;

	private String mStringNew;

	private float mTextStartY;

	private float mTextStartX1;

	private float mTextStartX2;

	private float mTextStartX3;

	public PieView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public PieView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	public PieView(Context context)
	{
		super(context);
		init();
	}

	private void init()
	{
		mFillPaint.setStyle(Paint.Style.FILL);
		mStrokePaint.setStyle(Paint.Style.STROKE);
		mGradientFillPaint.setStyle(Paint.Style.FILL);
		mTextPaint.setStyle(Paint.Style.STROKE);
		mTextPaint.setTextSize(15);
		mTextHeight = Math.abs(mTextPaint.descent() - mTextPaint.ascent());
		mStringUsed = getResources().getString(R.string.PV_TXT_USED);
		mStringUnUsed = getResources().getString(R.string.PV_TXT_UNUSED);
		mStringNew = getResources().getString(R.string.PV_TXT_NEW);

	}

	public void loadData(float percent1, float percent2, float percent3)
	{
		mStart1 = 0;
		mSweep1 = 360 * percent1;
		mStart2 = mStart1 + mSweep1;
		mSweep2 = 360 * percent2;
		mStart3 = mStart2 + mSweep2;
		mSweep3 = 360 * percent3;
		mHaveData = true;
		calcPositions();
		invalidate();
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(measureWidth(widthMeasureSpec), measureHeight(heightMeasureSpec));
	}

	protected int measureWidth(int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		logger.debug("measureWidth,mode:{},size:{}", Integer.toHexString(specMode), specSize);

		// int result = specSize;
		// if (specMode == MeasureSpec.AT_MOST)
		// {
		// result = Math.min(result, specSize);
		// }
		// else if (specMode == MeasureSpec.EXACTLY)
		// {
		// result = specSize;
		// }
		//
		// return result;
		return specSize;
	}

	protected int measureHeight(int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		logger.debug("measureHeight,mode:{},size:{}", Integer.toHexString(specMode), specSize);

		// int result = specSize;
		// if (specMode == MeasureSpec.AT_MOST)
		// {
		// result = specSize;
		// }
		// else if (specMode == MeasureSpec.EXACTLY)
		// {
		//
		// result = specSize;
		// }
		//
		// return result;
		return specSize;
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		mTotalWidth = w;
		mTotalHeight = h;
		calcPositions();
	}

	private void calcPositions()
	{
		if (mTotalHeight == 0 || mTotalWidth == 0)
		{
			return;
		}
		d = Math.min(mTotalHeight - mTextHeight - mIconPaddingBottom, mTotalWidth);
		d1 = d * 0.8f;
		d2 = 0.9f * d1;
		float pieAreaHeight = mTotalHeight - mTextHeight - mIconPaddingBottom;
		mCenterX = mTotalWidth / 2.0f;
		mCenterY = pieAreaHeight / 2.0f;

		// 设置渐变
		mOuterCircleRoundRect.set(0, 0, d1, d1);
		mOuterCircleRoundRect.offset((d - d1) / 2.0f, (d - d1) / 2.0f);

		mPieRect1.set(0, 0, d2, d2);
		mPieRect1.offset((mTotalWidth - d2) / 2, (pieAreaHeight - d2) / 2);
		mPieRect2.set(mPieRect1);
		mPieRect3.set(mPieRect1);

		logger.debug("mPieRect origin:{}", mPieRect1);

		float middleAngel1, middleAngel2, middleAngel3;
		middleAngel1 = mStart1 + mSweep1 / 2;
		middleAngel2 = mStart2 + mSweep2 / 2;
		middleAngel3 = mStart3 + mSweep3 / 2;

		mPieRect1.offset(offsetPixels * FloatMath.cos(middleAngel1 * floatPI2 / 360), offsetPixels * FloatMath.sin(middleAngel1 * floatPI2 / 360));
		mPieRect2.offset(offsetPixels * FloatMath.cos(middleAngel2 * floatPI2 / 360), offsetPixels * FloatMath.sin(middleAngel2 * floatPI2 / 360));
		mPieRect3.offset(offsetPixels * FloatMath.cos(middleAngel3 * floatPI2 / 360), offsetPixels * FloatMath.sin(middleAngel3 * floatPI2 / 360));

		RadialGradient gradient = new RadialGradient(mPieRect3.centerX(), mPieRect3.centerY(), d2 / 2, Color.rgb(129, 226, 35), Color.rgb(201, 254, 58), TileMode.CLAMP);
		mGradientFillPaint.setShader(gradient);

		float stringLength1 = mTextPaint.measureText(mStringUsed);
		float stringLength2 = mTextPaint.measureText(mStringUnUsed);
		float stringLength3 = mTextPaint.measureText(mStringNew);

		float textTop = mTotalHeight - mTextHeight - mIconPaddingBottom;
		mTextStartY = textTop - mTextPaint.ascent();

		float usedWidth = 3 * mIconWidth + 3 * mIconTextSpace + stringLength1 + stringLength2 + stringLength3;
		float spareWidth = mTotalWidth - usedWidth;
		float space = spareWidth / 4;

		mIconRect1.set(space, textTop, space + mIconWidth, textTop + mTextHeight);

		mTextStartX1 = mIconRect1.right + mIconTextSpace;

		mIconRect2.set(mIconRect1);
		mIconRect2.offset(mIconWidth + mIconTextSpace + stringLength1 + space, 0);

		mTextStartX2 = mIconRect2.right + mIconTextSpace;

		mIconRect3.set(mIconRect2);
		mIconRect3.offset(mIconWidth + mIconTextSpace + stringLength2 + space, 0);

		mTextStartX3 = mIconRect3.right + mIconTextSpace;

	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// 背景色
		canvas.drawColor(Color.WHITE);
		if (!mHaveData)
		{
			return;
		}
		// 外层灰色空心圆
		mStrokePaint.setColor(mColorOutCircle);
		canvas.drawCircle(mCenterX, mCenterY, d1 / 2, mStrokePaint);
		// 外层实心圆
		mFillPaint.setColor(Color.WHITE);
		canvas.drawCircle(mCenterX, mCenterY, d1 / 2, mFillPaint);

		// 饼图
		mFillPaint.setColor(mColorUsed); // 黄
		canvas.drawArc(mPieRect1, mStart1, mSweep1, true, mFillPaint);
		mFillPaint.setColor(mColorUnUsed); // 蓝
		canvas.drawArc(mPieRect2, mStart2, mSweep2, true, mFillPaint);
		// mFillPaint.setColor(Color.rgb(129, 226, 35)); //绿
		canvas.drawArc(mPieRect3, mStart3, mSweep3, true, mGradientFillPaint);
		// 中心白色圆
		mFillPaint.setColor(mColorInnerCircle);
		canvas.drawCircle(this.mCenterX, this.mCenterY, d3, mFillPaint);

		// Icon 1
		mFillPaint.setColor(mColorUsed);
		canvas.drawRect(mIconRect1, mFillPaint);

		// Text 1
		canvas.drawText(mStringUsed, mTextStartX1, mTextStartY, mTextPaint);

		// Icon2
		mFillPaint.setColor(mColorUnUsed);
		canvas.drawRect(mIconRect2, mFillPaint);

		// Text2
		canvas.drawText(mStringUnUsed, mTextStartX2, mTextStartY, mTextPaint);

		// Icon3
		mFillPaint.setColor(mColorNew);
		canvas.drawRect(mIconRect3, mFillPaint);

		// Text3
		canvas.drawText(mStringNew, mTextStartX3, mTextStartY, mTextPaint);
	}
}
