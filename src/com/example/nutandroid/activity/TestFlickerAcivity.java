package com.example.nutandroid.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

import com.example.nutandroid.R;

public class TestFlickerAcivity extends TabActivity
{

	private TabHost tabHost;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_test_flicker_acivity);
		tabHost = getTabHost();
		createTabs();
	}

	private void createTabs()
	{
		Intent tab1Intent = new Intent(this, Tab1Activity.class);
		tab1Intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		TabSpec tab1 = tabHost.newTabSpec("TAB1").setIndicator("tab1").setContent(tab1Intent);
		tabHost.addTab(tab1);
		
		Intent tab2Intent = new Intent(this,Tab2Activity.class);
		tab2Intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
		TabSpec tab2 = tabHost.newTabSpec("TAB2").setIndicator("tab2").setContent(tab2Intent);
		tabHost.addTab(tab2);
	}
}
