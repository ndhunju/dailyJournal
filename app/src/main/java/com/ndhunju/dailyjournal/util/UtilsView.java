package com.ndhunju.dailyjournal.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import android.transition.Transition;
import android.transition.TransitionInflater;
import android.transition.TransitionManager;
import android.util.TypedValue;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.StartNextYearActivity;
import com.ndhunju.dailyjournal.controller.preference.MyPreferenceActivity;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.Services;

import java.util.Date;

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

    public static void showAlertDialogForInvalidJournalDate(Context context, long date) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.str_alert)
                .setMessage(context.getString(
                        R.string.msg_date_not_in_range,
                        UtilsFormat.formatDate(new Date(date), context),
                        UtilsFormat.formatDate(
                                Services.getInstance(context).getFinancialYear(),
                                context
                        ),
                        UtilsFormat.getJournalFromPref(context),
                        context.getString(
                                R.string.pref_allow_out_of_range_journal_entry,
                                UtilsFormat.getJournalFromPref(context)
                        )
                ))
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(
                        R.string.title_activity_start_next_year,
                        (dialog, which) -> context.startActivity(new Intent(
                                context,
                                StartNextYearActivity.class
                        ))
                )
                .setNeutralButton(R.string.str_preference, (dialogInterface, i) -> {
                    context.startActivity(new Intent(context, MyPreferenceActivity.class));
                })
                .show();
    }

    /**
     * Shows an alert dialog with a message to user for using a {@code date} that has passed
     * current financial year date.
     */
    public static void showAlertDialogForFutureJournalDate(Context context, long date) {
        new AlertDialog.Builder(context)
                .setTitle(R.string.str_alert)
                .setMessage(context.getString(
                        R.string.msg_date_passed_financial_year_range,
                        UtilsFormat.formatDate(new Date(date), context),
                        UtilsFormat.formatDate(
                                Services.getInstance(context).getFinancialYear(),
                                context
                        ),
                        Services.getInstance(context).numOfDaySinceCurrentFinancialYear(date),
                        context.getString(R.string.title_activity_start_next_year)
                ))
                .setNegativeButton(android.R.string.cancel, null)
                .setPositiveButton(R.string.title_activity_start_next_year, (dialogInterface, i)-> {
                    context.startActivity(new Intent(context, StartNextYearActivity.class));
                })
                .show();
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

    public static void showKeyboard(EditText editText) {
        InputMethodManager imm = (InputMethodManager) editText.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
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

    public static void performTransition(int viewGroupId, Activity activity){
        //For example, the framework does not support animating changes to ListView
        // objects, so you should not try to animate them during a transition.
        Transition transition = TransitionInflater.from(activity)
                .inflateTransition(R.transition.transition_fade_set);
        transition.setDuration(500);
        // Start recording changes to the view hierarchy
        TransitionManager.beginDelayedTransition((ViewGroup) activity
                .findViewById(viewGroupId), transition);

    }

    public static void setMenuIconTint(Menu menu, @ColorInt int tint) {
        int i = 0;
        Drawable icon;
        while (i < menu.size()) {
            icon = DrawableCompat.wrap(menu.getItem(i).getIcon());
            DrawableCompat.setTint(icon, tint);
            menu.getItem(i).setIcon(icon);
            i++;
        }

    }

    public static int dpToPx(Context context, int px) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, context.getResources().getDisplayMetrics());
    }

    public static RecyclerView.ItemAnimator getDefaultItemAnimator() {
        RecyclerView.ItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setAddDuration(500);
        itemAnimator.setChangeDuration(100);
        return itemAnimator;
    }

    @SuppressLint("MissingPermission") // It is already added
    public static void addAdView(
            @Nullable FrameLayout adViewContainer,
            @NonNull String adUnitId,
            @NonNull String screenName
    ) {
        if (adViewContainer == null) {
            return;
        }

        AdView adView = new AdView(adViewContainer.getContext());
        adView.setAdSize(AdSize.BANNER);
        adView.setAdUnitId(adUnitId);
        adViewContainer.addView(adView);
        adView.setAdListener(new AdListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                super.onAdFailedToLoad(loadAdError);
                AnalyticsService.INSTANCE.logEvent(
                        "didFailToLoadAdIn" + screenName,
                        String.format(
                                "domain: %s, code: %d, message: %s",
                                loadAdError.getDomain(),
                                loadAdError.getCode(),
                                loadAdError.getMessage()
                        ));
            }
        });

        adView.loadAd(new AdRequest.Builder().build());
    }

}
