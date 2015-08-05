package com.ndhunju.dailyjournal.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.ndhunju.dailyjournal.R;

public class PreferencesFragment extends DialogFragment {

    //Constants
    public static final String KEY_LOCK_TIME = "lockTime";
    public static final String KEY_ENABLE_PASSCODE = "enablePasscode";
    public static final String KEY_PASSCODE = "makeThingsHappen.Passcode";

    public static final int DEFAULT_LOCK_TIME = 3;
    public static final String NO_PASSCODE_VAL = "";

    //Widget variables
    EditText passcodeET;
    EditText lockTimeET;
    LinearLayout timeLockLL;
    CheckBox enablePasscodeCB;
    LinearLayout setPassCodeLL;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View v = getActivity().getLayoutInflater().inflate(R.layout.dialog_preferences, new LinearLayout(getActivity()));

        //returns an instance with a default name and private permission
        final SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(getActivity());

        //Get saved value if any
        boolean enablePinCode = sharedPreference.getBoolean(KEY_ENABLE_PASSCODE, false);

        //Wire up widgets
        setPassCodeLL = (LinearLayout)v.findViewById(R.id.dialog_preferences_passcodeFL);
        timeLockLL = (LinearLayout)v.findViewById(R.id.dialog_preferences_lockTimeFL);

        passcodeET = (EditText)v.findViewById(R.id.dialog_preferences_passcodeET);
        passcodeET.setText(sharedPreference.getString(PreferencesFragment.KEY_PASSCODE, NO_PASSCODE_VAL));

        enablePasscodeCB = (CheckBox)v.findViewById(R.id.dialog_preferences_passcodeCB);
        enablePasscodeCB.setChecked(enablePinCode);
        enablePasscodeCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                setPassCodeLL.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                timeLockLL.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
            }
        });

        //Set Value
        setPassCodeLL.setVisibility(enablePasscodeCB.isChecked() ? View.VISIBLE : View.INVISIBLE);
        timeLockLL.setVisibility(enablePasscodeCB.isChecked() ? View.VISIBLE : View.INVISIBLE);

        lockTimeET = (EditText)v.findViewById(R.id.dialog_preferences_lockTimeET);
        lockTimeET.setText(String.valueOf(sharedPreference.getInt(KEY_LOCK_TIME, DEFAULT_LOCK_TIME)));

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.str_preference)
                .setPositiveButton(android.R.string.ok, new OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(enablePasscodeCB.isChecked()) //if the passcode has not been entered
                            enablePasscodeCB.setChecked(!passcodeET.getText().toString().equals(NO_PASSCODE_VAL));
					
                        sharedPreference.edit()
                                .putBoolean(KEY_ENABLE_PASSCODE, enablePasscodeCB.isChecked())
                                .putString(KEY_PASSCODE, passcodeET.getText().toString())
                                //if 0 is entered, put 1 minutes instead otherwise it will be locked immediately
                                .putInt(KEY_LOCK_TIME, Integer.parseInt(lockTimeET.getText().toString()) == 0 ?
                                        1 : Integer.parseInt(lockTimeET.getText().toString()))
                                .commit();
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .setView(v)
                .create();
    }

}
