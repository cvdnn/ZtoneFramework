package android.framework.mvp;

import android.os.Bundle;

public interface ActivityProxy {

	void onCreate(Bundle savedInstanceState);

	void onStart();

	void onRestart();

	void onResume();

	void onPause();

	void onStop();

	void onDestroy();
}
