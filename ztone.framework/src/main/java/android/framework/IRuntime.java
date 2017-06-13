package android.framework;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningAppProcessInfo;
import android.app.ActivityManager.RunningServiceInfo;
import android.app.ActivityManager.RunningTaskInfo;
import android.app.Service;
import android.assist.Assert;
import android.assist.DateFormatUtils;
import android.assist.Etcetera;
import android.assist.Library;
import android.concurrent.ThreadPool;
import android.concurrent.ThreadUtils;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.framework.builder.URLBuilder;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.module.AssertsMapping;
import android.framework.module.FilePath;
import android.framework.module.Setting;
import android.framework.module.VersionUtils;
import android.framework.pull.PullUtils;
import android.framework.pull.PulledFilter;
import android.framework.sign.Sign;
import android.framework.sign.SortSign;
import android.io.FileUtils;
import android.log.Log;
import android.os.Build;
import android.os.Bundle;
import android.reflect.ClazzLoader;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.PermissionChecker;

import java.io.File;
import java.io.FileFilter;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;
import static android.content.pm.PackageManager.GET_META_DATA;
import static android.framework.context.lifecycle.LifeCycleUtils.component;
import static android.log.Log.OUT_OF_CRASH_TIME;
import static android.log.Log.OUT_OF_WORK_TIME;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;

public final class IRuntime {
    private static final String TAG = "Runtime";

    private static final int CAPACITY = 64;

    private static final long TIME_CLEAN = 48 * 3600000;

    private static final Vriables mVriables = new Vriables();

    private static AppConfigure mAppConfigure;
    private static Sign mSign;
    private static Class<? extends Service> mMEServiceClazz;

    private static String mClientId;

    /**
     * 用于双进程守护
     */
    private static String mAPPClientAction;

    static {
        Library.load("framework");
    }

    public static Vriables vriables() {

        return mVriables;
    }

    public static boolean isLandscape() {

        return Shape.getShape().isLandscape;
    }

    /**
     * 初始化环境变量
     */
    public static void initEnvironmentVariables() {
        String packageName = LifeCycleUtils.component().app().getPackageName();
        vriables().setPackageName(packageName);

        ApplicationInfo appInfo = getApplicationInfo(packageName);
        if (appInfo != null) {
            vriables().setAppName(AppResource.getString(appInfo.labelRes));

            // 获取MetaData数据
            if (appInfo.metaData != null) {

            }
        }

        initRuntimeConfigure();
    }

    private static void initRuntimeConfigure() {
        // RuntimeConfigure
        RuntimeConfigure runtimeConfigure = RuntimeConfigure.obtain();
        if (runtimeConfigure != null) {
            vriables()
                    .setChannelId(runtimeConfigure.channelId())
                    .setAppId(runtimeConfigure.appId())
                    .setAppPath(runtimeConfigure.appPath())
                    .setAppCode(runtimeConfigure.appCode())
                    .setCASSecret(runtimeConfigure.appSecret());

            // app_id
            URLBuilder.setGeneralMeta(C.tag.app_id, vriables().getAppId());

            // version code
            URLBuilder.setGeneralMeta(C.tag.ver, VersionUtils.getAppVersionCode());

            URLBuilder.setGeneralMeta(C.tag.api, runtimeConfigure.api());
            URLBuilder.setGeneralMeta(C.tag.os, runtimeConfigure.os());
        }
    }

    /**
     * 创建应用运行所必须的目录
     * <p>
     * *在权限判断之后调用
     */
    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static void initAppFile() {
        if (FilePath.tryMKDirs()) {
            File appPath = new FilePath.Builder().build().toFile();
            if (FileUtils.exists(appPath)) {

                // bin
                new File(appPath, C.file.path_bin).mkdirs();

                // download
                new File(appPath, C.file.path_download).mkdirs();

                // image
                initFilePath(appPath, C.file.path_image, TIME_CLEAN);

                // cache
                initFilePath(appPath, C.file.path_cache, TIME_CLEAN);

                // temp
                initFilePath(appPath, C.file.path_temp, TIME_CLEAN);

                // log
                File logFile = new File(appPath, C.file.path_log);
                initLogFilePath(logFile, TIME_CLEAN);
                initLogFilePath(new File(logFile, C.file.path_crash), OUT_OF_CRASH_TIME);
            }
        } else {
            // 这里显示初始化失败對話框
            Intent errorIntent = new Intent(C.tag.action_error_dialog);
            errorIntent.setPackage(getPackageName());
            component().app().startActivity(errorIntent);

            Log.e(TAG, "Init dir error StorageFlag: %d, FilePath: %s", FilePath.getStorageFlag(),
                    new FilePath.Builder().build().toFilePath());
        }
    }

