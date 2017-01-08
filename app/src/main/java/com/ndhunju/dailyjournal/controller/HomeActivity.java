package com.ndhunju.dailyjournal.controller;

import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

public class HomeActivity extends NavDrawerActivity {

	Services mServices;
	TextView mDrAmount;
	TextView mCrAmount;
	TextView mTotal;

	@Override
	public void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);

        addContentFrame(R.layout.activity_home);

		mDrAmount = (TextView) findViewById(R.id.activity_home_dr_balance);
		mCrAmount = (TextView) findViewById(R.id.activity_home_cr_balance);
		mTotal    = (TextView) findViewById(R.id.activity_home_total_balance);

		findViewById(R.id.activity_home_refresh_balance).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setUserBalance();
			}
		});

		mServices = Services.getInstance(getContext());

		setUserBalance();

	}

	private void setUserBalance() {
		double drBalance = mServices.getDebitTotal();
		double crBalance = mServices.getCreditTotal();
		double balance = drBalance - crBalance;
		int red   = ContextCompat.getColor(getContext(), R.color.red_light_pressed);
		int green = ContextCompat.getColor(getContext(), R.color.green);

		((TextView) findViewById(R.id.activity_home_cr_text)).setText(UtilsFormat.getUserDrFromPref(getContext()));
		((TextView) findViewById(R.id.activity_home_dr_text)).setText(UtilsFormat.getUserCrFromPref(getContext()));

		mDrAmount.setText(UtilsFormat.formatCurrency(drBalance, getContext()));
		mCrAmount.setText(UtilsFormat.formatCurrency(crBalance, getContext()));
		mTotal.setText(UtilsFormat.formatCurrency(balance, getContext()));
		mTotal.setTextColor(balance > 0 ? green : red);


	}

}
