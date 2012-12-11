package com.example.nutandroid.activity;

import android.app.Activity;
import android.os.Bundle;

import com.example.nutandroid.R;

public class GestureSurfaceActivity extends Activity
{

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_gesture_surface);
	}


	@Override
	public void onBackPressed()
	{
		super.onBackPressed();
		//overridePendingTransition(0, 0);
	}
}
