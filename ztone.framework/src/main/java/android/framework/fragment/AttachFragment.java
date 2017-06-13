package android.framework.fragment;

import android.app.Activity;
import android.framework.context.FrameActivity;
import android.os.Bundle;
import android.support.annotation.Keep;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

@Keep
public class AttachFragment extends FrameFragment {
    private static final String TAG = "AttachFragment";

    /**
     * Fragment附着命令
     *
     * @author handy
     */
    public enum Attach {
        NONE,
        /**
         * 添加
         */
        ADD,
        /**
         * 替换原Fragment
         */
        REPLACE
    }

    /**
     * 附着的GroupView id
     */
    protected int mContainerViewId;
    /**
     * 是否附着到mContainerViewId的ViewGroup上， 该参数表示有可能多个子Fragment也附着到当前Fragment所在的ViewGroup上
     */
    protected boolean mIsAttachToContainerView;

    /**
     * 当前Fragment的创建者或者启动者
     */
    protected Fragment mCreatedByFragment;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // 在当前Fragment创建前，模拟父Fragment.onPause();
        if (mIsAttachToContainerView && mCreatedByFragment != null && !mCreatedByFragment.isRemoving()) {
            mCreatedByFragment.onPause();
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        // 在当前视图销毁后，模拟父Fragment.onResume();
        if (mIsAttachToContainerView && mCreatedByFragment != null && !mCreatedByFragment.isDetached()) {
            mCreatedByFragment.onResume();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mCreatedByFragment = null;
        mIsAttachToContainerView = false;
    }

    /**
     * 设置当前Fragment的创建者或者启动者，主要用于在销毁的时候级联销毁
     */
    public final void setCreatedByFragment(Fragment createByFragment) {

        mCreatedByFragment = createByFragment;
    }

    /**
     * 获取当前Fragment的创建者或者启动者
     */
    public final Fragment getCreateByFragment() {

        return mCreatedByFragment;
    }

    public final void setContainerViewId(int containerViewId) {

        mContainerViewId = containerViewId;
    }

    /**
     * 获取Fragment附着的View id
     */
    public final int getContainerViewId() {

        return mContainerViewId;
    }

    /**
     * 是否附着到mContainerViewId的ViewGroup上，
     * 该参数表示有可能多个子Fragment也附着到当前Fragment所在的ViewGroup上
     */
    public final boolean isAttachToContainerView() {

        return mIsAttachToContainerView;
    }

    /**
     * 设置附着标记
     */
    public final void setIsAttachToContainerView() {

        mIsAttachToContainerView = true;
    }

    /**
     * 绑定Fragment到Activity
     */
    public boolean add(FrameActivity activity, int containerViewId, String tag, boolean isBackStack) {
        boolean result = false;
        if (activity != null) {
            result = attach(activity.iSupportFragmentManager(), containerViewId, tag, isBackStack, Attach.ADD);
        }

        return result;
    }

    /**
     * 绑定Fragment到Activity
     */
    public boolean add(FrameFragment parent, int containerViewId, String tag, boolean isBackStack) {
        boolean result = false;
        if (parent != null) {
            result = attach(parent.iFragmentManager(), containerViewId, tag, isBackStack, Attach.ADD);
        }

        return result;
    }

    /**
     * 绑定Child Fragment
     */
    public boolean addChild(FrameFragment parent, int containerViewId, String tag, boolean isBackStack) {
        boolean result = false;
        if (parent != null) {
            result = attach(parent.iChildFragmentManager(), containerViewId, tag, isBackStack, Attach.ADD);
        }

        return result;
    }

    /**
     * 绑定Fragment到Activity
     */
    public boolean replace(FrameActivity activity, int containerViewId, String tag, boolean isBackStack) {
        boolean result = false;
        if (activity != null) {
            result = attach(activity.iSupportFragmentManager(), containerViewId, tag, isBackStack, Attach.REPLACE);
        }

        return result;
    }

    /**
     * 绑定Fragment到Activity
     */
    public boolean replace(FrameFragment parent, int containerViewId, String tag, boolean isBackStack) {
        boolean result = false;
        if (parent != null) {
            result = attach(parent.iFragmentManager(), containerViewId, tag, isBackStack, Attach.REPLACE);
        }

        return result;
    }

    /**
     * 绑定Child Fragment
     */
    public boolean replaceChild(FrameFragment parent, int containerViewId, String tag, boolean isBackStack) {
        boolean result = false;
        if (parent != null) {
            result = attach(parent.iChildFragmentManager(), containerViewId, tag, isBackStack, Attach.REPLACE);
        }

        return result;
    }

    /**
     * 绑定Fragment
     */
    public boolean attach(FragmentManager fragmentManager, int containerViewId, String tag, boolean isBackStack, Attach attach) {
        boolean result = false;
        if (fragmentManager != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            result = attach(fragmentTransaction, containerViewId, tag, isBackStack, attach);

            fragmentTransaction.commitAllowingStateLoss();
        }

        return result;
    }

    /**
     * 绑定Fragment
     *
     * @param fragmentTransaction 事务需要调用方关闭
     */
    public boolean attach(FragmentTransaction fragmentTransaction, int containerViewId, String tag, boolean isBackStack, Attach attach) {
        boolean result = false;
        if (fragmentTransaction != null && attach != null) {
            if (attach == Attach.ADD) {
                fragmentTransaction.add(containerViewId, this, tag);
            } else if (attach == Attach.REPLACE) {
                fragmentTransaction.replace(containerViewId, this, tag);
            }

            if (isBackStack) {
                fragmentTransaction.addToBackStack(tag);
            }

            mContainerViewId = containerViewId;

            result = true;
        }

        return result;
    }
}
