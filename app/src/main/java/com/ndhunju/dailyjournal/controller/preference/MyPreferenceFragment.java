package com.ndhunju.dailyjournal.controller.preference;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;

import androidx.annotation.Nullable;

import static android.app.FragmentTransaction.*;

public class MyPreferenceFragment extends PreferenceFragment {

    public static final String TAG = MyPreferenceFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(
                PreferenceService.DEF_NAME_SHARED_PREFERENCE
        );

        addPreferencesFromResource(R.xml.preference_headers);

        findPreference(getString(R.string.key_pref_item_rate_app))
                .setOnPreferenceClickListener(preference -> {
                    // User clicked on Rate App option
                    final String DEFAULT_TARGET_URI = "market://details?id=%s";
                    Intent appRateIntent = new Intent(
                            Intent.ACTION_VIEW,
                            Uri.parse(String.format(
                                    DEFAULT_TARGET_URI,
                                    getActivity().getPackageName()
                            ))
                    );

                    startActivityForResult(
                            Intent.createChooser(appRateIntent, getString(R.string.str_choose)),
                            0
                    );

                    return true;
                });

        findPreference(getString(R.string.key_pref_item_format))
                .setOnPreferenceClickListener(preference -> {
                    // User clicked on Format
                    getFragmentManager().beginTransaction()
                            .replace(
                                R.id.content_frame,
                                new FormatPreferenceFragment(),
                                FormatPreferenceFragment.TAG
                            )
                            .addToBackStack(FormatPreferenceFragment.TAG)
                            .setTransition(TRANSIT_FRAGMENT_OPEN).commit();
                    return true;
                });

        findPreference(getString(R.string.key_pref_item_security))
                .setOnPreferenceClickListener(preference -> {
                    // User clicked on Security
                    getFragmentManager().beginTransaction()
                            .replace(
                                    R.id.content_frame,
                                    new SecurityPreferenceFragment(),
                                    SecurityPreferenceFragment.TAG
                            )
                            .addToBackStack(SecurityPreferenceFragment.TAG)
                            .setTransition(TRANSIT_FRAGMENT_OPEN).commit();
                    return true;
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Restore the title which could have changed by other fragments
        getActivity().setTitle(R.string.str_preference);
    }
}
