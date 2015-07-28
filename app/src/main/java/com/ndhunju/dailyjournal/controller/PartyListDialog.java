package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
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

	//Variables
	ListView partyLV ;
	EditText srchPartyET;
	ArrayAdapter<Party> partyAdapter;

    public static PartyListDialog newInstance(int requestCode){
        PartyListDialog pld = new PartyListDialog();
        Bundle arg = new Bundle();
        arg.putInt(Utils.KEY_REQUEST_CODE, requestCode);
        pld.setArguments(arg);
        return pld;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_party_list, container);
		Storage storage = Storage.getInstance(getActivity());

		//Wire up widgets
		srchPartyET = (EditText)v.findViewById(R.id.fragment_party_list_search_et);
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
		
		//Populate the list view with existing Parties
		partyLV = (ListView)v.findViewById(R.id.fragment_party_list_party_list_lv);
		partyAdapter = new ArrayAdapter<Party>(getActivity(), android.R.layout.simple_list_item_1, storage.getParties());
		partyLV.setAdapter(partyAdapter);
		partyLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				Intent i = new Intent();
				i.putExtra(Utils.KEY_PARTY_ID, partyAdapter.getItem(position).getId());
				((JournalFragment)getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_home_journal_fl))
                        .OnDialogPressedOk(i, getArguments().getInt(Utils.KEY_REQUEST_CODE));
			}
		}); 

        //When user clicks on Add Party Button, add the mParty to the list and return its ID
		v.findViewById(R.id.fragment_party_list_add_party_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = srchPartyET.getText().toString();
				Party newParty = new Party(name, Party.incrementCurrentId());
				Storage storage = Storage.getInstance(getActivity());
				storage.addParty(newParty);
				Utils.toast(getActivity(), name + " saved.");

				Intent i = new Intent();
				i.putExtra(Utils.KEY_PARTY_ID, newParty.getId());
				((JournalFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.activity_home_journal_fl))
                        .OnDialogPressedOk(i, getArguments().getInt(Utils.KEY_REQUEST_CODE));

            }
		});

				
		return v;
	}
	
}
