package android.framework.push.entity;

import org.json.JSONObject;

import android.framework.entity.FindJNode;
import android.os.Parcel;
import android.os.Parcelable;

public class MessageMEEntity extends ResultMEEntity implements Parcelable {
	private static final long serialVersionUID = 1L;
	
	@FindJNode
	public String topic;

	@Override
	public MessageMEEntity parse(JSONObject jsonData) {
		super.parse(jsonData);

		return this;
	}

	@Override
	public MessageMEEntity readFromParcel(Parcel src) {
		super.readFromParcel(src);

		topic = src.readString();

		return this;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		writeString(dest, topic);
	}

	public static final Parcelable.Creator<MessageMEEntity> CREATOR = new Parcelable.Creator<MessageMEEntity>() {

		@Override
		public MessageMEEntity createFromParcel(Parcel in) {

			return new MessageMEEntity().readFromParcel(in);
		}

		@Override
		public MessageMEEntity[] newArray(int size) {
			return new MessageMEEntity[size];
		}
	};
}
