package com.example.android.popmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

/**
 * Created by jawad on 6/14/16.
 */
public class MyApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}
