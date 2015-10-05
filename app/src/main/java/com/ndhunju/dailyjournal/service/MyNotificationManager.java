package com.ndhunju.dailyjournal.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.importExport.ImportExportActivity;

/**
 * Created by dhunju on 10/2/2015.
 */
public class MyNotificationManager {

    private static final String TAG = MyNotificationManager.class.getSimpleName();
    public static final String ARG_NOTIF = "notificationObj";

    NotificationManager notificationManager;
    Context mContext;
    int numNotif;

    private static MyNotificationManager myNotificationManager;

    public static MyNotificationManager from(Context context){
        if(myNotificationManager == null)
            myNotificationManager = new MyNotificationManager(context);
        return myNotificationManager;
    }

    private MyNotificationManager(Context context) {
        notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = context;
        numNotif = 0;
    }

    public Notification create(String title, String msg, PendingIntent pendingIntent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setNumber(++numNotif)
                .setSmallIcon(R.drawable.ic_ganesh_book_small)
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return mBuilder.build();

    }

    public Notification create(int titleResId, int msgResId, PendingIntent pendingIntent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setNumber(++numNotif)
                .setSmallIcon(R.drawable.ic_ganesh_book_small)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_ganesh_book))
                .setContentTitle(mContext.getString(titleResId))
                .setContentText(mContext.getString(msgResId))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return mBuilder.build();

    }

    /**
     * Helper method to create reminder for backing up the data
     * @param interval
     * @return
     */
    public Notification createBackupNotif(String interval){
        Intent intent = new Intent(mContext, ImportExportActivity.class);
        PendingIntent notifPI = PendingIntent.getActivity(mContext, 0,intent,0 );
        return create(mContext.getString(R.string.str_backup), mContext.getString(R.string.msg_backup, interval),
                notifPI);
    }

    public void notify(Notification notification, int id) {
        notificationManager.notify(TAG, id, notification);
    }

    public void cancel(int id){
        notificationManager.cancel(TAG, id);
    }

    public void nukeAll() {
        NotificationManagerCompat.from(mContext).cancelAll();
    }
}
