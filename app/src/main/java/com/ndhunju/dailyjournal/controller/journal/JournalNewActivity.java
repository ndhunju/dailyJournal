package com.ndhunju.dailyjournal.controller.journal;

import android.os.Bundle;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.JournalPagerFragment;
import com.ndhunju.dailyjournal.controller.NavDrawerActivity;
import com.ndhunju.dailyjournal.controller.fragment.AppRater;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.LockService;

public class JournalNewActivity extends NavDrawerActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

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
	}

	@Override
	protected void onResume() {
		super.onResume();

		askUserToRate();

		//check pass code
		LockService.checkPassCode(JournalNewActivity.this);
	}

	@Override
	protected void onPause() {
		LockService.updatePasscodeTime();
		super.onPause();
	}

	private void askUserToRate(){
		//ask users to rate the app
		AppRater rater = new AppRater(JournalNewActivity.this);
		rater.setLaunchesBeforePrompt(20);
		rater.setPhrases(R.string.msg_rate_title, R.string.msg_rate_body, R.string.str_rate,
				R.string.str_later, R.string.str_no_thanks);
		rater.show();
	}
}
