package android.framework.binary;

import static android.framework.binary.BinaryUtils.RST_SUCCESS;

public class ResultProtocol extends BinaryProtocol {
	public final static byte FLAG_NONE = 0x00;
	public final static byte FLAG_DOING = 0x01;
	public final static byte FLAG_END = 0x02;
	public final static byte FLAG_FINISH = 0x7F;
	public final static byte FLAG_ERROR = 0x70;
	public final static byte FLAG_TEXT_ERROR = 0x71;
	public final static byte FLAG_BCC_ERROR = 0x72;
	public final static byte FLAG_LEN_ERROR = 0x73;

	/** 整条指令(ASCII码) */
	protected String mCommandText;

	/** 解析步骤 */
	protected byte mStep;

	public boolean success() {

		return RST_SUCCESS.equals(code);
	}

	/**
	 * 判断解析后的指令是否是有效的
	 * 
	 * @return
	 */
	@Override
	public boolean check() {

		return mStep == FLAG_FINISH && super.check();
	}

	public int getFlag() {

		return mStep;
	}

	@Override
	public String toString() {

		return mCommandText;
	}

}
