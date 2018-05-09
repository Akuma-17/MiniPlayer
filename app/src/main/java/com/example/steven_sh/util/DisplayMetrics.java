package com.example.steven_sh.util;

import android.content.res.Resources;

import com.example.steven_sh.App;

public class DisplayMetrics {

    public static int getDisplayWidth() {
        return App.getApp().getResources().getDisplayMetrics().widthPixels;
    }

    public static int getDisplayHeight() {
        return App.getApp().getResources().getDisplayMetrics().heightPixels;
    }

    public static float getDensity() {
        return App.getApp().getResources().getDisplayMetrics().density;
    }

    public static int getStatusBarHeight(Resources resources) {
        int result = 0;
        int resourceId = resources.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
