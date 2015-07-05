package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;
import com.ndhunju.dailyjournal.model.ViewUtils;

public class PartyActivity extends FragmentActivity {

	int mPartyId;
	TextView balanceTV;
	LinearLayout tableLL;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_party);

		mPartyId = getIntent().getIntExtra(PartyListDialog.KEY_PARTY_ID, 0);
		balanceTV = (TextView) findViewById(R.id.activity_party_balance_tv);
		tableLL = (LinearLayout)findViewById(R.id.activity_party_ll);

		Party party = Storage.getInstance(PartyActivity.this).getParty(mPartyId);
		
		Button infoBtn = (Button) findViewById(R.id.activity_party_info_btn);
		infoBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent i = new Intent(PartyActivity.this,PartyInformationActivity.class);
				i.putExtra(PartyListDialog.KEY_PARTY_ID, mPartyId);
				startActivity(i); //for result? <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
			}
		});
		
		((Button)findViewById(R.id.activity_party_delete_btn)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Storage.getInstance(PartyActivity.this).deleteParty(mPartyId);
				Utils.toast(PartyActivity.this, getString(R.string.str_party_deleted));
				PartyActivity.this.finish();
			}
		});
		
		double balance = party.getBalance();
		balanceTV.setText(String.valueOf(balance));
		balanceTV.setTextColor(balance > 0 ? Color.parseColor(Utils.RED): Color.parseColor(Utils.GREEN)); // red : green
		tableLL.addView(createLedgerView(party));
		setTitle(party.getName());
		if (balance > 0)
			Utils.alert(PartyActivity.this,	String.format(getString(R.string.warning_overpaid_msg), party.getName(),
					getString(R.string.currency_type), balance));

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
		case Utils.REQUEST_JOURNAL_CHGD:
			
			if(!data.getBooleanExtra(Utils.NAME_JOURNAL_CHGD, false))
				return;
			//You need to get a fresh copy of party from Storage 
			Party party = Storage.getInstance(PartyActivity.this).getParty(mPartyId);
			double balance = party.getBalance();
			balanceTV.setText(String.valueOf(balance));
			balanceTV.setTextColor(balance > 0 ? Color.parseColor("#f63752"): Color.parseColor("#5CB85C")); // red : green
			tableLL.removeAllViews();
			tableLL.addView(createLedgerView(party));
			if (balance > 0)
				Utils.alert(PartyActivity.this,	String.format(getString(R.string.warning_overpaid_msg), party.getName(),
						getString(R.string.currency_type), balance));
		}
	}
	
	public TableLayout createLedgerView(Party party) {

		TableLayout ledgerTL = new TableLayout(PartyActivity.this);
		ledgerTL.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		ledgerTL.addView(ViewUtils.createLedgerHeader(PartyActivity.this));

		for (final Journal j : party.getJournals()) {
			ledgerTL.addView(ViewUtils.createJournalRow(PartyActivity.this, j,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							// open journal activity to show the info
							Intent i = new Intent(PartyActivity.this,JournalActivity.class);
							i.putExtra(Utils.KEY_JOURNAL_ID,j.getId());
							i.putExtra(Utils.KEY_PARTY_ID, j.getPartyId());
							startActivityForResult(i, Utils.REQUEST_JOURNAL_CHGD);
						}
					}));
		}

		ledgerTL.addView(ViewUtils.createLedgerFooter(PartyActivity.this, party.getBalance()));

		return ledgerTL;
	}

}
