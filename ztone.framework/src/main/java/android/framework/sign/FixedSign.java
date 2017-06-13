package android.framework.sign;

import java.util.Map;

public class FixedSign implements Sign {

	/**
	 * sign算法
	 * 
	 */
	@Override
	public native String generate(Map<String, String> metaMap);
}
