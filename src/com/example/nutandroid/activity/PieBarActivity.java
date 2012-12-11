package com.example.nutandroid.activity;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.example.nutandroid.R;
import com.example.nutandroid.model.BarItem;
import com.example.nutandroid.view.BarView;
import com.example.nutandroid.view.PieView;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;

public class PieBarActivity extends Activity implements OnClickListener
{
	
	private PieView mPie;
	private BarView mBar;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pie_bar);
		mPie = (PieView) findViewById(R.id.nutPie);
		mBar = (BarView) findViewById(R.id.nutBar);
		generateRandomPie();
		generateRandomBar();
		mPie.setOnClickListener(this);
	}

	private void generateRandomBar()
	{
		BarItem[] barItems = new BarItem[20];
		SimpleDateFormat sdf = new SimpleDateFormat("MM/dd");
		Date d = new Date();
		for (int i = 0; i < barItems.length; i++)
		{
			String dateStr = sdf.format(d);
			float percent = (float) Math.random();
			String topText = MessageFormat.format("{0,number,#}MB", percent * 100);
			barItems[i] = new BarItem(dateStr, percent, topText);
			d.setTime(d.getTime() + 1 * 1000 * 60 * 60 * 24);
		}
		mBar.loadData(barItems, true);
	}

	private void generateRandomPie()
	{
		float percent1 = (float) Math.random();
		float percent2 = (float) (Math.random() * (1 - percent1));
		float percent3 = 1 - percent1 - percent2;
		mPie.loadData(percent1, percent2, percent3);
	}

	@Override
	public void onClick(View v)
	{
		generateRandomPie();
		generateRandomBar();
	}
	
}
