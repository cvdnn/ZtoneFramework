package android.framework.push;

import android.framework.push.entity.ResultMEEntity;

public abstract class OnMEArrivedSubscriber {

	/**
	 * 收到ME消息后回调监听,当返回false时会触发EventBus事件.
	 * 
	 */
	public boolean onArrived(ResultMEEntity entity) {

		return false;
	}
}