    /**
     * *在权限判断之后调用
     */
    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static void initClient() {
        try {
            vriables().setUUID(uuid());
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        // devid
        URLBuilder.setGeneralMeta(C.tag.devid, vriables().getClientId());
    }

    /**
     * 初始化过滤器
     */
    private static void initFilter() {
        // 數據過濾器
        PulledFilter pulledFilter = ClazzLoader.newInstance(RuntimeConfigure.obtain().pullFilter());
        PullUtils.registerPulledFilter(pulledFilter);

        // 事件过滤器
//        TrackFilter[] eventFilters = null;
//        String[] eventFilterClazz = RuntimeConfigure.obtain().eventFilter();
//        if (Assert.notEmpty(eventFilterClazz)) {
//            eventFilters = new TrackFilter[eventFilterClazz.length];
//            for (int i = 0; i < eventFilterClazz.length; i++) {
//                eventFilters[i] = ClazzLoader.newInstance(eventFilterClazz[i]);
//            }
//
//            TrackAgent.registerFilter(eventFilters);
//        }
    }

    /**
     * 初始化过滤器
     */
    private static void initLogFilePath(@NonNull final File file, final long timeout) {
        if (FileUtils.exists(file)) {
            // 清理旧日志
            ThreadUtils.start(new Runnable() {

                @Override
                public void run() {
                    FileUtils.delete(file, new FileFilter() {
                        private final long NOW = System.currentTimeMillis();
                        private final long TIME_OUT = timeout > 0 ? timeout : OUT_OF_WORK_TIME;

                        @Override
                        public boolean accept(File file) {
                            boolean result = false;

                            if (Assert.exists(file) && file.isDirectory()) {
                                String fileName = file.getName();
                                if (!C.file.path_crash.equals(fileName)) {
                                    Date logDate = DateFormatUtils.parse(fileName, "yyyyMMdd");
                                    // 旧数据
                                    result = (logDate != null && (NOW - logDate.getTime() > TIME_OUT));
                                }
                            }

                            return result;
                        }
                    });
                }

            }, "THREAD_INIT_LOG_FILE_" + System.currentTimeMillis());

        } else if (file != null) {
            file.mkdirs();
        }
    }

    public static void cleanCacheFiles() {
        String appPath = new FilePath.Builder().build().toFilePath();
        if (Assert.notEmpty(appPath)) {
            File appPathFile = new File(appPath);

            deleteFilePath(new File(appPathFile, C.file.path_cache));
            deleteFilePath(new File(appPathFile, C.file.path_temp));
        }
    }

    public static ApplicationInfo getApplicationInfo(String packageName) {
        ApplicationInfo appInfo = null;
        try {
            if (Assert.notEmpty(packageName)) {
                appInfo = LifeCycleUtils.component().app().getPackageManager().getApplicationInfo(packageName, GET_META_DATA);
            }
        } catch (Exception e) {
            Log.d(TAG, e);
        }

        return appInfo;
    }

    public static Sign getSign() {
        if (mSign == null) {
            String implSign = RuntimeConfigure.obtain().signImpl();
            mSign = Assert.notEmpty(implSign) ? (Sign) ClazzLoader.newInstance(implSign) : new SortSign();
        }

        return mSign;
    }

    public static <T extends AppConfigure> T appConfig() {
        if (mAppConfigure == null) {
            synchronized (IRuntime.class) {
                if (mAppConfigure == null) {
                    AssertsMapping assertsMapping = AssertsMapping.create();
                    InputStream fileInput = assertsMapping.open(C.file.app_config);

                    vriables().setIsLoadLocalConfigFile(assertsMapping.isLocalFile());

                    mAppConfigure = ClazzLoader.newInstance(RuntimeConfigure.obtain().appConfig());
                    mAppConfigure.load(fileInput, !vriables().isLoadLocalConfiguration()); // 本地文件无须加密
                }
            }

        }

        return ((T) mAppConfigure);
    }

    private static String getStringFromBundle(Bundle bundle, String key, String defaultValue) {
        String value = bundle.getString(key);

        if (Assert.isEmpty(value)) {
            int intValue = bundle.getInt(key);
            if (intValue != 0) {
                value = String.valueOf(bundle.getInt(key));
            }
        }

        if (Assert.isEmpty(value)) {
            value = defaultValue;
        }

        return value;
    }

    private static File initFilePath(@NonNull File dirFile, @NonNull String fileName, final long timeout) {
        final File file = new File(dirFile, fileName);
        if (FileUtils.exists(file)) {
            ThreadUtils.start(new Runnable() {
                private final long NOW = System.currentTimeMillis();
                private final long TIME_OUT = timeout > 0 ? timeout : TIME_CLEAN;

                @Override
                public void run() {
                    FileUtils.delete(file, new FileFilter() {

                        @Override
                        public boolean accept(File file) {
                            long lastModified = file.lastModified();

                            return Assert.exists(file) && lastModified > 0 && NOW - lastModified > TIME_OUT;
                        }
                    });
                }

            }, "THREAD_FILE_INIT_" + System.currentTimeMillis());

        } else {
            file.mkdirs();
        }

        return file;
    }

    private static void deleteFilePath(final File dirFile) {
        ThreadUtils.start(new Runnable() {

            @Override
            public void run() {
                FileUtils.delete(dirFile);
            }

        }, "CLEAN_FILES_THREAD_" + System.currentTimeMillis());
    }

    /**
     * Checks if the app has permission to write to device storage
     * <p/>
     * If the app does not has permission then the user will be prompted to
     * grant permissions
     */
    public static boolean checkStoragePermissions() {
        boolean result = true;

        if (Build.VERSION.SDK_INT >= Android.M) {
            int writeExternalStorage = PermissionChecker.checkSelfPermission(LifeCycleUtils.component().app(), WRITE_EXTERNAL_STORAGE);
            int readExternalStorage = PermissionChecker.checkSelfPermission(LifeCycleUtils.component().app(), READ_EXTERNAL_STORAGE);

            result = writeExternalStorage == PERMISSION_GRANTED && readExternalStorage == PERMISSION_GRANTED;
        }

        return result;
    }

    public static String getTopActivityName() {
        String topClazzName = C.value.empty;

        ActivityManager activityManager = component().getSystemService(android.content.Context.ACTIVITY_SERVICE);
        List<RunningTaskInfo> runningTaskInfos = activityManager.getRunningTasks(1);
        if (Assert.notEmpty(runningTaskInfos)) {
            ComponentName compName = runningTaskInfos.get(0).topActivity;
            if (compName != null) {
                topClazzName = compName.getClassName();
            }
        }

        return topClazzName;
    }

    public static boolean isRunningForeground(Activity activity) {
        ActivityManager am = (ActivityManager) activity.getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes == null) return false;

        String pkg = activity.getApplicationContext().getPackageName();
        ActivityManager.RunningAppProcessInfo self = null;
        for (ActivityManager.RunningAppProcessInfo p : processes) {
            if (p.processName.equals(pkg)) {
                self = p;
                break;
            }
        }
        if (self == null) return false;
        return self.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

    }

