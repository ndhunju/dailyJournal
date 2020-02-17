package com.ndhunju.dailyjournal.controller;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.ndhunju.dailyjournal.service.LockService;
import com.ndhunju.dailyjournal.service.Services;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // it is important to have a valid company info all the time
        // esp. a valid financial year as this is used to restrict the date of journal to correct range
        if (!(this instanceof CompanySettingsActivity) && !Services.getInstance(this).hasValidCompanyInfo()) {
            CompanySettingsActivity.startActivity(this, 0);
        }
    }

    protected void onResume() {
        super.onResume();
        //check pass code
        LockService.checkPassCode(this);
    }

    @Override
    protected void onPause() {
        LockService.updatePasscodeTime();
        super.onPause();
    }
}
