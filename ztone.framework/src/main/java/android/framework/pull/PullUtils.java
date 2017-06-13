package android.framework.pull;

import android.framework.entity.PullEntity;
import android.framework.module.FilePath;
import android.io.FileUtils;
import android.json.JSONUtils;
import android.log.Log;
import android.reflect.ClazzLoader;
import android.support.annotation.NonNull;
import android.network.http.HTTPx;

import java.io.File;

import okhttp3.HttpUrl;

/**
 * Created by handy on 17-1-20.
 */

public class PullUtils {
    private static final String TAG = "PullUtils";

    private static PulledFilter sPulledFilter;

    public static void registerPulledFilter(PulledFilter pulledFilter) {
        sPulledFilter = pulledFilter;
    }

    public static PulledFilter getPulledFilter() {

        return sPulledFilter;
    }

    @NonNull
    public static String getPullDataPath(HttpUrl httpURL) {

        return FilePath.cache().append(HTTPx.getTag(httpURL)).toFilePath();
    }

    public static <E extends PullEntity> E getCacheData(final File cacheFile, final Class<E> clazz) {
        E e = ClazzLoader.newInstance(clazz);
        if (e != null) {
            try {
                e.parse(JSONUtils.getJSONObject(cacheFile));
            } catch (Exception ex) {
                Log.e(TAG, ex);
            }
        }

        return e;
    }

    /**
     * 是否缓存文件过期
     */
    public static boolean isOverdue(File dataFile, long limitTime) {
        boolean result = true;

        if (FileUtils.exists(dataFile)) {
            if (limitTime < 0 || System.currentTimeMillis() - dataFile.lastModified() < limitTime) {
                result = false;
            }
        }

        return result;
    }
}
