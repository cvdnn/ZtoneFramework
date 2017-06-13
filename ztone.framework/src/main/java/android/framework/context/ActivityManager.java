package android.framework.context;

import android.app.Activity;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.assist.Assert;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.framework.IRuntime;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.log.Log;

import java.util.List;
import java.util.Stack;

import static android.framework.IRuntime.getPackageName;

/**
 * Activity管理器,只是对BaseActivity托管,其他动作由调用方自行处理.
 *
 * @author
 */
public class ActivityManager {
    private static final String TAG = "ActivityManager";

    private static final int STACK_SIZE = 16;

    /**
     * activity过滤器
     */
    public interface ActivityFilter {

        boolean accept(FrameActivity activity);
    }

    /**
     * 应用是否运行在后台
     *
     * @return
     */
    public static boolean isRunInBackground() {
        boolean result = false;

        android.app.ActivityManager appActivityManager = LifeCycleUtils.component().getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> appProcesses = appActivityManager.getRunningAppProcesses();
        for (RunningAppProcessInfo appProcess : appProcesses) {
            if (Assert.notEmpty(appProcess.processName)
                    && appProcess.processName.equals(getPackageName())
                    && appProcess.importance == RunningAppProcessInfo.IMPORTANCE_BACKGROUND) {
                result = true;

                Log.i(String.format("Background App:", appProcess.processName));
            }
        }

        return result;
    }

    /**
     * 判断程序是否在前台运行
     *
     * @return 程序在前台运行返回ture，否则返回false
     */
    public static boolean isTopApplication() {
        // 应用程序位于堆栈的顶层
        RunningTaskInfo tasksInfo = getTopRunningTask();

        return tasksInfo != null && IRuntime.getPackageName().equals(tasksInfo.topActivity.getPackageName());
    }

    /**
     * 判断Activity是否在前台运行
     *
     * @return 程序在前台运行返回ture，否则返回false
     */
    public static boolean isTopActivity(Class<? extends Activity> clazz) {
        boolean result = false;
        if (clazz != null) {
            // 应用程序位于堆栈的顶层
            RunningTaskInfo tasksInfo = getTopRunningTask();
            result = tasksInfo != null && clazz.getName().equals(tasksInfo.topActivity.getClassName());
        }

        return result;
    }

    /**
     * 判断Activity是否在前台运行
     *
     * @return 程序在前台运行返回ture，否则返回false
     */
    public static boolean isTopActivity(Activity activity) {
        boolean result = false;
        if (activity != null) {
            // 应用程序位于堆栈的顶层
            RunningTaskInfo tasksInfo = getTopRunningTask();
            result = tasksInfo != null && activity.getComponentName().equals(tasksInfo.topActivity);
        }

        return result;
    }

    public static RunningTaskInfo getTopRunningTask() {
        RunningTaskInfo taskInfo = null;

        List<RunningTaskInfo> tasksInfo = getRunningTask(1);
        if (tasksInfo != null && !tasksInfo.isEmpty()) {
            taskInfo = tasksInfo.get(0);
        }

        return taskInfo;
    }

    public static List<RunningTaskInfo> getRunningTask(int taskCount) {
        List<RunningTaskInfo> taskList = null;

        if (taskCount > 0) {
            android.app.ActivityManager activityManager = LifeCycleUtils.component().getSystemService(Context.ACTIVITY_SERVICE);
            taskList = activityManager.getRunningTasks(taskCount);
        }

        return taskList;
    }

    /**
     * 获取指定应用的版本号
     *
     * @param packageName
     * @return
     */
    public static int getApplicationVersionCode(Context context, String packageName) {
        int versionCode = -1;

        PackageInfo packageInfo = getPackageInfo(context, packageName);
        if (packageInfo != null) {
            versionCode = packageInfo.versionCode;
        }

        return versionCode;
    }

    /**
     * 获取指定应用的版本号
     *
     * @param packageName
     * @return
     */
    public static String getApplicationVersionName(Context context, String packageName) {
        String versionName = "V1.0";

        PackageInfo packageInfo = getPackageInfo(context, packageName);
        if (packageInfo != null && Assert.notEmpty(packageInfo.versionName)) {
            versionName = packageInfo.versionName.trim();

            if (Assert.notEmpty(versionName)) { // versionName.matches("^[0-9]+")
                char ch = versionName.charAt(0);

                if (ch >= '0' && ch <= '9') {
                    versionName = "V" + versionName;
                }
            }
        }

        return versionName;
    }

    public static PackageInfo getPackageInfo(Context context, String packageName) {
        PackageInfo packageInfo = null;

        if (context != null && Assert.notEmpty(packageName)) {
            try {
                // ---get the package info---
                PackageManager packageManager = context.getPackageManager();
                if (packageManager != null) {
                    packageInfo = packageManager.getPackageInfo(packageName, 0);
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        return packageInfo;
    }

	/* ****************************************
     *
	 * 
	 * ***************************************
	 */

    private static final Stack<FrameActivity> mActivityStack = new Stack<FrameActivity>();
    private static int mStackCount;

    public static void finish() {
        // 清空栈
        int stackCount = mStackCount;
        while (stackCount > 0) {
            Activity activity = pop();
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }

            stackCount--;
        }
    }

    public static void clear() {
        if (mActivityStack != null) {
            mActivityStack.clear();
        }
        mStackCount = 0;
    }

    public static FrameActivity pop() {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                FrameActivity act = Assert.notEmpty(mActivityStack) ? mActivityStack.pop() : null;
                if (act != null) {
                    mStackCount--;
                }

                return act;
            }
        };

        return estimator.judge();
    }

