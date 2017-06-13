package android.framework.module;

import android.assist.Assert;
import android.framework.AppResource;
import android.framework.C;
import android.framework.IRuntime;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.Entity;
import android.json.JSONUtils;
import android.log.Log;
import android.support.v4.content.PermissionChecker;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.support.v4.content.PermissionChecker.PERMISSION_GRANTED;


public class AssertsMapping {
    private static final String TAG = "AssertsMapping";

    private String mAssertRelativeDirectory, mLocalRelativeDirectory;
    private boolean mIsLocalFile;

    private AssertsMapping() {
        mAssertRelativeDirectory = "";
        mLocalRelativeDirectory = IRuntime.vriables().getAppPath();
    }

    public static AssertsMapping create() {

        return new AssertsMapping();
    }

    public static <E extends Entity> E open(Class<E> clazz, String fileName) {
        return open(clazz, "", IRuntime.vriables().getAppPath(), fileName);
    }

    public static <E extends Entity> E open(Class<E> clazz, String assertRelativeDirectory, String localRelativeDirectory, String fileName) {
        E e = null;

        InputStream inputStream = null;

        try {
            inputStream = AssertsMapping.create().cd(assertRelativeDirectory, localRelativeDirectory).open(fileName);
            JSONObject jsonObject = JSONUtils.getJSONObject(inputStream);

            e = new Entity.Builder().clazz(clazz).json(jsonObject).build();
        } catch (Exception ex) {
            Log.e(TAG, ex);
        } finally {

            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (Exception ex) {
                    Log.v(TAG, ex);
                }
            }
        }

        return e;
    }

    /**
     * change relative directory
     *
     * @return
     */
    public AssertsMapping cd(String assertRelativeDirectory, String localRelativeDirectory) {
        mAssertRelativeDirectory = assertRelativeDirectory;
        mLocalRelativeDirectory = localRelativeDirectory;

        return this;
    }

    /**
     * 打开文件流
     *
     * @param fileName
     * @return
     * @throws IOException
     */

    public InputStream open(String fileName) {
        InputStream inputStream = null;

        if (Assert.notEmpty(fileName)) {
            String rawName = fileName;
            if (fileName.endsWith(C.file.mix_suffixes_properties)) {
                rawName = fileName.replace(C.file.mix_suffixes_properties, C.file.raw_suffixes_properties);
            }

            int permission = PermissionChecker.checkSelfPermission(LifeCycleUtils.component().app(), READ_EXTERNAL_STORAGE);

            File stbFile = new FilePath.Builder().directory(mLocalRelativeDirectory).build().append(rawName).toFile();
            if (permission == PERMISSION_GRANTED && stbFile.exists()) {
                try {
                    FileInputStream fileStream = new FileInputStream(stbFile);
                    inputStream = new BufferedInputStream(fileStream);
                } catch (Exception e) {
                    Log.e(TAG, e);
                }

                mIsLocalFile = true;
            } else {
                inputStream = AppResource.getInputStreamFromAssets(new FilePath.Builder().relative().build().append(fileName).toFilePath());

                mIsLocalFile = false;
            }
        }

        return inputStream;
    }

    /**
     * 是否加载了本地文件
     *
     * @return
     */
    public boolean isLocalFile() {

        return mIsLocalFile;
    }
}
