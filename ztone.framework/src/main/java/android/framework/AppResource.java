package android.framework;

import android.assist.Assert;
import android.assist.TextUtilz;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.extend.ResourceUtils;
import android.framework.context.lifecycle.LifeCycleUtils;
import android.framework.entity.Entity;
import android.graphics.drawable.Drawable;
import android.io.StreamUtils;
import android.json.JSONUtils;
import android.os.Build;
import android.support.annotation.LayoutRes;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewConfiguration;

import java.io.InputStream;

/**
 * Created by handy on 17-2-9.
 */

public final class AppResource {

    public static float dip(float value) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getDisplayMetrics());
    }

    public static int dipInt(float value) {

        return (int) (TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getDisplayMetrics()) + 0.5f);
    }

    /**
     * 字体像素转变,方法中不用SP为单位,为了避免手机字体大小设置问题
     *
     * @param value
     * @return
     */
    public static float sp(float value) {

        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value, getDisplayMetrics());
    }

    /**
     * 使用最大可用内存值的1/8作为图片缓存的大小。
     *
     * @return
     */
    public static long getMaxMemoryForBitmap() {

        return Runtime.getRuntime().maxMemory() / 8;
    }

    public static boolean isAboveHoneycomb() {

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static int getTouchSlop() {

        return ViewConfiguration.get(LifeCycleUtils.component().app()).getScaledTouchSlop();
    }

    public static DisplayMetrics getDisplayMetrics() {

        return LifeCycleUtils.component().app().getResources().getDisplayMetrics();
    }

    public static float getDensity() {

        return LifeCycleUtils.component().app().getResources().getDisplayMetrics().density;
    }

    public static int getDensityDpi() {

        return LifeCycleUtils.component().app().getResources().getDisplayMetrics().densityDpi;
    }

    public static Resources getResources() {

        return LifeCycleUtils.component().app().getResources();
    }

    public static View inflate(@LayoutRes int resId) {
        View view = null;

        if (resId != 0) {
            view = View.inflate(LifeCycleUtils.component().app(), resId, null);
        }

        return view;
    }

    public static int getId(String idName) {

        return getIdentifier(idName, C.tag.resource_type_id);
    }

    public static String getString(int resId) {

        return ResourceUtils.getString(getResources(), resId);
    }

    public static String getString(int resId, Object... formatArgs) {

        return ResourceUtils.getString(getResources(), resId, formatArgs);
    }

    public static String getString(String resName) {

        return getString(getStringResourceId(resName));
    }

    public static String getString(String resName, Object... formatArgs) {

        return ResourceUtils.getString(getResources(), getStringResourceId(resName), formatArgs);
    }

    public static String[] getStringArray(int resId) {

        return ResourceUtils.getStringArray(getResources(), resId);
    }

    public static int getColor(int resId) {

        return ResourceUtils.getColor(getResources(), resId);
    }

    public static ColorStateList getColorStateList(int resId) {

        return ResourceUtils.getColorStateList(getResources(), resId);
    }

    public static float getDimension(int resId) {

        return ResourceUtils.getDimension(getResources(), resId);
    }

    public static int getInteger(int resId) {

        return resId != 0 ? ResourceUtils.getInteger(getResources(), resId) : 0;
    }

    public static Drawable getDrawable(int resId) {

        return ResourceUtils.getDrawable(getResources(), resId);
    }

    public static Drawable getDrawable(String drawableName) {

        return getDrawable(getDrawableResourceId(drawableName));
    }

    public static Drawable getDrawable(String drawableName, int defRseId) {

        return Assert.notEmpty(drawableName) ? ResourceUtils.getDrawable(IRuntime.getPackageName(), getResources(), drawableName) : getDrawable(defRseId);
    }

    public static int getDrawableResourceId(String drawableName) {

        return getIdentifier(drawableName, C.tag.resource_type_drawable);
    }

    public static int getDrawableResourceId(String drawableName, int resId) {
        int drawable = getIdentifier(drawableName, C.tag.resource_type_drawable);
        if (drawable == 0) {
            drawable = resId;
        }

        return drawable;
    }

    public static int getStringResourceId(String resName) {

        return getIdentifier(resName, C.tag.resource_type_string);
    }

    public static int getStringArrayResourceId(String resName) {

        return getIdentifier(resName, C.tag.resource_type_array);
    }

    public static int getAnimResourceId(String resName) {

        return getIdentifier(resName, C.tag.resource_type_anim);
    }

    public static int getDimenResourceId(String resName) {

        return getIdentifier(resName, C.tag.resource_type_dimen);
    }

    public static int getIdentifier(String resName, String type) {

        return ResourceUtils.getIdentifier(IRuntime.getPackageName(), getResources(), resName, type);
    }

    public static int getIdentifier(String pkgName, String resName, String type) {

        return ResourceUtils.getIdentifier(pkgName, getResources(), resName, type);
    }

    public static InputStream getInputStreamFromRaw(int resId) {

        return ResourceUtils.getInputStreamFromRaw(getResources(), resId);
    }

    public static InputStream getInputStreamFromAssets(String fileName) {

        return ResourceUtils.getInputStreamFromAssets(getResources(), fileName);
    }

    public static String getMapping(int resId) {

        return getString(resId);
    }

    public static String getMapping(String resName) {

        return getMapping(getStringResourceId(resName));
    }

    public static <V extends Entity> V getEntity(Class<V> clazz, int rawId) {

        return new Entity.Builder().clazz(clazz).json(JSONUtils.from(TextUtilz.fromFake(StreamUtils.getContent(AppResource.getInputStreamFromRaw(rawId))))).build();
    }
}
