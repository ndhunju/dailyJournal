package com.ndhunju.dailyjournal.controller.Party;

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
import com.ndhunju.dailyjournal.controller.LockScreenActivity;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Party.Type;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFormat;

public class PartyInformationActivity extends FragmentActivity {

	//Variables
	Spinner typeSpinner;
	Services mServices;
	EditText phoneET;
	EditText nameET;
	Party mParty;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);

		setContentView(R.layout.activity_party_information);

		//Get the party id
		long partyId = getIntent().getLongExtra(Constants.KEY_PARTY_ID, Constants.NO_PARTY);
		mServices =  Services.getInstance(this);
		mParty = mServices.getParty(partyId);
		
		setTitle(mParty.getName());

		//Wire up widgets
		TextView idTV = (TextView)findViewById(R.id.activity_merchant_info_id_tv);
		idTV.setText(UtilsFormat.getStringId(mParty.getId(), UtilsFormat.NUM_OF_DIGITS));
		
		nameET = (EditText)findViewById(R.id.activity_merchant_info_name_et);
		nameET.setText(mParty.getName());
		
		phoneET = (EditText)findViewById(R.id.activity_merchant_info_phone_et);
		phoneET.setText(mParty.getPhone());
		
		typeSpinner = (Spinner)findViewById(R.id.activity_merchant_info_type_spinner);
		String[] merchantTypes = new String[Type.values().length];
		for(int i = 0; i < Type.values().length ; i++)
			merchantTypes[i] = Type.values()[i].toString();
		
		typeSpinner.setAdapter(new ArrayAdapter<String>(this
				, android.R.layout.simple_list_item_1, merchantTypes ));
		typeSpinner.setSelection(mParty.getType().ordinal());
		
		Button okBtn = (Button)findViewById(R.id.activity_merchant_info_ok_btn);
		okBtn.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mParty.setName(nameET.getText().toString());
				mParty.setPhone(phoneET.getText().toString());
				mParty.setType(Party.Type.valueOf(typeSpinner.getSelectedItem().toString()));
				mServices.updateParty(mParty);
                //Let the previous activity know that party information was changed
				Intent i = new Intent();
				i.putExtra(Constants.KEY_PARTY_INFO_CHGD, true);
				i.putExtra(Constants.KEY_PARTY_NAME, mParty.getName());
				setResult(Activity.RESULT_OK, i);
				PartyInformationActivity.this.finish();
			}
		});
	}
	
	@Override
	protected void onPause() {
		LockScreenActivity.updatePasscodeTime();
		super.onPause();
	}

	@Override
	protected void onResume() {
		LockScreenActivity.checkPassCode(PartyInformationActivity.this);
		super.onResume();
	}
}
