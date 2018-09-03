package com.ndhunju.dailyjournal.controller;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.KeyValPersistence;

/**
 * Created by ndhunju on 3/11/18.
 * This fragment simply shows Disclaimer that user has to agree to before using this app.
 */

public class DisclaimerFragmentDialog extends DialogFragment {

    private static final String PREF_KEY_DISCLAIMER_AGREED = "PREF_KEY_DISCLAIMER_AGREED";

    public static boolean shouldShow(Context context) {
        boolean didAgreeToTerms =  KeyValPersistence.from(context).getBoolean(PREF_KEY_DISCLAIMER_AGREED, false);
        return !didAgreeToTerms;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setTitle(R.string.str_disclaimer)
                .setMessage(getString(R.string.msg_disclaimer, getString(R.string.app_name)))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // user agreed with Terms & Conditions. Record this in preference
                        KeyValPersistence.from(getContext()).putBoolean(PREF_KEY_DISCLAIMER_AGREED, true);
                    }
                })
                .setNegativeButton(R.string.str_exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // user didn't agree with Terms & Conditions. Exit the app
                        KeyValPersistence.from(getContext()).putBoolean(PREF_KEY_DISCLAIMER_AGREED, false);
                        getActivity().finish();
                    }
                })
                .create();

        // don't let user skip action on this dialog
        setCancelable(false);
        return dialog;
    }
}
