package android.framework.pull.interceptor;

import android.assist.Assert;
import android.framework.entity.PullEntity;
import android.framework.pull.PullFollow;

import java.util.List;

import okhttp3.Request;

/**
 * Created by handy on 17-1-20.
 */

class PullLoopRelate<P extends PullEntity> extends PullClan.Chain.Relate<P> {

    private final Request mRequest;
    private final int mIndex;

    private final P mPullEntity;

    private List<PullInterceptor> mInterceptors;

    protected PullLoopRelate(int index, Request request, P pullEntity) {
        this(index, request, pullEntity, null);
    }

    protected PullLoopRelate(int index, Request request, P pullEntity, PullFollow<P> pullFollow) {
        super(pullFollow);

        mIndex = index;
        mRequest = request;
        mPullEntity = pullEntity;
    }

    @Override
    public P proceed(Request request) {
        P p = entity();

        if (request != null && Assert.checkIndex(mInterceptors, mIndex)) {
            PullInterceptor interceptor = mInterceptors.get(mIndex);
            if (interceptor != null) {
                PullLoopRelate<P> loopRelate = new PullLoopRelate<>(mIndex + 1, request, p, follow());
                loopRelate.setInterceptors(mInterceptors);

                p = interceptor.onIntercept(loopRelate);
            }
        }

        return p;
    }

    @Override
    public P entity() {

        return mPullEntity;
    }

    @Override
    public Request request() {

        return mRequest;
    }

    public PullLoopRelate<P> setInterceptors(List<PullInterceptor> interceptors) {
        mInterceptors = interceptors;

        return this;
    }
}
