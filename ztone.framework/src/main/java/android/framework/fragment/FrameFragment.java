package android.framework.fragment;

import android.app.Activity;
import android.assist.Assert;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.framework.context.FrameActivity;
import android.framework.context.lifecycle.ExtendLifeCycle;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.pull.Pulley;
import android.log.Log;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;

import java.io.Serializable;
import java.lang.reflect.Field;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class FrameFragment extends Fragment implements ExtendLifeCycle, KeyEvent.Callback,
        GestureDetector.OnGestureListener, GestureDetector.OnDoubleTapListener {
    private static final String TAG = "FrameFragment";

    private boolean mIsFinishing, mIsDestroyed;

    private final Handler mContextHandler = new Handler();

    /**
     * 该Fragment归属的Activity
     */
    @Nullable
    protected FragmentActivity mAttachedActivity;

    protected final Pulley.Builder mPulleyBuilder = new Pulley.Builder();

    protected Unbinder mButterUnbinder;

    protected GestureDetectorCompat mGestureDetectorCompat;
    private boolean mIsLongpressEnabled, mDoubleTapEnabled;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mAttachedActivity = getActivity();

        mGestureDetectorCompat = new GestureDetectorCompat(mAttachedActivity, this);
        mGestureDetectorCompat.setIsLongpressEnabled(mIsLongpressEnabled);

        mIsFinishing = false;
        mIsDestroyed = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            if (Assert.notEmpty(savedInstanceState)) {
                onPrepareData(savedInstanceState, true);

            } else {
                Bundle arguments = getArguments();

                onPrepareData(Assert.notEmpty(arguments) ? arguments : new Bundle(), false);
            }

        } catch (Exception e) {
            Log.e(TAG, e);
        }
    }

    /**
     * 获取数据，在onCreate()中被调用。
     *
     * @param extraBundle
     * @param fromInstanceState
     * @throws Exception
     */
    @Override
    public void onPrepareData(@NonNull Bundle extraBundle, boolean fromInstanceState) throws Exception {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        View view = getView();
        if (view != null) {
            mButterUnbinder = ButterKnife.bind(this, view);
        }

        try {
            onPrepareView();
        } catch (Exception e) {
            Log.e(TAG, e);
        }

        dispatchTouchEvent();
    }

    /**
     * 初始化视图，在setContentView()之后被调用。
     */
    @Override
    public void onPrepareView() throws Exception {

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    public boolean dispatchKeyEvent(KeyEvent event) {
        View fragmentView = getView();

        return event.dispatch(this, fragmentView != null ? fragmentView.getKeyDispatcherState() : null, this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        return false;
    }

    @Override
    public boolean onKeyMultiple(int keyCode, int count, KeyEvent event) {

        return false;
    }

    /**
     * Set whether longpress is enabled, if this is enabled when a user
     * presses and holds down you get a longpress event and nothing further.
     * If it's disabled the user can press and hold down and then later
     * moved their finger and you will get scroll events. By default
     * longpress is enabled.
     *
     * @param enabled whether longpress should be enabled.
     */
    protected void setIsLongpressEnabled(boolean enabled) {
        mIsLongpressEnabled = enabled;

        if (mGestureDetectorCompat != null) {
            mGestureDetectorCompat.setIsLongpressEnabled(enabled);
        }
    }

    protected boolean isLongpressEnabled() {

        return mIsLongpressEnabled;
    }

    protected void setDoubleTapEnabled(boolean enabled) {
        mDoubleTapEnabled = enabled;

        if (mGestureDetectorCompat != null) {
            mGestureDetectorCompat.setOnDoubleTapListener(enabled ? this : null);
        }
    }

    protected boolean isDoubleTapEnabled() {

        return mDoubleTapEnabled;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTap(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onDoubleTapEvent(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onSingleTapConfirmed(MotionEvent e) {
        return false;
    }


    private void dispatchTouchEvent() {
        View shellView = getView();
        if (shellView != null) {
            shellView.setOnTouchListener(new OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (mGestureDetectorCompat != null) {
                        mGestureDetectorCompat.onTouchEvent(event);
                    }

                    // 用于防止点击泄漏
                    return true;
                }
            });
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

    public FragmentActivity getAttachedActivity() {

        return mAttachedActivity;
    }

    /**
     * 在getFragmentManager()的基础上添加isActivityFinished的判断
     *
     * @return
     */
    public final FragmentManager iFragmentManager() {

        return isActivityFinishing() ? null : getFragmentManager();
    }

    /**
     * 在getFragmentManager()的基础上添加isActivityFinished的判断
     *
     * @return
     */
    public final FragmentManager iChildFragmentManager() {

        return isActivityFinishing() ? null : getChildFragmentManager();
    }

    @Override
    public void onDetach() {
        super.onDetach();

        try {
            Field childFragmentManager = Fragment.class.getDeclaredField("mChildFragmentManager");
            if (childFragmentManager != null) {
                childFragmentManager.setAccessible(true);
                childFragmentManager.set(this, null);
            }
        } catch (Exception e) {
            Log.v(TAG, e);
        }

        if (mButterUnbinder != null) {
            mButterUnbinder.unbind();
        }
    }

    @Override
    public void onDestroy() {
        mIsDestroyed = true;

        mContextHandler.removeCallbacksAndMessages(null);

        hideWaitting();

        super.onDestroy();
    }

    @Override
    public void startActivity(Intent intent) {
        if (isActive()) {
            try {
                super.startActivity(intent);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        if (isActive()) {
            try {
                super.startActivityForResult(intent, requestCode);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode, Bundle options) {
        if (isActive()) {
            try {
                super.startActivityForResult(intent, requestCode, options);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }
    }

    protected boolean isActive() {

        return mAttachedActivity != null && isAdded() && !isFinishing() && !isActivityDestroyed();
    }

    /**
     * 设置横竖屏
     *
     * @param requestedOrientation
     */
    protected void setRequestedOrientation(int requestedOrientation) {
        if (!isFinishing()) {
            mAttachedActivity.setRequestedOrientation(requestedOrientation);
        }
    }

    protected boolean isActivityFinishing() {

        return LifeCycleUtils.isFinishing(getActivity());
    }

    protected boolean isActivityDestroyed() {
        FrameActivity activity = getActivity(FrameActivity.class);

        return activity != null && activity.isDestroyed();
    }

    @Override
    public boolean isDestroyed() {

        return mIsDestroyed;
    }

    public boolean isFinishing() {

        return mIsFinishing;
    }

    public void finishActivity() {
        if (mAttachedActivity != null) {
            mAttachedActivity.finish();
        }
    }

    /**
     * 关闭franment方法
     */
    public void finish() {
        FragmentManager fragmentManager = getParentFragment() == null ? iFragmentManager() : iChildFragmentManager();
        if (fragmentManager != null) {
            fragmentManager.beginTransaction().remove(this).commitAllowingStateLoss();
        }

        mIsFinishing = true;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {

        return onTouchEvent(event);
    }

    public boolean onTouchEvent(MotionEvent event) {

        return false;
    }

    protected <S> S getSystemService(@NonNull String name) {
        S s = null;

        if (mAttachedActivity != null && Assert.notEmpty(name)) {
            s = (S) mAttachedActivity.getSystemService(name);
        }

        return s;
    }

    protected Intent registerReceiver(@Nullable BroadcastReceiver receiver, @NonNull String action) {

        return receiver != null && Assert.notEmpty(action) ? registerReceiver(receiver, new IntentFilter(action)) : null;
    }

    protected Intent registerReceiver(@Nullable BroadcastReceiver receiver, IntentFilter filter) {
        Intent intent = null;

        if (mAttachedActivity != null && receiver != null && filter != null) {
            try {
                intent = mAttachedActivity.registerReceiver(receiver, filter);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }

        return intent;
    }

    protected void unregisterReceiver(@Nullable BroadcastReceiver receiver) {
        if (mAttachedActivity != null && receiver != null) {
            try {
                mAttachedActivity.unregisterReceiver(receiver);
            } catch (Exception e) {
                Log.d(TAG, e);
            }
        }
    }

    public final <A extends Activity> A getActivity(Class<A> clazz) {
        A a = null;

        Activity activity = getActivity();
        if (Assert.isInstanceOf(clazz, activity)) {
            a = (A) activity;
        }

        return a;
    }

    public final ActionBar getActionBar() {
        ActionBar actionBar = null;

        ActionBarActivity abActivity = getActivity(ActionBarActivity.class);
        if (abActivity != null) {
            actionBar = abActivity.getSupportActionBar();
        }

        return actionBar;
    }

    public final View findViewById(int resId) {
        View view = null;

        View fragmentView = getView();
        if (fragmentView != null) {
            view = fragmentView.findViewById(resId);
        }

        return view;
    }

    public final <F extends Fragment> F findFragmentById(int id) {
        F f = null;

        Fragment fragment = null;

        FragmentManager fragmentManager = iFragmentManager();
        if (fragmentManager != null) {
            fragment = fragmentManager.findFragmentById(id);
            if (fragment != null) {
                try {
                    f = (F) fragment;
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        }

        return f;
    }

    public final <F extends Fragment> F findFragmentByTag(String tag) {
        F f = null;

        FragmentManager fragmentManager = iFragmentManager();
        if (fragmentManager != null) {
            Fragment fragment = fragmentManager.findFragmentByTag(tag);
            if (fragment != null) {
                try {
                    f = (F) fragment;
                } catch (Exception e) {
                    Log.e(TAG, e);
                }
            }
        }

        return f;
    }

    protected Pulley.Builder pulleyBuilder() {

        return mPulleyBuilder;
    }

    protected boolean postAtTime(Runnable r) {

        return r != null && mContextHandler.postAtFrontOfQueue(r);
    }

    protected boolean postDelayed(Runnable r, long delayMillis) {

        return r != null && mContextHandler.postDelayed(r, delayMillis);
    }

    protected boolean post(Runnable r) {

        return r != null && mContextHandler.post(r);
    }

    protected boolean postAtTime(Runnable r, long uptimeMillis) {

        return r != null && mContextHandler.postAtTime(r, uptimeMillis);
    }

    protected boolean postAtTime(Runnable r, Object token, long uptimeMillis) {

        return r != null && mContextHandler.postAtTime(r, token, uptimeMillis);
    }

    protected void removeCallbacks(Runnable r) {
        if (r != null) {
            mContextHandler.removeCallbacks(r);
        }
    }

    protected void removeCallbacks(Runnable r, Object token) {
        if (r != null) {
            mContextHandler.removeCallbacks(r, token);
        }
    }

    protected void removeCallbacksAndMessages(Object token) {

        mContextHandler.removeCallbacksAndMessages(token);
    }

    public void showWaitting() {
        FrameActivity activity = getActivity(FrameActivity.class);
        if (activity != null) {
            activity.showWaitting();
        }
    }

    public void hideWaitting() {
        FrameActivity activity = getActivity(FrameActivity.class);
        if (activity != null) {
            activity.hideWaitting();
        }
    }

    public boolean isWaitting() {
        boolean result = false;
        FrameActivity activity = getActivity(FrameActivity.class);
        if (activity != null) {
            result = activity.isWaitting();
        }

        return result;
    }

    protected final boolean getArgumentValue(String key, boolean defaultValue) {

        return getArgumentValue(getArguments(), key, defaultValue);
    }

    protected final boolean getArgumentValue(Bundle arguments, String key, boolean defaultValue) {
        boolean value = defaultValue;

        if (Assert.notEmpty(arguments)) {
            value = arguments.getBoolean(key, defaultValue);
        }

        return value;
    }

    protected final boolean[] getArgumentBooleanArray(String key) {

        return getArgumentBooleanArray(getArguments(), key);
    }

    protected final boolean[] getArgumentBooleanArray(Bundle arguments, String key) {
        boolean[] value = null;

        if (Assert.notEmpty(arguments)) {
            value = arguments.getBooleanArray(key);
        }

        return value;
    }

    protected final int getArgumentValue(String key, int defaultValue) {

        return getArgumentValue(getArguments(), key, defaultValue);
    }

    protected final int getArgumentValue(Bundle arguments, String key, int defaultValue) {
        int value = defaultValue;

        if (Assert.notEmpty(arguments)) {
            value = arguments.getInt(key, defaultValue);
        }

        return value;
    }

    protected final int[] getArgumentIntArray(String key) {
        int[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getIntArray(key);
        }

        return value;
    }

    protected final float getArgumentValue(String key, float defaultValue) {
        float value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getFloat(key, defaultValue);
        }

        return value;
    }

    protected final float[] getArgumentFloatArray(String key) {
        float[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getFloatArray(key);
        }

        return value;
    }

    protected final long getArgumentValue(String key, long defaultValue) {
        long value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getLong(key, defaultValue);
        }

        return value;
    }

    protected final long[] getArgumentLongArray(String key) {
        long[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getLongArray(key);
        }

        return value;
    }

    protected final double getArgumentValue(String key, double defaultValue) {
        double value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getDouble(key, defaultValue);
        }

        return value;
    }

    protected final double[] getArgumentDoubleArray(String key) {
        double[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getDoubleArray(key);
        }

        return value;
    }

    protected final byte getArgumentValue(String key, byte defaultValue) {
        byte value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getByte(key, defaultValue);
        }

        return value;
    }

    protected final byte[] getArgumentByteArray(String key) {
        byte[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getByteArray(key);
        }

        return value;
    }

    protected final char getArgumentValue(String key, char defaultValue) {
        char value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getChar(key, defaultValue);
        }

        return value;
    }

    protected final char[] getArgumentCharArray(String key) {
        char[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getCharArray(key);
        }

        return value;
    }

    protected final CharSequence getArgumentValue(String key, CharSequence defaultValue) {
        CharSequence value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getCharSequence(key);
        }

        return value;
    }

    protected final String getArgumentValueForString(String key, String defaultValue) {
        String value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getString(key);
        }

        return value;
    }

    protected final String[] getArgumentStringArray(String key) {
        String[] value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getStringArray(key);
        }

        return value;
    }

    protected final short getArgumentValue(String key, short defaultValue) {
        short value = defaultValue;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getShort(key, defaultValue);
        }

        return value;
    }

    protected final <T extends Parcelable> T getArgumentValueForParcelable(String key) {
        T value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getParcelable(key);
        }

        return value;
    }

    protected final Serializable getArgumentValueForSerializable(String key) {
        Serializable value = null;
        Bundle arguments = getArguments();
        if (Assert.notEmpty(arguments)) {
            value = arguments.getSerializable(key);
        }

        return value;
    }

    @Override
    public String toString() {

        return getClass().getName();
    }
}
