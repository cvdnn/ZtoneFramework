package android.framework.pull.interceptor;

import android.assist.Assert;
import android.framework.entity.PullEntity;
import android.framework.module.Validator;
import android.framework.pull.PullFollow;
import android.framework.pull.PullUtils;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.Call;
import okhttp3.Request;

public abstract class PullClan {

    private static final ExecutorService PullThreadPool = new ThreadPoolExecutor(8, 16, 5000l, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());

    protected final ArrayList<PullInterceptor> mPullInterceptors = new ArrayList<>();

    protected final <P extends PullEntity> P handlePullRequest(@NonNull Request request, @NonNull P p) {
        PullLoopRelate<P> relate = new PullLoopRelate<>(0, request, p, new PullFollow());
        relate.setInterceptors(newPullInterceptArrays());

        return relate.proceed(request);
    }

    protected final <P extends PullEntity> Chain enqueuePullRequest(@NonNull Request request, @NonNull P p) {
        PullLoopRelate<P> pChain = new PullLoopRelate<>(0, request, p);
        pChain.setInterceptors(newPullInterceptArrays());

        return pChain;
    }

    public final PullClan add(PullInterceptor interceptor) {
        mPullInterceptors.add(interceptor);

        return this;
    }

    public void cancel() {
        PullThreadPool.shutdownNow();
    }

    private final ArrayList<PullInterceptor> newPullInterceptArrays() {
        ArrayList<PullInterceptor> interceptors = new ArrayList<>();

        interceptors.add(new PullEntityInterceptor());

        if (Assert.notEmpty(mPullInterceptors)) {
            interceptors.addAll(mPullInterceptors);
        }

        interceptors.add(new PullStateInterceptor());
        interceptors.add(new PulledFilterInterceptor(PullUtils.getPulledFilter()));
        interceptors.add(new RequestConnectionPullInterceptor());

        return interceptors;
    }

    public class Holder<P extends PullEntity> implements Validator {
        public int position = -1;
        public P entity;

        @Override
        public boolean check() {

            return entity != null;
        }
    }

    /**
     * ******************************************
     * <p>
     * ********************************************
     */

    public interface Chain {

        void enqueue(PullFollow follow);

        void cancel();

        boolean isCanceled();

        abstract class Relate<P extends PullEntity> implements Chain, PullRelate<P> {
            private final AtomicBoolean mIsGoOn = new AtomicBoolean(true);

            private Call mOKCall;
            private Future<P> mPullFuture;

            private PullFollow<P> mPullFollow;

            protected Relate(PullFollow<P> mPullFollow) {
                this.mPullFollow = mPullFollow;
            }

            @Override
            public void enqueue(PullFollow follow) {
                if (!isCanceled()) {
                    mIsGoOn.set(true);

                    mPullFollow = follow;
                    if (mPullFollow != null) {
                        mPullFuture = PullThreadPool.submit(new Callable<P>() {

                            @Override
                            public P call() throws Exception {
                                Request request = request();

                                P p = proceed(request);
                                if (mPullFollow != null) {
                                    int position = -1;

                                    PullClan.Holder<P> pHolder = mPullFollow.getRequestHolder(request);
                                    if (pHolder != null) {
                                        position = pHolder.position;
                                    }

                                    mPullFollow.post(position, p, request.url().toString());
                                }

                                return p;
                            }
                        });

                    }
                }
            }

            @Override
            public void cancel() {
                if (mOKCall != null) {
                    mOKCall.cancel();
                }

                if (mPullFuture != null) {
                    mPullFuture.cancel(true);
                }

                mIsGoOn.set(false);
            }

            @Override
            public boolean isCanceled() {

                return mPullFuture != null && mPullFuture.isCancelled() || mOKCall != null && mOKCall.isCanceled();
            }

            @Override
            public PullFollow<P> follow() {

                return mPullFollow;
            }

            public Relate<P> setCall(Call call) {
                mOKCall = call;

                return this;
            }
        }
    }
}
