package android.framework.util;

import android.framework.module.Validator;

/**
 * Created by handy on 17-3-14.
 */

public class ValidateUtils {
    public static boolean check(Validator v) {

        return v != null && v.check();
    }
}
