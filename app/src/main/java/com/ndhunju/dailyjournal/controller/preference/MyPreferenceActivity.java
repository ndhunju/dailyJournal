package com.ndhunju.dailyjournal.controller.preference;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
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

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.N_MR1) {
            // setting context view with below method causes crash in Android O
            setContentView(R.layout.activity_preference);
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);

            ActionBar bar = getSupportActionBar();
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowTitleEnabled(true);
            bar.setTitle(getString(R.string.str_preference));
        }
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
            startActivityForResult(Intent.createChooser(appRateIntent, getString(R.string.str_choose)), 0);
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
    protected void onResume() {

        //Set the title
        setTitle(getString(R.string.str_preference));

        //Check if this activity was called to import the old data ( data saved by v3.1)
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
