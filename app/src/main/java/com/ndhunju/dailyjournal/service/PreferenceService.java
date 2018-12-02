package com.ndhunju.dailyjournal.service;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.service.AutoBackupJobService;
import com.ndhunju.dailyjournal.controller.service.AutoBackupService;
import com.ndhunju.dailyjournal.util.UtilsDate;

import java.util.Set;

/**
 * Created by dhunju on 10/4/2015.
 * Service class for storing and retrieving user preferences stored on xml file
 */
public class PreferenceService {


    private static final String TAG = PreferenceService.class.getSimpleName();

    //Default values
    public static final String DEF_NAME_SHARED_PREFERENCE =  "preferences";
    public static final long DEF_AUTO_BACKUP_TIME = 604800000;    //1 week
    public static final String DEF_PASSCODE = "";
    public static final int DEF_LOCK_TIME = 3;                   //3 minutes


    private static PreferenceService mPreferenceService;
    private Context context;

    public static PreferenceService from(@NonNull Context context){
        if(mPreferenceService == null)
            mPreferenceService = new PreferenceService(context);
        return mPreferenceService;
    }

    private PreferenceService(@NonNull Context context){
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

    /**
     * Helper method that returns string for given string res id
     * @param resKeyId : resource id for the key
     * @return
     */
    private String getKey(@StringRes int resKeyId){
        return context.getString(resKeyId);
    }

    /**
     * Helper method that returns the selected value for a preference
     * @param resKeyId :  resource id for key for the preference
     * @param defaultVal : value to return if key's value doesn't exist
     * @return
     */
    public String getVal(@StringRes int resKeyId, String defaultVal){
        return getVal(getKey(resKeyId), defaultVal);
    }

    /**
     * Returns boolean value saved in the xml preference file. Default value eg false
     * are saved as String type. This method can be used to retrieve such values as
     * well
     * @param resKeyId
     * @param defaultVal
     * @return
     */
    public boolean getVal(@StringRes int resKeyId, boolean defaultVal){

        boolean val;
        try{
            //By default, values saved in xml file are of String type. So need to parse the value
            val = Boolean.parseBoolean(getSharedPreference().getString(getKey(resKeyId),
                    String.valueOf(defaultVal)));
        } catch (Exception ex){
            val = getSharedPreference().getBoolean(getKey(resKeyId), defaultVal);
        }

        return val;
    }

    public int getVal(@StringRes int resKeyId, int defaultVal){
        return Integer.parseInt(getSharedPreference().getString(getKey(resKeyId),
                String.valueOf(defaultVal)));
    }

    public long getVal(@StringRes int resKeyId, long defaultVal){
        return getVal(getKey(resKeyId), defaultVal);
    }

    public Set<String> getVal(@StringRes int resKeyId, Set<String> defaultVal) {
        return getSharedPreference().getStringSet(getKey(resKeyId), defaultVal);
    }

    public long getVal(String key, long defaultVal) {
        return Long.parseLong(getSharedPreference().getString(key,
                String.valueOf(defaultVal)));
    }

    public String getVal(String key, String defaultVal){
        return getSharedPreference().getString(key, defaultVal);
    }

    public PreferenceService putVal(String key, String value) {
        getSharedPreference().edit().putString(key, value).apply();
        return this;
    }

    public PreferenceService putVal(String key, long value) {
        // store all values as String as preference_backup.xml does it so
        getSharedPreference().edit().putString(key, String.valueOf(value)).apply();
        return this;
    }

    public PreferenceService putVal(String key, boolean value) {
        getSharedPreference().edit().putBoolean(key, value).apply();
        return this;
    }


    public boolean clear(){
        return getSharedPreference().edit().clear().commit();
    }


    /**
     * Helper method that checks if the auto backup option is enabled or not.
     * If it is enabled then it sets an alarm for auto backup
     */
    public void updateAutoBackup() {
        //get the key for Reminder Checkbox and Reminder Interval Preference List
        String reminderCbKey = context.getString(R.string.key_pref_auto_backup_cb);

        Intent intent = new Intent(context, AutoBackupService.class);
        PendingIntent pendingIntent = PendingIntent.getService(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT);

        //check if reminder Checkbox is enabled or not
        if (getSharedPreference().getBoolean(reminderCbKey, true)) {
            //get the selected value for reminder interval eg, 3600000
            long selectedInterval = getVal(R.string.key_pref_auto_backup_interval_lp,
                    PreferenceService.DEF_AUTO_BACKUP_TIME);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                AutoBackupJobService.schedule(context, UtilsDate.get(selectedInterval).getTimeInMillis(), selectedInterval);
            } else {
                //set an alarm
                AlarmManagerWrapper.from(context).setRepeating(UtilsDate.get(selectedInterval),
                        pendingIntent, selectedInterval);
            }

            Log.d(TAG, "alarm set for auto backup : " + selectedInterval);
        } else {
            //cancel the alarm
            AlarmManagerWrapper.from(context).cancel(pendingIntent);
            Log.d(TAG, "alarm not set for auto backup : ");
        }
    }

    /**
     * Helper method that finds equivalent entry for entry value.
     *
     * @param interval : entry value
     * @return
     */
    public String getEntryForIntervalPL(long interval) {
        String[] vals = context.getResources().getStringArray(R.array.reminder_intervals_val);
        String[] entries = context.getResources().getStringArray(R.array.reminder_intervals);
        for (int i = 0; i < vals.length; i++)
            if (vals[i].equals(String.valueOf(interval)))
                return entries[i];
        return "";
    }
}
