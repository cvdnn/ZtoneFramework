package android.framework.push;

public abstract class MEClient {

	protected IMEListener mMEListener;
	protected OnMERuntimeListener mMERuntimeListener;

	public abstract void connect(MEOptions meProperties) throws Exception;

	public abstract void disconnect() throws Exception;

	public abstract boolean isConnected();

	public abstract void publish(String topic, String text, int qos) throws Exception;

	public abstract void subscribe(String topic, int qos) throws Exception;

	public abstract void unsubscribe(String... topicNames) throws Exception;

	public void setIMEListener(IMEListener listener) {
		mMEListener = listener;
	}

	public void setOnMERuntimeListener(OnMERuntimeListener listener) {
		mMERuntimeListener = listener;
	}

	public interface OnMERuntimeListener {

		void onChanged(int meState, String msg);
	}
}
