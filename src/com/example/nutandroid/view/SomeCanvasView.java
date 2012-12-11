package com.example.nutandroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

public class SomeCanvasView extends View
{

	private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public SomeCanvasView(Context context)
	{
		super(context);
		init();
	}

	public SomeCanvasView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		init();
	}

	public SomeCanvasView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		init();
	}

	private void init()
	{
		mPaint.setStyle(Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		mPaint.setColor(Color.RED);
		canvas.drawRect(10, 10, 30, 30, mPaint);
		canvas.save();
		mPaint.setColor(Color.BLUE);
		canvas.rotate(45);
		canvas.drawRect(10, 10,110,110,mPaint);
		canvas.restore();
		canvas.drawRect(40, 40, 60, 60, mPaint);
	}

}
