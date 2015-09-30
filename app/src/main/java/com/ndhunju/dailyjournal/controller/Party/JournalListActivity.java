package com.ndhunju.dailyjournal.controller.Party;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
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
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFormat;
import com.ndhunju.dailyjournal.service.UtilsView;

public class JournalListActivity extends FragmentActivity {

    //Constants
    private static final int REQUEST_JOURNAL_CHGD = 5457;
    private static final int REQUEST_PARTY_INFO_CHGD = 5793;

	//Context menu id
	private static final int CONTEXT_MENU_DELETE = 0;
	private static final int CONTEXT_MENU_EDIT = 1;

    //Variables
	Party mParty;
	TextView balanceTV;
	ListView ledgerListView;
	LedgerAdapter ledgerAdapter;

	Services mServices;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_list_journal);

        //Get the mParty id
		long mPartyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, 0);

        //Wire up the widgets/view
		balanceTV = (TextView) findViewById(R.id.activity_party_balance_tv);
		ledgerListView = (ListView)findViewById(R.id.activity_party_ll);

		mServices = Services.getInstance(JournalListActivity.this);

        //Get the Party object and set values
		mParty = mServices.getParty(mPartyId);

		double balance = mParty.calculateBalances();
		balanceTV.setText(UtilsFormat.formatCurrency(balance));
		balanceTV.setTextColor(balance > 0 ? Color.parseColor(Constants.RED) : Color.parseColor(Constants.GREEN));

		ledgerAdapter = new LedgerAdapter(getBaseContext(), mServices.getJournals(mPartyId));
		ledgerListView.setAdapter(ledgerAdapter);
		ledgerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id) {
				createJournalIntent(id);
			}
		});

		ledgerListView.setOnCreateContextMenuListener(this);

		ledgerListView.addFooterView(getFooterRow(mParty));

		setTitle(mParty.getName());

        //Alert the user if balance is negative

	}

	private void createJournalIntent(long id) {
		//Open journal activity to show the detail info of the clicked Journal
		Intent intent = new Intent(JournalListActivity.this, JournalActivity.class);
		intent.putExtra(Constants.KEY_JOURNAL_ID, id);
		intent.putExtra(Constants.KEY_PARTY_ID, mParty.getId());
		startActivityForResult(intent, REQUEST_JOURNAL_CHGD);
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


                double balance = mParty.calculateBalances();
                balanceTV.setText(UtilsFormat.formatCurrency(balance));
                balanceTV.setTextColor(balance > 0 ? Color.parseColor(Constants.RED) : Color.parseColor(Constants.GREEN));

                ledgerAdapter = new LedgerAdapter(getBaseContext(), mServices.getJournals(mParty.getId()));
                ledgerListView.setAdapter(ledgerAdapter);

                //Alert the user if balance is negative
                break;

            case REQUEST_PARTY_INFO_CHGD:

                if(!data.getBooleanExtra(Constants.KEY_PARTY_INFO_CHGD, false))
                    return;

                Constants.ChangeType type = (Constants.ChangeType) data.getSerializableExtra(Constants.KEY_CHANGE_TYPE);

                Intent intent = new Intent();
                intent.putExtra(Constants.KEY_PARTY_INFO_CHGD, true);

                switch (type){
                    case EDITED:
                        //Party information was changed, update the title
                        setTitle(data.getStringExtra(Constants.KEY_PARTY_NAME));
                        intent.putExtra(Constants.KEY_CHANGE_TYPE, Constants.ChangeType.EDITED);
                        setResult(Activity.RESULT_OK, intent); //relay it to the parent activity
                        break;
                    case DELETED:
                        //close this activity and relay the deletion to parent activity
                        intent.putExtra(Constants.KEY_CHANGE_TYPE, Constants.ChangeType.DELETED );
                        setResult(Activity.RESULT_OK, intent);
                        finish();
                        break;

                }

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
                //Create intent to pass current mParty id to PartyActivity
				Intent i = new Intent(JournalListActivity.this,PartyActivity.class);
				i.putExtra(Constants.KEY_PARTY_ID, mParty.getId());
				startActivityForResult(i, REQUEST_PARTY_INFO_CHGD);
				break;

			case R.id.menu_party_activity_delete:
                //User wants to delete the Party but first confirm the deletion
				String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_party));
				UtilsView.alert(JournalListActivity.this, msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						boolean success = Services.getInstance(JournalListActivity.this).deleteParty(mParty);
						String msg1 = success ? String.format(getString(R.string.msg_deleted), getString(R.string.str_party))
								: String.format(getString(R.string.msg_failed), getString(R.string.str_delete));
						UtilsView.toast(JournalListActivity.this, msg1);
                        Intent intent = new Intent();
                        intent.putExtra(Constants.KEY_PARTY_INFO_CHGD, true);
                        intent.putExtra(Constants.KEY_CHANGE_TYPE, Constants.ChangeType.DELETED );
                        setResult(Activity.RESULT_OK, intent);
						JournalListActivity.this.finish();
					}
				}, null);
				break;

            case R.id.menu_party_activity_share:
                new ReportGeneratorAsync(JournalListActivity.this).execute(mParty.getId());
                break;
		}

		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {


		MenuItem itemEdit = menu.add(0, CONTEXT_MENU_EDIT, 1, getString(R.string.str_edit));
		itemEdit.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_edit));

		MenuItem itemDelete = menu.add(0, CONTEXT_MENU_DELETE, 2, getString(R.string.str_delete));
		itemDelete.setIcon(getResources().getDrawable(android.R.drawable.ic_menu_delete));

	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		int id = item.getItemId();


		//Get the list item position
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		ListView lv = (ListView)info.targetView.getParent();
		final Journal journal = (Journal)lv.getAdapter().getItem(info.position);

		//get the id of select journal
		long journalId = info.id;

		switch (id){
			case CONTEXT_MENU_EDIT:
				createJournalIntent(journalId);
				break;
			case CONTEXT_MENU_DELETE:
				String msg = String.format(getString(R.string.msg_delete_confirm), getString(R.string.str_journal));
				//Alert user before deleting the Journal
				UtilsView.alert(JournalListActivity.this, msg, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						mServices.deleteJournal(journal);
						//remove same journal from the adapter as well rather than reloading
						ledgerAdapter.remove(journal);
						ledgerAdapter.notifyDataSetChanged();
						String msg = String.format(getString(R.string.msg_deleted), getString(R.string.str_journal));
						UtilsView.toast(JournalListActivity.this, msg);
					}
				}, null);

				break;
		}
		return true;
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
		LockScreenActivity.checkPassCode(JournalListActivity.this);
	}
}
