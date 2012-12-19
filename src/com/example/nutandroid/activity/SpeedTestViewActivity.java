package com.example.nutandroid.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.nutandroid.R;
import com.example.nutandroid.view.GaugeView;

public class SpeedTestViewActivity extends RoboActivity implements OnClickListener
{

	@InjectView(R.id.gauge)
	private GaugeView gauge;

	@InjectView(R.id.btnGauge)
	private Button btnGauge;

	private final int[] angles = new int[] { 0, 60, 180, 359, 0, 180, 60, 270, 0, 350, 40 };
	private int i = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_speed_test_view);
		btnGauge.setOnClickListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if (v == btnGauge)
		{
			btnGaugeOnclick();
		}
	}

	private void btnGaugeOnclick()
	{
		gauge.setPointerAngle(angles[i++]);
		if (i == angles.length)
		{
			i = 0;
		}
	}

}
