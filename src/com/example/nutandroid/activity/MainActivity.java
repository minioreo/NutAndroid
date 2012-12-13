package com.example.nutandroid.activity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LayoutAnimationController;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.nutandroid.R;
import com.example.nutandroid.content.NutApplication;
import com.example.nutandroid.util.MenuMgr;
import com.example.nutandroid.util.MenuMgr.NutMenuItem;

public class MainActivity extends Activity
{
	private final static Logger logger = LoggerFactory.getLogger(MainActivity.class);

	private ListView lv;

	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		lv = (ListView) findViewById(R.id.lvMain);
		Animation a = new AlphaAnimation(0, 1);
		a.setDuration(200);
		lv.setLayoutAnimation(new LayoutAnimationController(a, 1));
		loadList();
	}

	private void loadList()
	{
		MenuMgr menuMgr = NutApplication.getInstance().getMenuMgr();
		List<NutMenuItem> rootItems = menuMgr.getRootItems();
		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		Iterator<NutMenuItem> iterator = rootItems.iterator();
		while (iterator.hasNext())
		{
			NutMenuItem menuItem = iterator.next();
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("TEXT", menuItem.getItemName());
			map.put("MENU_ITEM", menuItem);
			data.add(map);
		}
		SimpleAdapter sa = new SimpleAdapter(this, data, android.R.layout.simple_list_item_1, new String[] { "TEXT" }, new int[] { android.R.id.text1 });
		lv.setAdapter(sa);
		lv.setOnItemClickListener(new OnItemClickListener()
		{

			@Override
			public void onItemClick(AdapterView<?> av, View v, int pos, long rowid)
			{
				logger.debug("pos:{},rowid:{}", pos, rowid);
				Object item = av.getItemAtPosition(pos);
				logger.debug("item type:{}", item.getClass());
				HashMap<String, Object> mapItem = (HashMap<String, Object>) item;
				NutMenuItem menuItem = (NutMenuItem) mapItem.get("MENU_ITEM");
				Intent intent = null;
				if (menuItem.getItemType() == NutMenuItem.ITEM_TYPE_ACTIVITY)
				{
					intent = new Intent(MainActivity.this, menuItem.getActivityClass());
				}
				else
				{
					intent = new Intent(MainActivity.this, MenuActivity.class);
					intent.putExtra("MENU_ITEM", menuItem);
				}
				// intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
				startActivity(intent);
				overridePendingTransition(0, 0);
			}
		});
	}

}
