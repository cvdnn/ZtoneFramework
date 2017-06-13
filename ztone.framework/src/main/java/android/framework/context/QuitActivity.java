package android.framework.context;

import android.extend.view.module.ToastConsole;
import android.framework.R;
import android.os.SystemClock;
import android.support.v4.app.FragmentManager;
import android.view.KeyEvent;

public class QuitActivity extends FrameActivity {
    private long mLastExitTime;

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isStateSaved()) {
                final FragmentManager fragmentManager = iSupportFragmentManager();
                if (fragmentManager == null || !fragmentManager.popBackStackImmediate()) {
                    long now = SystemClock.uptimeMillis();
                    if (mLastExitTime > 0 && now - mLastExitTime < 1500) {
                        onBackPressed();

                        mLastExitTime = 0;
                    } else {
                        ToastConsole.show(R.string.toast_exit);
                        mLastExitTime = now;
                    }

                    return true;
                }
            }
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void finish() {
        super.finish();

        mLastExitTime = 0l;
    }
}
