package com.example.nutandroid.view;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.nutandroid.model.BarItem;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;


public class BarView extends SurfaceView implements Runnable, Callback, OnGestureListener
{
	private final int bgColor = Color.WHITE;

	private static final Logger logger = LoggerFactory.getLogger(BarView.class);

	private Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private Paint mTextBackgroundPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

	private int mTotalWidth;

	private float mBarItemMinWidth = 20;

	private int mTotalHeight;

	private final int mSpace = 2; // 上下文字和柱状图的间距

	private float mTextHeight; // 文字高度

	private RectF mTextBackgroudRect = new RectF(); // 文字区域背景

	private float mMarginSpace = 5; // 柱之间的间距

	private boolean mDrawingThreadIsRunning = false;

	private final float mAnimationDuration = 600;

	private boolean mAnimationFinished = true;;

	private float mTextAscent;

	private SurfaceHolder mHolder;

	private long mAnimationStartTime;

	private GestureDetector mGestureDetector;

	private ArrayList<BarItemInner> mBarItems = new ArrayList<BarItemInner>();

	private float mItemsAllWidth;

	private float mBottomTextStartY;

	private float mCurrentCanvasOffsetX;

	private float mMaxCanvasOffset;

	private float mBarAreaHeight; // 柱的总高度

	private float mBarAreaBottomY; // 柱的底部Y坐标

	public BarView(Context context)
	{
		super(context);
		init();
	}

