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
import com.ndhunju.dailyjournal.controller.HomeActivity;

/**
 * Created by dhunju on 10/2/2015.
 * This class
 */
public class MyNotificationManager {

    private static final String TAG = MyNotificationManager.class.getSimpleName();

    //Variables
    private NotificationManager mNotificationManager;
    private Context mContext;
    private int mNumNotif;

    private static MyNotificationManager myNotificationManager;

    public static MyNotificationManager from(Context context){
        if(myNotificationManager == null)
            myNotificationManager = new MyNotificationManager(context);
        return myNotificationManager;
    }

    private MyNotificationManager(Context context) {
        mNotificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);
        mContext = context;
        mNumNotif = 0;
    }

    private Notification create(String title, String msg, PendingIntent pendingIntent) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(mContext)
                .setNumber(++mNumNotif)
                .setSmallIcon(R.drawable.ic_ganesh_book_16px)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_ganesh_book_small))
                .setContentTitle(title)
                .setContentText(msg)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        return mBuilder.build();

    }

    public Notification create(int titleResId, int msgResId, PendingIntent pendingIntent) {
        return new NotificationCompat.Builder(mContext)
                .setNumber(++mNumNotif)
                .setSmallIcon(R.drawable.ic_ganesh_book_16px)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_ganesh_book_small))
                .setContentTitle(mContext.getString(titleResId))
                .setContentText(mContext.getString(msgResId))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true).build();

    }

    /**
     * This methods returns an instance of {@link android.support.v4.app.NotificationCompat.Builder}
     * so that you can add additional options to the builder before creating {@link NotificationCompat}
     * @param title
     * @param msg
     * @return
     */
    private NotificationCompat.Builder getBuilder(String title, String msg){
        return new NotificationCompat.Builder(mContext)
                .setSmallIcon(R.drawable.ic_ganesh_book_16px)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_ganesh_book_small))
                .setContentTitle(title)
                .setContentText(msg);
    }

    /**
     * Helper method to create reminder for backing up the data
     * @param interval
     * @return
     */
    public Notification createBackupCreatedNotif(String interval){
        Intent intent = new Intent(mContext, HomeActivity.class);
        PendingIntent notifPI = PendingIntent.getActivity(mContext, 0, intent, 0);
        return create(mContext.getString(R.string.app_name)
                        + "-" +mContext.getString(R.string.str_backup)
                        , mContext.getString(R.string.msg_finished, interval
                        + " "+ mContext.getString(R.string.str_backup)),
                        notifPI);
    }

    /**
     * Helper method that returns a {@link Notification} to display in notification
     * bar while the backup is being created
     * @return
     */
    public Notification createBackingUpNotif(){
        Intent intent = new Intent(mContext, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent = PendingIntent
                .getActivity(mContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        NotificationCompat.Builder builder = MyNotificationManager.from(mContext)
                .getBuilder(mContext.getString(R.string.str_auto_backup),
                        mContext.getString(R.string.msg_creating,
                        mContext.getString(R.string.str_backup)))
                .setProgress(100, 20, true)
                .setOngoing(true)
                .setContentIntent(pendingIntent);
        return builder.build();
    }

    public void notify(Notification notification, int id) {
        mNotificationManager.notify(TAG, id, notification);
    }

    public void cancel(int id){
        mNotificationManager.cancel(TAG, id);
    }

    public void nukeAll() {
        NotificationManagerCompat.from(mContext).cancelAll();
    }


}
