package com.example.steven_sh.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import com.example.steven_sh.App;

@Deprecated
public class MetricsUtils {

    static int displayWidth = 0;

    static int displayHeight = 0;

    static int displayResolution = 0;

    private static int DEFAULT_DENSITY_DPI = 240;

    private static final float DEFAULT_DENSITY = 1.5f;

    private MetricsUtils() {
    }

    public static int getDefaultDensity() {
        return DEFAULT_DENSITY_DPI;
    }

    public static int dipToPixel(float dip) {
        return (int) (dip * getDensity());
    }

    public static int dipToPixel(Context context, float dip) {
        return (int) (dip * context.getResources().getDisplayMetrics().density);
    }

    public static float pixelInDensityF(int pixel) {
        return pixel / DEFAULT_DENSITY * getDensity();
    }

    public static int pixelInDensity(int pixel) {
        return (int) pixelInDensityF(pixel);
    }

    /**
     * This method is scaled density for emoticon gif imags in animatedGifView.
     *
     * @return scaled density + roundding up. If density of device over then
     *         240(hdpi) then density of device.
     */
    public static int getScaledDensityDpi() {
        int defaultDensityDpi = getDensityDpi();
        float scaleFactor = (float) defaultDensityDpi / DEFAULT_DENSITY_DPI;
        return (int) (scaleFactor * defaultDensityDpi + .5f);
    }

    @SuppressWarnings("deprecation")
    public static int getDisplayWidth() {
        try {
            displayWidth = getDisplay().getWidth();
        } catch (Exception e) {
        }
        return displayWidth;
    }

    @SuppressWarnings("deprecation")
    public static int getDisplayWidth(Context context) {
        try {
            WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            displayWidth = wm.getDefaultDisplay().getWidth();
        } catch (Exception e) {
        }
        return displayWidth;
    }

    @SuppressWarnings("deprecation")
    public static int getDisplayHeight() {
        try {
            displayHeight = getDisplay().getHeight();
        } catch (Exception e) {
        }
        return displayHeight;
    }

    public static int getDisplayResolution() {
        if (displayResolution > 0) {
            return displayResolution;
        }
        displayResolution = displayWidth * displayHeight;
        return displayResolution;
    }

    public static int getOrientation() {
        return App.getApp().getResources().getConfiguration().orientation;
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        final DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics;
    }

    public static Rect calculateFitRect(Rect outFrameRect, int sourceAspectX, int sourceAspectY) {
        Rect result = new Rect();

        float outFrameRatio = (float)outFrameRect.width() / (float)outFrameRect.height();
        float sourceRatio = (float)sourceAspectX / (float)sourceAspectY;

        int calculatedSourceWidth;
        int calculatedSourceHeight;

        if (outFrameRatio > sourceRatio) {
            calculatedSourceWidth = sourceAspectX * outFrameRect.height() / sourceAspectY;
            calculatedSourceHeight = outFrameRect.height();
        } else {
            calculatedSourceWidth = outFrameRect.width();
            calculatedSourceHeight = sourceAspectY * outFrameRect.width() / sourceAspectX;
        }
        result.left = outFrameRect.centerX() - calculatedSourceWidth/2;
        result.top = outFrameRect.centerY() - calculatedSourceHeight/2;
        result.right = outFrameRect.centerX() + calculatedSourceWidth/2;
        result.bottom = outFrameRect.centerY() + calculatedSourceHeight/2;

        return result;
    }

    public static RectF rotateRect(RectF originRect, float pivotX, float pivotY, double degree) {
        double angle = Math.toRadians(degree);

        float pointLeftTopX = (originRect.left-pivotX) * (float)Math.cos(angle) - (originRect.top-pivotY) * (float)Math.sin(angle) + pivotX;
        float pointLeftTopY = (originRect.left-pivotX) * (float)Math.sin(angle) + (originRect.top-pivotY) * (float)Math.cos(angle) + pivotY;

        float pointRightBottomX = (originRect.right-pivotX) * (float)Math.cos(angle)
                - (originRect.bottom-pivotY) * (float)Math.sin(angle) + pivotX;
        float pointRightBottomY = (originRect.right-pivotX) * (float)Math.sin(angle)
                + (originRect.bottom-pivotY) * (float)Math.cos(angle) + pivotY;

        RectF rotatedDrawableRect = new RectF( Math.min(pointLeftTopX, pointRightBottomX),
                Math.min(pointLeftTopY, pointRightBottomY),
                Math.max(pointLeftTopX, pointRightBottomX),
                Math.max(pointLeftTopY, pointRightBottomY));

        return rotatedDrawableRect;
    }

    public static int getDisplayRealHeight() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Point p = new Point();
                getDisplay().getRealSize(p);
                return p.y;
            }catch (Exception e) {
            }
        }

        return getDisplayHeight();
    }

    public static int getDisplayRealWidth() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            try {
                Point p = new Point();
                getDisplay().getRealSize(p);
                return p.x;
            }catch (Exception e) {
            }
        }

        return getDisplayWidth();
    }

    public enum HeightDp {
        UNDER_540DP,
        UNDER_610DP,
        UNDER_640DP,
        UNDER_670DP,
        UNDER_790DP,
        OVER_790DP
    }

    public static HeightDp checkHeightDp() {
        int heightDp = (int) (getDisplayRealHeight() / getDensity());
        int availableHeightDp = (int) (getDisplayHeight() / getDensity());
//        Logger.i("@@@ checkHeightDp:" + heightDp + "|" + availableHeightDp);
        if (heightDp != availableHeightDp) {
            heightDp = availableHeightDp;
        }

        if (heightDp > 790) {
            return HeightDp.OVER_790DP;
        } else if (heightDp > 670) {
            return HeightDp.UNDER_790DP;
        } else if (heightDp > 640) {
            return HeightDp.UNDER_670DP;
        } else if (heightDp > 610) {
            return HeightDp.UNDER_640DP;
        } else if (heightDp > 540) {
            return HeightDp.UNDER_610DP;
        } else {
            return HeightDp.UNDER_540DP;
        }
    }

    public static int getDensityDpi() {
        return App.getApp().getResources().getDisplayMetrics().densityDpi;
    }

    public static float getDensity() {
        return App.getApp().getResources().getDisplayMetrics().density;
    }

    public static Display getDisplay() {
        return ((WindowManager) App.getApp().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
    }

    public static DisplayMetrics getDisplayMetrics() {
        DisplayMetrics displayMetrics = App.getApp().getResources().getDisplayMetrics();
        return displayMetrics;
    }
}
