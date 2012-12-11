package com.example.nutandroid.activity;

import com.example.nutandroid.R;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TabHost.TabSpec;

public class TabShowActivity extends TabActivity
{
	private TabHost tabHost;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tab_show);
		//getTabWidget().setOrientation(TabWidget.VERTICAL);
		tabHost = getTabHost();
		initTabs();
	}

	private void initTabs()
	{
		TabSpec tabSpec = tabHost.newTabSpec("tabHome").setIndicator(getPictureIndicator(R.drawable.home_drawable)).setContent(R.id.tabTV1);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabShop").setIndicator(getPictureIndicator(R.drawable.shop_drawable)).setContent(new Intent(this, RelativePositionActivity.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabSpeed").setIndicator(getPictureIndicator(R.drawable.speed_drawable)).setContent(new Intent(this, RingViewActivity.class));
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabDoc").setIndicator(getPictureIndicator(R.drawable.doc_drawable)).setContent(R.id.tabImg1);
		tabHost.addTab(tabSpec);

		tabSpec = tabHost.newTabSpec("tabMore").setIndicator(getPictureIndicator(R.drawable.more_drawable)).setContent(new Intent(this, MainActivity.class));
		tabHost.addTab(tabSpec);
	}

	private View getPictureIndicator(int resID)
	{
		ImageView iv = new ImageView(this);
		iv.setLayoutParams(new LayoutParams(96, 96));
		iv.setBackgroundResource(resID);
		return iv;
	}

}
