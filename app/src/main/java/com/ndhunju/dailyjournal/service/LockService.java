package com.ndhunju.dailyjournal.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.lock.LockScreenActivity;

/**
 * Created by dhunju on 10/2/2015.
 * This class provides service for Locking the app with pincode.
 * This class could adopt Singleton design but just to try the
 * other way all variables and methods are made static
 */
public class LockService {


    //Variable
    private static long passcodeActivatedTime;
    private static PreferenceService preferenceService;

    /**
     * Checks if the user has enabled the pass code. If yes, start
     * LockScreenActivity
     * @param whichActivity : Activity that this method is called from
     */
    public static void checkPassCode(Activity whichActivity){

        preferenceService = PreferenceService.from(whichActivity);
        boolean pincodeOn = preferenceService.getVal(R.string.key_pref_pincode_cb, false);
        if(pincodeOn && !isPasscodeActive(whichActivity)){
            Intent i = new Intent(whichActivity, LockScreenActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            whichActivity.startActivity(i);
            updatePasscodeTime();
        }
    }

    public static void showLockScreen(Activity activity) {
        boolean isPinCodeEnabled = preferenceService.getVal(R.string.key_pref_pincode_cb, false);
        if (isPinCodeEnabled) {
            Intent i = new Intent(activity, LockScreenActivity.class);
            i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            activity.startActivity(i);
        }
    }

    /**
     * Checks if the pass code or lock time has expired.
     * if lock time has exceeded, it returns true
     * @return
     */
    private static boolean isPasscodeActive(Context con){
        preferenceService = PreferenceService.from(con);
        int lockTimeInMin = getLockTime(con);
        long difference = (System.currentTimeMillis()-passcodeActivatedTime);
        updatePasscodeTime();
        return difference  < lockTimeInMin*60*1000;
    }

    /**
     * Update the active time
     */
    public static void updatePasscodeTime(){
        passcodeActivatedTime = System.currentTimeMillis();
    }

    /**
     * Returns the lock time in minute that the user has set
     * @param con
     * @return
     */
    public static int getLockTime(Context con){
        preferenceService = PreferenceService.from(con);
        return preferenceService.getVal(R.string.key_pref_pincode_time_et,
                PreferenceService.DEF_LOCK_TIME);
    }
}
