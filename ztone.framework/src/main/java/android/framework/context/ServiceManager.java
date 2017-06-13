package android.framework.context;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.assist.Assert;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.os.Bundle;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class ServiceManager {
    private static ServiceManager sServiceManager;

    private static final int CAPACITY = 128;

    private ActivityManager mActivityManager;

    private ServiceManager() {
        mActivityManager = LifeCycleUtils.component().getSystemService(ACTIVITY_SERVICE);
    }

    public static ServiceManager getInstance() {
        if (sServiceManager == null) {
            synchronized (ServiceManager.class) {
                if (sServiceManager == null) {
                    sServiceManager = new ServiceManager();
                }
            }
        }

        return sServiceManager;
    }

    /**
     * 开启服务,Context::getApplicationContext();
     */
    public ComponentName startService(Context context, Class<?> clazz) {
        return startService(context, clazz, null);
    }

    /**
     * 开启服务
     */
    public ComponentName startService(Context context, Class<?> clazz, Bundle bundle) {
        ComponentName componentName = null;
        assertParameters(context, clazz);

        if (!isServiceStarted(clazz)) {
            Intent intent = new Intent(context, clazz);
            if (bundle != null) {
                intent.putExtras(bundle);
            }
            componentName = context.startService(intent);
        }

        return componentName;
    }

    /**
     * 停止服务
     */
    public boolean stopService(Context context, Class<?> clazz) {
        boolean result = false;
        assertParameters(context, clazz);

        if (isServiceStarted(clazz)) {
            Intent intent = new Intent();
            intent.setComponent(new ComponentName(LifeCycleUtils.component().app(), clazz));


            result = context.stopService(intent);
        }

        return result;
    }

    /**
     * 绑定服务
     */
    public boolean bindService(Context context, Class<?> clazz, Bundle bundle, ServiceConnection connection, int flags, boolean isStartServiceFirst) {
        assertParameters(context, clazz);

        if (isStartServiceFirst) {
            startService(context, clazz, bundle);
        }

        Intent intent = new Intent(context, clazz);
        return context.bindService(intent, connection, flags);
    }

    /**
     * 解绑定
     */
    public boolean unbindService(Context context, Class<?> clazz, ServiceConnection connection, boolean isStopServiceAfter) {
        boolean result = false;
        assertParameters(context, clazz);

        context.unbindService(connection);

        if (isStopServiceAfter) {
            result = stopService(context, clazz);
        }

        return result;
    }

    public boolean isServiceStarted(Class<?> clazz) {

        return clazz != null && isServiceStarted(clazz.getName());
    }

    public boolean isServiceStarted(String clazz) {
        boolean isStarted = false;
        if (Assert.notEmpty(clazz)) {
            List<RunningServiceInfo> runningServices = mActivityManager.getRunningServices(CAPACITY);
            if (runningServices != null && !runningServices.isEmpty()) {
                for (RunningServiceInfo rsi : runningServices) {
                    if (rsi != null && rsi.service != null && clazz.equals(rsi.service.getClassName())) {
                        isStarted = true;
                        break;
                    }
                }
            }
        }

        return isStarted;
    }

    private void assertParameters(Context context, Class<?> clazz) {
        if (context == null) {
            throw new NullPointerException("Context is null");
        } else if (clazz == null) {
            throw new NullPointerException("service is null");
        }
    }
}
