package com.ndhunju.dailyjournal.controller;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;

import com.ndhunju.dailyjournal.controller.journal.JournalActivity;

public class HomeActivity extends FragmentActivity {
	
	@Override
	protected void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);
		
		//TODO Add a Splash Screen as it might take long time for data to load
		//Immediately jump to JournalActivity for now
		Intent newActIntent = new Intent(HomeActivity.this, JournalActivity.class);
		startActivity(newActIntent);

		//Finish current activity. This way, when user presses back he doesn't come to
		//this activity which will be blank white.
		HomeActivity.this.finish();
	}

}
