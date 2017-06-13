package android.framework;

import android.content.Context;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.context.lifecycle.OnLifeCycleListener;
import android.os.Handler;
import android.os.Looper;

/**
 * Loop Handle
 * <p>
 * Created by handy on 17-2-6.
 */

public class Loople {
    private static Handler mMainHandler;

    static {
        LifeCycleUtils.adhere().register(new OnLifeCycleListener.Impl() {

            @Override
            public <C extends Context> void onStart(C context) {
                mMainHandler = new Handler(Looper.getMainLooper());
            }

            @Override
            public void onDestroy() {
                if (mMainHandler != null) {
                    mMainHandler.removeCallbacksAndMessages(null);
                    mMainHandler = null;
                }
            }
        });
    }

    public static boolean inMainThread() {

        return Thread.currentThread() == Looper.getMainLooper().getThread();
    }

    public static Handler handler() {

        return mMainHandler;
    }

    public static void post(Runnable r) {
        if (r != null) {
            if (!inMainThread()) {
                if (mMainHandler != null) {
                    mMainHandler.post(r);
                }
            } else {
                r.run();
            }

        }
    }

    public static void postDelayed(Runnable r, long delayMillis) {
        if (mMainHandler != null && r != null) {
            mMainHandler.postDelayed(r, delayMillis);
        }
    }

    public static void postAtFrontOfQueue(Runnable r) {
        if (mMainHandler != null && r != null) {
            mMainHandler.postAtFrontOfQueue(r);
        }
    }

    public static void postAtTime(Runnable r, long uptimeMillis) {
        if (mMainHandler != null && r != null) {
            mMainHandler.postAtTime(r, uptimeMillis);
        }
    }

    public static void postAtTime(Runnable r, Object token, long uptimeMillis) {
        if (mMainHandler != null && r != null) {
            mMainHandler.postAtTime(r, token, uptimeMillis);
        }
    }

    public static void removeCallbacks(Runnable r) {
        if (mMainHandler != null && r != null) {
            mMainHandler.removeCallbacks(r);
        }
    }

    public static void removeCallbacks(Runnable r, Object token) {
        if (mMainHandler != null && r != null && token != null) {
            mMainHandler.removeCallbacks(r, token);
        }
    }

    public static void removeCallbacks(Object token) {
        if (mMainHandler != null && token != null) {
            mMainHandler.removeCallbacksAndMessages(token);
        }
    }
}
