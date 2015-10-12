package com.ndhunju.dailyjournal.controller;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.MyNotificationManager;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

/**
 * Created by dhunju on 10/6/2015.
 */
public class AutoBackupService extends Service {

    private static final String TAG = AutoBackupService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        //create expensive objects
        super.onCreate();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");
        Thread backupThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    MyNotificationManager notifMgr = MyNotificationManager.from(getBaseContext());
                    startForeground(1, notifMgr.createBackingUpNotif());
                    Services.getInstance(getBaseContext())
                            .createBackUp(UtilsFile.getAutoBackupDir(getBaseContext()));

                    PreferenceService ps = PreferenceService.from(getBaseContext());

                    //get the selected value for reminder interval eg, 3600000
                    long selectedInterval = ps.getVal(R.string.key_pref_auto_backup_interval_lp,
                            PreferenceService.DEFAULT_REMINDER_TIME);

                    //get respective Entry eg. 1 hours
                    String selectedEntry = ps.getEntryForIntervalPL(selectedInterval);

                    notifMgr.notify(notifMgr.createBackupCreatedNotif(selectedEntry), 1);

                    Log.d(TAG, "onStartCommand finished");


                    stopSelf();
                } catch (Exception e) {
                    Log.d(TAG, "auto backup failed");
                    e.printStackTrace();
                }
            }
        });

        backupThread.start();

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //destroy expensive objects
        super.onDestroy();
    }
}
