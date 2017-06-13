package android.framework.push;

import android.assist.Assert;
import android.framework.C;
import android.framework.IRuntime;
import android.log.Log;
import android.support.annotation.NonNull;

/**
 * Created by handy on 17-2-17.
 */

public class MEAction {

    /**
     * 消息传递服务的action
     */
    private static String mMessageExchangeAction;

    private static String mMEConnectionAction;

    @NonNull
    public static String event() {
        Log.printCalledStatus("ME");

        if (Assert.isEmpty(mMEConnectionAction)) {
            mMEConnectionAction = String.format(C.tag.format_action_me_event, IRuntime.vriables().getAppCode());
        }

        return mMEConnectionAction;
    }

    @NonNull
    public static String messageExchange() {
        if (Assert.isEmpty(mMessageExchangeAction)) {
            mMessageExchangeAction = String.format(C.tag.format_action_message_exchange, IRuntime.vriables().getAppCode());
        }

        return mMessageExchangeAction;
    }
}