	public BarView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		readAttrs(context, attrs);
		init();
	}

	public BarView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
		readAttrs(context, attrs);
		init();
	}

	private void readAttrs(Context context, AttributeSet attrs)
	{
//		TypedArray array = context.getTheme().obtainStyledAttributes(attrs, R.styleable.BarItemView, 0, 0);
//		try
//		{
//			setMarginSpace(array.getDimension(R.styleable.BarItemView_marginSpace, 5));
//		}
//		finally
//		{
//			array.recycle();
//		}
	}

	public void setItemMinWidth(float itemWidth)
	{
		mBarItemMinWidth = itemWidth;
	}

	public void setMarginSpace(float marginSpace)
	{
		mMarginSpace = marginSpace;
	}

	private void init()
	{
		mTextPaint.setStyle(Paint.Style.FILL_AND_STROKE);
		mTextPaint.setColor(Color.BLACK);
		mTextPaint.setTextSize(20);
		mTextPaint.setTextAlign(Align.CENTER);
		mTextBackgroundPaint.setStyle(Paint.Style.FILL_AND_STROKE);

		FontMetrics fontMetrics = mTextPaint.getFontMetrics();
		mTextHeight = FloatMath.ceil(fontMetrics.descent - fontMetrics.ascent); // 对应字体的文字高度
		mTextAscent = Math.abs(fontMetrics.ascent);
		LinearGradient textBackgroundGradient = new LinearGradient(0, 0, 0, mTextHeight, new int[] { Color.rgb(231, 241, 242), Color.rgb(215, 229, 229), Color.rgb(231, 241, 242) }, new float[] { 0, 0.5f, 1 }, TileMode.REPEAT);
		mTextBackgroundPaint.setShader(textBackgroundGradient);

		getHolder().addCallback(this);

		mGestureDetector = new GestureDetector(this);
	}

	public void loadData(BarItem[] items, boolean useAnimation)
	{
		synchronized (mBarItems)
		{
			mBarItems.clear();
			float totalPos = -mMarginSpace;
			for (int i = 0; i < items.length; i++)
			{
				BarItemInner itemEx = new BarItemInner(items[i]);
				float belowTextWidth = mTextPaint.measureText(itemEx.belowText);
				float topTextWidth = mTextPaint.measureText(itemEx.topText);
				float maxTextWidth = Math.max(belowTextWidth, topTextWidth);
				itemEx.width = Math.max(mBarItemMinWidth, maxTextWidth);
				// logger.debug("belowWidth:{},topWidth:{},minWidth:{}",
				// belowTextWidth, topTextWidth, mBarItemMinWidth);
				LinearGradient gradient = new LinearGradient(0, 0, itemEx.width, 0, new int[] { Color.rgb(11, 207, 255), Color.rgb(61, 243, 255), Color.rgb(11, 207, 255) }, new float[] { 0, 0.5f, 1 }, TileMode.REPEAT);
				itemEx.paint.setShader(gradient);
				itemEx.leftPos = totalPos + mMarginSpace;
				totalPos = totalPos + mMarginSpace + itemEx.width;
				if (useAnimation)
				{
					itemEx.currentPercentInAnimation = 0;
					itemEx.isAnimating = true;
				}
				mBarItems.add(itemEx);
			}
			mItemsAllWidth = Math.max(0, totalPos);
			logger.debug("totalWidth:{}", mItemsAllWidth);
			// logger.debug("mBarItems:{}", mBarItems);
			calcItemSize();
			if (useAnimation)
			{
				this.mAnimationFinished = false;
				mAnimationStartTime = new Date().getTime();
			}
			synchronized (this)
			{
				notify();
			}
		}

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
		return specSize;
	}

	protected int measureHeight(int measureSpec)
	{
		int specMode = MeasureSpec.getMode(measureSpec);
		int specSize = MeasureSpec.getSize(measureSpec);

		logger.debug("measureHeight,mode:{},size:{}", Integer.toHexString(specMode), specSize);

//		int result = specSize;
//		if (specMode == MeasureSpec.AT_MOST)
//		{
//			result = Math.min(result, specSize);
//		}
//		else if (specMode == MeasureSpec.EXACTLY)
//		{
//			result = specSize;
//		}
//
//		return result;
		return specSize;
	}

	private void calcItemSize()
	{
		float bottomTextTopPosition = mTotalHeight - mTextHeight;
		mBottomTextStartY = bottomTextTopPosition + mTextAscent; // 底部文字的基线起始坐标
		mTextBackgroudRect.set(0, bottomTextTopPosition, mItemsAllWidth, mTotalHeight); // 底部文字的背景色区域
		mBarAreaHeight = mTotalHeight - 2 * mTextHeight - 2 * mSpace;
		mBarAreaBottomY = mTotalHeight - mTextHeight - mSpace;
		mCurrentCanvasOffsetX = mTotalWidth - mItemsAllWidth;
		mMaxCanvasOffset = mTotalWidth - mItemsAllWidth;
//		logger.debug("mBottomTextStartY:{},mAllWidth:{},mTotalWidth:{},mCurrentCanvasOffsetX:{}", mBottomTextStartY, mItemsAllWidth, mTotalWidth, mCurrentCanvasOffsetX);
	}

	protected void drawItemStatic()
	{
		synchronized (mBarItems)
		{
			Canvas canvas = mHolder.lockCanvas();
			canvas.translate(mCurrentCanvasOffsetX, 0);
			canvas.drawColor(bgColor);
			canvas.drawRect(mTextBackgroudRect, mTextBackgroundPaint);
			for (int i = 0; i < mBarItems.size(); i++)
			{
				BarItemInner item = mBarItems.get(i);
				canvas.drawText(item.belowText, item.leftPos + item.width / 2, mBottomTextStartY, mTextPaint);
				float barTop = mBarAreaBottomY - mBarAreaHeight * item.percent;
				canvas.drawRect(item.leftPos, barTop, item.leftPos + item.width, mBarAreaBottomY, item.paint);
				canvas.drawText(item.topText, item.leftPos + item.width / 2, barTop - mSpace - mTextHeight + mTextAscent, mTextPaint);
			}
			mHolder.unlockCanvasAndPost(canvas);
		}

		try
		{
			synchronized (this)
			{
				wait();
			}
		}
		catch (InterruptedException e)
		{
			logger.error("wait interrupted.", e);
		}
	}

	private void prepareAnimation()
	{
		long currentTime = new Date().getTime();
		long currentDuration = currentTime - mAnimationStartTime;
		float animationPercent = Math.min(1, currentDuration / mAnimationDuration);
		// logger.debug("animationPercent:{},currentDuration:{},mAnimationDuration:{}",animationPercent,currentDuration,mAnimationDuration);
		mAnimationFinished = (animationPercent == 1);
		cnt++;
		if (mAnimationFinished)
		{
			logger.debug("currentDuration:{},cnt:{}", currentDuration, cnt);
		}

		for (int i = 0; i < mBarItems.size(); i++)
		{
			BarItemInner item = mBarItems.get(i);
			if (mAnimationFinished)
			{
				item.isAnimating = false;
				item.currentPercentInAnimation = item.percent;
			}
			else
			{
				item.currentPercentInAnimation = item.percent * animationPercent;
			}

		}
	}

	private long cnt = 0;

	protected void drawItemAnimation()
	{
		synchronized (mBarItems)
		{
			prepareAnimation();
			if (mAnimationFinished)
			{
				return;
			}
			Canvas canvas = mHolder.lockCanvas();
			canvas.translate(mCurrentCanvasOffsetX, 0);
			canvas.drawColor(bgColor);
			canvas.drawRect(mTextBackgroudRect, mTextBackgroundPaint);
			for (int i = 0; i < mBarItems.size(); i++)
			{
				BarItemInner item = mBarItems.get(i);
				canvas.drawText(item.belowText, item.leftPos + item.width / 2, mBottomTextStartY, mTextPaint);
				float barTop = mBarAreaBottomY - mBarAreaHeight * item.currentPercentInAnimation;
				canvas.drawRect(item.leftPos, barTop, item.leftPos + item.width, mBarAreaBottomY, item.paint);
			}
			mHolder.unlockCanvasAndPost(canvas);
		}
	}

	@Override
	public void run()
	{
		calcItemSize();
		if (!mAnimationFinished)
		{
			mAnimationStartTime = new Date().getTime();
		}
		while (mDrawingThreadIsRunning)
		{
			if (mAnimationFinished)
			{
				drawItemStatic();
			}
			else
			{
				drawItemAnimation();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		logger.debug("surfaceChanged,width:{},height:{}", width, height);
		mTotalWidth = width;
		mTotalHeight = height;
		LinearGradient textBackgroundGradient = new LinearGradient(0, 0, 0, mTextHeight, new int[] { Color.rgb(231, 241, 242), Color.rgb(215, 229, 229), Color.rgb(231, 241, 242) }, new float[] { 0, 0.5f, 1 }, TileMode.REPEAT);
		mTextBackgroundPaint.setShader(textBackgroundGradient);
		calcItemSize();

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		logger.debug("surfaceCreated");
		mHolder = holder;
		Thread drawingThread = new Thread(this, "BarItemViewDrawingWorker");
		drawingThread.setDaemon(true);
		mDrawingThreadIsRunning = true;
		drawingThread.start();
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		logger.debug("surfaceDestroy");
		mDrawingThreadIsRunning = false;
		synchronized (this)
		{
			notify();
		}
	}

	@Override
	public boolean onDown(MotionEvent e)
	{
		// logger.debug("onDown");
		return true;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY)
	{
		// logger.debug("onFling,velocityX:{},velocityY:{}", velocityX,
		// velocityY);
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e)
	{
		// logger.debug("onLongPress");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY)
	{
		// logger.debug("onScroll,distanceX:{},distanceY:{}", distanceX,
		// distanceY);
		float newOffsetX = mCurrentCanvasOffsetX - distanceX;
		// logger.debug("currentOffset:{},newOffsetX:{},mMaxCanvasOffset:{}",
		// mCurrentCanvasOffsetX, newOffsetX, mMaxCanvasOffset);
		if (newOffsetX < mMaxCanvasOffset)
		{
			mCurrentCanvasOffsetX = mMaxCanvasOffset;
		}
		else if (newOffsetX > 0)
		{
			mCurrentCanvasOffsetX = 0;
		}
		else
		{
			mCurrentCanvasOffsetX = newOffsetX;
		}
		synchronized (this)
		{
			notify();
		}
		return true;
	}

	@Override
	public void onShowPress(MotionEvent e)
	{
		// logger.debug("onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e)
	{
		// logger.debug("onSingleTapUp");
		return true;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		return mGestureDetector.onTouchEvent(event);
	}
}

class BarItemInner
{
	public String belowText;
	public float percent;
	public String topText;

	public BarItemInner(BarItem item)
	{
		this.belowText = item.getBelowText();
		this.percent = Math.min(1, item.getBarHeightPercent());
		this.topText = item.getTopText();
		this.currentPercentInAnimation = percent;
		this.isAnimating = false;
	}

	public float width;

	public Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);

	public float leftPos;

	public float currentPercentInAnimation;

	public boolean isAnimating;

	@Override
	public String toString()
	{
		return MessageFormat.format("belowText:{0},barHeightPercent:{1},topText:{2},width:{3},leftPos:{4}\r\n", belowText, percent, topText, width, leftPos);
	}

}
