package com.ndhunju.dailyjournal.service;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.util.List;

/**
 * Created by dhunju on 10/4/2015.
 * This class can be used to permanently store Key Value pair.
 * This class uses {@link SharedPreferences} to store the value.
 */
public class KeyValPersistence{

    //Variables
    private Context mContext;
    private SharedPreferences mPreference;
    private final String XML_NAME = "hashMap";
    private static KeyValPersistence mKeyValPersistence;


    public static KeyValPersistence from(Context context){
        if(mKeyValPersistence == null)
            mKeyValPersistence = new KeyValPersistence(context);
        return mKeyValPersistence;
    }

    private KeyValPersistence(Context context){
        mPreference = context.getSharedPreferences(XML_NAME, Context.MODE_PRIVATE);
        mContext = context;
    }

    /**
     * This method will replace default {@link SharedPreferences} instance.
     * All the values will be saved through passed Shared preference
     * @param sp : Shared Preference you want to use
     * @return
     */
    public boolean setSharedPreference(SharedPreferences sp){
        if(sp == null) return false;
        mPreference = sp;
        return true;
    }

    public String get(String key, String defVal) {
        return mPreference.getString(key, defVal);
    }

    public long get(String key, long defLongVal){
        return mPreference.getLong(key, defLongVal);
    }

    public boolean get(String key, boolean defBoolVal){
        return mPreference.getBoolean(key, defBoolVal);
    }

    public int get(String key, int defIntVal){
        return mPreference.getInt(key, defIntVal);
    }

    public String get(int keyResId, String defVal){
        return get(mContext.getString(keyResId), defVal);
    }

    public long get(int keyResId, long defVal){
        return get(mContext.getString(keyResId), defVal);
    }

    public void putString(String key, String value){
        mPreference.edit().putString(key, value).apply();
    }

    public void putString(int keyResId, String value){
        putString(mContext.getString(keyResId), value);

    }

    public void putLong(String key, long val){
        mPreference.edit().putLong(key, val).apply();
    }

    public void putInt(String key, int val){
        mPreference.edit().putInt(key, val).apply();
    }

    public void putBoolean(String key, boolean val){
        mPreference.edit().putBoolean(key, val).apply();
    }

    public void putLong(int keyResId, long value){
        putLong(mContext.getString(keyResId), value);
    }

    /**
     * Erases all stored SharedPreferences
     * @return true is successful
     */
    public boolean clear(){
        return  mPreference.edit().clear().commit();
    }

    //Static methods and variables
    private static final String KEY_OLD_DATA_IMPORTED = Constants.APP_PREFIX + "oldDataImported";

    public static boolean isOldDataImported(Context context){
        //old versions have value saved in default preference file
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_OLD_DATA_IMPORTED, false);
    }

    public static boolean isOldDataImported(SharedPreferences sharedPreferences){
        //old versions have value saved in default preference file
        return sharedPreferences.getBoolean(KEY_OLD_DATA_IMPORTED, false);
    }

    public static void oldDataImportAttempted(boolean imported, Context context){
        //old versions have value saved in default preference file
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putBoolean(KEY_OLD_DATA_IMPORTED, imported).apply();
    }

}
