package com.ndhunju.dailyjournal.controller;

import android.content.Intent;

/**
 * Created by dhunju on 8/16/2015.
 * /**
 * This listener can be used to transfer data between a dialog and this activity
 */
public interface OnDialogButtonPressedListener {

    public void onDialogPositiveBtnClicked(Intent data, int result, int requestCode);

    public void onDialogNegativeBtnClicked(Intent data, int result, int requestCode);
}
