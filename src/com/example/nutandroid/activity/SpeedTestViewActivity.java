package com.example.nutandroid.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.example.nutandroid.R;
import com.example.nutandroid.view.GaugeView;

public class SpeedTestViewActivity extends RoboActivity
{

	@InjectView(R.id.gauge)
	private GaugeView gauge;

	private int duration = 100;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed_test_view);
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				int newAngle = (gauge.getPointerAngle() + 10) % 360;
				gauge.setPointerAngle(newAngle);
				handler.postDelayed(this, duration);
			}
		}, duration);
	}

}
