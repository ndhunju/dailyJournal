package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by dhunju on 10/4/2015.
 */
public class KeyValPersistence {


    //Constant variables
    private static final String KEY_CURRENT_PARTY_ID = Constants.APP_PREFIX + "currentMerchantId";
    private static final String KEY_CURRENT_JOURNAL_ID = Constants.APP_PREFIX + "currentJournalId";
    private static final String KEY_OLD_DATA_IMPORTED = Constants.APP_PREFIX + "oldDataImported";

    private static final String XML_NAME = "hashMap";

    private Context mContext;
    private SharedPreferences preference;

    private static KeyValPersistence keyValPersistence;

    public static KeyValPersistence from(Context context){
        if(keyValPersistence == null)
            keyValPersistence = new KeyValPersistence(context);
        return keyValPersistence;
    }

    private KeyValPersistence(Context context){
        mContext = context;
        preference = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE);
    }

    public String get(String key) {
        return preference.getString(key, null);
    }

    public boolean put(String key, String value){
        return preference.edit().putString(key, value).commit();
    }

    /**
     * Created to use it while JUnit testing
     * @param sp
     * @return
     */
    public boolean setSharedPreference(SharedPreferences sp){
        if(sp == null) return false;
        preference = sp;
        return true;
    }


    public static boolean isOldDataImported(Context context){
        //old versions have value saved in default preference file
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_OLD_DATA_IMPORTED, false);
    }

    public static void oldDataImportAttempted(boolean imported, Context context){
        //old versions have value saved in default preference file
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(KEY_OLD_DATA_IMPORTED, imported).commit();
    }

    /**
     * Erases all stored SharedPreferences
     * @return
     */
    public boolean nukeAll(Context context){
        return  preference.edit().clear().commit();
    }
}
