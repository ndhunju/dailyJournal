package com.ndhunju.dailyjournal.controller;

import java.util.Locale;

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
	
	Party merchant ;
	EditText nameET;
	EditText phoneET;
	Spinner typeSpinner;
	Storage mStorage;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_party_information);
		
		int merchantId = getIntent().getIntExtra(PartyListDialog.KEY_PARTY_ID, Utils.NO_PARTY);
		mStorage =  Storage.getInstance(this);
		merchant = mStorage.getParty(merchantId);
		
		setTitle(merchant.getName());
		
		TextView idTV = (TextView)findViewById(R.id.activity_merchant_info_id_tv);
		idTV.setText(Utils.getStringId(merchant.getId()));
		
		nameET = (EditText)findViewById(R.id.activity_merchant_info_name_et);
		nameET.setText(merchant.getName());
		
		phoneET = (EditText)findViewById(R.id.activity_merchant_info_phone_et);
		phoneET.setText(merchant.getPhone());
		
		typeSpinner = (Spinner)findViewById(R.id.activity_merchant_info_type_spinner);
		String[] merchantTypes = new String[Type.values().length];
		for(int i = 0; i < Type.values().length ; i++)
			merchantTypes[i] = Type.values()[i].toString();
		
		typeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, merchantTypes ));
		typeSpinner.setSelection(merchant.getType().ordinal());
		
		Button okBtn = (Button)findViewById(R.id.activity_merchant_info_ok_btn);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				//if the name is changed, put the merchant in right place with alphabetical order
				boolean nameChanged = false;
				if(!merchant.getName().toLowerCase(Locale.getDefault()).equals(nameET.getText().toString().toLowerCase(Locale.getDefault())))
					nameChanged = true;
				merchant.setName(nameET.getText().toString());
				merchant.setPhone(phoneET.getText().toString());
				merchant.setType(Party.Type.valueOf(typeSpinner.getSelectedItem().toString()));
				if(nameChanged){
					 mStorage.getParties().remove(merchant);
					 mStorage.addParty(merchant);
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
