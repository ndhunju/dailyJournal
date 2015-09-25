package com.ndhunju.dailyjournal.controller.Party;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.LockScreenActivity;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsView;

public class PartyListActivity extends FragmentActivity {

	//Declare variables
	private Services mServices;
	private ListView partyLV;
	private EditText srchPartyET;
	private ArrayAdapter<Party> partyAdapter;

	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_party_list);
		setTitle(getString(R.string.title_activity_party));
		
		mServices = Services.getInstance(PartyListActivity.this);

		//Wire up widgets
		srchPartyET = (EditText)findViewById(R.id.fragment_party_list_search_et);
		srchPartyET.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				partyAdapter.getFilter().filter(s); //filter the list below
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});
		
		partyLV = (ListView)findViewById(R.id.fragment_party_list_party_list_lv);
		partyLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
									int position, long id) {
				Intent i = new Intent(PartyListActivity.this, PartyActivity.class);
				i.putExtra(Constants.KEY_PARTY_ID, partyAdapter.getItem(position).getId()); //ids array is parallel
				startActivity(i);
			}

		}); 

		//When user clicks on Add Party button, create a Party and pass the ID to previous activity
		((Button)findViewById(R.id.fragment_party_list_add_party_btn)).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = srchPartyET.getText().toString();
				Party newParty = mServices.addParty(name);
				UtilsView.toast(PartyListActivity.this, name + " saved.");

				Intent i = new Intent(PartyListActivity.this, PartyActivity.class);
				i.putExtra(Constants.KEY_PARTY_ID, newParty.getId());
				startActivity(i);
			}
		});

				
	}
	
	@Override
	protected void onPause() {
		//Services.getInstance(PartyListActivity.this).writeToDB();
		//update pass code time
		LockScreenActivity.updatePasscodeTime();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		//check pass code
		LockScreenActivity.checkPassCode(PartyListActivity.this);

		partyAdapter = new ArrayAdapter<Party>(this, android.R.layout.simple_list_item_1, mServices.getParties());
		partyLV.setAdapter(partyAdapter);
		super.onResume();
	}

}
