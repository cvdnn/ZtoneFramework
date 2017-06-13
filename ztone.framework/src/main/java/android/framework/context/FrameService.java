package android.framework.context;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.assist.Assert;
import android.concurrent.ThreadUtils;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.graphics.PixelFormat;
import android.log.Log;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Process;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static android.app.Notification.FLAG_FOREGROUND_SERVICE;
import static android.app.Notification.FLAG_NO_CLEAR;
import static android.app.Notification.FLAG_ONGOING_EVENT;
import static android.content.pm.PackageManager.MATCH_DEFAULT_ONLY;
import static android.framework.Android.ECLAIR;

public class FrameService extends Service {
    private static final String TAG = "FrameService";

    protected String mClassName;

    protected int mStartFlag;

    /**
     * 小悬浮窗View的实例
     */
    private TextView mFloatWindow;

    /**
     * 小悬浮窗View的参数
     */
    private WindowManager.LayoutParams mFloatParams;

    /**
     * 用于控制在屏幕上添加或移除悬浮窗
     */
    private WindowManager mWindowManager;

    private final ArrayList<Action> mActionReceiverList = new ArrayList<>();

    protected final Handler mContextHandler = new Handler();

    @Override
    public void onCreate() {
        super.onCreate();

        mClassName = getClass().getSimpleName();

        mStartFlag = getApplicationInfo().targetSdkVersion < ECLAIR ? START_STICKY_COMPATIBILITY : START_STICKY;

        Log.i(mClassName, "## [Sevice]: pid:" + Process.myPid() + ", tid: " + Process.myTid() + ", uid: " + Process.myUid());
    }

    @Override
    public final int onStartCommand(Intent intent, int flags, int startId) {
        int flag = super.onStartCommand(intent, flags, startId);

        boolean result = handleAction(intent);
        if (!result) {
            postStartCommand(intent, flags, startId);
        }

        return mStartFlag;
    }

    protected void setStartFlag(int startFlag) {
        mStartFlag = startFlag;
    }

    protected void postStartCommand(final Intent intent, int flags, int startId) {

    }

