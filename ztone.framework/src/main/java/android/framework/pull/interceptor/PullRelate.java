package android.framework.pull.interceptor;

import android.framework.entity.PullEntity;
import android.framework.pull.PullFollow;

import okhttp3.Request;

/**
 * Created by handy on 17-3-31.
 */

public interface PullRelate<P extends PullEntity> {
    Request request();

    PullFollow<P> follow();

    P entity();

    P proceed(Request request);
}