    public static boolean isRunningForeground() {
//        ActivityManager am = (ActivityManager) E.sAppContext.getSystemService(Context.ACTIVITY_SERVICE);
//        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
//        if (processes == null) return false;
//
//        String processName = E.sAppContext.getApplicationInfo().processName;
//        ActivityManager.RunningAppProcessInfo self = null;
//        for (ActivityManager.RunningAppProcessInfo p : processes) {
//            if (p.processName.equals(processName)) {
//                self = p;
//                break;
//            }
//        }
//        if (self == null) return false;
//        if (self.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND) return true;
//
//        return false;

        // Get all the processes of device (1)
        ActivityManager am = component().getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningAppProcessInfo> processes = am.getRunningAppProcesses();
        if (processes == null) return false;

        // Gather all the processes of current application (2)
        // Above 5.1.1, this may be equals to (1), on the safe side, we also
        // filter the processes with current package name.
        String pkg = getPackageName();
        List<RunningAppProcessInfo> currentAppProcesses = new ArrayList<>(processes.size());
        for (RunningAppProcessInfo p : processes) {
            if (p.pkgList == null) continue;

            boolean match = false;
            int N = p.pkgList.length;
            for (int i = 0; i < N; i++) {
                if (p.pkgList[i].equals(pkg)) {
                    match = true;
                    break;
                }
            }
            if (!match) continue;

            currentAppProcesses.add(p);
        }

        // The top process of current application processes.
        RunningAppProcessInfo currentProcess = currentAppProcesses.get(0);
        return currentProcess.importance == RunningAppProcessInfo.IMPORTANCE_FOREGROUND;

    }

