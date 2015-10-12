package com.ndhunju.dailyjournal.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.AutoBackupService;
import com.ndhunju.dailyjournal.util.UtilsDate;

/**
 * Created by dhunju on 10/4/2015.
 */
public class PreferenceService {


    private static final String TAG = PreferenceService.class.getSimpleName();
    public static final String DEF_NAME_SHARED_PREFERENCE =  "preferences";
    public static final long DEFAULT_REMINDER_TIME = 604800000; //1 week
    private static final int DEFAULT_LOCK_TIME = 3;             //3 mins
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
    private void loadSharedPreference(){
        //Set the name of the preference file
        PreferenceManager.setDefaultValues(context, DEF_NAME_SHARED_PREFERENCE,
                Context.MODE_PRIVATE, R.xml.preference_backup, true);
    }

    private SharedPreferences getSharedPreference(){
        return context.getSharedPreferences(DEF_NAME_SHARED_PREFERENCE,
                Context.MODE_PRIVATE);
    }

    public void updateAutoBackup(){
        //get the key for Reminder Checkbox and Reminder Interval Preference List
        String reminderCbKey = context.getString(R.string.key_pref_auto_backup_cb);

        Intent intent = new Intent(context, AutoBackupService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //check if reminder Checkbox is enabled or not
        if (getSharedPreference().getBoolean(reminderCbKey, true)) {
            //get the selected value for reminder interval eg, 3600000
            long selectedInterval = getVal(R.string.key_pref_auto_backup_interval_lp,
                    PreferenceService.DEFAULT_REMINDER_TIME);

            //set an alarm
            MyAlarmManager.from(context).setRepeating(UtilsDate.get(selectedInterval),
                    pendingIntent, selectedInterval);
            Log.d(TAG, "alarm set for auto backup : " + selectedInterval);
        } else {
            //cancel the alarm
            MyAlarmManager.from(context).cancel(pendingIntent);
            Log.d(TAG, "alarm not set for auto backup : ");
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
    private String getKey(int resKeyId){
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

    private int getVal(int resKeyId, int defaultVal){
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