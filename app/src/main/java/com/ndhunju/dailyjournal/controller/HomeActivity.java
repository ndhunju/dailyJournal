package com.ndhunju.dailyjournal.controller;

import android.content.Intent;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;

import java.util.Date;

public class HomeActivity extends NavDrawerActivity {

	private static final int REQUEST_CODE_COMPANY_SETTING = 34534;

	Services mServices;
	TextView mCompanyName;
	TextView mFinancialYear;
	TextView mDrAmount;
	TextView mCrAmount;
	TextView mTotal;

	@Override
	public void onCreate(android.os.Bundle arg0) {
		super.onCreate(arg0);

        addContentFrame(R.layout.activity_home);

		mFinancialYear = (TextView) findViewById(R.id.activity_home_financial_year);
		mCompanyName = (TextView) findViewById(R.id.activity_home_company_name);
		mDrAmount = (TextView) findViewById(R.id.activity_home_dr_balance);
		mCrAmount = (TextView) findViewById(R.id.activity_home_cr_balance);
		mTotal    = (TextView) findViewById(R.id.activity_home_total_balance);

		findViewById(R.id.activity_home_refresh_balance).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				setUserBalance();
			}
		});

		setCompanySettings();

		mServices = Services.getInstance(getContext());

		setUserBalance();

	}

	private void setCompanySettings() {
		PreferenceService preferenceService = PreferenceService.from(this);
		String companyName = preferenceService.getVal(R.string.key_company_name, "");
		long startingYearDate = preferenceService.getVal(R.string.key_financial_year, new Date().getTime());

		if (TextUtils.isEmpty(companyName)) {
			startActivityForResult(new Intent(getContext(), CompanySettingsActivity.class), REQUEST_CODE_COMPANY_SETTING);
		}

		mCompanyName.setText(companyName);
		mFinancialYear.setText(getString(R.string.msg_financial_year, UtilsFormat.formatDate(new Date(startingYearDate), getContext())));
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_COMPANY_SETTING) {
			setCompanySettings();
		}
	}
}
