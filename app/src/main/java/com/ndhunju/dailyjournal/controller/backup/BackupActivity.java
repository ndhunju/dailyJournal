package com.ndhunju.dailyjournal.controller.backup;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.NavDrawerActivity;

/** Created by Dhunju on 8/14/2016.
 * This activity groups together tools relevant to backing up. */
public class BackupActivity extends NavDrawerActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().add(R.id.content_frame,
                new BackupPreferenceFragment(), BackupPreferenceFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();


    }
}
