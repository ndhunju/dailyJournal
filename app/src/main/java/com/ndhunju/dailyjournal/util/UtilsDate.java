package com.ndhunju.dailyjournal.util;

import java.util.Calendar;
import java.util.Date;


/**
 * Created by dhunju on 10/2/2015.
 * This is a Utility Class
 */
public class UtilsDate {

    private static Calendar get(int timeOfDay){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, timeOfDay);
        return calendar;
    }

    public static Calendar get(int year, int month, int day, int hour, int minute){
        Calendar calendar = get(hour);
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        calendar.set(Calendar.MINUTE, minute);
        return  calendar;
    }

    /**
     * This is a utility method that adds passed time in
     * milli second to current time and returns a calendar
     * with the new time. This method can be used if you
     * want a time after say 30 mins regardless of the current
     * time. Eg get(30 * 60 * 1000 );
     * @param timeInMilliSec
     * @return
     */
    public static Calendar get(long timeInMilliSec){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis() + timeInMilliSec);
        return calendar;
    }

    /**
     * Returns true if date1 and date2 is same day in point of time
     */
    public static boolean isSameDay(Date date1, Date date2) {
        Calendar calendar = Calendar.getInstance();

        calendar.setTime(date1);
        int dayOfYear1 = calendar.get(Calendar.DAY_OF_YEAR);
        int monthOfYear1 = calendar.get(Calendar.MONTH);
        int year1 = calendar.get(Calendar.YEAR);

        calendar.setTime(date2);
        int dayOfYear2 = calendar.get(Calendar.DAY_OF_YEAR);
        int monthOfYear2 = calendar.get(Calendar.MONTH);
        int year2 = calendar.get(Calendar.YEAR);

        return dayOfYear1 == dayOfYear2          // Same day?
                && monthOfYear1 == monthOfYear2  // Same month?
                && year1 == year2;               // Same year?
    }

}
