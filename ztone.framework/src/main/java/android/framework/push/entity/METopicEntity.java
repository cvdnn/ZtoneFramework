package android.framework.push.entity;

import org.json.JSONObject;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.framework.entity.FindJNode;
import android.log.Log;
import android.os.Parcel;
import android.os.Parcelable;

public class METopicEntity extends MEEntity {
	private static final String TAG = "METopicEntity";

	private static final long serialVersionUID = 1L;
	
	/* **************************************
	 * static
	 * 
	 * **************************************
	 */

	private static final String NODE_TOPIC = "topic";
	private static final String NODE_QOS = "qos";

	public static final byte FLAG_NONE = 0x00;
	public static final byte FLAG_KEEPALIVE = 0x01;

	public static METopicEntity create(String topic, int qos) {

		return create(topic, qos, false);
	}

	public static METopicEntity create(String topic, int qos, boolean needKeepalive) {
		METopicEntity meMessage = new METopicEntity();

		meMessage.topic = topic;
		meMessage.payload = "";
		meMessage.qos = qos;
		meMessage.needToKeepAlive = needKeepalive;

		return meMessage;
	}

	public static METopicEntity create(String topic, String payload, int qos) {
		METopicEntity meMessage = new METopicEntity();

		meMessage.topic = topic;
		meMessage.payload = payload;
		meMessage.qos = qos;

		return meMessage;
	}

	public static boolean check(METopicEntity meTopic) {

		return meTopic != null && Assert.notEmpty(meTopic.topic);
	}

	public static boolean isEmpty(METopicEntity meTopic) {

		return check(meTopic) && Assert.notEmpty(meTopic.payload);
	}

	public static final Parcelable.Creator<METopicEntity> CREATOR = new Parcelable.Creator<METopicEntity>() {

		public METopicEntity createFromParcel(Parcel src) {

			return new METopicEntity().readFromParcel(src);
		}

		public METopicEntity[] newArray(int size) {
			return new METopicEntity[size];
		}
	};

	/* *************************************** */

	@FindJNode
	public String topic;

	@FindJNode
	public int qos;

	@FindJNode
	public String payload;

	public boolean needToKeepAlive;

	@Override
	public METopicEntity parse(JSONObject jsonData) {
		super.parse(jsonData);

		return this;
	}

	public String toJSON() {
		String strJSON = null;

		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put(NODE_TOPIC, topic);
			jsonObject.put(NODE_QOS, qos);

			strJSON = TextUtilz.toFake(jsonObject.toString());
		} catch (Exception e) {
			Log.e(TAG, e);
		}

		return strJSON;
	}

	public boolean fromJSON(String text) {
		boolean result = false;
		if (Assert.notEmpty(text)) {
			String json = TextUtilz.fromFake(text);
			if (Assert.notEmpty(json)) {
				try {
					JSONObject jsonObject = new JSONObject(json);

					if (jsonObject.has(NODE_TOPIC)) {
						topic = jsonObject.optString(NODE_TOPIC);
					}

					if (jsonObject.has(NODE_QOS)) {
						qos = jsonObject.optInt(NODE_QOS);
					}

					result = true;
				} catch (Exception e) {
					Log.e(TAG, e);
				}
			}
		}

		return result;
	}

	public boolean equals(METopicEntity meTopic) {

		return check(meTopic) && meTopic.topic.equals(topic);
	}

	@Override
	public int hashCode() {

		return toString().hashCode();
	}

	@Override
	public String toString() {

		return new StringBuilder().append("topic: ").append(topic)//
				.append(", qos: ").append(qos)//
				.append(", payload: ").append(payload).toString();
	}

	@Override
	public METopicEntity readFromParcel(Parcel src) {
		super.readFromParcel(src);

		topic = src.readString();
		qos = src.readInt();
		payload = src.readString();
		needToKeepAlive = src.readByte() == FLAG_KEEPALIVE;

		return this;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		super.writeToParcel(dest, flags);

		writeString(dest, topic);
		dest.writeInt(qos);
		writeString(dest, payload);

		dest.writeByte(needToKeepAlive ? FLAG_KEEPALIVE : FLAG_NONE);
	}
}
