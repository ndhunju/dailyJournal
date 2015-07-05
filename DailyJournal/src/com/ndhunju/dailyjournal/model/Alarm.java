package com.ndhunju.dailyjournal.model;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

public class Alarm {
	
	public final static String MSG = Utils.APP_PREFIX + "Alarm.msg";
	public static final String TITLE = Utils.APP_PREFIX + ".Alarm.title";

	public static void setAlarmForNotification(Context context, Class<?> serviceClass, Calendar cal, String title, String msg, boolean repeating, int repeatIntervel ){
		//constructing a Pending Intent to starts DataService
		Intent i = new Intent(context, serviceClass );
		i.putExtra(TITLE, title);
		i.putExtra(MSG, msg);
		
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		if (repeating)
			am.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
					repeatIntervel, pi);
		else
			am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);
	}
	
	public static boolean isServiceAlarmOn(Context context, Class<?> c){
		Intent i = new Intent(context, c);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		
		return pi != null;
	}
	
}
