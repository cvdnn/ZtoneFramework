package android.framework.pull;

import android.framework.entity.PullEntity;
import android.support.annotation.NonNull;

/**
 * Created by handy on 17-1-23.
 */

public interface OnPullListener<E extends PullEntity> {

    /**
     * 获取回调数据用于刷新页面,回调的数据已经经过一层result过滤.
     */
    void onPulled(int position, @NonNull E entity, String url);

    void onError(int errorCode, String message);

    abstract class Impl<E extends PullEntity> implements OnPullListener<E> {

        @Override
        public void onError(int errorCode, String message) {

        }
    }
}