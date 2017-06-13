package android.framework.context.lifecycle;

import android.analytics.Analytics;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.framework.AppConfigure;
import android.framework.C;
import android.framework.Component;
import android.framework.IRuntime;
import android.framework.RuntimeConfigure;
import android.framework.context.FrameActivity;
import android.framework.module.Setting;
import android.log.Log;
import android.log.monitoring.MonitorUtils;
import android.net.wifi.WifiManager;
import android.network.DNSUtils;
import android.network.http.HTTPx;
import android.os.Handler;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.WIFI_SERVICE;


/**
 * Created by handy on 17-1-19.
 */

final class ApplicationLifeCycleManager extends LifeCycleManager implements Component {
    private static final String TAG = "ApplicationLifeCycleManager";

    private WifiManager mWiFiManager;
    private FrameActivity mMainActivity;

    private Setting mSystemSetting;

    @Override
    public <C extends Context> void onStart(@NonNull C context) {
        super.onStart(context);

        // 监控
        MonitorUtils.setBlockThreshold(500);
        MonitorUtils.startMonitorMainThread();
//        MonitorUtils.startMonitorUserThread();

        IRuntime.onInit(context);

        RuntimeConfigure runtimeConfig = RuntimeConfigure.obtain();
        if (runtimeConfig != null) {
            Analytics.Impl.onInit((Application) context, runtimeConfig.toProperties());
        }

        AppConfigure appConfigure = IRuntime.appConfig();
        RuntimeConfigure runtimeConfigure = RuntimeConfigure.obtain();
        if (runtimeConfigure != null && appConfigure != null) {
            DNSUtils.onInit(context, runtimeConfigure.alibabaAccountId(), appConfigure.dnsResolveHosts());
        }

        HTTPx.Client.onInit(context);

        Log.i(TAG, "## [APP]: pid: %d, tid: %d, uid: %d", Process.myPid(), Process.myTid(), Process.myUid());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.i(TAG, "## [App]: onTerminate!!!");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Log.i(TAG, "## [App]: onTrimMemory: %d!!!", level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Log.i(TAG, "## [App]: onLowMemory!!!");
    }

    @Override
    public Application app() {

        return (Application) mContext;
    }

    @Override
    public Component set(@NonNull Application app) {
        mContext = app;

        return this;
    }

    @Override
    public FrameActivity main() {

        return mMainActivity;
    }

    @Override
    public Component set(@NonNull FrameActivity activity) {
        mMainActivity = activity;

        return this;
    }

    @Override
    public <S> S getSystemService(@NonNull String name) {

        return (S) mContext.getApplicationContext().getSystemService(name);
    }

    @Override
    public Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        Intent intent = null;

        if (receiver != null && filter != null && mContext != null) {
            try {
                intent = mContext.registerReceiver(receiver, filter);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }

        return intent;
    }

    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter,
                                   @Nullable String broadcastPermission, @Nullable Handler scheduler) {
        Intent intent = null;

        if (receiver != null && filter != null && mContext != null) {
            try {
                intent = mContext.registerReceiver(receiver, filter, broadcastPermission, scheduler);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }

        return intent;
    }

    @Override
    public void unregisterReceiver(@Nullable BroadcastReceiver receiver) {
        if (receiver != null && mContext != null) {
            try {
                mContext.unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }
    }

    @Override
    public WifiManager wifi() {
        if (mWiFiManager == null && mContext != null) {
            mWiFiManager = (WifiManager) mContext.getApplicationContext().getSystemService(WIFI_SERVICE);
        }

        return mWiFiManager;
    }

    @Override
    public Setting setting() {
        if (mSystemSetting == null && mContext != null) {
            mSystemSetting = new Setting(mContext.getSharedPreferences(C.file.shared_prefs_system_config, MODE_PRIVATE));
        }

        return mSystemSetting;
    }
}
