package com.ndhunju.dailyjournal.controller.preference;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Sub class of {@link PreferenceFragment} for handling Security Preferences
 */
public class SecurityPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener{

    //Constants
    public static final String TAG = SecurityPreferenceFragment.class.getSimpleName();
    private static final int PIN_PREF_POS = 1; // this position is hard coded and determined from R.xml.preference_security position

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

        pincodeEnableCB = (CheckBoxPreference)findPreference(getString(R.string.key_pref_pincode_cb));

        ((EditTextPreference)findPreference(getString(R.string.key_pref_pincode_time_et)))
                .setDialogTitle(getString(R.string.str_minutes));

    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorBackground));
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //Check if the changed preference is Reminder Checkbox or reminder interval preference
        if (key.equals(getString(R.string.key_pref_pincode_cb))) {
            if (((CheckBoxPreference) findPreference(key)).isChecked()){
                //Alert user to enter the pin code
                UtilsView.alert(getActivity(), getString(R.string.msg_please, getString(R.string.sum_pincode_val)), (dialogInterface, i) -> {
                    try {
                        // auto show the dialog to enter the pin
                        int positionInListView = PIN_PREF_POS +1 /*for title*/;
                        ListView list = getView().findViewById(android.R.id.list);
                        list.performItemClick(list.getChildAt(positionInListView), positionInListView, list.getAdapter().getItemId(positionInListView));
                    } catch (Exception ignore) {}
                });
            }

        } else if (key.equals(getString(R.string.key_pref_pincode_val_et))) {
            String pinCode = ((EditTextPreference) findPreference(key)).getText();
            try {
                Integer.valueOf(pinCode);
                UtilsView.alert(getActivity(), getString(R.string.msg_pin_code));
            } catch (NumberFormatException ex) {
                //Alert user
                UtilsView.alert(getActivity(), getString(R.string.msg_is_not_valid, getString(R.string.str_pincode)));
                // disable the pincode lock
                PreferenceService.from(getActivity()).putVal(getString(R.string.key_pref_pincode_cb), "false");
            }
        } else if (key.equals(getString(R.string.key_pref_pincode_time_et))) {
            String timeStr = ((EditTextPreference) findPreference(key)).getText();
            try {
                int time = Integer.valueOf(timeStr);
                if (time < 1) {
                    UtilsView.alert(getActivity(), getString(R.string.msg_is_not_valid, getString(R.string.str_lock_time)));
                    // set to minimum value allowed
                    PreferenceService.from(getActivity()).putVal(getString(R.string.key_pref_pincode_time_et), 1);
                }
            } catch (NumberFormatException ex) {
                //Alert user
                UtilsView.alert(getActivity(), getString(R.string.msg_is_not_valid, getString(R.string.str_pincode)));
                // set to default value
                PreferenceService.from(getActivity()).putVal(getString(R.string.key_pref_pincode_time_et), 3);
            }
        }
    }




    @Override
    public void onStop() {
        //disable the pincode lock if pincode is empty
        if(pincodeET.getText().equals(PreferenceService.DEF_PASSCODE))
            pincodeEnableCB.setChecked(false);
        super.onStop();
    }
}
