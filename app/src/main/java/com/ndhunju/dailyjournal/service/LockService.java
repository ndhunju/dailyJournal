package com.ndhunju.dailyjournal.service;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.lock.LockScreenActivity;

/**
 * Created by dhunju on 10/2/2015.
 */
public class LockService {

    //Variable
    public static long passcodeActivatedTime;
    static PreferenceService preferenceService;

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
            whichActivity.startActivity(i);
            updatePasscodeTime();
        }
    }

    /**
     * Checks if the pass code or lock time has expired.
     * if lock time has exceeded, it returns true
     * @return
     */
    public static boolean isPasscodeActive(Context con){
        preferenceService = PreferenceService.from(con);
        int lockTimeInMin = preferenceService.getLockTime();
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
}
