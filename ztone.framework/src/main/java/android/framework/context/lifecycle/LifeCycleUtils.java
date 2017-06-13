package android.framework.context.lifecycle;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.framework.Component;
import android.framework.Loople;
import android.framework.context.FrameActivity;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.framework.IRuntime.getPackageName;

public class LifeCycleUtils {
    private static final String TAG = "LifecycleUtils";

    static final String FRAGMENT_TAG = "android.framework.context.lifecycle";

    private static final int ID_REMOVE_SUPPORT_FRAGMENT_MANAGER = 2;

    /**
     * The top application level RequestManager.
     */
    private static volatile LifeCycleManager mApplicationLifeCycleManager = new ApplicationLifeCycleManager();

    /**
     * Pending adds for SupportRequestManagerFragments.
     */
    private static final Map<FragmentManager, LifeCycleTracker> mPendingRetrieverMap = new HashMap<>();

    /**
     * Main thread handler to handle cleaning up pending fragment maps.
     */
    private static final Handler mLifecycleHandler = new Handler(Looper.getMainLooper(), LifeCycleUtils.mHandlerCallback);

    public static Component component() {

        return ((Component) mApplicationLifeCycleManager);
    }

    /**
     * 注意：绑定到该LifeCycleManager的生命周期随Application！！！
     */
    @NonNull
    @UiThread
    public static LifeCycleManager adhere() {

        return mApplicationLifeCycleManager;
    }

    /**
     * 注意：绑定到该LifeCycleManager的生命周期随綁定的對象
     */
    @NonNull
    @UiThread
    public static <C> LifeCycleManager adhere(@NonNull C c) {
        LifeCycleManager manager = mApplicationLifeCycleManager;

        if (onMainThread()) {
            if (c instanceof FrameActivity) {
                manager = find((FrameActivity) c);

            } else if (c instanceof Fragment) {

                manager = find((Fragment) c);
            } else {

                manager = find((Context) c);
            }
        }

        return manager;
    }

    @UiThread
    private static LifeCycleManager find(Context context) {
        LifeCycleManager lifeCycleManager = null;

        if (context instanceof FrameActivity) {
            lifeCycleManager = find((FrameActivity) context);
        }

        return lifeCycleManager != null ? lifeCycleManager : mApplicationLifeCycleManager;
    }

    @UiThread
    private static LifeCycleManager find(FrameActivity activity) {
        LifeCycleManager lifeCycleManager = null;

        if (!isDestroyed(activity)) {
            LifeCycleTracker retriever = findLifecycleRetriever(activity.iSupportFragmentManager());

            lifeCycleManager = retriever.getLifecycleManager();

        }

        return lifeCycleManager != null ? lifeCycleManager : mApplicationLifeCycleManager;
    }

    @UiThread
    private static LifeCycleManager find(Fragment fragment) {
        LifeCycleManager lifeCycleManager = null;

        if (fragment != null) {
            Activity activity = fragment.getActivity();
            if (activity instanceof FrameActivity && !isDestroyed((FrameActivity) activity)) {
                LifeCycleTracker retriever = findLifecycleRetriever(fragment.getChildFragmentManager());

                lifeCycleManager = retriever.getLifecycleManager();
            }
        }


        return lifeCycleManager != null ? lifeCycleManager : mApplicationLifeCycleManager;
    }

    private static boolean isDestroyed(FrameActivity activity) {

        return activity != null && activity.isDestroyed();
    }

    @UiThread
    @NonNull
    protected static LifeCycleTracker findLifecycleRetriever(final FragmentManager fm) {
        LifeCycleTracker retriever = (LifeCycleTracker) fm.findFragmentByTag(FRAGMENT_TAG);
        if (retriever == null) {
            retriever = mPendingRetrieverMap.get(fm);
            if (retriever == null) {
                retriever = new LifeCycleTracker();

                mPendingRetrieverMap.put(fm, retriever);

                fm.beginTransaction().add(retriever, FRAGMENT_TAG).commitAllowingStateLoss();

                mLifecycleHandler.obtainMessage(ID_REMOVE_SUPPORT_FRAGMENT_MANAGER, fm).sendToTarget();
            }
        }

        return retriever;
    }

    private static final Handler.Callback mHandlerCallback = new Handler.Callback() {

        @Override
        public boolean handleMessage(Message message) {
            boolean handled = true;
            Object removed = null;
            Object key = null;
            switch (message.what) {
                case ID_REMOVE_SUPPORT_FRAGMENT_MANAGER:
                    FragmentManager supportFm = (FragmentManager) message.obj;
                    key = supportFm;
                    removed = mPendingRetrieverMap.remove(supportFm);
                    break;
                default:
                    handled = false;
            }
            if (handled && removed == null && Log.isLoggable(TAG, Log.WARN)) {
                Log.w(TAG, "Failed to remove expected request manager fragment, manager: " + key);
            }

            return handled;
        }
    };

    /**
     * Returns {@code true} if called on the main thread, {@code false} otherwise.
     */
    public static boolean onMainThread() {
        return Looper.myLooper() == Looper.getMainLooper();
    }

    /**
     * Returns {@code true} if called on the main thread, {@code false} otherwise.
     */
    public static boolean isOnBackgroundThread() {
        return !onMainThread();
    }


    public static boolean isMainActivity(Activity activity) {

        return component().main() == activity;
    }

    public static boolean isFinishing(Activity activity) {

        return activity == null || activity.isFinishing();
    }

    public static void notifyMainActivityFinish() {
        Loople.post(new Runnable() {

            @Override
            public void run() {
                FrameActivity activity = component().main();
                if (!isFinishing(activity)) {
                    activity.finish();
                }

                component().set((FrameActivity) null);
            }
        });
    }

    public static void reboot() {
        Intent intent = component().app().getPackageManager().getLaunchIntentForPackage(getPackageName());
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP | FLAG_ACTIVITY_NEW_TASK);

        ContextCompat.startActivity(component().app(), intent, null);
    }

    public static void exit() {
        Loople.postDelayed(new Runnable() {

            @Override
            public void run() {
                Process.killProcess(Process.myPid());

                System.exit(0);// 结束app进程
            }
        }, 500);
    }
}
