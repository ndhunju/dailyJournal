package com.ndhunju.dailyjournal.controller.preference;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.backup.BackupPreferenceFragment;
import com.ndhunju.dailyjournal.controller.backup.TransferOldDataAsyncTask;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.LockService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 10/8/2015.
 * This class reads Preference Header from .xml file
 */
public class MyPreferenceActivity extends AppCompatPreferenceActivity {

    //Variables
    public static List<Header> headers = new ArrayList<>();
    private ListAdapter preferenceListAdapter;

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
        headers.clear();
        for (Header header : target) {
            headers.add(header);
        }

        setContentView(R.layout.activity_preference);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar bar = getSupportActionBar();
        bar.setHomeButtonEnabled(true);
        bar.setDisplayHomeAsUpEnabled(true);
        bar.setDisplayShowTitleEnabled(true);
        bar.setTitle(getString(R.string.str_preference));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null){
            preferenceListAdapter = new PreferenceListAdapter(headers, MyPreferenceActivity.this);
            setListAdapter(preferenceListAdapter);
        }

    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {


        if (headers.get(position).id == R.id.preference_header_rate) {
            //user clicked on Rate App option
            final String DEFAULT_TARGET_URI = "market://details?id=%s";
            Intent appRateIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format(DEFAULT_TARGET_URI, MyPreferenceActivity.this.getPackageName())));
            startActivityForResult(appRateIntent, 0);
        } else

        {
            //for rest use the default settings
            super.onListItemClick(l, v, position, id);
        }

    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        for(Header header : headers){
            if(fragmentName.equals(header.fragment))
                return true;
        }
        return false;
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Update lock time
        LockService.updatePasscodeTime();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Set the title
        setTitle(getString(R.string.str_preference));

        //Check if this activity was called to import the old data ( data saved by v3.1)
        boolean importOldData = getIntent().getBooleanExtra(Constants.KEY_IMPORT_OLD_DATA, false);
        if (importOldData) new TransferOldDataAsyncTask(MyPreferenceActivity.this).execute();

        //check pass code
        LockService.checkPassCode(MyPreferenceActivity.this);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        switch(requestCode){
            case BackupPreferenceFragment.REQUEST_CODE_GDRIVE_PICKER:
            case BackupPreferenceFragment.REQUEST_CODE_GDRIVE_RESOLUTION:
                //forward it to {@link BackupPreferenceFragment}
                getFragmentManager().findFragmentByTag(BackupPreferenceFragment.TAG)
                        .onActivityResult(requestCode, resultCode, data);
                break;
        }
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
