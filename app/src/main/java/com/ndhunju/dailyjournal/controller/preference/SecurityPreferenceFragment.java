package com.ndhunju.dailyjournal.controller.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsView;

public class SecurityPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    //Constants
    private static final String TAG = SecurityPreferenceFragment.class.getSimpleName();

    //Variables
    private CheckBoxPreference pincodeEnableCB;
    private EditTextPreference pincodeET;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        getActivity().setTitle(getString(R.string.str_security));

        //Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(PreferenceService.DEF_NAME_SHARED_PREFERENCE);

        addPreferencesFromResource(R.xml.preference_security);

        pincodeET = (EditTextPreference)findPreference(getString(R.string.key_pref_pincode_val_et));
        pincodeET.setDialogTitle(getString(R.string.sum_pincode_val));

        ((EditTextPreference)findPreference(getString(R.string.key_pref_pincode_time_et)))
                .setDialogTitle(getString(R.string.str_minutes));

        pincodeEnableCB = (CheckBoxPreference)findPreference(getString(R.string.key_pref_pincode_cb));

    }

    @Override
    public void onResume() {
        super.onResume();
        getView().setBackgroundColor(getResources().getColor(android.R.color.white));
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Check if the changed preference is Reminder Checkbox or reminder interval preference
        if (key.equals(getString(R.string.key_pref_pincode_cb))) {
            if (((CheckBoxPreference) findPreference(key)).isChecked())
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
}