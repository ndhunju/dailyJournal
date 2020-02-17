package com.ndhunju.dailyjournal.controller.erase;

import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.core.content.ContextCompat;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.EraseAllAsyncTask;
import com.ndhunju.dailyjournal.controller.backup.EraseJournalsAsyncTask;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsView;

/**
 * Created by ndhunju on 9/4/17.
 * This fragment group together all the tools relevant to erasing data.
 */

public class ErasePreferenceFragment extends PreferenceFragment {

    public static final String TAG = ErasePreferenceFragment.class.getSimpleName();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(PreferenceService.DEF_NAME_SHARED_PREFERENCE);

        addPreferencesFromResource(R.xml.preference_erase);

        findPreference(getString(R.string.key_pref_erase_all))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.msg_erase_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new EraseAllAsyncTask(getActivity()).execute();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        return true;
                    }
                });

        findPreference(getString(R.string.key_pref_erase_journals))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.msg_erase_journals), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new EraseJournalsAsyncTask(getActivity()).execute();
                            }
                        }, null);
                        return true;
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        //block the previous view
        getView().setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
    }

}
