package android.framework.module;

import android.assist.Assert;
import android.content.Context;
import android.content.SharedPreferences;
import android.framework.C;
import android.framework.context.lifecycle.LifeCycleUtils;

public class Setting {
    private static final String TAG = "Setting";

    private final SharedPreferences mPreferences;
    private SharedPreferences.Editor mEditor;


    public Setting(String fileName) {
        this(LifeCycleUtils.component().app().getSharedPreferences( //
                Assert.notEmpty(fileName) ? fileName : C.file.shared_prefs_system_config, Context.MODE_PRIVATE));
    }

    public Setting(SharedPreferences prefs) {
        mPreferences = prefs;
    }

    public boolean getBoolean(String tag) {

        return get(tag, false);
    }

    public boolean get(String tag, boolean defValue) {

        return Assert.notEmpty(tag) && mPreferences != null ? mPreferences.getBoolean(tag, defValue) : defValue;
    }

    public Setting put(String tag, boolean value) {
        if (Assert.notEmpty(tag)) {
            if (mEditor != null) {
                mEditor.putBoolean(tag, value);

            } else if (mPreferences != null) {
                mPreferences.edit().putBoolean(tag, value).apply();
            }
        }

        return this;
    }

    public int getInt(String tag) {

        return get(tag, 0);
    }

    public int get(String tag, int defValue) {

        return Assert.notEmpty(tag) && mPreferences != null ? mPreferences.getInt(tag, defValue) : defValue;
    }

    public Setting put(String tag, int value) {
        if (Assert.notEmpty(tag)) {
            if (mEditor != null) {
                mEditor.putInt(tag, value);

            } else if (mPreferences != null) {
                mPreferences.edit().putInt(tag, value).apply();
            }
        }

        return this;
    }

    public long getLong(String tag) {

        return get(tag, 0l);
    }

    public long get(String tag, long defValue) {

        return Assert.notEmpty(tag) && mPreferences != null ? mPreferences.getLong(tag, defValue) : defValue;
    }

    public Setting put(String tag, long value) {
        if (Assert.notEmpty(tag)) {
            if (mEditor != null) {
                mEditor.putLong(tag, value);

            } else if (mPreferences != null) {
                mPreferences.edit().putLong(tag, value).apply();
            }
        }

        return this;
    }

    public float getFloat(String tag) {

        return get(tag, 0.0f);
    }

    public float get(String tag, float defValue) {

        return Assert.notEmpty(tag) && mPreferences != null ? mPreferences.getFloat(tag, defValue) : defValue;
    }

    public Setting put(String tag, float value) {
        if (Assert.notEmpty(tag)) {
            if (mEditor != null) {
                mEditor.putFloat(tag, value);

            } else if (mPreferences != null) {
                mPreferences.edit().putFloat(tag, value).apply();
            }
        }

        return this;
    }

    public String getString(String tag) {

        return get(tag, "");
    }

    public String get(String tag, String defValue) {

        return Assert.notEmpty(tag) && mPreferences != null ? mPreferences.getString(tag, defValue) : defValue;
    }

    public Setting put(String tag, String value) {
        if (Assert.notEmpty(tag)) {
            if (mEditor != null) {
                mEditor.putString(tag, value);

            } else if (mPreferences != null) {
                mPreferences.edit().putString(tag, value).apply();
            }
        }

        return this;
    }

    public Setting edit() {
        if (mPreferences != null) {
            mEditor = mPreferences.edit();
        }

        return this;
    }

    public Setting remove(String tag) {
        if (Assert.notEmpty(tag)) {
            if (mEditor != null) {
                mEditor.remove(tag);

            } else if (mPreferences != null) {
                mPreferences.edit().remove(tag).apply();
            }
        }

        return this;
    }

    public Setting clear() {
        if (mEditor != null) {
            mEditor.clear();

            mEditor = null;
        } else {
            mPreferences.edit().clear().apply();
        }

        return this;
    }

    public Setting apply() {
        if (mEditor != null) {
            mEditor.apply();

            mEditor = null;
        }

        return this;
    }

    public Setting commit() {
        if (mEditor != null) {
            mEditor.commit();

            mEditor = null;
        }

        return this;
    }
}
