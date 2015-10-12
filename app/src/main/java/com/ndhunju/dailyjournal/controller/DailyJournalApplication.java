package com.ndhunju.dailyjournal.controller;

import android.app.Application;

/**
 * Created by dhunju on 10/7/2015.
 * Base class for those who need to maintain global application state. You can provide your own
 * implementation by specifying its name in your AndroidManifest.xml's <application> tag, which will
 * cause that class to be instantiated for you when the process for your application/package is
 * created.
 */
public class DailyJournalApplication extends Application {

    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * (excluding content providers) have been created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        //Load defaults setttings
        Setup.from(getBaseContext()).loadSettings();
    }
}
