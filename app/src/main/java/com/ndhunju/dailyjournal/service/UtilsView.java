package com.ndhunju.dailyjournal.service;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import com.ndhunju.dailyjournal.R;

/**
 * Created by dhunju on 9/24/2015.
 */
public class UtilsView {


    public static void toast(Context context, String msg){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toast(Context con, String msg, int length){
        Toast.makeText(con, msg, length).show();
    }

    /**
     * This method shows a message to users. Similar to alert function
     * in JavaScript
     * @param msg : message to show in the alert dialog
     * @param con : context
     */
    public static void alert(Context con,String msg){
        new AlertDialog.Builder(con).setMessage(msg)
        .setTitle(con.getString(R.string.str_alert))
        .setPositiveButton(android.R.string.ok, null)
        .create().show();
    }

    public static void alert(Context con,String msg, DialogInterface.OnClickListener listener, DialogInterface.OnClickListener CancelLister){
        new AlertDialog.Builder(con).setMessage(msg)
                .setTitle(con.getString(R.string.str_alert))
                .setPositiveButton(android.R.string.ok, listener)
                .setNegativeButton(android.R.string.cancel, CancelLister)
                .create().show();
    }

    public static void alert(Context con,String msg, DialogInterface.OnClickListener OkListener){
        new AlertDialog.Builder(con).setMessage(msg)
                .setTitle(con.getString(R.string.str_alert))
                .setPositiveButton(android.R.string.ok, OkListener)
                .setCancelable(false)
                .create().show();
    }
}
