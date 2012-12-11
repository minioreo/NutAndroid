package com.example.nutandroid.view;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

public class Custom2DView extends View
{
	private static final Logger logger = LoggerFactory.getLogger(Custom2DView.class);

	private Paint fillPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint wallPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint ceilPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint floorPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private RectF centerRectangle = new RectF();

	public Custom2DView(Context context)
	{
		super(context);
		initPaint();
	}

	public Custom2DView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		initPaint();
	}

	public Custom2DView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		initPaint();
	}

	private void initPaint()
	{
		logger.debug("begin init");
		fillPaint.setColor(Color.BLUE);
		fillPaint.setStrokeWidth(1);
		fillPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		strokePaint.setColor(Color.RED);
		strokePaint.setStrokeWidth(1);
		strokePaint.setTextSize(30);
		// strokePaint.setTextAlign(Align.CENTER);
		strokePaint.setStyle(Paint.Style.STROKE);

		wallPaint.setColor(Color.rgb(196, 191, 187));
		wallPaint.setStyle(Paint.Style.FILL);

		// ceilPaint.setColor(Color.rgb(220, 218, 216));
		ceilPaint.setColor(Color.YELLOW);
		ceilPaint.setStyle(Paint.Style.FILL);

		floorPaint.setColor(Color.WHITE);
		floorPaint.setStyle(Paint.Style.FILL);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		super.onDraw(canvas);
		// canvas.drawColor(Color.BLUE);
		// canvas.drawText("矮油，来，draw一个", canvas.getWidth()/2,
		// canvas.getHeight()/2, paint);
		int totalWidth = getMeasuredWidth(), totalHeight = getMeasuredHeight(), halfTotalWidth = totalWidth / 2, halfTotalHeight = totalHeight / 2;
		logger.debug("canvas width:{},height:{},measured width:{},height:{}", new Integer[] { canvas.getWidth(), canvas.getHeight(), totalWidth, totalHeight });
		// draw diagonal lines
		canvas.drawLine(0, 0, totalWidth, totalHeight, fillPaint);
		canvas.drawLine(totalWidth, 0, 0, totalHeight, fillPaint);

		// draw a rectangle
		calcCenterRectangle(halfTotalWidth, halfTotalHeight);
		canvas.drawRect(centerRectangle, fillPaint);

		// 画侧面
		Path leftWallPath = new Path(), rightWallPath = new Path();
		leftWallPath.lineTo(centerRectangle.left, centerRectangle.top);
		leftWallPath.lineTo(centerRectangle.left, centerRectangle.bottom);
		leftWallPath.lineTo(0, totalHeight);
		canvas.drawPath(leftWallPath, wallPaint);
		rightWallPath.moveTo(totalWidth, 0);
		rightWallPath.lineTo(centerRectangle.right, centerRectangle.top);
		rightWallPath.lineTo(centerRectangle.right, centerRectangle.bottom);
		rightWallPath.lineTo(totalWidth, totalHeight);
		canvas.drawPath(rightWallPath, wallPaint);

		// 天花板
		Path ceilPath = new Path();
		ceilPath.lineTo(totalWidth, 0);
		ceilPath.lineTo(centerRectangle.right, centerRectangle.top);
		ceilPath.lineTo(centerRectangle.left, centerRectangle.top);
		ceilPath.lineTo(0, 0);
		canvas.drawPath(ceilPath, ceilPaint);

		// 地板
		Path floorPath = new Path();
		floorPath.moveTo(0, totalHeight);
		floorPath.lineTo(centerRectangle.left, centerRectangle.bottom);
		floorPath.lineTo(centerRectangle.right, centerRectangle.bottom);
		floorPath.lineTo(totalWidth, totalHeight);
		canvas.drawPath(floorPath, floorPaint);

		int originColor = strokePaint.getColor();
		// draw another rectangle
		calcCenterRectangle(halfTotalWidth * 1.3f, halfTotalHeight * 1.3f);
		strokePaint.setColor(Color.GREEN);
		canvas.drawRect(centerRectangle, strokePaint);

		// draw the third rectangle
		calcCenterRectangle(halfTotalWidth * 1.5f, halfTotalHeight * 1.5f);
		strokePaint.setColor(Color.RED);
		canvas.drawRect(centerRectangle, strokePaint);

		// draw the fourth rectangle
		calcCenterRectangle(halfTotalWidth * 1.7f, halfTotalHeight * 1.7f);
		strokePaint.setColor(Color.BLUE);
		canvas.drawRect(centerRectangle, strokePaint);

		// restore color
		strokePaint.setColor(originColor);

		// draw a circle
		float r = Math.min(totalWidth, totalHeight) / 4;
		canvas.drawCircle(halfTotalWidth, halfTotalHeight, r, strokePaint);

		// draw another circle
		fillPaint.setColor(Color.RED);
		canvas.drawCircle(halfTotalWidth, halfTotalHeight, r, fillPaint);

		// 内切圆的园内接四边形，白色
		float squareLeft, squareTop, squareRight, squareBottom, halfSideLength;
		halfSideLength = (float) (r / Math.sqrt(2));
		logger.debug("halfSideLength:{}", halfSideLength);
		squareLeft = halfTotalWidth - halfSideLength;
		squareRight = halfTotalWidth + halfSideLength;
		squareTop = halfTotalHeight - halfSideLength;
		squareBottom = halfTotalHeight + halfSideLength;
		RectF rect = new RectF(squareLeft, squareTop, squareRight, squareBottom);
		logger.debug("inner square rect:{}", rect);
		fillPaint.setColor(Color.WHITE);
		canvas.drawRect(rect, fillPaint);

		// 写字
		String msg = "地狱无门";
		Rect bounds = new Rect();
		strokePaint.getTextBounds(msg, 0, msg.length(), bounds);
		logger.debug("bounds is :{}", bounds);
		float msgWidth = Math.abs(bounds.right - bounds.left), msgHeight = Math.abs(bounds.bottom - bounds.top);
		float msgStartX = squareLeft + halfSideLength - msgWidth / 2, msgStartY = squareTop + halfSideLength - msgHeight / 2;
		msgStartY += Math.abs(strokePaint.ascent());
		logger.debug("startX is {},startY is {}", msgStartX, msgStartY);
		canvas.drawText("地狱无门", msgStartX, msgStartY, strokePaint);

//		FontMetrics fontMetrics = strokePaint.getFontMetrics();
//		logger.debug("ascent:{},descent:{},font ascent:{},descent:{}", new Float[] { strokePaint.ascent(), strokePaint.descent(), fontMetrics.ascent, fontMetrics.descent });
//		logger.debug("fontMetrics top:{},bottom:{},leading:{}", new Float[] { fontMetrics.top, fontMetrics.bottom, fontMetrics.leading });
//		// 画baseline
//		strokePaint.setColor(Color.BLUE);
//		canvas.drawLine(0, msgStartY, totalWidth, msgStartY, strokePaint);
//		// 画topline
//		strokePaint.setColor(Color.GREEN);
//		canvas.drawLine(0, msgStartY + fontMetrics.top, totalWidth, msgStartY + fontMetrics.top, strokePaint);
//		// 画 asentline
//		strokePaint.setColor(Color.RED);
//		canvas.drawLine(0, msgStartY + fontMetrics.ascent, totalWidth, msgStartY + fontMetrics.ascent, strokePaint);
//		// 画descentline
//		strokePaint.setColor(Color.BLACK);
//		canvas.drawLine(0, msgStartY + fontMetrics.descent, totalWidth, msgStartY + fontMetrics.descent, strokePaint);
//		// 画bottomline
//		strokePaint.setColor(Color.YELLOW);
//		canvas.drawLine(0, msgStartY + fontMetrics.bottom, totalWidth, msgStartY + fontMetrics.bottom, strokePaint);
		// 重置画笔
		initPaint();
	}

	private void calcCenterRectangle(float rectWidth, float rectHeight)
	{
		float startX = (getMeasuredWidth() - rectWidth) / 2, startY = (getMeasuredHeight() - rectHeight) / 2;
		float endX = (getMeasuredWidth() + rectWidth) / 2, endY = (getMeasuredHeight() + rectHeight) / 2;
		centerRectangle.set(startX, startY, endX, endY);
		logger.debug("rectangle is {}", centerRectangle);
	}
}
