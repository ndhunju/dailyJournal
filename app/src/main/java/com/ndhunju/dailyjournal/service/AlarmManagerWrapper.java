package com.ndhunju.dailyjournal.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;

import java.util.Calendar;

/**
 * This is a Wrapper class for {@link AlarmManager}
 */
public class AlarmManagerWrapper {

	private static AlarmManagerWrapper alarmManagerWrapper;
	private AlarmManager mAlarmMgr;
	private Context mContext;

    private static final long INTERVAL_DAY = AlarmManager.INTERVAL_DAY;
    public static final long INTERVAL_WEEK = INTERVAL_DAY * 7;
    public static final long INTERVAL_MONTH = INTERVAL_DAY * 30;


	//Private Constructor for Singleton
	private AlarmManagerWrapper(Context context){
		mContext = context;
		mAlarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
	}

	public static AlarmManagerWrapper from(Context context){
		if(alarmManagerWrapper == null)
			alarmManagerWrapper = new AlarmManagerWrapper(context);
		return alarmManagerWrapper;
	}

	/**
	 * Sets an alarm that will fire up PendingIntent on passed time
	 * @param time time to trigger the alarm
	 * @param pendingIntent action to perform
	 */
    public void set(Calendar time, PendingIntent pendingIntent){
        mAlarmMgr.set(AlarmManager.RTC, time.getTimeInMillis(), pendingIntent);
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
		mAlarmMgr.setInexactRepeating(AlarmManager.RTC, time.getTimeInMillis(),
				intervalMillis, pendingIntent);
	}

	public void cancel(PendingIntent pendingIntent){
		mAlarmMgr.cancel(pendingIntent);
	}
}
