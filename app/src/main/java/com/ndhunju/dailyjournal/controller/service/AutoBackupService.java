package com.ndhunju.dailyjournal.controller.service;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * Created by dhunju on 10/6/2015.
 * This Background service create backup in default
 */
public class AutoBackupService extends Service implements AutoBackupHelper.EventListener {

    private static final String TAG = AutoBackupService.class.getSimpleName();

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        AutoBackupHelper autoBackupHelper = new AutoBackupHelper(this);
        autoBackupHelper.setEventListener(this);
        autoBackupHelper.startBackup();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onStartForeground(int id, Notification notification) {
        startForeground(id, notification);
    }

    @Override
    public void onFinishBackUp() {
        stopForeground(true);
        stopSelf();
    }
}
