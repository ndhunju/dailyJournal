package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Utils;

public class JournalActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home);

		//Get Journal and Party ID if passed through Intent
		int journalId = getIntent().getIntExtra(Utils.KEY_JOURNAL_ID, Utils.ID_NEW_JOURNAL);
		int partyId = getIntent().getIntExtra(Utils.KEY_PARTY_ID, Utils.NO_PARTY);

		//Add an instance of JournalFragment
		Fragment jf = JournalFragment.newInstance(journalId, partyId);
		getSupportFragmentManager().beginTransaction().add(R.id.activity_home_journal_fl, jf).commit();

	}

	@Override
	protected void onResume() {
		super.onResume();
		//check pass code
		LockScreenActivity.checkPassCode(JournalActivity.this);
	}
}
