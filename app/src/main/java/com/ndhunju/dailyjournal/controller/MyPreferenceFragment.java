package com.ndhunju.dailyjournal.controller;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsView;

public class MyPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    //Constants
    private static final String TAG = MyPreferenceFragment.class.getSimpleName();

    //Variables
    PreferenceService preferenceService;
    CheckBoxPreference pincodeEnableCB;
    EditTextPreference pincodeET;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Set the title
        getActivity().setTitle(getString(R.string.str_preference));

        //load the preference file
        preferenceService = PreferenceService.from(getActivity());

        //Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(PreferenceService.DEF_NAME_SHARED_PREFERENCE);

        addPreferencesFromResource(R.xml.preferences);

        pincodeET = (EditTextPreference)findPreference(getString(R.string.key_pref_pincode_val_et));
        pincodeET.setDialogTitle(getString(R.string.sum_pincode_val));

        pincodeEnableCB = (CheckBoxPreference)findPreference(getString(R.string.key_pref_pincode_cb));

    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //get the key for Reminder Checkbox and Reminder Interval Preference List
        String reminderCbKey = getString(R.string.key_pref_reminder_backup_cb);
        String reminderIntervalPl = getString(R.string.key_pref_reminder_interval_lp);
        //Check if the changed preference is Reminder Checkbox or reminder interval preference
        if(key.equals(reminderCbKey) || key.equals(reminderIntervalPl)) {
            preferenceService.updateBackupReminder();
        }else if(key.equals(getString(R.string.key_pref_pincode_cb))){
                UtilsView.alert(getActivity(), getString(R.string.msg_please, getString(R.string.sum_pincode_val)));
        }
    }

    @Override
    public void onStop() {
        //disable the pincode lock if pincode is empty
        if(pincodeET.getText().equals(PreferenceService.NO_PASSCODE_VAL))
            pincodeEnableCB.setChecked(false);
        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

}
