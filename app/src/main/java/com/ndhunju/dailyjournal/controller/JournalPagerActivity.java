package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.LockService;

/**
 * Created by Dhunju on 8/7/2016.
 * This activity is responsible for showing already existing journals in a pager.
 */
public class JournalPagerActivity extends AppCompatActivity {

    public static final String BUNDLE_JOURNAL_IDS = JournalPagerActivity.class.getSimpleName() + "journalIds";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        // find the retained fragment on activity restarts
        if(savedInstanceState == null) {
            if(getIntent().getExtras().containsKey(BUNDLE_JOURNAL_IDS)){
                JournalPagerFragment journalPagerFragment = new JournalPagerFragment();
                journalPagerFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, journalPagerFragment, JournalPagerFragment.TAG).commit();
            }else{
                throw new RuntimeException(BUNDLE_JOURNAL_IDS + " key value should be passed to " + JournalPagerActivity.class.getSimpleName());
            }
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check pass code
        LockService.checkPassCode(JournalPagerActivity.this);
    }

    @Override
    protected void onPause() {
        LockService.updatePasscodeTime();
        super.onPause();
    }

}
