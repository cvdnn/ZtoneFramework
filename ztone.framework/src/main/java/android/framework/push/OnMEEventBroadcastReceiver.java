package android.framework.push;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.framework.C;
import android.log.Log;

import static android.framework.push.MEState.MESTATE_CONNECT_SUCCESS;
import static android.framework.push.MEState.MESTATE_NONE;

/**
 * ME事件接收器,此接收器必须和应用在同一进程
 *
 * @author handy
 */
public class OnMEEventBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "OnMEEventBroadcastReceiver";

    @Override
    public final void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        int stateCode = intent.getIntExtra(C.tag.me_state_code, MESTATE_NONE);
        if (MEAction.event().equals(action)) {
            String meStateMessage = intent.getStringExtra(C.tag.me_state_message);

            Log.d(TAG, "ME: MEEvent: %d: %s", stateCode, meStateMessage);

            MEState.postMEState(stateCode, meStateMessage);

            if (stateCode == MESTATE_CONNECT_SUCCESS) {
                MEBridge.signalAllLockCondition();
            }
        }
    }
}
