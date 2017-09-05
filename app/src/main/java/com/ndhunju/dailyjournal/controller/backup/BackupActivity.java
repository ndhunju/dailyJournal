package com.ndhunju.dailyjournal.controller.backup;

import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ndhunju.dailyjournal.R;

/** Created by Dhunju on 8/14/2016.
 * This activity groups together tools relevant to backing up. */
public class BackupActivity extends AppCompatActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_backup);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getFragmentManager().beginTransaction().add(R.id.content_frame,
                new BackupPreferenceFragment(), BackupPreferenceFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
