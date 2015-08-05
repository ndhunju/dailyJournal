package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
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

    //Constants
    private static final int REQUEST_JOURNAL_CHGD = 5457;
    private static final int REQUEST_PARTY_INFO_CHGD = 5793;

    //Variables
	int mPartyId;
	TextView balanceTV;
	LinearLayout tableLL;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_party);

        //Get the mParty id
		mPartyId = getIntent().getIntExtra(Utils.KEY_PARTY_ID, 0);

        //Wire up the widgets/view
		balanceTV = (TextView) findViewById(R.id.activity_party_balance_tv);
		tableLL = (LinearLayout)findViewById(R.id.activity_party_ll);

        //Get the Party object and set values
		Party party = Storage.getInstance(PartyActivity.this).getParty(mPartyId);

		double balance = party.calculateBalances();
		balanceTV.setText(Utils.formatCurrency(balance));
		balanceTV.setTextColor(balance > 0 ? Color.parseColor(Utils.RED): Color.parseColor(Utils.GREEN));

		tableLL.addView(createLedgerView(party));

		setTitle(party.getName());

        //Alert the user if balance is negative
		/*if (balance > 0)
			Utils.alert(PartyActivity.this,	String.format(getString(R.string.warning_overpaid_msg), mParty.getName(),
					getString(R.string.currency_type), balance));*/

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
            case REQUEST_JOURNAL_CHGD:

                if(!data.getBooleanExtra(Utils.KEY_JOURNAL_CHGD, false))
                    return;

                //Since the Journal was changed, you need to get a fresh copy of mParty from Storage to reflect the change
                Party party = Storage.getInstance(PartyActivity.this).getParty(mPartyId);

                double balance = party.calculateBalances();
                balanceTV.setText(Utils.formatCurrency(balance));
                balanceTV.setTextColor(balance > 0 ? Color.parseColor(Utils.RED) : Color.parseColor(Utils.GREEN));

                tableLL.removeAllViews();
                tableLL.addView(createLedgerView(party));

                //Alert the user if balance is negative
                /*if (balance > 0)
                    Utils.alert(PartyActivity.this,	String.format(getString(R.string.warning_overpaid_msg), mParty.getName(),
                            getString(R.string.currency_type), balance));*/
                break;

            case REQUEST_PARTY_INFO_CHGD:

                if(!data.getBooleanExtra(Utils.KEY_PARTY_INFO_CHGD, false))
                    return;

                //Party information was changed, update the title
                //Party party2 = Storage.getInstance(PartyActivity.this).getParty(mPartyId); //Results in more overhead
                setTitle(data.getStringExtra(Utils.KEY_PARTY_NAME));
                break;
		}
	}

    /**
     * This method creates a Ledger(Table) of the passed Party object. It takes balance of the mParty as well since
     * the balance is usually already calculated and calculateBalance() increases overhead
     * @param party
     * @return
     */
	public TableLayout createLedgerView(Party party) {

		TableLayout ledgerTL = new TableLayout(PartyActivity.this);
		ledgerTL.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
		ledgerTL.addView(ViewUtils.createLedgerHeader(PartyActivity.this));

		for (final Journal j : party.getJournals()) {
			ledgerTL.addView(ViewUtils.createJournalRow(PartyActivity.this, j,
					new OnClickListener() {
						@Override
						public void onClick(View v) {
							//Open journal activity to show the detail info of the clicked Journal
							Intent i = new Intent(PartyActivity.this,JournalActivity.class);
							i.putExtra(Utils.KEY_JOURNAL_ID,j.getId());
							i.putExtra(Utils.KEY_PARTY_ID, j.getPartyId());
							startActivityForResult(i, REQUEST_JOURNAL_CHGD);
						}
					}));
		}

		ledgerTL.addView(ViewUtils.createLedgerFooter(PartyActivity.this, party.getDebitTotal(), party.getCreditTotal()));

		return ledgerTL;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();
		getMenuInflater().inflate(R.menu.menu_party_activity, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch(item.getItemId()){
			case R.id.menu_party_activity_info:
                //Create intent to pass current mParty id to PartyInformationActivity
				Intent i = new Intent(PartyActivity.this,PartyInformationActivity.class);
				i.putExtra(Utils.KEY_PARTY_ID, mPartyId);
				startActivityForResult(i, REQUEST_PARTY_INFO_CHGD);
				break;

			case R.id.menu_party_activity_delete:
                //User wants to delete the Party but first confirm the deletion
				String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_party));
				Utils.alert(PartyActivity.this, msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						boolean success = Storage.getInstance(PartyActivity.this).deleteParty(mPartyId);
						String msg1 = success ? String.format(getString(R.string.msg_deleted), getString(R.string.str_party))
								: String.format(getString(R.string.msg_failed), getString(R.string.str_delete));
						Utils.toast(PartyActivity.this, msg1);
						PartyActivity.this.finish();
					}
				}, null);
				break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();

		//update pass code time
		LockScreenActivity.updatePasscodeTime();
	}

	@Override
	protected void onResume() {
		super.onResume();

		//check pass code
		LockScreenActivity.checkPassCode(PartyActivity.this);
	}
}
