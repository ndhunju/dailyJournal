package com.ndhunju.dailyjournal.util;

import android.os.Build;

import java.util.Calendar;

/**
 * Created by dhunju on 1/25/2016.
 */
public class Utils {

    public static boolean isKitKat(){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
    }

    public static boolean contains(long[] array, long value) {
        for (long item : array) {
            if (item == value) {
                return true;
            }
        }
        return false;
    }

    /** Removes hours, minutes, seconds and milliseconds from {@code calendar}*/
    public static Calendar removeValuesBelowHours(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }
}
