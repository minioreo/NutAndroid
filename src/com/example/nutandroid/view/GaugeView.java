package com.example.nutandroid.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

public class GaugeView extends View
{
	private float mAngel = 60;
	private float mDialWidth = 80, mDialHeight = 80;

	public GaugeView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}

	public GaugeView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public GaugeView(Context context)
	{
		super(context);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{

	}
}
