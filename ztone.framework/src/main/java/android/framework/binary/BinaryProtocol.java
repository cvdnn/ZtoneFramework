package android.framework.binary;

import android.assist.Assert;
import android.framework.C;
import android.framework.module.Validator;
import android.log.Log;
import android.math.Maths;

public abstract class BinaryProtocol implements Validator {
	private static final String TAG = "BinaryProtocol";

	public static final int SESSION_NONE = -1;

	private static byte mProtocolSession;

	/* *********************************************
	 * 
	 * ********************************************
	 */

	/** 会话 */
	public byte session = SESSION_NONE;

	/** 命令 */
	public String code;

	/** 指令内容 */
	public String content;

	public BinaryProtocol() {

	}

	public BinaryProtocol(String code) {
		this.code = code;
	}

	public byte nextSession() {

		return ++mProtocolSession;
	}

	public String getContentText() {
		String text = "";

		try {
			text = new String(Maths.toByte(content), C.value.encoding);
		} catch (Exception e) {
			Log.v(TAG, e);
		}

		return text;
	}

	/**
	 * 判断解析后的指令是否是有效的
	 * 
	 * @return
	 */
	@Override
	public boolean check() {

		return Assert.notEmpty(code);
	}
}
