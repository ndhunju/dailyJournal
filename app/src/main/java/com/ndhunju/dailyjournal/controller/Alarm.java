package com.ndhunju.dailyjournal.controller;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Constants;

public class Alarm {
	
	public final static String MSG = Constants.APP_PREFIX + "Alarm.msg";
	public static final String TITLE = Constants.APP_PREFIX + ".Alarm.title";

	public static void setAlarmForNotification(Context context, Class<?> serviceClass, Calendar cal, String title,
                                               String msg, boolean repeating, int repeatIntervel ){

		//constructing a Pending Intent to starts DataService
		Intent i = new Intent(context, serviceClass );
		i.putExtra(TITLE, title);
		i.putExtra(MSG, msg);
		PendingIntent pi = PendingIntent.getService(context, 0, i, 0);
		
		AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);

		if (repeating) am.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),	repeatIntervel, pi);
		else am.set(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), pi);

	}

    /**
     * Checks if the Alarm Service has already been activated
     * @param context
     * @param c : Class to invoke when the alarm turns on
     * @return true is Alarm Service is running
     */
	public static boolean isServiceAlarmOn(Context context, Class<?> c){
		Intent i = new Intent(context, c);
		PendingIntent pi = PendingIntent.getService(context, 0, i, PendingIntent.FLAG_NO_CREATE);
		
		return pi != null;
	}

    /**
     * Set the alarm
     * @param con
     */
	public static void setDefaultAlarm(Context con){
		if (Alarm.isServiceAlarmOn(con, NotificationService.class))
			return;
		// set the alarm to notify user to fill the journal
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.HOUR_OF_DAY, 21); // 9 PM
		cal.set(Calendar.MINUTE, 30); // 30 mins

		Alarm.setAlarmForNotification(con, NotificationService.class, Calendar.getInstance(),
				con.getString(R.string.str_reminder),
				con.getString(R.string.msg_reminder), true, 1000 * 60 * 60 * 24);
	}


}
