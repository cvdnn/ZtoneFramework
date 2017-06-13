/*
 * Sign.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.sign;

import java.util.Map;

/**
 * 
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-11-9
 */
public interface Sign {

	String generate(Map<String, String> metaMap);
}
