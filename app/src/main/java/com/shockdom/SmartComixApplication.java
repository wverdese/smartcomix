package com.shockdom;

import android.app.Application;

import com.shockdom.fresco.FrescoHelper;

public class SmartComixApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FrescoHelper.initialize(this);
    }
}
