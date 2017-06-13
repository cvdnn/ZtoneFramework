package android.framework.context;

import android.annotation.SuppressLint;
import android.assist.Assert;
import android.content.Intent;
import android.extend.view.ViewUtils;
import android.extend.wait.WaitUtils;
import android.extend.wait.Waitting;
import android.extend.wait.Waitting.OnWaitCancelListener;
import android.framework.Android;
import android.framework.AppResource;
import android.framework.context.lifecycle.ExtendLifeCycle;
import android.framework.pull.Pulley;
import android.graphics.drawable.Drawable;
import android.log.Log;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.reflect.ClazzLoader;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class FrameActivity extends AppCompatActivity implements ExtendLifeCycle {
    private static final String TAG = "FrameActivity";

    private boolean mIsDestroyed;

    private boolean mStateSaved;

    private int mWindowsBackground;

    private Waitting mWaitting;

    protected CharSequence mSubTitle;

    protected ActionBar mActionBar;
    protected int mActionBarViewId;

    private View mActionBarView;
    private View mHomeView;
    private View mUpView;

    private boolean mAnalyticsEnable;

    protected final Handler mContextHandler = new Handler();
    protected final Pulley.Builder mPulleyBuilder = new Pulley.Builder();

    protected Unbinder mButterUnbinder;

    /**
     * 初始化数据，在onCreate()中被调用。
     */
    @Override
    public void onPrepareData(@NonNull Bundle extraBundle, boolean fromInstanceState) throws Exception {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mIsDestroyed = false;

        try {
            if (Assert.notEmpty(savedInstanceState)) {
                onPrepareData(savedInstanceState, true);
            } else {
                Bundle extraBundle = getIntent().getExtras();

                onPrepareData(Assert.notEmpty(extraBundle) ? extraBundle : new Bundle(), false);
            }
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);

        try {
            onPrepareView();
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Override
    public void setContentView(View view) {
        super.setContentView(view);

        try {
            onPrepareView();
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    @Override
    public void setContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);

        try {
            onPrepareView();
        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    protected final void superContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }


    public final void superContentView(View view) {
        super.setContentView(view);
    }

    public final void superContentView(View view, ViewGroup.LayoutParams params) {
        super.setContentView(view, params);
    }

    /**
     * 初始化视图，在setContentView()之后被调用。
     */
    @Override
    public void onPrepareView() throws Exception {
        mButterUnbinder = ButterKnife.bind(this);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.hide();
        }

        if (AppResource.isAboveHoneycomb()) {
            mActionBarViewId = ClazzLoader.getFieldValue(ClazzLoader.forName("com.android.internal.R$id"), "action_bar");
        } else {
            mActionBarViewId = AppResource.getId("action_bar");
        }
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();

        if (mButterUnbinder != null) {
            mButterUnbinder.unbind();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;

        switch (item.getItemId()) {
            case android.R.id.home: {
                finish();

                break;
            }
            default: {
                result = super.onOptionsItemSelected(item);

                break;
            }
        }

        return result;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mWindowsBackground != 0) {
            getWindow().getDecorView().setBackgroundResource(mWindowsBackground);
        }

        mStateSaved = false;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mStateSaved = true;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();

    }

    public boolean isStateSaved() {
        return mStateSaved;
    }

    @SuppressLint("NewApi")
    @Override
    public boolean isDestroyed() {

        return Build.VERSION.SDK_INT >= Android.JELLY_BEAN_MR1 ? super.isDestroyed() : mIsDestroyed;
    }

    @Override
    protected void onDestroy() {
        mIsDestroyed = true;

        removeCallbacksAndMessages(null);

        if (mWaitting != null) {
            mWaitting.dismiss();

            mWaitting = null;
        }

        WaitUtils.destroy(this);

        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        try {
            super.startActivity(intent);
        } catch (Exception e) {
            Log.d(TAG, e);
        }
    }

    @Override
    public void startActivity(Intent intent, @Nullable Bundle options) {
        try {
            super.startActivity(intent, options);
        } catch (Exception e) {
            Log.d(TAG, e);
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        try {
            super.startActivityForResult(intent, requestCode);
        } catch (Exception e) {
            Log.d(TAG, e);
        }
    }

    @RequiresApi(16)
    @Override
    public void startActivityForResult(Intent intent, int requestCode, @Nullable Bundle options) {

        try {
            super.startActivityForResult(intent, requestCode, options);
        } catch (Exception e) {
            Log.d(TAG, e);
        }
    }


    public void setWindowsBackgroundResource(int resId) {
        mWindowsBackground = resId;
    }

    /**
     * 锁屏后显示界面
     */
    protected void showWhenLocked() {
        final Window window = getWindow();
        if (window != null) {
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                    | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    protected Pulley.Builder pulleyBuilder() {

        return mPulleyBuilder;
    }

    /**
     * 是否开启统计分析
     *
     * @param analyticsEnable
     */
    protected void setAnalyticsEnable(boolean analyticsEnable) {
        mAnalyticsEnable = analyticsEnable;
    }

    public void finish(Fragment... fragments) {
        if (Assert.notEmpty(fragments)) {
            FragmentManager fragmentManager = iSupportFragmentManager();
            if (fragmentManager != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                for (Fragment fragment : fragments) {
                    if (fragment != null) {
                        transaction.remove(fragment);
                    }
                }
                transaction.commitAllowingStateLoss();
                fragmentManager.executePendingTransactions();
            }

        }
    }

    public void finish(boolean isExit) {
        finish();

        if (isExit) {
            new Handler() {

                @Override
                public void handleMessage(android.os.Message msg) {
                    System.exit(0);// 结束app进程
                }

            }.sendEmptyMessageDelayed(0, 500);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {

        return super.dispatchTouchEvent(event);
    }

    protected final boolean postAtTime(Runnable r) {

        return r != null && mContextHandler.postAtFrontOfQueue(r);
    }

    protected final boolean postDelayed(Runnable r, long delayMillis) {

        return r != null && mContextHandler.postDelayed(r, delayMillis);
    }

    protected final boolean post(Runnable r) {

        return r != null && mContextHandler.post(r);
    }

    protected final boolean postAtTime(Runnable r, long uptimeMillis) {

        return r != null && mContextHandler.postAtTime(r, uptimeMillis);
    }

    protected final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {

        return r != null && mContextHandler.postAtTime(r, token, uptimeMillis);
    }

    protected final void removeCallbacks(Runnable r) {
        if (r != null) {
            mContextHandler.removeCallbacks(r);
        }
    }

    protected final void removeCallbacks(Runnable r, Object token) {
        if (r != null) {
            mContextHandler.removeCallbacks(r, token);
        }
    }

    protected final void removeCallbacksAndMessages(Object token) {

        mContextHandler.removeCallbacksAndMessages(token);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {

        return super.dispatchKeyEvent(event);
    }

    /**
     * 不建议使用
     */
    @Deprecated
    @Override
    public final FragmentManager getSupportFragmentManager() {

        return super.getSupportFragmentManager();
    }

    /**
     * 在getSupportFragmentManager()的基础上添加activity isfinish到判断
     *
     * @return
     */
    public final FragmentManager iSupportFragmentManager() {
        FragmentManager fragmentManager = null;
        if (!isFinishing()) {
            fragmentManager = super.getSupportFragmentManager();
        }

        return fragmentManager;
    }

    public void showWaitting(final OnWaitCancelListener waitCancelListener) {
        mContextHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mWaitting == null) {
                    mWaitting = WaitUtils.obtain(FrameActivity.this);
                }

                if (mWaitting.isInActivityLifecycle() && !isWaitting()) {
                    mWaitting.setOnWaitCancelListener(waitCancelListener);
                    mWaitting.show();
                }
            }
        });
    }

    public void showWaitting() {
        showWaitting(null);
    }

    public void hideWaitting() {
        mContextHandler.post(new Runnable() {

            @Override
            public void run() {
                if (mWaitting != null && isWaitting()) {
                    try {
                        mWaitting.hide();
                    } catch (Throwable t) {
                        Log.e(TAG, t);
                    }
                }
            }
        });
    }

    public boolean isWaitting() {

        return mWaitting != null && mWaitting.isShowing();
    }

    public void setDisplayHomeAsUpEnabled(boolean homeAsUpEnabled) {
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(homeAsUpEnabled);
        }
    }

    public void setDisplayShowCustomEnabled(boolean showCustom) {
        if (mActionBar != null) {
            mActionBar.setDisplayShowCustomEnabled(showCustom);
        }
    }

    public void setDisplayShowTitleEnabled(boolean showTitle) {
        if (mActionBar != null) {
            mActionBar.setDisplayShowTitleEnabled(showTitle);
        }
    }

    public void setDisplayShowHomeEnabled(boolean showHome) {
        if (mActionBar != null) {
            mActionBar.setDisplayShowHomeEnabled(showHome);
        }
    }

    public <V> V getActionBarView() {
        V v = null;

        if (mActionBarView == null) {
            mActionBarView = findViewById(mActionBarViewId);
        }

        if (mActionBarView != null) {
            try {
                v = (V) mActionBarView;
            } catch (Throwable t) {
                Log.e(TAG, t);
            }
        }

        return v;
    }

    public Class<? extends View> getActionBarViewClass() {
        Class<? extends View> clazz = null;

        if (AppResource.isAboveHoneycomb()) {
            clazz = ClazzLoader.forName("com.android.internal.widget.ActionBarView");
        } else {
            clazz = ClazzLoader.forName("android.support.v7.internal.widget.ActionBarView");//android.support.v7.internal.widget.ActionBarView.class;
        }

        return clazz;
    }

    public View getHomeView() {
        if (mHomeView == null) {
            mHomeView = ClazzLoader.getFieldValue(getActionBarView(), "mHomeLayout");

            if (mHomeView != null) {
                ClazzLoader.setFieldValue(ViewUtils.getLayoutParams(mHomeView), "gravity", Gravity.CENTER_VERTICAL);

                mHomeView.requestLayout();
            }
        }

        return mHomeView;
    }

    public void setHomeAsUpIndicator(int resId) {
        if (mUpView == null) {
            mUpView = ClazzLoader.getFieldValue(getHomeView(), "mUpView");
        }

        if (mUpView != null) {
            if (mUpView instanceof ImageView) {
                ((ImageView) mUpView).setImageResource(resId);
            } else {
                mUpView.setBackgroundResource(resId);
            }
        }
    }

    public void setHomeAsUpIndicator(Drawable indicator) {
        if (mUpView == null) {
            mUpView = ClazzLoader.getFieldValue(getHomeView(), "mUpView");
        }

        if (mUpView != null) {
            if (mUpView instanceof ImageView) {
                ((ImageView) mUpView).setImageDrawable(indicator);
            } else {
                mUpView.setBackgroundDrawable(indicator);
            }
        }
    }

    @Override
    public void setTitle(int titleId) {
        setTitle(getString(titleId));
    }

    @Override
    public void setTitle(CharSequence title) {
        if (mActionBar != null) {
            mActionBar.setTitle(title);
        }
    }

    public CharSequence getSubTitle() {

        return mSubTitle;
    }

    public void setSubTitle(int titleId) {
        setSubTitle(getString(titleId));
    }

    public void setSubTitle(CharSequence title) {
        mSubTitle = title;

        if (mActionBar != null) {
            mActionBar.setSubtitle(title);
        }
    }

    public void setIcon(int iconId) {
        if (mActionBar != null) {
            mActionBar.setIcon(iconId);
        }
    }

    public void setIcon(Drawable iconDrawable) {
        if (mActionBar != null) {
            mActionBar.setIcon(iconDrawable);
        }
    }

    public void setActionBarCustomView(int viewId) {
        if (mActionBar != null) {
            mActionBar.setCustomView(viewId);
        }
    }

    public void setActionBarCustomView(View view) {
        if (mActionBar != null) {
            mActionBar.setCustomView(view);
        }
    }

    public void setActionBarCustomView(View view, ActionBar.LayoutParams layoutParams) {
        if (mActionBar != null) {
            mActionBar.setCustomView(view, layoutParams);
        }
    }

    public void toActivity(Class<?> clazz, boolean flag) {
        if (clazz != null) {
            Intent intent = new Intent(this, clazz);

            startActivity(intent);
            if (flag) {
                finish();
            }
        }
    }

    public void toActivity(Class<?> clazz, Bundle bundle, boolean flag) {
        if (clazz != null) {
            Intent intent = new Intent(this, clazz);

            if (bundle != null) {
                intent.putExtras(bundle);
            }

            startActivity(intent);
        }
    }
}
