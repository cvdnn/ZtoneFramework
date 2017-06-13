/*
 * FilePathBuilder.java
 * 
 * Copyright 2011 sillar team, Inc. All rights reserved.
 * 
 * SILLAR PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package android.framework.builder;

import android.assist.Assert;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.module.FilePath;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * @author sillar team
 * @version 1.0.0
 * @since 1.0.0 Handy 2013-9-24
 */
@Deprecated
public class FilePathBuilder {
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

	/* *********************************************
     *
	 * ********************************************
	 */

    private StringBuilder mFilePathBuilder;

    /**
     * 目标路径是否是相对路径
     */
    private boolean mIsPathBuildToRelative;

    private FilePathBuilder(String path, boolean isPathBuildToRelative) {
        mIsPathBuildToRelative = isPathBuildToRelative;
        mFilePathBuilder = (isPathBuildToRelative || isAbsolutePath(path)) ? new StringBuilder(path) : createPathStringBuilder(path);
    }

    /**
     * 默认创建在APP_PATH下
     *
     * @return
     */
    public static FilePathBuilder create() {
        return create(IRuntime.vriables().getAppPath());
    }

    public static FilePathBuilder createExternalStorageBuilder() {
        return create("");
    }

    public static FilePathBuilder createAssetsBuilder() {

        return create(C.file.assets, true);
    }

    public static FilePathBuilder create(String path) {

        return create(path, false);
    }

    public static FilePathBuilder create(String path, boolean isPathBuildToRelative) {

        return new FilePathBuilder(path, isPathBuildToRelative);
    }

    public FilePathBuilder append(int path) {

        return append(String.valueOf(path));
    }

    public FilePathBuilder append(long path) {

        return append(String.valueOf(path));
    }

    public FilePathBuilder append(String path) {
        if (Assert.notEmpty(path)) {
            appendPath(mFilePathBuilder, path);
        }

        return this;
    }

    public FilePathBuilder join(String text) {
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
    public FilePathBuilder branch() {

        return new FilePathBuilder(toFilePath(), true);
    }

    public String toFilePath() {

        return Assert.notNull(mFilePathBuilder) ? mFilePathBuilder.toString() : createPathStringBuilder(IRuntime.vriables().getAppPath()).toString();
    }

    @NonNull
    public File toFile() {

        return new File(toFilePath());
    }

    private StringBuilder createPathStringBuilder(String path) {

        return appendPath(new StringBuilder(FilePath.dispatchDataDirectory(mRootPathFlag).getAbsolutePath()), path);
    }

    private StringBuilder appendPath(@NonNull StringBuilder sb, String path) {
        if (Assert.notEmpty(path)) {
            if (Assert.isEmpty(sb)) {
                // 相对路径要去掉路径最前面的‘/’
                if (mIsPathBuildToRelative && path.startsWith(File.separator)) {
                    path = path.substring(1);

                    // 绝对路径要添加路径最前面的‘/’
                } else if (!mIsPathBuildToRelative && !path.startsWith(File.separator)) {
                    sb.append(File.separator);
                }

                // 去掉一个‘/’
            } else if (Assert.endsWith(sb, File.separator) && path.startsWith(File.separator)) {
                sb.deleteCharAt(sb.length() - 1);

                // 添加一个‘/’
            } else if (!Assert.endsWith(sb, File.separator) && !path.startsWith(File.separator)) {
                sb.append(File.separator);
            }

            sb.append(path);
        }

        return sb;
    }

    private boolean isAbsolutePath(String path) {

        return Assert.notEmpty(path) && path.startsWith(File.separator);
    }
}
