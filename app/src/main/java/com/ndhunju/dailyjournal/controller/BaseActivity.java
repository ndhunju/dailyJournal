package com.ndhunju.dailyjournal.controller;

import android.support.v7.app.AppCompatActivity;

import com.ndhunju.dailyjournal.service.LockService;

public class BaseActivity extends AppCompatActivity {

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
