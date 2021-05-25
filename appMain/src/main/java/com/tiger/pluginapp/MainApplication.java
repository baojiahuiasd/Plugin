package com.tiger.pluginapp;

import android.app.Application;


import com.tiger.plugin.PlugIn;

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        PlugIn.getInstance().init(getBaseContext(),"com.tiger.appbranch");
    }
}