    protected void registerAction(Action... receivers) {
        if (Assert.notEmpty(receivers)) {
            for (Action r : receivers) {
                if (r != null) {
                    mActionReceiverList.add(r);
                }
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        Log.i(mClassName, "## [Service]: onLowMemory!!!");
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        Log.i(mClassName, ">> onTrimMemory: " + level);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mContextHandler.removeCallbacksAndMessages(null);

        Log.i(mClassName, "## [Service]: onDestroy!!!");
    }

    protected final boolean handleAction(final Intent intent) {
        int result = 0;

        if (Assert.notEmpty(mActionReceiverList) && intent != null) {
            final String action = intent.getAction();
            for (final Action receiver : mActionReceiverList) {
                if (receiver != null && receiver.as(action)) {
                    result++;

                    ThreadUtils.start(new Runnable() {

                        @Override
                        public void run() {
                            if (receiver != null && intent != null) {
                                receiver.onAction(action, intent);

                                post(new Runnable() {

                                    @Override
                                    public void run() {
                                        if (receiver != null) {
                                            receiver.postAction();
                                        }
                                    }
                                });
                            }
                        }

                    }, "HANDLE_COMMAND_ACTION_THREAD");
                }
            }
        }

        return result > 0;
    }

    public void onCreateNotification(@NonNull NotificationCompat.Builder notifyBuilder) {

    }

    /**
     * 让该service前台运行，避免手机休眠时系统自动杀掉该服务
     */
    public void startForeground(int id, boolean isAttachWindow) {
        Intent intent = new Intent(this, getClass());
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, 0);

        NotificationCompat.Builder nb = new NotificationCompat.Builder(LifeCycleUtils.component().app());
        onCreateNotification(nb);
        nb.setWhen(System.currentTimeMillis()).setContentIntent(pendingIntent);

        Notification notification = nb.build();
        notification.flags = FLAG_ONGOING_EVENT | FLAG_NO_CLEAR | FLAG_FOREGROUND_SERVICE;

        // 如果 id 为 0 ，那么状态栏的 notification 将不会显示。
        startForeground(id, notification);

        if (isAttachWindow) {
            new Handler(Looper.myLooper()).postDelayed(new Runnable() {

                @Override
                public void run() {
                    try {
                        createDaemonWindow();
                    } catch (Exception e) {
                        Log.v(TAG, e);
                    }
                }
            }, 100);
        }
    }

    public void stopForeground() {
        stopForeground(true);

        try {
            removeDaemonWindow();
        } catch (Exception e) {
            Log.v(TAG, e);
        }
    }

    /**
     * 创建一个大悬浮窗。位置为屏幕正中间。
     */
    protected void createDaemonWindow() throws Exception {
        WindowManager windowManager = getWindowManager();

        if (mFloatWindow == null) {
            mFloatWindow = new TextView(this);
            // test
            // mFloatWindow.setText("123");
            // mFloatWindow.setTextColor(Color.WHITE);
            // mFloatWindow.setBackgroundColor(Color.RED);

            if (mFloatParams == null) {
                mFloatParams = new WindowManager.LayoutParams();
                mFloatParams.type = WindowManager.LayoutParams.TYPE_PHONE;
                mFloatParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
                mFloatParams.format = PixelFormat.RGBA_8888;
                mFloatParams.gravity = Gravity.LEFT | Gravity.TOP;
                mFloatParams.width = 1;
                mFloatParams.height = 1;
                mFloatParams.x = 0;
                mFloatParams.y = 0;
            }

            windowManager.addView(mFloatWindow, mFloatParams);
        }
    }

    /**
     * 将小悬浮窗从屏幕上移除。
     */
    protected void removeDaemonWindow() throws Exception {
        if (mFloatWindow != null) {
            WindowManager windowManager = getWindowManager();
            windowManager.removeView(mFloatWindow);
            mFloatWindow = null;
        }
    }

    /**
     * 是否有悬浮窗(包括小悬浮窗和大悬浮窗)显示在屏幕上。
     *
     * @return 有悬浮窗显示在桌面上返回true，没有的话返回false。
     */
    protected boolean isWindowShowing() {
        return mFloatWindow != null;
    }

    /**
     * 如果WindowManager还未创建，则创建一个新的WindowManager返回。否则返回当前已创建的WindowManager。
     *
     * @return WindowManager的实例，用于控制在屏幕上添加或移除悬浮窗。
     */
    protected WindowManager getWindowManager() {
        if (mWindowManager == null) {
            mWindowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        }

        return mWindowManager;
    }

    /**
     * 判断当前界面是否是桌面
     */
    @Deprecated
    protected boolean isHome() {
        ActivityManager mActivityManager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        List<ActivityManager.RunningTaskInfo> rti = mActivityManager.getRunningTasks(1);
        return getHomes().contains(rti.get(0).topActivity.getPackageName());
    }

    /**
     * 获得属于桌面的应用的应用包名称
     *
     * @return 返回包含所有包名的字符串列表
     */
    protected ArrayList<String> getHomes() {
        ArrayList<String> names = new ArrayList<String>();
        PackageManager packageManager = this.getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfo = packageManager.queryIntentActivities(intent, MATCH_DEFAULT_ONLY);
        for (ResolveInfo ri : resolveInfo) {
            names.add(ri.activityInfo.packageName);
        }
        return names;
    }

    protected boolean postAtTime(Runnable r) {

        return r != null && mContextHandler.postAtFrontOfQueue(r);
    }

    protected boolean postDelayed(Runnable r, long delayMillis) {

        return r != null && mContextHandler.postDelayed(r, delayMillis);
    }

    protected boolean post(Runnable r) {

        return r != null && mContextHandler.post(r);
    }

    protected boolean postAtTime(Runnable r, long uptimeMillis) {

        return r != null && mContextHandler.postAtTime(r, uptimeMillis);
    }

    protected boolean postAtTime(Runnable r, Object token, long uptimeMillis) {

        return r != null && mContextHandler.postAtTime(r, token, uptimeMillis);
    }

    public interface Action {

        void onAction(@NonNull String action, @NonNull Intent intent);

        void postAction();

        boolean as(String action);

        class Impl implements Action {
            private final List<String> mActionArrays;

            public Impl(String... actions) {
                mActionArrays = Assert.notEmpty(actions) ? Arrays.asList(actions) : new ArrayList(0);
            }

            @Override
            public boolean as(String action) {

                return Assert.notEmpty(mActionArrays) && Assert.notEmpty(action) && mActionArrays.contains(action);
            }

            public void onAction(@NonNull String action, @NonNull Intent intent) {

            }

            public void postAction() {

            }
        }
    }
}
