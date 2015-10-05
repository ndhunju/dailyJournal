package com.ndhunju.dailyjournal.controller;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.ndhunju.dailyjournal.service.MyAlarmManager.Type;
import com.ndhunju.dailyjournal.service.MyNotificationManager;

/**
 * Created by dhunju on 10/2/2015.
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "receiver called");
        String type = intent.getType();

        switch (Type.valueOf(type)){

            case NOTIFICATION:
                Intent notifServiceIntent = new Intent(context, NotificationService.class);
                Notification parcelable = intent.getParcelableExtra(MyNotificationManager.ARG_NOTIF);
                notifServiceIntent.putExtra(MyNotificationManager.ARG_NOTIF, parcelable);
                //notifServiceIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //context.startActivity(notifServiceIntent);
                context.startService(notifServiceIntent);
                break;
        }
    }
}
