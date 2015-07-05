package com.ndhunju.dailyjournal.controller;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;

public class PartyListDialog extends DialogFragment {
	
	public static final int SELECTED_PARTY_CODE = 31;
	public static final String KEY_PARTY_ID = Utils.APP_PREFIX + "PartyList.partyIdKey"; 
	
	ListView partyLV ;
	ArrayAdapter<Party> partyAdapter;
	EditText srchPartyET;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_party_list, container);
		Storage storage = Storage.getInstance(getActivity());
		
		srchPartyET = (EditText)v.findViewById(R.id.fragment_party_list_search_et);
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
		
		
		partyLV = (ListView)v.findViewById(R.id.fragment_party_list_party_list_lv);
		partyAdapter = new ArrayAdapter<Party>(getActivity(), android.R.layout.simple_list_item_1, storage.getParties());
		partyLV.setAdapter(partyAdapter);
		
		partyLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent();
				i.putExtra(KEY_PARTY_ID	, partyAdapter.getItem(position).getId());
				((JournalFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_home_journal_fl)).OnDialogPressedOk(i, SELECTED_PARTY_CODE);				
			}
		}); 
		
		((Button)v.findViewById(R.id.fragment_party_list_add_party_btn)).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String name = srchPartyET.getText().toString();
				Party newParty = new Party(name, Storage.getInstance(getActivity()).getNextPartyId());
				Storage storage = Storage.getInstance(getActivity());
				storage.addParty(newParty);
				Utils.toast(getActivity(), name + " saved.");
				
				Intent i = new Intent();
				i.putExtra(KEY_PARTY_ID	, newParty.getId() );
				((JournalFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_home_journal_fl)).OnDialogPressedOk(i, SELECTED_PARTY_CODE);		
				
			}
		});

				
		return v;
	}
	
}
