package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.ndhunju.dailyjournal.OnDatePickerDialogBtnClickedListener;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.Calendar;
import java.util.Date;

public class CompanySettingsActivity
        extends BaseActivity
        implements OnDatePickerDialogBtnClickedListener {

    private static final int REQUEST_CHGED_DATE = 656;

    EditText companyNameEt;
    Button dateBtn;
    Button doneBtn;

    Services services;
    Date financialYear;

    public static void startActivity(Activity callingActivity, int requestCode) {
        Intent intent = new Intent(callingActivity, CompanySettingsActivity.class).setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (requestCode > 0) {
            callingActivity.startActivityForResult(intent, requestCode);
        } else {
            callingActivity.startActivity(intent);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_settings);
        setSupportActionBar(findViewById(R.id.toolbar));

        services = Services.getInstance(getContext());

        TextView welcomeTextView = findViewById(R.id.activity_company_settings_welcome_msg);
        welcomeTextView.setText(getString(
                R.string.company_settings_welcome_msg,
                getString(R.string.app_name)
        ));

        companyNameEt = findViewById(R.id.activity_company_settings_company_name_et);

        dateBtn = findViewById(R.id.activity_company_settings_date_btn);
        dateBtn.setOnClickListener(v -> {
            DatePickerFragment dpf = DatePickerFragment.newInstance(new Date(), REQUEST_CHGED_DATE);
            dpf.show(getSupportFragmentManager(), DatePickerFragment.TAG);
        });

        doneBtn = findViewById(R.id.activity_company_settings_done_btn);
        doneBtn.setOnClickListener(v -> {
            if (TextUtils.isEmpty(companyNameEt.getText())) {
                companyNameEt.setError(getString(R.string.company_settings_company_name_empty_error));
                return;
            }

            services.setCompanyName(companyNameEt.getText().toString());
            try {
                services.setFinancialYear(financialYear);
            } catch (IllegalStateException ex) {
                UtilsView.alert(getContext(), getString(R.string.msg_financial_year_set, services.getFinancialYear()));
                return;
            } catch (Exception ex) {
                UtilsView.alert(getContext(), getString(R.string.msg_is_not_valid, getString(R.string.str_date)));
                return;
            }

            finish();
        });

        companyNameEt.setText(services.getCompanyName());
        if (services.getFinancialYear() != null) {
            financialYear = new Date(services.getFinancialYear().getTime());
        } else {
            financialYear = new Date();
        }
        dateBtn.setText(UtilsFormat.formatDate(financialYear, this));

    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("CompanySettings");
    }

    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        switch (requestCode) {

            case REQUEST_CHGED_DATE: //A Date is selected
                if (data == null) return;
                financialYear.setTime(((Calendar) data.getSerializableExtra(DatePickerFragment.EXTRA_CAL)).getTimeInMillis());
                dateBtn.setText(UtilsFormat.formatDate(financialYear, this));
                break;

        }

    }

    @Override
    public void onBackPressed() {
        // don't let user exit this screen until complete
        //super.onBackPressed();
    }

    private Context getContext() {
        return this;
    }

}
