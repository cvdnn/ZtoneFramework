/*
 * AppConfog.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework;

import android.assist.TextUtilz;
import android.framework.builder.FilePathBuilder;
import android.framework.module.FilePath;
import android.io.StreamUtils;
import android.log.Log;
import android.support.annotation.RequiresPermission;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-8-30
 */
public final class PersistenceUtils {
    private static String TAG = "SystemConfigure";

    private static Configure mSystemConfigure, mCommonSettingConfigure;
    private static String mUUID;

    private static String getSystemConfigFilePath() {
        String configPath = FilePathBuilder.createExternalStorageBuilder().append(C.file.android_system_config).toFilePath();
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "Android setting create error: %s", configPath);

                configPath = FilePath.bin()
                        .append(C.file.shared_prefs_system_config).join(C.file.raw_suffixes_properties).toFilePath();

                configFile = new File(configPath);
                if (!configFile.exists()) {
                    try {
                        configFile.getParentFile().mkdirs();
                        configFile.createNewFile();
                    } catch (Exception exc) {
                        Log.e(TAG, "create system config error: %s", configPath);
                    }
                }
            }
        }

        return configPath;
    }

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    private static Configure obtain() {
        if (mSystemConfigure == null) {
            synchronized (PersistenceUtils.class) {
                if (mSystemConfigure == null) {
                    mSystemConfigure = new Configure() {

                        @Override
                        protected void onLoadProperties() throws Exception {
                            mUUID = TextUtilz.fromFake(get(C.properties.uuid));
                        }
                    };

                    InputStream fin = null;

                    try {
                        fin = new FileInputStream(getSystemConfigFilePath());
                        mSystemConfigure.load(fin, false);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    } finally {
                        StreamUtils.close(fin);
                    }
                }
            }
        }

        return mSystemConfigure;
    }

    @RequiresPermission(READ_EXTERNAL_STORAGE)
    public static String getUUID() {
        Configure persistence = obtain();

        return mUUID;
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static void setUUID(String uuid) {
        mUUID = uuid;

        Configure persistence = obtain();
        if (persistence != null) {
            persistence.set(C.properties.uuid, TextUtilz.toFake(mUUID));
            persistence.write(new FilePath.Builder().build().append(C.file.android_system_config).toFilePath());
        }
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static String get(String key) {
        return obtainCommonSetting().get(key);
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static String get(String key, String defaultValue) {
        return obtainCommonSetting().get(key, defaultValue);
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static void set(String key, String value) {

        obtainCommonSetting().set(key, value);
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static void remove(String key) {

        obtainCommonSetting().remove(key);
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    public static void write() {
        Configure persistence = obtainCommonSetting();

        if (persistence != null) {
            persistence.write(getSettingFilePath());
        }
    }

    @RequiresPermission(WRITE_EXTERNAL_STORAGE)
    private static String getSettingFilePath() {
        final String configPath = FilePath.bin().append(C.file.app_setting).toFilePath();
        File configFile = new File(configPath);

        if (!configFile.exists()) {
            try {
                configFile.getParentFile().mkdirs();
                configFile.createNewFile();
            } catch (Exception e) {
                Log.e(TAG, "common setting error: %s", configPath);
            }
        }

        return configPath;
    }

    @RequiresPermission(allOf = {WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE})
    private static Configure obtainCommonSetting() {
        if (mCommonSettingConfigure == null) {
            synchronized (PersistenceUtils.class) {
                if (mCommonSettingConfigure == null) {
                    mCommonSettingConfigure = new Configure() {

                        @Override
                        protected void onLoadProperties() throws Exception {

                        }
                    };

                    InputStream fin = null;

                    try {
                        fin = new FileInputStream(getSettingFilePath());
                        mCommonSettingConfigure.load(fin, true);
                    } catch (Exception e) {
                        Log.e(TAG, e);
                    } finally {
                        StreamUtils.close(fin);
                    }
                }
            }
        }

        return mCommonSettingConfigure;
    }
}
