package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsView;

public class PartyListDialog extends DialogFragment {

	//Variables
	private RecyclerView partyLV ;
	private EditText srchPartyET;
	private PartyCardAdapter partyAdapter;

	private Services mServices;

    public static PartyListDialog newInstance(int requestCode){
        PartyListDialog pld = new PartyListDialog();
        Bundle arg = new Bundle();
        arg.putInt(Constants.KEY_REQUEST_CODE, requestCode);
        pld.setArguments(arg);
        return pld;
    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View view = inflater.inflate(R.layout.fragment_party_list, container);
		mServices = Services.getInstance(getActivity());

		//Wire up widgets
		srchPartyET = (EditText)view.findViewById(R.id.fragment_party_list_search_et);
		srchPartyET.addTextChangedListener(new TextWatcher() {
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				partyAdapter.filter(s); //filter the list below
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,	int after) {}
			@Override
			public void afterTextChanged(Editable s) {}
		});

		//Populate the list view with existing Parties
		partyLV = (RecyclerView) view.findViewById(R.id.fragment_party_list_party_list);
		partyLV.setLayoutManager(new LinearLayoutManager(getContext()));
        partyAdapter = new PartyCardAdapter(getActivity(), mServices.getParties());
		partyLV.setAdapter(partyAdapter);
		partyAdapter.setOnItemClickListener(new PartyCardAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(View view, int position, long id) {
				Intent i = new Intent();
				i.putExtra(Constants.KEY_PARTY_ID, partyAdapter.getItem(position).getId());
				((OnDialogBtnClickedListener)getTargetFragment()).onDialogBtnClicked(i,
						OnDialogBtnClickedListener.BUTTON_NEUTRAL, Activity.RESULT_OK, getArguments().getInt(Constants.KEY_REQUEST_CODE));
			}

			@Override
			public void onContextItemClick(View view, int position, long id) {
				PartyListFragment.onContextItemClick(getActivity(), partyAdapter, view, position, id);
			}
		});

        //When user clicks on Add Party Button, add the mParty to the list and return its ID
		view.findViewById(R.id.fragment_party_list_add_party_btn).setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				String name = srchPartyET.getText().toString();
				Party addedParty = mServices.addParty(name);
				UtilsView.toast(getActivity(), name + " saved. ");

				Intent i = new Intent();
				i.putExtra(Constants.KEY_PARTY_ID, addedParty.getId());
				((OnDialogBtnClickedListener)getTargetFragment()).onDialogBtnClicked(i,
						OnDialogBtnClickedListener.BUTTON_POSITIVE, Activity.RESULT_OK,
						getArguments().getInt(Constants.KEY_REQUEST_CODE));

              }
		});

		((FloatingActionButton)view.findViewById(R.id.fragment_party_list_fab)).setVisibility(View.INVISIBLE);

        mServices.registerPartyObserver(partyAdapter);

		return view;
	}

    @Override
    public void onDestroyView() {
        mServices.unregisterPartyObserver(partyAdapter);
        super.onDestroyView();
    }
}
