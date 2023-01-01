package com.ndhunju.dailyjournal.controller.preference;

import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.View;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

/**
 * Created by dhunju on 10/9/2015.
 */
public class FormatPreferenceFragment extends PreferenceFragment {

    public static final String TAG = FormatPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(PreferenceService.DEF_NAME_SHARED_PREFERENCE);

        addPreferencesFromResource(R.xml.preference_format);

        getActivity().setTitle(getString(R.string.str_format));
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        view.setBackgroundColor(ContextCompat.getColor(view.getContext(), R.color.colorBackground));
    }
}
