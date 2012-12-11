package com.example.nutandroid.activity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;

import com.example.nutandroid.R;

public class CustomTab extends TabActivity
{
	private static final Logger logger = LoggerFactory.getLogger(CustomTab.class);

	private TabHost tabHost;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_custom_tab);
		getTabWidget().setOrientation(TabWidget.VERTICAL);
		tabHost = getTabHost();
		initTabs();

	}

	private void initTabs()
	{
		TabSpec tabSpec = tabHost.newTabSpec("tabHome").setIndicator(getIndicator("数据")).setContent(R.id.tabTV1);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabShop").setIndicator(getIndicator("语音")).setContent(new Intent(this, RelativePositionActivity.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabSpeed").setIndicator(getIndicator("短信")).setContent(new Intent(this, RingViewActivity.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabDoc").setIndicator(getIndicator("WLAN")).setContent(R.id.tabImg1);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabMore").setIndicator(getIndicator("优惠")).setContent(new Intent(this, MainActivity.class));
		tabHost.addTab(tabSpec);
	}

	private View getIndicator(String text)
	{
		View indicator = getLayoutInflater().inflate(R.layout.indicator, getTabWidget(), false);
		indicator.setTag("text");
		TextView indicatorText = (TextView) indicator.findViewById(R.id.indicator_text);
		indicatorText.setText(text);
		return indicator;
	}
}
