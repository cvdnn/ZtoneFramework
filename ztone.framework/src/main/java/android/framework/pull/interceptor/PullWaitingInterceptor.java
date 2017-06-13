package android.framework.pull.interceptor;

import android.app.Activity;
import android.content.Context;
import android.extend.wait.WaitUtils;
import android.extend.wait.Waitting;
import android.framework.Loople;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.PullEntity;
import android.log.Log;
import android.support.v4.app.Fragment;

import static android.framework.Loople.inMainThread;

/**
 * Created by handy on 17-1-24.
 */

public final class PullWaitingInterceptor implements PullInterceptor {
    private static final String TAG = "PullWaitingInterceptor";

    private Waitting mWaitingView;

    private boolean mHideFlag = true;

    public PullWaitingInterceptor(Fragment fragment) {
        if (fragment != null) {
            Activity activity = fragment.getActivity();
            if (!LifeCycleUtils.isFinishing(activity)) {
                mWaitingView = WaitUtils.obtain(activity);
            }
        }
    }

    public PullWaitingInterceptor(Context context) {
        if (context != null) {
            mWaitingView = WaitUtils.obtain(context);
        }
    }

    @Override
    public <P extends PullEntity> P onIntercept(PullRelate<P> relate) {
        if (!inMainThread()) {
            showWaiting();
        }

        P p = relate.proceed(relate.request());

        if (!inMainThread() && mHideFlag) {
            hideWaiting();
        }

        return p;
    }

    public PullWaitingInterceptor setHideFlag(boolean flag) {
        mHideFlag = flag;

        return this;
    }

    public void showWaiting() {
        Loople.post(new Runnable() {

            @Override
            public void run() {
                if (mWaitingView != null && mWaitingView.isInActivityLifecycle()) {
                    try {
                        mWaitingView.show();
                    } catch (Throwable t) {
                        Log.e(TAG, t);
                    }
                }
            }
        });
    }

    public void hideWaiting() {
        Loople.post(new Runnable() {

            @Override
            public void run() {
                if (mWaitingView != null && mWaitingView.isShowing()) {
                    try {
                        mWaitingView.dismiss();
                    } catch (Throwable t) {
                        Log.v(TAG, t);
                    }
                }
            }
        });
    }

    public boolean isWaiting() {
        return mWaitingView != null && mWaitingView.isShowing();
    }
}
