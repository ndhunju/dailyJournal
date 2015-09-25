package com.ndhunju.dailyjournal.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import com.ndhunju.dailyjournal.R;

public class MyPreferenceFragment extends PreferenceFragment {

    //Constants
    public static final int DEFAULT_LOCK_TIME = 3;      //3 mins
    public static final String NO_PASSCODE_VAL = "";
    public static final String DEF_NAME_SHARED_PREFERENCE =  "preferences";

    EditTextPreference passcodeET;
    CheckBoxPreference passcodeEnableCB;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the name of the preference file
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(DEF_NAME_SHARED_PREFERENCE);

        //Load the preferences from XML file
        addPreferencesFromResource(R.xml.preferences);

        passcodeET = (EditTextPreference)findPreference(getString(R.string.key_pref_pincode_val_et));
        passcodeET.setDialogTitle(getString(R.string.sum_pincode_val));

        passcodeEnableCB = (CheckBoxPreference)findPreference(getString(R.string.key_pref_pincode_cb));

        //Set the title
        getActivity().setTitle(getString(R.string.str_preference));
    }

    @Override
    public void onStop() {
        //disable the pincode lock if pincode is empty
        if(passcodeET.getText().equals(NO_PASSCODE_VAL))
            passcodeEnableCB.setChecked(false);
        super.onStop();
    }
}
