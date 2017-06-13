/*
 * Page.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.entity;


/**
 *
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-10-21
 */
public class Page extends Entity {
	private static final long serialVersionUID = 1L;

	/** 当前页数 */
	@FindJNode
	public int index;

	/** 每页条数 */
	@FindJNode
	public int size;

	/** 总条数 */
	@FindJNode
	public int total;

    public void set(Page page) {
        if (page != null) {
            index = page.index;
            size = page.size;
            total = page.total;
        }
    }

    public void clear() {
        index = 0;
        size = 0;
        total = 0;
    }
}
