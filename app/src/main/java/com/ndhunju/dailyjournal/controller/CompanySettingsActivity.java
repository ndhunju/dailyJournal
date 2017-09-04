package com.ndhunju.dailyjournal.controller;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.fragment.DatePickerFragment;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.util.UtilsFormat;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import java.util.Calendar;
import java.util.Date;

public class CompanySettingsActivity extends AppCompatActivity implements OnDialogBtnClickedListener {

    private static final int REQUEST_CHGED_DATE = 656;

    EditText companyNameEt;
    Button dateBtn;
    Button doneBtn;

    PreferenceService preferenceService;
    Date financialYear;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_company_settings);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        preferenceService = PreferenceService.from(getContext());

        companyNameEt = (EditText) findViewById(R.id.activity_company_settings_company_name_et);

        dateBtn = (Button) findViewById(R.id.activity_company_settings_date_btn);
        dateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dpf = DatePickerFragment.newInstance(new Date(), REQUEST_CHGED_DATE);
                dpf.show(getSupportFragmentManager(), DatePickerFragment.TAG);
            }
        });

        doneBtn = (Button) findViewById(R.id.activity_company_settings_done_btn);
        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (TextUtils.isEmpty(companyNameEt.getText())) {
                    companyNameEt.setError("");
                    return;
                }

                preferenceService.putVal(R.string.key_company_name, companyNameEt.getText().toString())
                        .putVal(R.string.key_financial_year, financialYear.getTime());

                finish();
            }
        });

        companyNameEt.setText(preferenceService.getVal(R.string.key_company_name, ""));
        financialYear = new Date(preferenceService.getVal(R.string.key_financial_year, System.currentTimeMillis()));
        dateBtn.setText(UtilsFormat.formatDate(financialYear, this));

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
