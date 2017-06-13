package android.framework.context.lifecycle;

import android.assist.Assert;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.UiThread;

import java.util.HashSet;
import java.util.Set;

/**
 * A class for managing and starting requests for Glide. Can use activity, fragment and connectivity lifecycle events to
 * intelligently stop, start, and restart requests. Retrieve either by instantiating a new object, or to take advantage
 * built in Activity and Fragment lifecycle handling, use the static Glide.load methods with your Fragment or Activity.
 */
final class ComponentLifeCycleManager extends LifeCycleManager {

    @NonNull
    private final LifeCycleTracker mLifecycleRetriever;

    protected ComponentLifeCycleManager(@NonNull LifeCycleTracker lifecycleRetriever) {
        mLifecycleRetriever = lifecycleRetriever;
    }

    /**
     * @see android.content.ComponentCallbacks2#onTrimMemory(int)
     */
    public void onTrimMemory(int level) {

    }

    /**
     * @see android.content.ComponentCallbacks2#onLowMemory()
     */
    public void onLowMemory() {

    }

    /**
     * Lifecycle callback that registers for connectivity events
     * (if the android.permission.ACCESS_NETWORK_STATE permission is present) and restarts failed or paused requests.
     */
    @Override
    public <C extends Context> void onStart(@NonNull C context) {
        super.onStart(context);
    }

    /**
     * Lifecycle callback that unregisters for connectivity events
     * (if the android.permission.ACCESS_NETWORK_STATE permission is present) and pauses in progress loads.
     */
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     * Lifecycle callback that cancels all in progress requests and clears and recycles resources for all completed requests.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * 在pre-JELLY_BEAN_MR1上会出现ChildFragment的生命周期不随ParentFragment的情况
     */
    @UiThread
    public void onStartLifecycles() {
        HashSet<ComponentLifeCycleManager> descendants = getDescendants();
        if (Assert.notEmpty(descendants)) {
            for (ComponentLifeCycleManager manager : descendants) {
                if (manager != null) {
                    manager.onStart(mContext);
                }
            }
        }
    }

    /**
     * 在pre-JELLY_BEAN_MR1上会出现ChildFragment的生命周期不随ParentFragment的情况
     */
    @UiThread
    public void onStopLifecycles() {
        HashSet<ComponentLifeCycleManager> descendants = getDescendants();
        if (Assert.notEmpty(descendants)) {
            for (ComponentLifeCycleManager manager : descendants) {
                if (manager != null) {
                    manager.onStop();
                }
            }
        }
    }

    /**
     * 在pre-JELLY_BEAN_MR1上会出现ChildFragment的生命周期不随ParentFragment的情况
     */
    @UiThread
    public void onDestroyLifecycles() {
        HashSet<ComponentLifeCycleManager> descendants = getDescendants();
        if (Assert.notEmpty(descendants)) {
            for (ComponentLifeCycleManager manager : descendants) {
                if (manager != null) {
                    manager.onDestroy();
                }
            }
        }
    }

    private HashSet<ComponentLifeCycleManager> getDescendants() {
        Set<LifeCycleTracker> descendantRetrievers = mLifecycleRetriever.getDescendantLifecycleRetrievers();
        HashSet<ComponentLifeCycleManager> descendants = new HashSet<>(descendantRetrievers.size());
        for (LifeCycleTracker retriever : descendantRetrievers) {
            if (retriever != null) {
                ComponentLifeCycleManager manager = retriever.getLifecycleManager();
                if (manager != null) {
                    descendants.add(manager);
                }
            }
        }

        return descendants;
    }
}
