package com.ndhunju.dailyjournal.util;

import android.os.Build;

/**
 * Created by dhunju on 1/25/2016.
 */
public class Utils {

    public static boolean isKitKat(){
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT);
    }
}
