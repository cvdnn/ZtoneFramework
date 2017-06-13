package android.framework.pull.interceptor;

import android.concurrent.ThreadPool;
import android.framework.entity.Entity;
import android.framework.entity.PullEntity;
import android.framework.entity.ResultEntity;
import android.framework.mvp.Interacter;
import android.framework.pull.PullUtils;
import android.framework.util.ValidateUtils;
import android.io.FileUtils;
import android.json.JSONUtils;

import java.io.File;

import okhttp3.HttpUrl;
import okhttp3.Request;

import static android.framework.C.value.charset_encoding;

/**
 * 从缓存中获取
 * <p>
 * Created by handy on 17-1-20.
 */


public final class LocalCachePullInterceptor implements PullInterceptor {

    @Override
    public <P extends PullEntity> P onIntercept(PullRelate<P> relate)  {
        P entity = relate.entity();

        Request request = relate.request();
        HttpUrl url = request.url();

        File cacheFile = new File(PullUtils.getPullDataPath(url));
        if (FileUtils.exists(cacheFile)) {
            entity.parse(JSONUtils.getJSONObject(cacheFile));

            // 当文件过期
            if (PullUtils.isOverdue(cacheFile, entity.intervalTime)) {
                entity = relate.proceed(request);

                handleEntityData(url, entity);
            }
        } else {
            entity = relate.proceed(request);

            handleEntityData(url, entity);
        }

        return entity;
    }

    private void handleEntityData(HttpUrl url, final PullEntity entity) {
        if (ValidateUtils.check(entity)) {
            final File dataFile = new File(PullUtils.getPullDataPath(url));
            if (entity.intervalTime > 0) {
                ThreadPool.Impl.execute(new Runnable() {

                    @Override
                    public void run() {
                        synchronized (Interacter.class) {
                            String text = new Entity.Formatter().entity(entity).format().toString();
                            FileUtils.write(dataFile, text, charset_encoding);
                        }
                    }
                });
            } else {
                dataFile.delete();
            }
        } else {
            entity.result = ResultEntity.RESPONSE_ERROR;
        }
    }
}
