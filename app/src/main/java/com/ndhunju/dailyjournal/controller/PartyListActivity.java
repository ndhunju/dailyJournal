package com.ndhunju.dailyjournal.controller;

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
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;

public class PartyListActivity extends FragmentActivity {

	ListView partyLV ;
	ArrayAdapter<Party> partyAdapter;
	EditText srchPartyET;
	Storage storage;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.fragment_party_list);
		setTitle("Party List");
		
		storage = Storage.getInstance(PartyListActivity.this);
		
		srchPartyET = (EditText)findViewById(R.id.fragment_party_list_search_et);
		srchPartyET.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				//filter the list below
				partyAdapter.getFilter().filter(s);
			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		
		partyLV = (ListView)findViewById(R.id.fragment_party_list_party_list_lv);
		/*merchantAdapter = new ArrayAdapter<Merchant>(this, android.R.layout.simple_list_item_1, storage.getMerchants());
		merchantLV.setAdapter(merchantAdapter);*/
		partyLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent(PartyListActivity.this, PartyActivity.class);
				i.putExtra(PartyListDialog.KEY_PARTY_ID	, partyAdapter.getItem(position).getId()); //ids array is parallel
				startActivity(i);
			}

		}); 
		
		((Button)findViewById(R.id.fragment_party_list_add_party_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = srchPartyET.getText().toString();
				Party newParty = new Party(name, Party.incrementCurrentId());
				Storage.getInstance(PartyListActivity.this).addParty(newParty);
				Utils.toast(PartyListActivity.this, name + " saved.");
				
				
				Intent i = new Intent(PartyListActivity.this, PartyActivity.class);
				i.putExtra(PartyListDialog.KEY_PARTY_ID	, newParty.getId());
				startActivity(i);
			}
		});

				
	}
	
	@Override
	protected void onPause() {
		Storage.getInstance(PartyListActivity.this).writeToDB();
		super.onPause();
	}
	
	@Override
	protected void onResume() {
		partyAdapter = new ArrayAdapter<Party>(this, android.R.layout.simple_list_item_1, storage.getParties());
		partyLV.setAdapter(partyAdapter);
		super.onResume();
	}
}
