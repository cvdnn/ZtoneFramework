package android.framework.pull;

import android.app.Activity;
import android.app.Fragment;
import android.assist.Assert;
import android.content.Context;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.context.lifecycle.OnLifeCycleListener;
import android.framework.entity.Entity;
import android.framework.entity.PullEntity;
import android.framework.pull.interceptor.PullClan;
import android.support.annotation.NonNull;
import android.support.annotation.WorkerThread;

import okhttp3.Request;
import okhttp3.RequestBody;

public final class Pulley extends PullClan implements OnLifeCycleListener {
    private static final String TAG = "Pulley";

    protected final Context mContext;

    private Pulley(Context context) {
        super();

        mContext = context;
    }

    @WorkerThread
    public <E extends PullEntity> E shuttle(Request request, Class<E> clazz) {
        E e = new Entity.Builder().clazz(clazz).build();

        Holder<E> holder = new Holder<>();
        holder.entity = e;

        Request desRequest = request.newBuilder().tag(holder).build();

        return handlePullRequest(desRequest, e);
    }

    @Deprecated
    public <E extends PullEntity> Chain enqueue(final Request request, final Class<E> clazz, final OnPullListener<E> pullDataListener) {

        return enqueue(-1, request, clazz, null, PullFollow.wrap(pullDataListener));
    }

    @Deprecated
    public <E extends PullEntity> Chain enqueue(final int position, final Request request, final Class<E> clazz,
                                                final PullFlag pullFlag, final OnPullListener<E> pullDataListener) {

        return enqueue(position, request, clazz, pullFlag, PullFollow.wrap(pullDataListener));
    }

    public <E extends PullEntity> Chain enqueue(final Request request, final Class<E> clazz, final PullFollow<E> pullFollow) {

        return enqueue(-1, request, clazz, null, pullFollow);
    }

    public <E extends PullEntity> Chain enqueue(final int position, final Request request, final Class<E> clazz,
                                                final PullFlag pullFlag, final PullFollow<E> pullFollow) {
        E e = new Entity.Builder().clazz(clazz).build();
        if (e != null) {
            e.pullFlag = pullFlag;
        }

        Holder<E> holder = new Holder<>();
        holder.position = position;
        holder.entity = e;

        Request desRequest = request.newBuilder().tag(holder).build();

        Chain pChain = enqueuePullRequest(desRequest, e);
        pChain.enqueue(pullFollow);

        return pChain;
    }

    private boolean isContextFinish() {

        return mContext instanceof Activity && ((Activity) mContext).isFinishing();
    }

    @Override
    public <C extends Context> void onStart(@NonNull C context) {

    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {
        cancel();
    }

    public static class Builder {
        private static String URL_NONE = "http://jzg01.com/app";

        private Object mAttachObject;

        public <O> Builder attach(O o) {
            mAttachObject = o;

            return this;
        }

        public Request request(@NonNull String url) {

            return new Request.Builder().get().url(Assert.notEmpty(url) ? url : URL_NONE).build();
        }

        public Request delete(@NonNull String url) {

            return new Request.Builder().delete().url(Assert.notEmpty(url) ? url : URL_NONE).build();
        }

        public Request request(@NonNull String url, RequestBody requestBody) {

            return new Request.Builder().url(Assert.notEmpty(url) ? url : URL_NONE).post(requestBody).build();
        }

        public final Pulley build() {
            Pulley pulley = null;

            if (mAttachObject != null) {
                Activity activity = null;
                if (mAttachObject instanceof Fragment) {
                    activity = ((Fragment) mAttachObject).getActivity();

                } else if (mAttachObject instanceof Activity) {
                    activity = (Activity) mAttachObject;
                }

                if (activity != null) {
                    pulley = new Pulley(activity);
                }
            }

            if (pulley == null) {
                pulley = new Pulley(null);
            }

            // 为了规避调用者可能存在的listener泄漏，当不明绑定对象不注册生命周期监听
            if (mAttachObject != null) {
                LifeCycleUtils.adhere(mAttachObject).register(pulley);
            }

            return pulley;
        }
    }
}