    public static FrameActivity pop(final FrameActivity activity) {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                return popActivity(activity);
            }
        };

        return estimator.judge();
    }

    public static boolean remove(final FrameActivity activity) {
        Estimator<Boolean> estimator = new Estimator<Boolean>() {

            @Override
            public Boolean inScope() {
                boolean resRemove = mActivityStack != null && mActivityStack.remove(activity);
                if (resRemove) {
                    mStackCount--;
                }

                return resRemove;
            }
        };

        return estimator.judge();
    }

    public static boolean contains(final ActivityFilter filter) {

        return peek(filter) != null;
    }

    public static FrameActivity peek() {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                return Assert.notEmpty(mActivityStack) ? mActivityStack.peek() : null;
            }
        };

        return estimator.judge();
    }

    /**
     * 依据ActivityFilter循环向下判断,直至ActivityFilter为true或循环结束
     *
     * @param filter
     * @return
     */
    public static FrameActivity peek(final ActivityFilter filter) {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                FrameActivity activity = null;
                if (mStackCount > 0) {
                    for (int i = mStackCount - 1; i >= 0; i--) {
                        activity = mActivityStack.get(i);
                        if (filter.accept(activity)) {
                            break;

                        } else {
                            activity = null;
                        }
                    }
                }

                return activity;
            }
        };

        return estimator.judge();
    }

    /**
     * 获取当前栈里的从栈顶往下的第n个activity
     *
     * @param step ，从0开始计算
     * @return
     */
    public static FrameActivity peek(final int step) {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                FrameActivity activity = null;
                if (mStackCount > 0) {
                    int position = mStackCount - step - 1;
                    if (position >= 0 && position < mStackCount) {
                        activity = mActivityStack.get(position);
                    }
                }

                return activity;
            }
        };

        return estimator.judge();
    }

    /**
     * 循环向下判断,直至循环结束并返回活动activity
     *
     * @return
     */
    public static FrameActivity peekByIterate() {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                FrameActivity activity = null;
                if (mStackCount > 0) {
                    for (int i = mStackCount - 1; i >= 0; i--) {
                        activity = mActivityStack.get(i);
                        if (activity != null && !activity.isFinishing()) {
                            break;
                        } else {
                            activity = null;
                        }
                    }
                }

                return activity;
            }
        };

        return estimator.judge();
    }

    public static FrameActivity push(final FrameActivity activity) {
        Estimator<FrameActivity> estimator = new Estimator<FrameActivity>() {

            @Override
            public FrameActivity inScope() {
                // 先把栈底的activity弹出,然后再压到栈顶
                popActivity(activity);
                mStackCount++;// 在Estimator中已经有统一的溢出处理.

                return mActivityStack != null ? mActivityStack.push(activity) : null;
            }
        };

        return estimator.judge();
    }

    public static boolean has(final Class<?> clazz) {
        Estimator<Boolean> estimator = new Estimator<Boolean>() {

            @Override
            public Boolean inScope() {
                boolean has = false;

                FrameActivity activity = null;
                if (mStackCount > 0) {
                    for (int i = mStackCount - 1; i >= 0; i--) {
                        activity = mActivityStack.get(i);
                        if (activity != null && clazz != null && clazz.isInstance(activity)) {
                            has = true;
                            break;
                        }
                    }
                }

                return has;
            }
        };

        return estimator.judge();
    }

    public static int getStackCount() {
        return mStackCount;
    }

    public static void debug() {
        if (Assert.notEmpty(mActivityStack)) {
            StringBuilder sb = new StringBuilder("[");
            int size = mActivityStack.size();
            for (int i = size - 1; i >= 0; i--) {
                FrameActivity activity = mActivityStack.get(i);
                if (activity != null) {
                    sb.append(activity.getClass().getSimpleName());
                    if (i > 0) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("]");

            Log.d(TAG, sb.toString());
        }
    }

    private static FrameActivity popActivity(FrameActivity activity) {
        FrameActivity act = null;
        if (mStackCount > 0 && activity != null) {
            for (int i = mStackCount - 1; i >= 0; i--) {
                act = Assert.notEmpty(mActivityStack) ? mActivityStack.get(i) : null;
                // 判断是否是同一activity
                if (act != null && act == activity) {// act.getClass().getName().equals(activity.getClass().getName())
                    if (mActivityStack != null) {
                        mActivityStack.remove(i);
                        mStackCount--;
                    }

                    break;
                } else {
                    act = null;
                }
            }
        }

        return act;
    }

    /**
     * 栈容器估计量,用于校正栈作用范围并溢出修正.
     *
     * @param <T>
     * @author handy
     */
    private static abstract class Estimator<T> {

        /**
         * 栈操作
         *
         * @return
         */
        public abstract T inScope();

        /**
         * 栈容量判断(自动溢出维护)
         *
         * @return
         */
        public T judge() {
            // 溢出处理
            if (mStackCount > STACK_SIZE) {
                for (int i = mStackCount - STACK_SIZE; i > 0; i--) {
                    if (Assert.notEmpty(mActivityStack)) {
                        mActivityStack.remove(0);
                    }
                }
                mStackCount = STACK_SIZE;
            } else if (mStackCount <= 0) {
                if (Assert.notEmpty(mActivityStack)) {
                    mActivityStack.clear();
                }

                mStackCount = 0;
            }

            // 测试
            // T t=inScope();
            //
            // for(int i=activityStack.size()-1;i>=0;i--){
            // Log.d("<<<<<<<< "+activityStack.get(i).getClass().getName()+" >>>>>>>");
            // }
            // return t;

            return inScope();
        }
    }
}
