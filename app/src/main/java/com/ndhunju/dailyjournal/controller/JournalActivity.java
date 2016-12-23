package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.journal.JournalFragmentNew;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.LockService;

/**
 * Created by Dhunju on 8/7/2016.
 */
public class JournalActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);
        //Get Journal and Party ID if passed through Intent
        long journalId = getIntent().getLongExtra(Constants.KEY_JOURNAL_ID, Constants.ID_NEW_JOURNAL);
        //long partyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, Constants.NO_PARTY);
        //long currentJournalPos = getIntent().getIntExtra(Constants.KEY_JOURNAL_POS, 0);

        // find the retained fragment on activity restarts
        if(savedInstanceState == null){
            if(journalId != Constants.ID_NEW_JOURNAL){
                JournalPagerFragment journalPagerFragment = new JournalPagerFragment();
                journalPagerFragment.setArguments(getIntent().getExtras());
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame, journalPagerFragment, JournalPagerFragment.TAG).commit();
            }else{
                getSupportFragmentManager().beginTransaction().replace(R.id.content_frame,
                        JournalFragmentNew.newInstance(), JournalFragmentNew.TAG).commit();
            }
        }

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
    }

    @Override
    protected void onResume() {
        super.onResume();

        //check pass code
        LockService.checkPassCode(JournalActivity.this);
    }

    @Override
    protected void onPause() {
        LockService.updatePasscodeTime();
        super.onPause();
    }

}
