package com.example.nutandroid.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.widget.AnalogClock;

import com.example.nutandroid.R;

public class SimpleTestActivity extends RoboActivity

{

	@InjectView(R.id.analogClock1)
	private AnalogClock clock; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_simple_test);
	}

}
