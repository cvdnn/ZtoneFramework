package android.framework.push.entity;

import org.json.JSONObject;

import android.framework.entity.FindJNode;
import android.os.Parcel;

public class ResultMEEntity extends MEEntity {
	private static final long serialVersionUID = 1L;

	public static final int RESULT_TIMEOUT = -9;

	@FindJNode(jpath = "$code")
	public int code;

	@FindJNode(jpath = "$msg")
	public String message;

	@FindJNode(jpath = "$t")
	public long t;

	@FindJNode(jpath = "$msgId")
	public String messageId;

	@Override
	public ResultMEEntity parse(JSONObject jsonData) {
		super.parse(jsonData);

		return this;
	}

	@Override
	public ResultMEEntity readFromParcel(Parcel src) {
		super.readFromParcel(src);

		code = src.readInt();
		message = src.readString();
		t = src.readLong();
		messageId = src.readString();

		return this;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		dest.writeInt(code);
		writeString(dest, message);
		dest.writeLong(t);
		writeString(dest, messageId);
	}
}
