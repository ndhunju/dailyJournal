package com.ndhunju.dailyjournal.controller.preference;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.DataSetObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.importExport.TransferOldDataAsyncTask;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.LockService;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 10/8/2015.
 */
public class MyPreferenceActivity extends PreferenceActivity {

    public static List<String> fragments = new ArrayList<>();
    public static List<Header> headers = new ArrayList<>();

    ListAdapter preferenceListAdapter;

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
            final String DEFAULT_TARGET_URI = "market://details?id=%s";
            Intent appRateIntent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(String.format(DEFAULT_TARGET_URI, getActivity().getPackageName())));
            startActivityForResult(appRateIntent, 0);
        } else

        if (headers.get(position).id == R.id.preference_header_share){
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.msg_share_subject));
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.msg_share_body) + " "
                    + getString(R.string.link_app));
            shareIntent.setType("text/plain");
            startActivity(Intent.createChooser(shareIntent, getString(R.string.str_choose)));
        }else

        if(headers.get(position).id == R.id.preference_header_backup_frag){
            getFragmentManager().beginTransaction().replace(android.R.id.content,
                    new BackupPreferenceFragment(), BackupPreferenceFragment.TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .addToBackStack(BackupPreferenceFragment.TAG)
                    .commit();
        }else
        {
            super.onListItemClick(l, v, position, id);
        }

    }

    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.preference_headers, target);
        fragments.clear();
        headers.clear();
        for (Header header : target) {
            fragments.add(header.fragment);
            headers.add(header);
        }
    }


    public Activity getActivity() {
        return MyPreferenceActivity.this;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return fragments.contains(fragmentName);
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
}
