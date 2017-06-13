package android.framework.util;

import android.app.KeyguardManager;
import android.content.Context;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.os.PowerManager;

/**
 * Created by handy on 17-3-14.
 */

public class WakeLockUtils {
    /**
     * 判断是否锁屏状态
     */
    public static boolean isScreenLocked(Context context) {
        boolean result = false;

        if (context != null) {
            ((KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE)).inKeyguardRestrictedInputMode();
        }

        return result;
    }

    /**
     * 判断当前屏幕是否处在锁屏状态
     *
     * @return
     */
    public static boolean isScreenLocked() {
        boolean result = false;

        KeyguardManager keyguardManager = LifeCycleUtils.component().getSystemService(Context.KEYGUARD_SERVICE);
        keyguardManager.inKeyguardRestrictedInputMode();

        return result;
    }

    /**
     * 获取电源锁，保持该服务在屏幕熄灭时仍然获取CPU时，保持运行
     * FIXME 慎用
     */
    @Deprecated
    public static PowerManager.WakeLock acquireWakeLock() {
        PowerManager.WakeLock wakeLock = null;

        PowerManager pm = LifeCycleUtils.component().getSystemService(Context.POWER_SERVICE);
        wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "PostLocationService");
        if (null != wakeLock) {
            wakeLock.acquire();
        }

        return wakeLock;
    }

    /**
     * 释放设备电源锁
     * FIXME 慎用
     */
    @Deprecated
    public static void releaseWakeLock(PowerManager.WakeLock wakeLock) {
        if (null != wakeLock) {
            wakeLock.release();
        }
    }
}
