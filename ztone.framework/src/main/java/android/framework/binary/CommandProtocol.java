package android.framework.binary;

public class CommandProtocol extends BinaryProtocol {

	public CommandProtocol(String code) {
		super(code);
	}

	/**
	 * 按照协议需求组装content部分
	 */
	public String fittedContent() {

		return "";
	}

	protected void fitted() {
		content = fittedContent();
	}
}
