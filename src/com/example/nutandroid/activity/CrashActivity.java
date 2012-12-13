package com.example.nutandroid.activity;

import roboguice.activity.RoboActivity;
import roboguice.inject.ContentView;
import roboguice.inject.InjectView;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.nutandroid.R;
import com.example.nutandroid.util.NutLogger;

@ContentView(R.layout.activity_crash)
public class CrashActivity extends RoboActivity implements OnClickListener
{
	private final static NutLogger logger = NutLogger.getLogger(CrashActivity.class);

	@InjectView(R.id.btnCrash)
	private Button btnCrash;
	
	private Object nullPointer;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		btnCrash.setOnClickListener(this);
//		logger.debug("application context equals activity context:{}", this.c)
	}

	@Override
	public void onClick(View arg0)
	{
		doSth();
		
	}

	private void MakeException()
	{
		nullPointer.toString();
	}

	private void doSth()
	{
		MakeException();		
	}

}
