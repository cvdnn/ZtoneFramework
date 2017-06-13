package android.framework.push;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.context.FrameService;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.log.Log;
import android.network.NetState;

import static android.app.AlarmManager.RTC_WAKEUP;
import static android.app.PendingIntent.FLAG_CANCEL_CURRENT;
import static android.app.PendingIntent.FLAG_NO_CREATE;
import static android.app.PendingIntent.FLAG_ONE_SHOT;
import static android.app.PendingIntent.FLAG_UPDATE_CURRENT;
import static android.content.Context.ALARM_SERVICE;
import static android.framework.push.MEOptions.CONNECTION_LONG_RETRY_INTERVAL;
import static android.framework.push.MEOptions.CONNECTION_SHORT_INTERVAL;
import static android.net.ConnectivityManager.CONNECTIVITY_ACTION;

public class MEDeamon extends BroadcastReceiver {
    private static final String TAG = "MEDeamon";

    private FrameService mSevice;

    private long mKATInterval;

    public void start(FrameService sevice) {
        mSevice = sevice;

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_ON);
        LifeCycleUtils.component().app().registerReceiver(this, filter);
    }

    public void stop() {
        Intent intent = new Intent(MEAction.messageExchange());
        intent.setPackage(IRuntime.getPackageName());
        PendingIntent pendingIntent = PendingIntent.getService(LifeCycleUtils.component().app(), 0, intent, FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = LifeCycleUtils.component().getSystemService(ALARM_SERVICE);
        alarmManager.cancel(pendingIntent);
    }

    public void setRetryInterval(long triggerAtMillis) {
        Log.printCalledStatus("ME");

        Intent intent = new Intent(MEAction.messageExchange());
        intent.setPackage(IRuntime.getPackageName());

        intent.putExtra(C.tag.me_clazz, getClass().getName());
        intent.putExtra(C.tag.me_flag, C.flag.me_start_sevice);

        PendingIntent pendingIntent = PendingIntent.getService(LifeCycleUtils.component().app(), 0, intent, FLAG_ONE_SHOT | FLAG_CANCEL_CURRENT);

        AlarmManager alarmManager = LifeCycleUtils.component().getSystemService(ALARM_SERVICE);
        alarmManager.set(RTC_WAKEUP, System.currentTimeMillis() + triggerAtMillis, pendingIntent);
    }

    public void setKeepAliveTopicInterval(long katInterval) {
        mKATInterval = katInterval;
    }

    /**
     * Query's the AlarmManager to check if there is a keep alive currently scheduled
     *
     * @return true if there is currently one scheduled false otherwise
     */
    public boolean has(MEService service) {
        boolean result = false;

        if (service != null) {
            synchronized (service) {
                Intent intent = new Intent(MEAction.messageExchange());
                intent.setPackage(IRuntime.getPackageName());

                PendingIntent pi = PendingIntent.getBroadcast(LifeCycleUtils.component().app(), 0, intent, FLAG_NO_CREATE);

                result = pi != null;
            }
        }

        return result;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        long now = System.currentTimeMillis();

        String action = intent.getAction();

        if (CONNECTIVITY_ACTION.equals(action)) {
            if (NetState.isConnected(context) && !MEBridge.isMEClientConnected()) {
                setRetryInterval(CONNECTION_SHORT_INTERVAL);
            } else {
                setRepeatingInterval(C.flag.me_start_sevice, CONNECTION_LONG_RETRY_INTERVAL, false);
            }
        } else {
            if (!MEBridge.isMEServiceStart()) {
                MEBridge.destroyAppMEBridge();

                MEBridge.startMEService(context);
                MEBridge.createAppMEBridge();

                Log.v(TAG, "ME: Start MEService by %s", action);
            }
        }

        Log.e(TAG, "BB: action: %dms, Receiver: %s", System.currentTimeMillis() - now, intent.getAction());
    }

    private void setRepeatingInterval(int flag, long interval, boolean retryNow) {
        Intent intent = new Intent(MEAction.messageExchange());
        intent.setPackage(IRuntime.getPackageName());

        intent.putExtra(C.tag.me_clazz, getClass().getName());
        intent.putExtra(C.tag.me_flag, flag);

        PendingIntent pendingIntent = PendingIntent.getService(LifeCycleUtils.component().app(), 0, intent, FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = LifeCycleUtils.component().getSystemService(ALARM_SERVICE);
        alarmManager.setRepeating(RTC_WAKEUP, System.currentTimeMillis() + (retryNow ? 10000 : interval), interval, pendingIntent);
    }
}
