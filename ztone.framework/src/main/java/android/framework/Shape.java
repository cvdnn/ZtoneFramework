package android.framework;

import android.framework.context.lifecycle.LifeCycleUtils;
import android.util.DisplayMetrics;

public class Shape {
    public static final int BOUNDARY = 960;

    public boolean isLandscape;
    public int width;
    public int height;

    public Shape(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Shape(boolean isLandscape, int width, int height) {
        this.isLandscape = isLandscape;
        this.width = width;
        this.height = height;
    }

    public int getCalculateWidth() {
        return isLandscape ? width >> 1 : width;
    }

    public double getDiagonal() {
        return Math.hypot(width, height);
    }

    public static Shape getShape() {
        DisplayMetrics dm = LifeCycleUtils.component().app().getResources().getDisplayMetrics();

        return new Shape(dm.widthPixels > dm.heightPixels, dm.widthPixels, dm.heightPixels);
    }

    public static boolean isXHdpi() {
        Shape shape = getShape();

        return Math.max(shape.height, shape.width) > BOUNDARY;
    }

    @Override
    public boolean equals(Object o) {
        boolean result = false;
        if (o != null && o instanceof Shape) {
            Shape s = (Shape) o;
            result = s.width == width && s.height == height;
        }

        return result;
    }

}