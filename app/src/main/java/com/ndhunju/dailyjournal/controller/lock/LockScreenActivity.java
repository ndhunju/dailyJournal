package com.ndhunju.dailyjournal.controller.lock;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.LockService;

public class LockScreenActivity extends Activity {

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_lock_screen);
        setTitle(getString(R.string.title_activity_lock_screen));

        final String savedPassCode = getSharedPreferences(
                PreferenceService.DEF_NAME_SHARED_PREFERENCE, Activity.MODE_PRIVATE)
                .getString(getString(R.string.key_pref_pincode_val_et),
                        PreferenceService.NO_PASSCODE_VAL);

        //wire up
        final TextView passcodeTV = (TextView)findViewById(R.id.activity_lock_screen_passcodeTV);
        final EditText passcodeET = (EditText)findViewById(R.id.activity_lock_screen_passcodeET);

        passcodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passcodeTV.setTextColor(getResources().getColor(android.R.color.black));
                //Unlock as soon as the pin code matches
                if(savedPassCode.equals(s.toString())){
                    LockService.updatePasscodeTime();
                    finish();
                }
            }
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,int after) {}
            @Override
            public void afterTextChanged(Editable s) {}
        });

        passcodeET.setOnEditorActionListener(new OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //Change the Default behavior of Enter button
                if((event!=null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER))
                        || (actionId == EditorInfo.IME_ACTION_DONE)){
                    if(savedPassCode.equals(passcodeET.getText().toString())){
                        LockService.updatePasscodeTime();
                        finish();
                    }else{
                        //if the user clicks Enter with wrong pass code,
                        //Clear text and make PassCodeTextView red
                        passcodeET.setText(PreferenceService.NO_PASSCODE_VAL);
                        passcodeTV.setTextColor(getResources().getColor(R.color.red_light_pressed));
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Don't let user exit the activity with back button
    }
}
