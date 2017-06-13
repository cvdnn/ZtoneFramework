/*
 * PageEntity.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.entity;

import android.json.JSONUtils;
import android.log.Log;

import org.json.JSONObject;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-10-21
 */
@FindEntity(inject = false)
public abstract class PageEntity extends PullEntity {
    private static final String TAG = "PageEntity";

    private static final long serialVersionUID = 1L;

    public static final String JPATH_PAGE = "/page";

    public final Page page = new Page();

    @Override
    public PageEntity parse(JSONObject jsonData) {
        super.parse(jsonData);

        if (check()) {
            JSONObject jsonPage = JSONUtils.getJSONObject(jsonData, JPATH_PAGE);
            if (jsonPage != null) {
                page.parse(jsonPage);
            }

            Log.d(TAG, "PAGE: " + (jsonPage == null ? "null" : jsonPage.toString()));
        }

        return this;
    }
}
