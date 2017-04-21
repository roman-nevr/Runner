package org.berendeev.roma.runner.presentation;

import android.app.Application;

import org.berendeev.roma.runner.BuildConfig;

import timber.log.Timber;


public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();
        initTimber();
    }

    private void initTimber(){
        if (BuildConfig.DEBUG) Timber.plant(new Timber.DebugTree());
    }
}
