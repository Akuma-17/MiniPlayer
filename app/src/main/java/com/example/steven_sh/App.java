package com.example.steven_sh;

import android.app.Application;

public class App extends Application {

    private static App sInstance;

    public static App getApp() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }
}
