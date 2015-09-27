package com.ndhunju.dailyjournal.controller;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.Journal.JournalActivity;

public class NotificationService extends IntentService{
	/*Imp Note: Services needs to be declared in AndroidManifest.xml file to work!!!!*/
	private static final String TAG = "NotificationService";
	
	public NotificationService() {
		super(TAG);
	}
	
	@Override
	protected void onHandleIntent(Intent intent) {
		Log.i(TAG, "Received a notification intent : " + intent);
		
		String msg = (String)intent.getSerializableExtra(Alarm.MSG);
		String title = (String)intent.getSerializableExtra(Alarm.TITLE);


		sendNotification(title, msg);
		
	}

    /**
     * Sends notification to the user.
     * @param title: Title for the notification
     * @param text: Notification message
     */
	public void sendNotification(String title, String text){

		PendingIntent pi = PendingIntent.getActivity(this, 0, new Intent(this, JournalActivity.class), 0);
		Notification notification = new NotificationCompat.Builder(this)
										.setSmallIcon(R.drawable.ic_ganesh_book_small)
										.setContentTitle(title)
										.setContentText(text)
										.setContentIntent(pi)
										.setAutoCancel(true)
										.build();


		NotificationManager nm = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
		//assign different notification id so that old one is not updated by new one
		nm.notify(7, notification);
	}
}
