package com.ndhunju.dailyjournal.controller;

import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Party.Type;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;

public class PartyInformationActivity extends FragmentActivity {

	//Variables
	Party mParty;
	EditText nameET;
	Storage mStorage;
	EditText  phoneET;
	Spinner typeSpinner;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_party_information);

		//Get the party id
		int partyId = getIntent().getIntExtra(Utils.KEY_PARTY_ID, Utils.NO_PARTY);
		mStorage =  Storage.getInstance(this);
		mParty = mStorage.getParty(partyId);
		
		setTitle(mParty.getName());

		//Wire up widgets
		TextView idTV = (TextView)findViewById(R.id.activity_merchant_info_id_tv);
		idTV.setText(Utils.getStringId(mParty.getId(),Utils.NUM_OF_DIGITS));
		
		nameET = (EditText)findViewById(R.id.activity_merchant_info_name_et);
		nameET.setText(mParty.getName());
		
		phoneET = (EditText)findViewById(R.id.activity_merchant_info_phone_et);
		phoneET.setText(mParty.getPhone());
		
		typeSpinner = (Spinner)findViewById(R.id.activity_merchant_info_type_spinner);
		String[] merchantTypes = new String[Type.values().length];
		for(int i = 0; i < Type.values().length ; i++)
			merchantTypes[i] = Type.values()[i].toString();
		
		typeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, merchantTypes ));
		typeSpinner.setSelection(mParty.getType().ordinal());
		
		Button okBtn = (Button)findViewById(R.id.activity_merchant_info_ok_btn);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//if the name is changed, put the party in right place with alphabetical order
				boolean nameChanged = false;
				if(!mParty.getName().toLowerCase(Locale.getDefault()).equals(nameET.getText().toString().toLowerCase(Locale.getDefault())))
					nameChanged = true;

				mParty.setName(nameET.getText().toString());
				mParty.setPhone(phoneET.getText().toString());
				mParty.setType(Party.Type.valueOf(typeSpinner.getSelectedItem().toString()));
				if (nameChanged) {
                    //Remove the party and add it again so that it is placed in right order
					mStorage.getParties().remove(mParty);
					mStorage.addParty(mParty);
                    //Let the previous activity know that party information was changed
					Intent i = new Intent();
					i.putExtra(Utils.KEY_PARTY_INFO_CHGD, true);
					i.putExtra(Utils.KEY_PARTY_NAME, mParty.getName());
					setResult(Activity.RESULT_OK, i);
				}
				PartyInformationActivity.this.finish();
			}
		});
	}
	
	@Override
	protected void onPause() {
		Storage.getInstance(PartyInformationActivity.this).writeToDB();
		super.onPause();
	}

}
