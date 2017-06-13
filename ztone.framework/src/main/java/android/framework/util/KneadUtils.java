package android.framework.util;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.framework.C;
import android.framework.RuntimeConfigure;
import android.math.MD5;
import android.support.v4.util.ArrayMap;

import java.util.Map;
import java.util.Set;

import static android.assist.TextUtilz.toTrim;

/**
 * Created by handy on 17-3-14.
 */

public class KneadUtils {

    public static String fixed(Map<String, String> metaMap) {
        StringBuilder tmpBuilder = new StringBuilder();

        if (Assert.notEmpty(metaMap)) {
            String timestamp = TextUtilz.nullTo(TextUtilz.toString(metaMap.get(C.tag.t)));

            if (Assert.notEmpty(metaMap)) {
                Set<Map.Entry<String, String>> entrySet = metaMap.entrySet();
                for (Map.Entry<String, String> meta : entrySet) {
                    tmpBuilder.append(TextUtilz.nullTo(TextUtilz.toString(meta.getKey())));
                }
            }

            tmpBuilder.append(MD5.encrypt(timestamp));
        }

        // Log.d(TAG, "## " + tmpBuilder.toString());

        return toTrim(tmpBuilder.toString());
    }

    public static String sort(Map<String, String> metaMap) {
        String sign = "";

        if (Assert.notEmpty(metaMap)) {
            ArrayMap<String, String> signMap = null;
            if (metaMap instanceof ArrayMap) {
                signMap = (ArrayMap<String, String>) metaMap;

            } else {
                signMap = new ArrayMap<>(metaMap.size());
                signMap.putAll(metaMap);
            }

            StringBuilder query = new StringBuilder(RuntimeConfigure.obtain().appSecret());

            // 把字典按Key的字母顺序排序, 把所有参数名和参数值串在一起
            final Set<Map.Entry<String, String>> paramSet = signMap.entrySet();
            for (Map.Entry<String, String> param : paramSet) {
                String key = param.getKey(), value = param.getValue();
                if (Assert.notEmpty(key) && Assert.notEmpty(value)) {
                    query.append(key).append(value);
                }
            }

            sign = MD5.encrypt(query.toString());
        }

        return sign;
    }
}
