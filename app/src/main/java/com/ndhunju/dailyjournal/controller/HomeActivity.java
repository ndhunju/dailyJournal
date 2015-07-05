package com.ndhunju.dailyjournal.controller;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;

public class HomeActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);
		
		//read the database
		Storage.getInstance(HomeActivity.this).readPartiesFromDB();
		
		//set notification if not set already
		Utils.setNotification(HomeActivity.this);
		
		Intent newActIntent = new Intent(HomeActivity.this, JournalActivity.class);
		startActivity(newActIntent);
		
		/*//Ask user if he wants to export the info before quiting
		new AlertDialog.Builder(HomeActivity.this)
			.setTitle(getString(R.string.str_alert))
			.setMessage(getString(R.string.msg_export))
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Storage.getInstance(HomeActivity.this).exportJSON();
				}
				
			})
			.create().show();*/

		HomeActivity.this.finish();
		
	};
	
	

}
