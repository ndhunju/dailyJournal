package com.ndhunju.dailyjournal.controller.backup;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;

import android.view.MenuItem;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.CompanySettingsActivity;

import static com.ndhunju.dailyjournal.controller.backup.BackupPreferenceFragment.KEY_MODE;
import static com.ndhunju.dailyjournal.controller.backup.BackupPreferenceFragment.MODE_RESTORE;

/** Created by Dhunju on 8/14/2016.
 * This activity groups together tools relevant to backing up. */
public class BackupActivity extends BaseActivity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_backup);

        setSupportActionBar((Toolbar)findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportFragmentManager().beginTransaction().add(R.id.content_frame,
                new BackupPreferenceFragment(), BackupPreferenceFragment.TAG)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
    }

    @Override
    protected boolean byPassCheckingCompanyInfo() {
        // Bypass checking company info if it is being called by CompanySettingsActivity to restore
        if (getCallingActivity() != null
                && getCallingActivity().getClassName().equals(CompanySettingsActivity.class.getName())
                && MODE_RESTORE.equals(getIntent().getStringExtra(KEY_MODE))) {
            return true;
        }

        return super.byPassCheckingCompanyInfo();
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

    private AppCompatActivity getActivity() {
        return this;
    }
}
