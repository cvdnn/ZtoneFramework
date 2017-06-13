package android.framework.util;

import android.log.Log;

/**
 * Created by handy on 17-3-27.
 */

public class Debug {
    public static final String TAG = "Debug";
    private long mStarTime;

    private Debug() {
        mStarTime = System.currentTimeMillis();
    }

    public void record(String tag) {
        long time = System.currentTimeMillis() - mStarTime;

        Log.i(TAG, "DD: [%s]: %dms", tag, time);

        mStarTime = System.currentTimeMillis();
    }

    public static Debug start() {

        return new Debug();
    }
}
