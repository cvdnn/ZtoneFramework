package android.framework.context.lifecycle;

import android.os.Bundle;
import android.support.annotation.NonNull;

public interface ExtendLifeCycle {

	void onPrepareData(@NonNull Bundle extraBundle, boolean fromInstanceState) throws Exception;

	void onPrepareView() throws Exception;

	boolean isDestroyed();
}
