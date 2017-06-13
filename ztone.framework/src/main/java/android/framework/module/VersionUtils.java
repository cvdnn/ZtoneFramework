package android.framework.module;

import android.assist.Assert;
import android.concurrent.ThreadPool;
import android.framework.IRuntime;
import android.framework.Loople;
import android.framework.context.ActivityManager;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.VersionEntity;
import android.framework.pull.Pulley;
import android.framework.util.APKUtils;
import android.io.FileUtils;
import android.log.Log;
import android.network.http.ResponseTransform;

import java.io.File;

import okhttp3.Request;

public final class VersionUtils {
    private static final String TAG = "Version";

    public static int getAppVersionCode() {

        return ActivityManager.getApplicationVersionCode(LifeCycleUtils.component().app(), IRuntime.getPackageName());
    }

    public static String getAppVersionName() {

        return ActivityManager.getApplicationVersionName(LifeCycleUtils.component().app(), IRuntime.getPackageName());
    }

    public static int getVersionCode(VersionEntity versionEntity) {

        return versionEntity != null ? versionEntity.verCode : 0;
    }

    public static String getVersionName(VersionEntity versionEntity) {

        return versionEntity != null ? versionEntity.verName : "";
    }

    public static String getInstallApkPath(VersionEntity versionEntity) {
        String versionName = versionEntity != null && Assert.notEmpty(versionEntity.verName) ? versionEntity.verName : "V1.0";

        return FilePath.download().append(IRuntime.vriables().getAppName()).join("_").join(versionName).join(".apk").toFilePath();
    }

    public static String getApkUrl(VersionEntity versionEntity) {

        return versionEntity == null ? "" : versionEntity.url;
    }

    public static void downloadApk(final VersionEntity versionEntity, final OnAPKDownloadListener downloadListener) {
        ThreadPool.Impl.execute(new Runnable() {

            @Override
            public void run() {
                if (downloadApk(versionEntity)) {
                    Loople.post(new Runnable() {

                        @Override
                        public void run() {
                            if (downloadListener != null) {
                                downloadListener.onFinished(versionEntity);
                            }
                        }
                    });
                }
            }
        });
    }

    public static boolean downloadApk(VersionEntity versionEntity) {
        boolean result = false;
        if (versionEntity != null) {
            String installPath = getInstallApkPath(versionEntity);

            File apkFile = new File(installPath);
            if (apkFile.exists()) {
                result = true;
            } else {
                try {
                    Request request = new Request.Builder().get().url(versionEntity.url).build();
                    result = ResponseTransform.download(request, installPath);
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        }

        return result;
    }

    public static boolean needToUpdate(VersionEntity versionEntity) {

        return versionEntity != null && versionEntity.needToUpdate(getAppVersionCode());
    }

    public static void installApk(VersionEntity versionEntity) {
        String installApkPath = getInstallApkPath(versionEntity);

        // 防止重复提交
        if (ActivityManager.isTopApplication() && needToUpdate(versionEntity) && FileUtils.exists(installApkPath)) {
            APKUtils.installApk(LifeCycleUtils.component().app(), installApkPath);
        }
    }

    public static void check(final String checkUrl, final OnVersionCheckListener checkListener) {
        ThreadPool.Impl.execute(new Runnable() {

            @Override
            public void run() {
                final VersionEntity versionEntity = getVersionEntity(checkUrl);

                Loople.post(new Runnable() {

                    @Override
                    public void run() {
                        if (checkListener != null) {
                            checkListener.onChecked(needToUpdate(versionEntity));
                        }
                    }
                });
            }
        });
    }

    private static VersionEntity getVersionEntity(String checkUrl) {
        VersionEntity entity = null;
        if (Assert.notEmpty(checkUrl)) {
            try {
                Pulley.Builder pullBuilder = new Pulley.Builder();
                Request request = pullBuilder.request(checkUrl);

                entity = pullBuilder.build().shuttle(request, VersionEntity.class);
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        return entity;
    }

    public interface OnAPKDownloadListener {

        void onFinished(VersionEntity entity);
    }

    public interface OnVersionCheckListener {

        void onChecked(boolean needToUpdate);
    }
}
