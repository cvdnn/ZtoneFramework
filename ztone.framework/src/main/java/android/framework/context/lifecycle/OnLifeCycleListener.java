package android.framework.context.lifecycle;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * An interface for listener to {@link android.app.Fragment} and {@link android.app.Activity} lifecycle events.
 */
public interface OnLifeCycleListener {

    /**
     * Callback for when {@link android.app.Fragment#onStart()}} or {@link android.app.Activity#onStart()} is called.
     */
    <C extends Context> void onStart(@NonNull C context);

    /**
     * Callback for when {@link android.app.Fragment#onStop()}} or {@link android.app.Activity#onStop()}} is called.
     */
    void onStop();

    /**
     * Callback for when {@link android.app.Fragment#onDestroy()}} or {@link android.app.Activity#onDestroy()} is called.
     */
    void onDestroy();

    class Impl implements OnLifeCycleListener {

        @Override
        public <C extends Context> void onStart(@NonNull C context) {

        }

        @Override
        public void onStop() {

        }

        @Override
        public void onDestroy() {

        }
    }
}
