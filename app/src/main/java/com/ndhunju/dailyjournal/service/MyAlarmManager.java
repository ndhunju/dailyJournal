package com.ndhunju.dailyjournal.service;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.AlarmReceiver;
import com.ndhunju.dailyjournal.util.UtilsDate;

public class MyAlarmManager {

	private static MyAlarmManager myAlarmManager;
	private AlarmManager am;
	Context mContext;

	public enum Type { NOTIFICATION }

    public static final long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;
    public static final long INTERVAL_WEEK = INTERVAL_DAY * 7;
    public static final long INTERVAL_MONTH = INTERVAL_DAY * 30;


	//Private Constructor for Singleton
	private MyAlarmManager(Context context){
		mContext = context;
		am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}

	public static MyAlarmManager from(Context context){
		if(myAlarmManager == null)
			myAlarmManager = new MyAlarmManager(context);
		return myAlarmManager;
	}

    public void set(Calendar time, PendingIntent pendingIntent){
        am.set(AlarmManager.RTC, time.getTimeInMillis(), pendingIntent);
    }

	/**
	 * Sets an alarm with passed arguments
	 * @param time : time to fire the alarm
	 * @param pendingIntent : action to perform
     * @param intervalMillis : repeat interval
	 */
	public void setRepeating(Calendar time, PendingIntent pendingIntent, long intervalMillis){
		// With setInexactRepeating(), you have to use one of the AlarmManager interval
		// constants--in this case, AlarmManager.INTERVAL_DAY.
		am.setInexactRepeating(AlarmManager.RTC, time.getTimeInMillis(),
				intervalMillis, pendingIntent);
	}

	public void cancel(PendingIntent pendingIntent){
		am.cancel(pendingIntent);
	}


	/**
	 * Sets reminder to back up data at passed time
	 * @param timeInMilliSec
	 */
	public static void setNotificationReminder(long timeInMilliSec, Context context, Notification notif){
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setType(Type.NOTIFICATION.toString());
		intent.putExtra(MyNotificationManager.ARG_NOTIF, notif);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        MyAlarmManager.from(context).setRepeating(UtilsDate.get(timeInMilliSec), pendingIntent, timeInMilliSec);
	}

	public static void cancelNotificationReminder(Context context, Notification notif){
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setType(Type.NOTIFICATION.toString());
		intent.putExtra(MyNotificationManager.ARG_NOTIF, notif);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		MyAlarmManager.from(context).cancel(pendingIntent);
	}

	public static void setJournalEntryReminder(long timeInMilliSec, Context con){
		MyNotificationManager nm = MyNotificationManager.from(con);
		Notification notif = nm.create(R.string.str_reminder, R.string.msg_reminder, null);
        setNotificationReminder(timeInMilliSec, con, notif);
	}

}
