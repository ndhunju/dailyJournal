package com.ndhunju.dailyjournal.controller.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

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
		Log.d(TAG, "Received a notification intent : " + intent);
		stopSelf();

		return START_NOT_STICKY;
	}

}
