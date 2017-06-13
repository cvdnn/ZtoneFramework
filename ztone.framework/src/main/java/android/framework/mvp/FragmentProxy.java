package android.framework.mvp;

import android.os.Bundle;

public interface FragmentProxy {

	void onCreate(Bundle savedInstanceState);

	void onStart();

	void onRestart();

	void onResume();

	void onPause();

	void onStop();

	void onDestroy();
}
