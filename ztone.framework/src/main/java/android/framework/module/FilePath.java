/*
 * FilePathBuilder.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.module;

import android.assist.Assert;
import android.framework.C;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.io.FileUtils;
import android.os.Environment;
import android.support.annotation.NonNull;

import java.io.File;

import static android.framework.IRuntime.checkStoragePermissions;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-9-24
 */
public class FilePath {
    public static final byte SF_SDCARD = 0x01;
    public static final byte SF_DATA_DATA = 0x02;

    public static final byte SF_MARK = SF_SDCARD | SF_DATA_DATA;

    private static byte mRootPathFlag = SF_MARK;

    public static void setStorageFlag(byte flag) {
        mRootPathFlag = flag != 0 ? flag : SF_MARK;
    }

    public static int getStorageFlag() {

        return mRootPathFlag;
    }

    public static FilePath bin() {

        return new FilePath.Builder().build().append(C.file.path_bin);
    }

    public static FilePath image() {

        return new FilePath.Builder().build().append(C.file.path_image);
    }

    public static FilePath cache() {

        return new FilePath.Builder().build().append(C.file.path_cache);
    }

    public static FilePath download() {

        return new FilePath.Builder().build().append(C.file.path_download);
    }

    public static FilePath log() {

        return new FilePath.Builder().build().append(C.file.path_log);
    }

    public static FilePath mqtt() {

        return new FilePath.Builder().build().append(C.file.path_mqtt);
    }

    public static FilePath crash() {

        return new FilePath.Builder().build().append(C.file.path_crash);
    }

    public static FilePath temp() {

        return new FilePath.Builder().build().append(C.file.path_temp);
    }

    public static boolean tryMKDirs() {
        boolean result = false;
        for (byte flag = SF_SDCARD; flag < SF_MARK; flag = (byte) (flag << 1)) {
            if (lookupDataDirectory(flag)) {
                setStorageFlag(flag);

                result = true;

                break;
            }
        }

        if (!result) {
            setStorageFlag(SF_MARK);
        }

        return result;
    }

    public static File dispatchDataDirectory(byte flag) {
        File baseFile = null;

        // 需要进行读写权限判断
        if (Assert.as(flag, SF_SDCARD)) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && checkStoragePermissions()) {
                File extdir = Environment.getExternalStorageDirectory();
                if (FileUtils.exists(extdir) && extdir.canRead() && extdir.canWrite()) {
                    baseFile = extdir;
                }
            }
        }

        if (Assert.as(flag, SF_DATA_DATA)) {
            baseFile = LifeCycleUtils.component().app().getFilesDir();
        }

        if (baseFile == null || (flag & SF_MARK) == 0) {
            baseFile = LifeCycleUtils.component().app().getFilesDir();
        }

        return baseFile;
    }

    public static boolean lookupDataDirectory(byte flag) {
        boolean result = false;

        File baseFile = null;

        // 需要进行读写权限判断
        if (Assert.as(flag, SF_SDCARD)) {
            if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) && checkStoragePermissions()) {
                baseFile = Environment.getExternalStorageDirectory();
            }
        } else if (Assert.as(flag, SF_DATA_DATA)) {
            baseFile = LifeCycleUtils.component().app().getFilesDir();
        }

        if (FileUtils.exists(baseFile) && baseFile.canRead() && baseFile.canWrite()) {
            result = true;
        }

        return result;
    }

    public static void mkdirs(String... relativePaths) {
        if (Assert.notEmpty(relativePaths)) {
            for (String path : relativePaths) {
                File file = new FilePath.Builder().build().append(path).toFile();
                if (!file.exists()) {
                    if (file.isDirectory()) {
                        file.mkdirs();

                    } else {
                        file.getParentFile().mkdirs();
                    }
                }
            }
        }
    }

    public static boolean isAbsolutePath(String path) {

        return Assert.notEmpty(path) && path.startsWith(File.separator);
    }

    public static StringBuilder append(@NonNull StringBuilder pathBuilder, String path) {
        if (Assert.notEmpty(path)) {
            if (Assert.notEmpty(pathBuilder)) {
                // 去掉一个‘/’
                if (Assert.endsWith(pathBuilder, File.separator) && path.startsWith(File.separator)) {
                    pathBuilder.deleteCharAt(pathBuilder.length() - 1);

                    // 添加一个‘/’
                } else if (!Assert.endsWith(pathBuilder, File.separator) && !path.startsWith(File.separator)) {
                    pathBuilder.append(File.separator);
                }
            }

            pathBuilder.append(path);
        }

        return pathBuilder;
    }

	/* *********************************************
     *
	 * ********************************************
	 */

    private final StringBuilder mFilePathBuilder;

    private FilePath(String path) {
        mFilePathBuilder = new StringBuilder(path);
    }

    public FilePath append(int path) {

        return append(String.valueOf(path));
    }

    public FilePath append(long path) {

        return append(String.valueOf(path));
    }

    public FilePath append(String path) {
        if (Assert.notEmpty(path)) {
            append(mFilePathBuilder, path);
        }

        return this;
    }

    public FilePath join(String text) {
        if (Assert.notEmpty(text) && mFilePathBuilder != null) {
            mFilePathBuilder.append(text);
        }

        return this;
    }

    /**
     * 创建分支,与源FilePathBuilder分离
     *
     * @return
     */
    @NonNull
    public FilePath branch() {

        return new FilePath(toFilePath());
    }

    @NonNull
    public String toFilePath() {

        return mFilePathBuilder.toString();
    }

    @NonNull
    public File toFile() {

        return new File(toFilePath());
    }

    /* *****************************
     *
     * *****************************/

    public static class Builder {

        private String mFileDirectory;
        private boolean mIsRelative, mIsAssets;

        public Builder directory(String directory) {
            mFileDirectory = directory;

            return this;
        }

        public Builder relative() {
            mIsRelative = true;

            return this;
        }

        public Builder assets() {
            mIsAssets = true;

            return this;
        }

        public FilePath build() {
            StringBuilder pathBuilder = new StringBuilder();

            // 相对路径
            if (mIsRelative) {
                // 相对路径要去掉路径最前面的‘/’
                if (Assert.notEmpty(mFileDirectory) && mFileDirectory.startsWith(File.separator)) {
                    mFileDirectory = mFileDirectory.substring(1);
                }

                if (Assert.notEmpty(mFileDirectory)) {
                    pathBuilder.append(mFileDirectory);
                }

                // 绝对路径
            } else {
                if (mIsAssets) {
                    pathBuilder.append(C.file.assets);

                    // 初始化绝对路径根目录
                } else if (Assert.isEmpty(mFileDirectory) || !mFileDirectory.startsWith(File.separator)) {
                    pathBuilder.append(dispatchDataDirectory(mRootPathFlag).getAbsolutePath());

                }

                append(pathBuilder, mFileDirectory);
            }

            return new FilePath(pathBuilder.toString());
        }
    }
}
