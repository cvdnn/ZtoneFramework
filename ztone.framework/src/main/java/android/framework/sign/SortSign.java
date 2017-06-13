package android.framework.sign;

import java.util.Map;

public class SortSign implements Sign {

	@Override
	public native String generate(Map<String, String> metaMap) ;

}
