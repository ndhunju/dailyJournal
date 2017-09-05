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
                setCompanySettings();
                setUserBalance();
			}
		});

		mServices = Services.getInstance(getContext());

		setCompanySettings();

		setUserBalance();

	}

	private boolean setCompanySettings() {

		if (TextUtils.isEmpty(mServices.getCompanyName())) {
			startActivityForResult(new Intent(getContext(), CompanySettingsActivity.class), REQUEST_CODE_COMPANY_SETTING);
			return false;
		}

		mCompanyName.setText(mServices.getCompanyName());
		mFinancialYear.setText(getString(R.string.msg_financial_year, UtilsFormat.formatDate(mServices.getFinancialYear(), getContext())));
		return true;
	}

	private void setUserBalance() {
		double drBalance = mServices.getDebitTotal();
		double crBalance = mServices.getCreditTotal();
		double balance = drBalance - crBalance;

		((TextView) findViewById(R.id.activity_home_cr_text)).setText(UtilsFormat.getUserDrFromPref(getContext()));
		((TextView) findViewById(R.id.activity_home_dr_text)).setText(UtilsFormat.getUserCrFromPref(getContext()));

		mDrAmount.setText(UtilsFormat.formatCurrency(drBalance, getContext()));
		mCrAmount.setText(UtilsFormat.formatCurrency(crBalance, getContext()));
		mTotal.setText(UtilsFormat.formatCurrency(balance, getContext()));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE_COMPANY_SETTING) {
			setCompanySettings();
		}
	}
}
