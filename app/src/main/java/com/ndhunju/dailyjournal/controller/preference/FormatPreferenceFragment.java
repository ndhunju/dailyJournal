package com.ndhunju.dailyjournal.controller.preference;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;

/**
 * Created by dhunju on 10/9/2015.
 */
public class FormatPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(PreferenceService.DEF_NAME_SHARED_PREFERENCE);

        addPreferencesFromResource(R.xml.preference_format);

        getActivity().setTitle(getString(R.string.str_format));
    }
}
