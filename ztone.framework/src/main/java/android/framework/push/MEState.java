package android.framework.push;

import android.assist.Assert;
import android.bus.EventBusUtils;
import android.bus.OnEBArrivedListener;
import android.content.Intent;
import android.framework.C;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.log.Log;

public class MEState {
    private static final String TAG = "MEState";

    public static final int MESTATE_NONE = 0;
    public static final int MESTATE_CONNECT_SUCCESS = 1;
    public static final int MESTATE_CONNECT_DESTROY = 2;
    /**
     * 本次连接时重连标志,当离线后触发重连会设置该标志
     */
    public static final int MESTATE_CONNECT_RETRY = 3;
    public static final int MESTATE_START_MQTT_RUNNING = 4;
    public static final int MESTATE_AVAILABLE = 10;

    public static final int MESTATE_CONNECT_FAIL = -1;
    public static final int MESTATE_CONNECT_OPTION_ERROR = -2;
    public static final int MESTATE_CONNECT_FAILED_AUTHENTICATION = -3;
    public static final int MESTATE_CONNECT_LOSTED = -4;
    public static final int MESTATE_CONNECT_LOSTED_LONG_TIME = -5;
    public static final int MESTATE_PUBLISH_FAIL = -6;
    public static final int MESTATE_SUBSCRIBE_FAIL = -7;

    public interface OnMEStateChangedSubscriber extends OnEBArrivedListener {

    }

    public static MEState create(int code, String message) {
        MEState meState = new MEState();

        meState.code = code;
        meState.message = message;

        return meState;
    }

    public static void register(OnMEStateChangedSubscriber subscriber) {
        EventBusUtils.register(MEBridge.TAG_ME_EVENT_BUS, subscriber);
    }

    public static void unregister(OnMEStateChangedSubscriber subscriber) {
        EventBusUtils.unregister(MEBridge.TAG_ME_EVENT_BUS, subscriber);
    }

    public static void postMEState(MEState meState) {
        EventBusUtils.post(MEBridge.TAG_ME_EVENT_BUS, meState);
    }

    public static void postMEState(int code, String message) {
        EventBusUtils.post(MEBridge.TAG_ME_EVENT_BUS, create(code, message));
    }

    public static void sendMEStateBroadcast(int meState, String msg) {
        Log.printCalledStackTrace("ME");

        Intent intent = new Intent(MEAction.event());
        intent.putExtra(C.tag.me_state_code, meState);

        if (Assert.notEmpty(msg)) {
            intent.putExtra(C.tag.me_state_message, msg);
        }

        LifeCycleUtils.component().app().sendBroadcast(intent, C.tag.permission_message_exchange);
    }

	/* *****************************************
     *
	 * ****************************************
	 */

    public int code;
    public String message;

    @Override
    public String toString() {

        return new StringBuilder().append("code: ").append(code).append(", message: ").append(message).toString();
    }
}
