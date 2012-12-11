package com.example.nutandroid.util;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import android.app.Activity;
import android.text.TextUtils;

public class MenuMgr
{
	private ArrayList<NutMenuItem> rootNodes = new ArrayList<MenuMgr.NutMenuItem>();
	private HashMap<String, NutMenuItem> allItems = new HashMap<String, NutMenuItem>();
	private HashSet<String> paths = new HashSet<String>();

	public void addItem(String path, Class<? extends Activity> cls)
	{
		if (TextUtils.isEmpty(path))
		{
			throw new IllegalArgumentException("path is null");
		}
		path = path.replaceAll("^/+", "");
		path = path.replaceAll("/+$", "");
		if (TextUtils.isEmpty(path))
		{
			throw new IllegalArgumentException("path only contains slash");
		}

		if (paths.contains(path))
		{
			throw new IllegalArgumentException("key already exist");
		}
		if (cls == null)
		{
			throw new IllegalArgumentException("class is null");
		}

		int slashIndex = 0;
		NutMenuItem item = null, parentItem = null;
		while ((slashIndex = path.lastIndexOf("/")) != -1)
		{
			String key = path.substring(slashIndex + 1);
			if (item != null)
			{
				NutMenuItem childItem = item;
				item = new NutMenuItem(key);
				item.addSubItem(childItem);
			}
			else
			{
				item = new NutMenuItem(key, cls);
			}
			allItems.put(path, item);
			paths.add(path);
			String parentPath = path.substring(0, slashIndex);

			parentItem = allItems.get(parentPath);
			if (parentItem != null)
			{
				parentItem.addSubItem(item);
				break;
			}
			path = parentPath;
		}

		if (item == null)
		{
			item = new NutMenuItem(path, cls);
			rootNodes.add(item);
			allItems.put(path, item);
			paths.add(path);
		}
		else if (parentItem == null)
		{
			parentItem = new NutMenuItem(path);
			parentItem.addSubItem(item);
			allItems.put(path, parentItem);
			paths.add(path);
			rootNodes.add(parentItem);
		}
	}

	public NutMenuItem getMenuItemByPath(String path)
	{
		return allItems.get(path);
	}

	public List<NutMenuItem> getRootItems()
	{
		return rootNodes;
	}

	public static class NutMenuItem implements Serializable, Comparable<NutMenuItem>
	{
		public static final byte ITEM_TYPE_ACTIVITY = 0;
		public static final byte ITEM_TYPE_CATAGORY = 1;
		private byte itemType;
		private String itemName;
		private String itemPath;
		private Class<? extends Activity> activityClass;
		private ArrayList<NutMenuItem> subItems;

		public NutMenuItem(String catagoryName)
		{
			itemType = ITEM_TYPE_CATAGORY;
			itemName = catagoryName;
			subItems = new ArrayList<NutMenuItem>();
		}

		public NutMenuItem(String activityName, Class<? extends Activity> activityClass)
		{
			itemType = ITEM_TYPE_ACTIVITY;
			itemName = activityName;
			this.activityClass = activityClass;
		}

		public String getItemName()
		{
			return itemName;
		}

		public byte getItemType()
		{
			return itemType;
		}

		public List<NutMenuItem> getSubItems()
		{
			if (itemType == ITEM_TYPE_ACTIVITY)
			{
				throw new UnsupportedOperationException("activity have no sub items.");
			}
			return subItems;
		}

		public void addSubItem(NutMenuItem item)
		{
			if (itemType == ITEM_TYPE_ACTIVITY)
			{
				throw new UnsupportedOperationException("activity have no sub items.");
			}
			subItems.add(item);
		}

		public Class<? extends Activity> getActivityClass()
		{
			if (itemType == ITEM_TYPE_CATAGORY)
			{
				throw new UnsupportedOperationException("catagory have no activity class.");
			}
			return activityClass;
		}

		@Override
		public int compareTo(NutMenuItem another)
		{
			return this.itemName.compareTo(another.itemName);
		}
	}
}
