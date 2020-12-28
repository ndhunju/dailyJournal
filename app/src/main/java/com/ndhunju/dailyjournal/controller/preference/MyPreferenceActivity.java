package com.ndhunju.dailyjournal.controller.preference;

import android.app.FragmentTransaction;
import android.os.Bundle;

import androidx.annotation.Nullable;
import android.view.MenuItem;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.backup.TransferOldDataAsyncTask;
import com.ndhunju.dailyjournal.service.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 10/8/2015.
 * This class reads Preference Header from .xml file
 */
public class MyPreferenceActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_preference);

        // Setup Action Bar
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        setTitle(getString(R.string.str_preference));

        if (savedInstanceState == null) {
            getFragmentManager().beginTransaction().add(
                    R.id.content_frame,
                    new MyPreferenceFragment(), MyPreferenceFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

    @Override
    protected void onResume() {
        // Check if this activity was called to import the old data ( data saved by v3.1)
        boolean importOldData = getIntent().getBooleanExtra(Constants.KEY_IMPORT_OLD_DATA, false);
        if (importOldData) new TransferOldDataAsyncTask(MyPreferenceActivity.this).execute();

        super.onResume();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
