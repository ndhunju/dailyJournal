package com.ndhunju.dailyjournal.service;

import android.app.Activity;
import android.app.Notification;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ndhunju.dailyjournal.R;

/**
 * Created by dhunju on 10/4/2015.
 */
public class PreferenceService {


    private static final String TAG = PreferenceService.class.getSimpleName();
    public static final String DEF_NAME_SHARED_PREFERENCE =  "preferences";
    public static final long DEFAULT_REMINDER_TIME = 604800000; //1 week
    public static final int DEFAULT_LOCK_TIME = 3;             //3 mins
    public static final String NO_PASSCODE_VAL = "";




    private static PreferenceService mPreferenceService;
    private Context context;

    public static PreferenceService from(Context context){
        if(mPreferenceService == null)
            mPreferenceService = new PreferenceService(context);
        return mPreferenceService;
    }

    private PreferenceService(Context context){
        this.context = context;
        loadSharedPreference();

    }

    /**
     * This method does the same things as
     * {@link android.preference.PreferenceFragment#addPreferencesFromResource(int)}
     * and {@link PreferenceManager#setSharedPreferencesName(String)}
     */
    public void loadSharedPreference(){
        //Set the name of the preference file
        PreferenceManager.setDefaultValues(context, DEF_NAME_SHARED_PREFERENCE, context.MODE_PRIVATE, R.xml.preferences, false);
    }



    public SharedPreferences getSharedPreference(){
        return context.getSharedPreferences(DEF_NAME_SHARED_PREFERENCE, context.MODE_PRIVATE);
    }

    /**
     * This method either sets the alarm for back up reminder or disables it depending
     * upon the user preference
     */
    public  void updateBackupReminder(){
            //get the selected value for reminder interval eg, 3600000
            long selectedVal = getVal(R.string.key_pref_reminder_interval_lp, DEFAULT_REMINDER_TIME);
            //get respective Entry eg. 1 hours
            String selectedEntry = getEntryForIntervalPL(selectedVal);
            //create notification
            Notification notif= MyNotificationManager.from(context).createBackupNotif(selectedEntry);
            //get the key for Reminder Checkbox and Reminder Interval Preference List
            String reminderCbKey = context.getString(R.string.key_pref_reminder_backup_cb);

            //check if reminder Checkbox is enabled or not
            if(getSharedPreference().getBoolean(reminderCbKey, true)){
                //parse the value and create a reminder
                MyAlarmManager.setNotificationReminder(selectedVal, context, notif);
                Log.d(TAG, "reminder set for backup : " + selectedVal);
            }else{
                //cancel the reminder
                MyAlarmManager.cancelNotificationReminder(context, notif);
                Log.d(TAG, "reminder not set for backup : " + selectedVal);
            }
    }

    /**
     * Helper method that finds equivalent entry for entry value.
     * @param interval : entry value
     * @return
     */
    public String getEntryForIntervalPL(long interval){
        String[] vals = context.getResources().getStringArray(R.array.reminder_intervals_val);
        String[] entries = context.getResources().getStringArray(R.array.reminder_intervals);
        for(int i = 0 ; i < vals.length ; i++)
            if(vals[i].equals(String.valueOf(interval)))
                return entries[i];
        return "";
    }

    /**
     * Helper method that returns string for given string res id
     * @param resKeyId : resource id for the key
     * @return
     */
    public String getKey(int resKeyId){
        return context.getString(resKeyId);
    }

    /**
     * Helper method that returns the selected value for a preference
     * @param key : key for the preference
     * @param defaultVal
     * @return
     */
    public String getVal(String key, String defaultVal){
        return getSharedPreference().getString(key, defaultVal);
    }

    public boolean getVal(String key, boolean defaultVal){
        return getSharedPreference().getBoolean(key, defaultVal);
    }

    public String getVal(int resKeyId, String defaultVal){
        return getSharedPreference().getString(getKey(resKeyId), defaultVal);
    }

    public boolean getVal(int resKeyId, boolean defaultVal){
        return getSharedPreference().getBoolean(getKey(resKeyId), defaultVal);
    }

    public int getVal(int resKeyId, int defaultVal){
        return Integer.parseInt(getSharedPreference().getString(getKey(resKeyId),
                String.valueOf(defaultVal)));
    }

    public long getVal(int resKeyId, long defaultVal){
        return Long.parseLong(getSharedPreference().getString(getKey(resKeyId),
                String.valueOf(defaultVal)));
    }

    public int getLockTime(){
        return getVal(R.string.key_pref_pincode_time_et, PreferenceService.DEFAULT_LOCK_TIME);
    }

    public boolean nukeAll(){
        return getSharedPreference().edit().clear().commit();
    }
}
