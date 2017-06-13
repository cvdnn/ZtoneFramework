package android.framework.context.lifecycle;

import android.assist.Assert;
import android.content.Context;
import android.support.annotation.NonNull;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/**
 * A class for managing and starting requests for Glide. Can use activity, fragment and connectivity lifecycle events to
 * intelligently stop, start, and restart requests. Retrieve either by instantiating a new object, or to take advantage
 * built in Activity and Fragment lifecycle handling, use the static Glide.load methods with your Fragment or Activity.
 */
public abstract class LifeCycleManager implements OnLifeCycleListener, Emergency {
    protected final Set<OnLifeCycleListener> mLifecycleListenerArrays = Collections.newSetFromMap(new WeakHashMap<OnLifeCycleListener, Boolean>());

    protected Context mContext;
    private LifeCycleState mLifecycleState = LifeCycleState.NONE;

    /**
     * Lifecycle callback that registers for connectivity events
     * (if the android.permission.ACCESS_NETWORK_STATE permission is present) and restarts failed or paused requests.
     */
    @Override
    public <C extends Context> void onStart(@NonNull C context) {
        mLifecycleState = LifeCycleState.STARTED;

        mContext = context;

        Set<OnLifeCycleListener> listenerSet = Collections.unmodifiableSet(mLifecycleListenerArrays);
        if (Assert.notEmpty(listenerSet)) {
            for (OnLifeCycleListener listener : listenerSet) {
                listener.onStart(context);
            }
        }
    }

    /**
     * Lifecycle callback that unregisters for connectivity events
     * (if the android.permission.ACCESS_NETWORK_STATE permission is present) and pauses in progress loads.
     */
    @Override
    public void onStop() {
        mLifecycleState = LifeCycleState.PAUSED;

        Set<OnLifeCycleListener> listenerSet = Collections.unmodifiableSet(mLifecycleListenerArrays);
        if (Assert.notEmpty(listenerSet)) {
            for (OnLifeCycleListener listener : listenerSet) {
                listener.onStop();
            }
        }
    }

    /**
     * Lifecycle callback that cancels all in progress requests and clears and recycles resources for all completed requests.
     */
    @Override
    public void onDestroy() {
        mLifecycleState = LifeCycleState.DESTROYED;

        Set<OnLifeCycleListener> listenerSet = Collections.unmodifiableSet(mLifecycleListenerArrays);
        if (Assert.notEmpty(listenerSet)) {
            for (OnLifeCycleListener listener : listenerSet) {
                listener.onDestroy();
            }
        }

        mLifecycleListenerArrays.clear();
    }

    /**
     * Returns true if loads for this {@link LifeCycleManager} are currently paused.
     */
    public boolean isPaused() {

        return mLifecycleState == LifeCycleState.PAUSED || mLifecycleState == LifeCycleState.DESTROYED;
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

    protected Context context() {

        return mContext;
    }

    public LifeCycleManager register(OnLifeCycleListener listener) {
        mLifecycleListenerArrays.add(listener);

        if (mLifecycleState == LifeCycleState.DESTROYED) {
            listener.onDestroy();

        } else if (mLifecycleState == LifeCycleState.STARTED) {
            listener.onStart(mContext);

        } else if (mLifecycleState == LifeCycleState.PAUSED) {
            listener.onStop();
        }

        return this;
    }
}