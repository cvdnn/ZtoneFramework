package android.framework.context.lifecycle;

import android.content.Context;
import android.framework.Android;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * A view-less {@link Fragment} used to safely store an
 * {@link ComponentLifeCycleManager} that can be used to start, stop and manage Glide requests started for
 * targets within the fragment or activity this fragment is a child of.
 */
public class LifeCycleTracker extends Fragment {
    private final ComponentLifeCycleManager mLifeCycleManager = new ComponentLifeCycleManager(this);
    private final HashSet<LifeCycleTracker> mChildLifecycleFragments = new HashSet<LifeCycleTracker>();
    private LifeCycleTracker mRootLifecycleFragment;

    /**
     * Returns the current {@link ComponentLifeCycleManager} or null if none is set.
     */
    public ComponentLifeCycleManager getLifecycleManager() {
        return mLifeCycleManager;
    }

    private void addChildRetriever(LifeCycleTracker child) {
        mChildLifecycleFragments.add(child);
    }

    private void removeChildRetriever(LifeCycleTracker child) {
        mChildLifecycleFragments.remove(child);
    }

    /**
     * Returns the set of fragments that this RequestManagerFragment's parent is a parent to. (i.e. our parent is
     * the fragment that we are annotating).
     */
    @NonNull
    protected Set<LifeCycleTracker> getDescendantLifecycleRetrievers() {
        if (mRootLifecycleFragment == null) {
            return Collections.emptySet();

        } else if (mRootLifecycleFragment == this) {
            return Collections.unmodifiableSet(mChildLifecycleFragments);

        } else {
            HashSet<LifeCycleTracker> descendants = new HashSet<LifeCycleTracker>();
            for (LifeCycleTracker fragment : mRootLifecycleFragment.getDescendantLifecycleRetrievers()) {
                if (isDescendant(fragment.getParentFragment())) {
                    descendants.add(fragment);
                }
            }
            return Collections.unmodifiableSet(descendants);
        }
    }

    /**
     * Returns true if the fragment is a descendant of our parent.
     */
    private boolean isDescendant(Fragment fragment) {
        Fragment root = this.getParentFragment();
        while (fragment.getParentFragment() != null) {
            if (fragment.getParentFragment() == root) {
                return true;
            }
            fragment = fragment.getParentFragment();
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // FIXME 有待验证：在pre-JELLY_BEAN_MR1上会出现ChildFragment的生命周期不随ParentFragment的情况
        if (Build.VERSION.SDK_INT == Android.JELLY_BEAN_MR1) {
            mRootLifecycleFragment = LifeCycleUtils.findLifecycleRetriever(getActivity().getSupportFragmentManager());
            if (mRootLifecycleFragment != this) {
                mRootLifecycleFragment.addChildRetriever(this);
            }
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();

        if (Build.VERSION.SDK_INT == Android.JELLY_BEAN_MR1 && mRootLifecycleFragment != null) {
            if (mRootLifecycleFragment != null) {
                mRootLifecycleFragment.removeChildRetriever(this);
                mRootLifecycleFragment = null;
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        mLifeCycleManager.onStart(getActivity());
    }

    @Override
    public void onStop() {
        super.onStop();

        mLifeCycleManager.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mLifeCycleManager.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

        mLifeCycleManager.onLowMemory();
    }
}
