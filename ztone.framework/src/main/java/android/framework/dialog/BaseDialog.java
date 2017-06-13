/**
 * 
 */
package android.framework.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

/**
 * @author Linzh
 *
 */
public class BaseDialog extends Dialog{
	private Context context;

	public BaseDialog(Context context) {
		super(context);
		this.context = context;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
	}

}
