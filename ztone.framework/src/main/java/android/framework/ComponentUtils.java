package android.framework;

import android.assist.Assert;
import android.reflect.ClazzLoader;
import android.support.v4.app.Fragment;

/**
 * Created by handy on 17-2-9.
 */

public final class ComponentUtils {
    /**
     * 通过Fragment的类名，映射找到相应的Fragment，该配置在mapping.xml中定义
     *
     * @param clazz
     * @param objs
     * @return
     */
    public static <F extends Fragment> F newFragmentInstance(Class<F> clazz, Object... objs) {

        return newFragmentInstance(AppResource.getMapping(clazz != null ? clazz.getSimpleName() : null), objs);
    }

    /**
     * 通过mapping.xml中定义的类名，映射找到相应的Fragment
     *
     * @param resId
     * @param objs
     * @return
     */
    public static <F extends Fragment> F newFragmentInstance(int resId, Object... objs) {

        return newFragmentInstance(AppResource.getString(resId), objs);
    }

    public static <F extends Fragment> F newFragmentInstance(String className, Object... objs) {
        F f = null;

        if (Assert.notEmpty(className)) {
            f = ClazzLoader.newInstance(className, objs);
        }

        return f;
    }
}
