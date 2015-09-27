package com.ndhunju.dailyjournal.controller.Party;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.Journal.JournalActivity;
import com.ndhunju.dailyjournal.controller.LockScreenActivity;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFormat;
import com.ndhunju.dailyjournal.service.UtilsView;

public class PartyActivity extends FragmentActivity {

    //Constants
    private static final int REQUEST_JOURNAL_CHGD = 5457;
    private static final int REQUEST_PARTY_INFO_CHGD = 5793;

    //Variables
	long mPartyId;
	TextView balanceTV;
	ListView ledgerListView;
	LedgerAdapter ledgerAdapter;

	Services mServices;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_party);

        //Get the mParty id
		mPartyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, 0);

        //Wire up the widgets/view
		balanceTV = (TextView) findViewById(R.id.activity_party_balance_tv);
		ledgerListView = (ListView)findViewById(R.id.activity_party_ll);

		mServices = Services.getInstance(PartyActivity.this);

        //Get the Party object and set values
		Party party = mServices.getParty(mPartyId);

		double balance = party.calculateBalances();
		balanceTV.setText(UtilsFormat.formatCurrency(balance));
		balanceTV.setTextColor(balance > 0 ? Color.parseColor(Constants.RED) : Color.parseColor(Constants.GREEN));

		ledgerAdapter = new LedgerAdapter(getBaseContext(), mServices.getJournals(mPartyId));
		ledgerListView.setAdapter(ledgerAdapter);
		ledgerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //Open journal activity to show the detail info of the clicked Journal
                Intent intent = new Intent(PartyActivity.this, JournalActivity.class);
                intent.putExtra(Constants.KEY_JOURNAL_ID, l);
                intent.putExtra(Constants.KEY_PARTY_ID, mPartyId);
                startActivityForResult(intent, REQUEST_JOURNAL_CHGD);
            }
        });

		ledgerListView.addFooterView(getFooterRow(party));

		setTitle(party.getName());

        //Alert the user if balance is negative

	}

    private TableRow getFooterRow(Party party) {
        TableRow footerRow = (TableRow)getLayoutInflater().inflate(R.layout.ledger_row, null);
        TextView col0 = (TextView) footerRow.findViewById(R.id.ledger_row_col0);
        TextView col1 = (TextView) footerRow.findViewById(R.id.ledger_row_col1);
        TextView col2 = (TextView) footerRow.findViewById(R.id.ledger_row_col2);
        TextView col3 = (TextView) footerRow.findViewById(R.id.ledger_row_col3);
        TextView col4 = (TextView) footerRow.findViewById(R.id.ledger_row_col4);

        col1.setText("");
        col2.setText(getString(R.string.str_total));
        col3.setText(UtilsFormat.formatCurrency(party.getDebitTotal()));
        col4.setText(UtilsFormat.formatCurrency(party.getCreditTotal()));
        col0.setBackgroundDrawable(getResources().getDrawable(R.drawable.heading_shape));
        col1.setBackgroundDrawable(getResources().getDrawable(R.drawable.heading_shape));
        col2.setBackgroundDrawable(getResources().getDrawable(R.drawable.heading_shape));
        col3.setBackgroundDrawable(getResources().getDrawable(R.drawable.heading_shape));
        col4.setBackgroundDrawable(getResources().getDrawable(R.drawable.heading_shape));
        return footerRow;
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (resultCode != Activity.RESULT_OK)
			return;

		switch (requestCode) {
            case REQUEST_JOURNAL_CHGD:

                if(!data.getBooleanExtra(Constants.KEY_JOURNAL_CHGD, false))
                    return;

                //Since the Journal was changed, you need to get a fresh copy of mParty from Services to reflect the change
                Party party = mServices.getParty(mPartyId);

                double balance = party.calculateBalances();
                balanceTV.setText(UtilsFormat.formatCurrency(balance));
                balanceTV.setTextColor(balance > 0 ? Color.parseColor(Constants.RED) : Color.parseColor(Constants.GREEN));

                ledgerAdapter = new LedgerAdapter(getBaseContext(), mServices.getJournals(mPartyId));
                ledgerListView.setAdapter(ledgerAdapter);

                //Alert the user if balance is negative
                break;

            case REQUEST_PARTY_INFO_CHGD:

                if(!data.getBooleanExtra(Constants.KEY_PARTY_INFO_CHGD, false))
                    return;

                //Party information was changed, update the title
                setTitle(data.getStringExtra(Constants.KEY_PARTY_NAME));
                break;
		}
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
				i.putExtra(Constants.KEY_PARTY_ID, mPartyId);
				startActivityForResult(i, REQUEST_PARTY_INFO_CHGD);
				break;

			case R.id.menu_party_activity_delete:
                //User wants to delete the Party but first confirm the deletion
				String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_party));
				UtilsView.alert(PartyActivity.this, msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						boolean success = Services.getInstance(PartyActivity.this).deleteParty(mPartyId);
						String msg1 = success ? String.format(getString(R.string.msg_deleted), getString(R.string.str_party))
								: String.format(getString(R.string.msg_failed), getString(R.string.str_delete));
						UtilsView.toast(PartyActivity.this, msg1);
						PartyActivity.this.finish();
					}
				}, null);
				break;

            case R.id.menu_party_activity_share:
                new ReportGeneratorAsync(PartyActivity.this).execute(mPartyId);
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
