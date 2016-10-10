package com.example.android.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by mahme4 on 9/30/2016.
 */
public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}