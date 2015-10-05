package com.ndhunju.dailyjournal.controller.party;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.LockService;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.ImportContacts;
import com.ndhunju.dailyjournal.service.Services;

import java.util.ArrayList;
import java.util.List;

public class PartyListActivityOld extends FragmentActivity {

	private static final int REQUEST_PARTY_INFO_CHGD = 135;

	//Declare variables
    private ListView partyLV;
    private Services mServices;
	private EditText srchPartyET;
	private ArrayAdapter<Party> partyAdapter;

	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.fragment_party_list_old);
		setTitle(getString(R.string.title_activity_party));
		
		mServices = Services.getInstance(PartyListActivityOld.this);

		//Wire up widgets
		srchPartyET = (EditText)findViewById(R.id.fragment_party_list_search_et);
		srchPartyET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //filter the list below
                partyAdapter.getFilter().filter(s); }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });
		
		partyLV = (ListView)findViewById(R.id.fragment_party_list_party_list_lv);
		partyLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent i = new Intent(PartyListActivityOld.this, PartyLedgerActivityOld.class);
                i.putExtra(Constants.KEY_PARTY_ID, partyAdapter.getItem(position).getId()); //ids array is parallel
                startActivityForResult(i, REQUEST_PARTY_INFO_CHGD);
            }

        });

		//When user clicks on Add Party button, create a Party and pass the ID to previous activity
		((Button)findViewById(R.id.fragment_party_list_add_party_btn)).setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                startActivity(new Intent(PartyListActivityOld.this, PartyListActivity.class));
                /*String name = srchPartyET.getText().toString();
                Party newParty = mServices.addParty(name);
                UtilsView.toast(PartyListActivityOld.this, name + " saved.");

                Intent i = new Intent(PartyListActivityOld.this, PartyLedgerActivityOld.class);
                i.putExtra(Constants.KEY_PARTY_ID, newParty.getId());
                startActivity(i);*/
            }
        });

        refreshList();

				
	}
	
	@Override
	protected void onPause() {
		//Services.getInstance(PartyListActivityOld.this).writeToDB();
		//update pass code time
		LockService.updatePasscodeTime();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		//check pass code
		LockService.checkPassCode(PartyListActivityOld.this);
		super.onResume();
	}

    @Override
    protected void onStart() {
        super.onStart();
        //clear the search field
        srchPartyET.getText().clear();
    }

    @Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(data == null) return;;

		switch (requestCode){
			case REQUEST_PARTY_INFO_CHGD:
				if(!data.getBooleanExtra(Constants.KEY_PARTY_INFO_CHGD, false))
					return;
                refreshList();
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.clear();;
		getMenuInflater().inflate(R.menu.menu_party_list_activity, menu);
		return  super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		switch (item.getItemId()){
			case R.id.menu_party_list_activity_add:
				Intent newPartyIntent = new Intent(PartyListActivityOld.this, PartyActivity.class);
				startActivityForResult(newPartyIntent, REQUEST_PARTY_INFO_CHGD);
				break;

            case R.id.menu_party_list_activity_import:
                final List<ImportContacts.Contact> contacts = ImportContacts.getContacts(PartyListActivityOld.this);
                final ArrayList<ImportContacts.Contact> importContacts = new ArrayList<>();

                AlertDialog.Builder builder = new AlertDialog.Builder(PartyListActivityOld.this);
                builder.setTitle(getString(R.string.msg_choose, getString(R.string.str_contact)));
                builder.setNegativeButton(getString(android.R.string.cancel), null);
                builder.setMultiChoiceItems(ImportContacts.getNames(contacts), null,
                        new DialogInterface.OnMultiChoiceClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                                //Add checked contacts into importContacts list
                                if (b) importContacts.add(contacts.get(i));
                                else importContacts.remove(contacts.get(i));
                            }
                        });
                builder.setPositiveButton(getString(android.R.string.ok),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new ImportContactsAsync(PartyListActivityOld.this, new ImportContactsAsync.Callback() {
                                    @Override
                                    public void onFinished() {
                                        refreshList();
                                    }
                                })
                                        .execute(importContacts);
                            }
                        });
                AlertDialog selectContactsAD = builder.create();
                selectContactsAD.show();
                break;
		}

		return super.onOptionsItemSelected(item);
	}

    public void refreshList(){
        partyAdapter = new ArrayAdapter<Party>(this, android.R.layout.simple_list_item_1, mServices.getParties());
        partyLV.setAdapter(partyAdapter);
    }
}
