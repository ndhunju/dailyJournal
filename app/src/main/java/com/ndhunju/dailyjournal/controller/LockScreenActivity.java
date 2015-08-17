package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Utils;

public class LockScreenActivity extends Activity {

    //Variable
    public static long passcodeActivatedTime;

    /**
     * Checks if the user has enabled the pass code. If yes, starts
     * LockScreenActivity
     * @param whichActivity : Activity that this method is called from
     */
    public static void checkPassCode(Activity whichActivity){
        SharedPreferences sp = whichActivity.getSharedPreferences(MyPreferenceFragment.DEF_NAME_SHARED_PREFERENCE, Activity.MODE_PRIVATE);
        if(sp.getBoolean(whichActivity.getString(R.string.key_pref_pincode_cb), false) && !isPasscodeActive(sp, whichActivity)){
            Intent i = new Intent(whichActivity, LockScreenActivity.class);
            whichActivity.startActivity(i);
            updatePasscodeTime();
        }
    }

    /**
     * Checks if the pass code or lock time has expired.
     * @param sp
     * @return
     */
    public static boolean isPasscodeActive(SharedPreferences sp, Activity con){
        String lockTimeStr = sp.getString(con.getString(R.string.key_pref_pincode_time_et), String.valueOf(MyPreferenceFragment.DEFAULT_LOCK_TIME));
        int lockTimeInMin = Integer.parseInt(lockTimeStr);
        long difference = (System.currentTimeMillis()-passcodeActivatedTime);
        passcodeActivatedTime = System.currentTimeMillis();
        return difference  < lockTimeInMin*60*1000;
    }

    /**
     * Update the active time
     */
    public static void updatePasscodeTime(){
        passcodeActivatedTime = System.currentTimeMillis();
    }

    @Override
    protected void onCreate(Bundle arg0) {
        super.onCreate(arg0);
        setContentView(R.layout.activity_lock_screen);
        setTitle(getString(R.string.title_activity_lock_screen));

        final String savedPassCode = getSharedPreferences(MyPreferenceFragment.DEF_NAME_SHARED_PREFERENCE, Activity.MODE_PRIVATE)
                .getString(getString(R.string.key_pref_pincode_val_et), MyPreferenceFragment.NO_PASSCODE_VAL);

        //wire up
        final TextView passcodeTV = (TextView)findViewById(R.id.activity_lock_screen_passcodeTV);
        final EditText passcodeET = (EditText)findViewById(R.id.activity_lock_screen_passcodeET);
        passcodeET.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passcodeTV.setTextColor(Color.parseColor(Utils.BLACK));
                //Unlock as soon as the pin code matches
                if(savedPassCode.equals(s.toString())){
                    passcodeActivatedTime = System.currentTimeMillis();
                    LockScreenActivity.this.finish();
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
                //Block user from pressing BACK button
                if((event!=null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE)){
                    if(savedPassCode.equals(passcodeET.getText().toString())){
                        passcodeActivatedTime = System.currentTimeMillis();
                        LockScreenActivity.this.finish();
                    }else{
                        passcodeET.setText(MyPreferenceFragment.NO_PASSCODE_VAL);
                        passcodeTV.setTextColor(Color.parseColor(Utils.RED));
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Don't let user exit the activity with back button
        return;
    }
}
