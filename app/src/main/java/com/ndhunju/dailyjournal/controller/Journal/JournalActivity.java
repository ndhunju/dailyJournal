package com.ndhunju.dailyjournal.controller.journal;

import android.app.FragmentManager;
import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.LockService;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;

public class JournalActivity extends Activity {

	private static final String TAG_JOURNAL_FRAG = "journalFragment";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//Get Journal and Party ID if passed through Intent
		long journalId = getIntent().getLongExtra(Constants.KEY_JOURNAL_ID, Constants.ID_NEW_JOURNAL);
		long partyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, Constants.NO_PARTY);

		// find the retained fragment on activity restarts
		if(savedInstanceState == null){
			Fragment journalFrag;
			if(journalId != Constants.ID_NEW_JOURNAL){
				 journalFrag = JournalFragment.newInstance(journalId, partyId);
			}else{
				journalFrag = JournalFragmentNew.newInstance();
			}
			getFragmentManager().beginTransaction().replace(R.id.activity_home_journal_fl,
					journalFrag, TAG_JOURNAL_FRAG).commit();
		}
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
