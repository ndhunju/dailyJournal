package com.ndhunju.dailyjournal.controller;

import android.content.Context;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.KeyValPersistence;
import com.ndhunju.dailyjournal.service.PreferenceService;

/**
 * Created by dhunju on 10/4/2015.
 * This class holds settings or things that needs to be initialized
 * when the app starts
 */
class Setup {

    public static final String KEY_APP_START_TOTAL = Constants.APP_PREFIX + "noOfTimesAppStarted";

    private KeyValPersistence mKeyVal;
    private static Setup mSetup;
    private Context mContext;

    public static Setup from(Context context){
        if(mSetup == null)
            mSetup = new Setup(context);
        return mSetup;
    }

    private Setup(Context context){
        mContext = context;
        mKeyVal = KeyValPersistence.from(context);
    }

    public void loadSettings(){

        //check if app is running the first time
        if(mKeyVal.get(KEY_APP_START_TOTAL, 0) <= 0){
            onAppStartFirstTime();
        }
        incrementAppStartTimes();



    }

    private void onAppStartFirstTime(){
        setUpBackUpReminder();
    }

    private boolean incrementAppStartTimes(){
        return mKeyVal.putLong(KEY_APP_START_TOTAL, mKeyVal.get(KEY_APP_START_TOTAL, 0) + 1);
    }

    private void setUpBackUpReminder(){
        //Check the preference for reminder
        PreferenceService preferenceService = PreferenceService.from(mContext);
        preferenceService.updateAutoBackup();
    }

}
