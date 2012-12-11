package com.example.nutandroid.activity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import roboguice.activity.RoboListActivity;
import roboguice.inject.InjectExtra;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.nutandroid.util.MenuMgr.NutMenuItem;
import com.example.nutandroid.util.NutLogger;

public class MenuActivity extends RoboListActivity
{

	private final static NutLogger logger = NutLogger.getLogger(MenuActivity.class);

	private static final String BACK = "Back";
	@InjectExtra("MENU_ITEM")
	private NutMenuItem menuItem;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		ArrayList<HashMap<String, Object>> data = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> backMap = new HashMap<String, Object>();
		backMap.put("TEXT", BACK);
		data.add(backMap);
		List<NutMenuItem> subItems = menuItem.getSubItems();
		logger.info("child size:{}", subItems.size());
		Iterator<NutMenuItem> iterator = subItems.iterator();
		while(iterator.hasNext())
		{
			NutMenuItem item = iterator.next();
			HashMap<String, Object> obj = new HashMap<String, Object>();
			obj.put("TEXT", item.getItemName());
			obj.put("MENU_ITEM", item);
			data.add(obj);
		}
		SimpleAdapter adapter = new SimpleAdapter(this, data, android.R.layout.simple_list_item_1, new String[] { "TEXT" }, new int[] { android.R.id.text1 });
		setListAdapter(adapter);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		HashMap<String, Object> item = (HashMap<String, Object>) getListAdapter().getItem(position);
		if (BACK.equals(item.get("TEXT")))
		{
			finish();
		}
		else
		{
			NutMenuItem menuItem = (NutMenuItem) item.get("MENU_ITEM");
			Intent intent = null;
			if (menuItem.getItemType() == NutMenuItem.ITEM_TYPE_ACTIVITY)
			{
				intent = new Intent(MenuActivity.this, menuItem.getActivityClass());
			}
			else
			{
				intent = new Intent(MenuActivity.this, MenuActivity.class);
				intent.putExtra("MENU_ITEM", menuItem);
			}
			startActivity(intent);
			
//			try
//			{
//				Class<?> clazz = Class.forName(className);
//				Intent intent = new Intent(this, clazz);
//				startActivity(intent);
//			}
//			catch (ClassNotFoundException e)
//			{
//				Toast.makeText(this, "no class configured", Toast.LENGTH_SHORT).show();
//			}
		}
	}

}
