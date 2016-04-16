package com.ndhunju.dailyjournal.util;

import android.os.Build;

/**
 * Created by dhunju on 1/25/2016.
 */
public class Utils {

    public static boolean isLollipop(){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }
}
