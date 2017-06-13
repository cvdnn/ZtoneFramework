package android.framework.pull.interceptor;

import android.framework.entity.PullEntity;

/**
 * Created by handy on 17-1-20.
 */

public interface PullInterceptor {

    <P extends PullEntity> P onIntercept(PullRelate<P> relate);
}
