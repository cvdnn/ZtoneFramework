package android.framework.context.lifecycle;

/**
 * Created by handy on 17-1-19.
 */

public interface Emergency {
    /**
     * @see android.content.ComponentCallbacks2#onTrimMemory(int)
     */
    void onTrimMemory(int level);

    /**
     * @see android.content.ComponentCallbacks2#onLowMemory()
     */
    void onLowMemory();
}
