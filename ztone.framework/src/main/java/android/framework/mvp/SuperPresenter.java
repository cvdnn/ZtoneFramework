package android.framework.mvp;

import android.bus.EventBusProxy;
import android.framework.context.lifecycle.ExtendLifeCycle;
import android.framework.module.Validator;
import android.support.annotation.NonNull;

public class SuperPresenter<C extends ExtendLifeCycle> extends EventBusProxy implements Presenter, Validator {

	@NonNull
	protected final C mAttachHost;

	protected SuperPresenter(C c) {
		mAttachHost = c;

		if (check()) {

		}
	}

	@Override
	public boolean check() {

		return mAttachHost != null && !mAttachHost.isDestroyed();
	}

}
