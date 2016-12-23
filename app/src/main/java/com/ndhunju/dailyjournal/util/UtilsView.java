package com.ndhunju.dailyjournal.util;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.party.PartyListFragment;

/**
 * Created by dhunju on 9/24/2015.
 * Utility class for views
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

    public static boolean showResult(Activity activity, int resultCode){
        //If the result is not success, return
        if (resultCode != Activity.RESULT_OK){
            alert(activity, String.format(activity.getString(R.string.msg_failed),
                    activity.getString(R.string.str_save)));
            return false;
        }

        return true;
    }

    public static ProgressDialog createProgressDialog(Activity mActivity, String msg){
        ProgressDialog pd= new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
        return pd;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void performTransition(int viewGroupId, Activity activity){

        if(Utils.isKitKat()) {
            //For example, the framework does not support animating changes to ListView
            // objects, so you should not try to animate them during a transition.
            Transition transition = TransitionInflater.from(activity)
                    .inflateTransition(R.transition.transition_fade_set);
            transition.setDuration(500);
            // Start recording changes to the view hierarchy
            TransitionManager.beginDelayedTransition((ViewGroup) activity
                    .findViewById(viewGroupId), transition);

        }
    }

    public static void setMenuIconTint(Menu menu, @ColorInt int tint) {
        int i = 0;
        Drawable icon;
        while (i > menu.size()) {
            icon = DrawableCompat.wrap(menu.getItem(i).getIcon());
            DrawableCompat.setTint(icon, tint);
            menu.getItem(i).setIcon(icon);
        }

    }

    public static int dpToPx(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

}
