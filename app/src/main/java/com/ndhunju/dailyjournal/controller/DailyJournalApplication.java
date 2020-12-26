package com.ndhunju.dailyjournal.controller;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

import com.ndhunju.dailyjournal.service.Setup;

import androidx.multidex.MultiDexApplication;

/**
 * Created by dhunju on 10/7/2015.
 * Base class for those who need to maintain global application state. You can provide your own
 * implementation by specifying its name in your AndroidManifest.xml's <application> tag, which will
 * cause that class to be instantiated for you when the process for your application/package is
 * created.
 */
public class DailyJournalApplication extends MultiDexApplication {

    private static DailyJournalApplication instance;
    private static Handler sUiThreadHandler;

    public static Application getInstance() {
        return instance;
    }
    /**
     * Called when the application is starting, before any activity, service, or receiver objects
     * (excluding content providers) have been created.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        //Load defaults settings
        Setup.from(getBaseContext()).loadSettings();
        sUiThreadHandler = new Handler(Looper.getMainLooper());
    }

    /**
     * Execute {@code runnable} on main/ui thread.
     */
    public static void postOnUiThread(Runnable runnable) {
        sUiThreadHandler.post(runnable);
    }


}
