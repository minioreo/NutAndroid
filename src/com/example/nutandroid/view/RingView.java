package com.example.nutandroid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.SweepGradient;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.View;

public class RingView extends View
{

	private final static Logger logger = LoggerFactory.getLogger(RingView.class);

	private Paint backgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint ringOutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint ringInPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private RectF outterRect = new RectF();
	private RectF innerRect = new RectF();

	public RingView(Context context)
	{
		super(context);
		initPaint();
	}

	public RingView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initPaint();
	}

	public RingView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint();
	}

	private void initPaint()
	{
		backgroundPaint.setColor(Color.BLACK);
		backgroundPaint.setStyle(Paint.Style.FILL);

		ringOutPaint.setARGB(200, 255, 255, 0);
		ringOutPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		// LinearGradient gradiantShader = new LinearGradient(0, 0, 100, 100,
		// Color.RED, Color.WHITE, TileMode.REPEAT);

		ringInPaint.set(ringOutPaint);
		ringInPaint.setARGB(100, 0, 255, 255);
		ringInPaint.setStyle(Paint.Style.STROKE);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh)
	{
		super.onSizeChanged(w, h, oldw, oldh);
		logger.debug("onSizeChanged:w:{},h:{},oldw:{},oldh:{}", new Integer[] { w, h, oldw, oldh });
		int radius = w < h ? w : h, halfRadius = radius / 2;
		SweepGradient sweepGradient = new SweepGradient(halfRadius, halfRadius, new int[] { Color.RED, Color.GREEN, Color.BLUE, Color.GRAY, Color.WHITE }, new float[] { 0, 0.25f, 0.5f, 0.75f, 1 });
		ringOutPaint.setShader(sweepGradient);
		// RadialGradient radialGradient = new RadialGradient(halfRadius,
		// halfRadius, halfRadius, Color.GREEN, Color.WHITE, TileMode.REPEAT);
		// ringOutPaint.setShader(radialGradient);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// TODO Auto-generated method stub
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		logger.debug("onMeasure");
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		logger.debug("ondraw");
		canvas.drawPaint(backgroundPaint);
		int width = getMeasuredWidth(), height = getMeasuredHeight();
		float radius = width < height ? width : height, radius2 = 0.5f * radius;
		float innerLeft = (radius - radius2) / 2, innerRight = innerLeft + radius2, innerTop = innerLeft, innerBottom = innerTop + radius2;
		float startAngel = 100f, sweepAngel = 340;

		outterRect.set(0, 0, radius, radius);
		canvas.drawArc(outterRect, startAngel, sweepAngel, true, ringOutPaint);

		innerRect.set(innerLeft, innerTop, innerRight, innerBottom);
		canvas.drawArc(innerRect, startAngel, sweepAngel, true, ringInPaint);

		canvas.drawArc(innerRect, startAngel, sweepAngel, false, backgroundPaint);
	}
}
