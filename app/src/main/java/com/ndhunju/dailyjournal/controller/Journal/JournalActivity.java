package com.ndhunju.dailyjournal.controller.Journal;

import android.os.Bundle;
import android.app.Fragment;
import android.app.Activity;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.LockScreenActivity;
import com.ndhunju.dailyjournal.service.Constants;

public class JournalActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//Get Journal and Party ID if passed through Intent
		long journalId = getIntent().getLongExtra(Constants.KEY_JOURNAL_ID, Constants.ID_NEW_JOURNAL);
		long partyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, Constants.NO_PARTY);

		//Add an instance of JournalFragment
		Fragment jf = JournalFragment.newInstance(journalId, partyId);
		getFragmentManager().beginTransaction().add(R.id.activity_home_journal_fl, jf).commit();

	}

	@Override
	protected void onResume() {
		super.onResume();
		//check pass code
		LockScreenActivity.checkPassCode(JournalActivity.this);
	}
}