    public static String getLaunchActivityClassName() {
        String launchClassName = C.value.empty;

        PackageManager packageManager = component().app().getPackageManager();
        Intent appIntent = packageManager.getLaunchIntentForPackage(getPackageName());
        if (appIntent != null) {
            ComponentName componentName = appIntent.getComponent();
            if (componentName != null) {
                launchClassName = componentName.getShortClassName();
            }
        }

        return launchClassName;
    }

    public static int getTargetSdkVersion() {

        return component().app().getApplicationInfo().targetSdkVersion;
    }

    /**
     * 获取包名
     *
     * @return
     */
    public static String getPackageName() {

        return vriables().getPackageName();
    }

    public static String getProcessName() {
        String processName = "";

        int pid = android.os.Process.myPid();
        ActivityManager mActivityManager = LifeCycleUtils.component().getSystemService(Context.ACTIVITY_SERVICE);
        if (mActivityManager != null) {
            List<RunningAppProcessInfo> pinfoList = mActivityManager.getRunningAppProcesses();
            if (Assert.notEmpty(pinfoList)) {
                for (RunningAppProcessInfo appProcess : pinfoList) {
                    if (appProcess != null && appProcess.pid == pid) {
                        processName = appProcess.processName;

                        break;
                    }
                }
            }
        }

        return processName;
    }

    public static String getMEServicePackageName() {
        String meServicePackageName = "";

        ActivityManager mActivityManager = LifeCycleUtils.component().getSystemService(Context.ACTIVITY_SERVICE);
        List<RunningServiceInfo> runningServices = mActivityManager.getRunningServices(CAPACITY);
        if (Assert.notEmpty(runningServices)) {
            for (RunningServiceInfo rsInfo : runningServices) {
                if (rsInfo != null && rsInfo.service != null && rsInfo.process.endsWith(C.tag.process_message_exchange)) {
                    meServicePackageName = rsInfo.service.getPackageName();
                    break;
                }
            }
        }

        return meServicePackageName;
    }

    /**
     * 获取开发环境Index
     *
     * @param dlps
     * @param according
     * @return
     */
    public static int getISODE(String[] dlps, String according) {
        int result = -1;

        if (Assert.notEmpty(dlps) && Assert.notEmpty(according)) {
            for (int i = 0; i < dlps.length; i++) {
                if (according.equals(dlps[i])) {
                    result = i;

                    break;
                }
            }
        }

        return result;
    }

    /**
     * flurry sdk
     *
     * @return
     */
    public static String getB() {

        return RuntimeConfigure.obtain().b();
    }

    public static synchronized String obtainAPPClientAction() {
        if (Assert.isEmpty(mAPPClientAction)) {
            mAPPClientAction = String.format(C.tag.format_action_app_client, vriables().getAppCode());
        }

        return mAPPClientAction;
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static String uuid() {
        String strUUID = "";

        final Setting setting = LifeCycleUtils.component().setting();

        // 1.本地
        if (checkStoragePermissions()) {
            strUUID = PersistenceUtils.getUUID();
        }

        if (Assert.isEmpty(strUUID)) {
            try {
                // 2.SharedPreferences
                strUUID = setting.get(C.properties.uuid, "");
                if (Assert.notEmpty(strUUID)) {
                    // 备份到本地
                    writeUUID(strUUID);

                } else {
                    // 创建一个新的UUID
                    strUUID = Etcetera.uuid();

                    // 备份到SharedPreferences
                    setting.edit().put(C.properties.uuid, strUUID).apply();

                    writeUUID(strUUID);
                }
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        return strUUID;
    }

    /**
     * 备份到本地
     *
     * @param uuidText
     */
    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    private static final void writeUUID(final String uuidText) {
        if (checkStoragePermissions()) {
            ThreadPool.Impl.execute(new Runnable() {

                @Override
                @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
                public void run() {
                    PersistenceUtils.setUUID(uuidText);
                }
            });
        }
    }


    /* ********************
     * native method
     *
     * ********************
     */
    public static native void onInit(Context context);
}
