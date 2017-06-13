package android.framework;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.collection.IProperties;
import android.io.FileUtils;
import android.io.StreamUtils;
import android.log.Log;

import java.io.File;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Properties;
import java.util.Set;

import static android.framework.C.value.charset_encoding;

abstract class Configure {
    private static final String TAG = "Configure";

    protected boolean mIsFakeMix;

    private IProperties mProperties;

    public <O extends Configure> O load(InputStream inputStream, boolean isFakeMix) {
        mIsFakeMix = isFakeMix;

        if (inputStream != null) {
            IProperties.Builder iPropertiesBuilder = new IProperties.Builder();

            if (mIsFakeMix) {
                String fakeMix = TextUtilz.fromFake(StreamUtils.getContent(inputStream));

                if (Assert.notEmpty(fakeMix)) {
                    iPropertiesBuilder.from(new StringReader(fakeMix));
                }
            } else {
                iPropertiesBuilder.from(inputStream);
            }

            mProperties = iPropertiesBuilder.build();

            try {
                onLoadProperties();
            } catch (Exception e) {
                Log.e(TAG, e);
            }
        }

        return (O) this;
    }

    protected abstract void onLoadProperties() throws Exception;

    public final String get(String key) {

        return get(key, "");
    }

    public final String get(String key, String defaultValue) {
        String value = defaultValue;

        if (mProperties != null && mProperties.containsKey(key)) {
            String temp = mProperties.get(key);
            if (Assert.notEmpty(temp)) {
                value = temp;
            }
        }

        return value;
    }

    public final Configure set(String key, String value) {
        if (mProperties != null) {
            mProperties.put(key, value);
        }

        return this;
    }

    public final Configure remove(String key) {
        if (mProperties != null) {
            mProperties.remove(key);
        }

        return this;
    }

    public final IProperties iproperties() {

        return mProperties;
    }

    public final Properties toProperties() {
        Properties properties = new Properties();
        if (mProperties != null && mProperties.size() > 0) {
            Set<String> nameSet = mProperties.stringPropertyNames();
            for (String key : nameSet) {
                if (Assert.notEmpty(key)) {
                    properties.put(key, mProperties.getProperty(key, ""));
                }
            }
        }

        return properties;
    }

    public void write(String filePath) {
        if (Assert.notEmpty(filePath) && mProperties != null) {
            if (mIsFakeMix) {
                FileUtils.write(new File(filePath), TextUtilz.toFake(mProperties.toString()), charset_encoding);

            } else {
                try {
                    mProperties.write(filePath, C.value.encoding);
                } catch (Exception e) {
                    Log.i(TAG, e);
                }
            }
        }
    }
}
