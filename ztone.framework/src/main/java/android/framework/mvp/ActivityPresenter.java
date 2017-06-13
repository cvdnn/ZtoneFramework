package android.framework.mvp;

import android.framework.context.FrameActivity;

public abstract class ActivityPresenter extends SuperPresenter<FrameActivity> {

    protected ActivityPresenter(FrameActivity activity) {
        super(activity);
    }

}
