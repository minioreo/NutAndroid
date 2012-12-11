package com.example.nutandroid.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.example.nutandroid.R;

public class RelativePositionActivity extends Activity
{

	private Button btnOK;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_relative_position);
		btnOK = (Button) findViewById(R.id.btnOK);
		btnOK.setOnClickListener(new OnClickListener()
		{

			@Override
			public void onClick(View v)
			{
				startActivity(new Intent(RelativePositionActivity.this, RingViewActivity.class));

			}
		});
	}

}
