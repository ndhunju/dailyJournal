package com.ndhunju.dailyjournal.controller;

import android.content.Context;

import com.ndhunju.dailyjournal.service.PreferenceService;

/**
 * Created by dhunju on 10/4/2015.
 */
public class Setup {

    public static void loadDefault(Context con){

        setUpBackUpReminder(con);

    }

    public static void setUpBackUpReminder(Context con){
        //Check the preference for reminder
        PreferenceService preferenceService = PreferenceService.from(con);
        preferenceService.updateBackupReminder();
    }
}
