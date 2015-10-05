package com.ndhunju.dailyjournal.controller;

import android.app.Notification;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.ndhunju.dailyjournal.service.MyNotificationManager;

public class NotificationService extends Service{
	/*Imp Note: Services needs to be declared in AndroidManifest.xml file to work!!!!*/
	private static final String TAG = NotificationService.class.getSimpleName();
	
	public NotificationService() {
		super();
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		//Log.d(TAG, "Received a notification intent : " + intent);
		Notification notif = intent.getParcelableExtra(MyNotificationManager.ARG_NOTIF);
		MyNotificationManager nm = MyNotificationManager.from(this);
		nm.notify(notif, 0);
		stopSelf();

		return START_NOT_STICKY;
	}

}
