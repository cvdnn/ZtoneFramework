/*
 * MenuEntity.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.entity;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import android.json.JSONUtils;
import android.log.Log;

/**
 * 
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-10-20
 */
public class MenuEntity extends PullEntity {
	private static final String TAG = "SmartHomeMenuEntity";

	private static final long serialVersionUID = 1L;

	private static final String JPATH_MENUS = "/menus";

	@FindJNode
	public int index;

	@FindJArray(jpath = "/menus", meta = Menu.class)
	public ArrayList<Menu> dataList;

	@Override
	public MenuEntity parse(JSONObject jsonData) {
		super.parse(jsonData);

		if (check()) {
			JSONArray jsonList = JSONUtils.getJSONArray(jsonData, JPATH_MENUS);
			if (jsonList != null) {
				int len = jsonList.length();
				if (len > 0) {
					dataList = new ArrayList<Menu>(len);

					try {
						for (int i = 0; i < len; i++) {
							dataList.add((Menu) (new Menu().parse(jsonList.getJSONObject(i))));
						}
					} catch (Exception e) {
						Log.e(TAG, e);
					}
				}
			}

		}

		return this;
	}

	public static class Menu extends Entity {
		private static final long serialVersionUID = 1L;

		@FindJNode
		public String code;

		@FindJNode
		public String title;

		@FindJNode
		public String icon;

		@FindJNode
		public String clazz;

		@FindJNode
		public int flag;

	}
}
