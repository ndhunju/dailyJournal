package com.ndhunju.dailyjournal.controller.ImportExport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsView;

/**
 * Created by dhunju on 9/24/2015.
 * * This class is a child class of {@link AsyncTask} that erases
 * all data by calling {@link Services#eraseAll(Context)}. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class EraseAllAsyncTask extends AsyncTask<Void,Integer,Boolean> {

    ProgressDialog pd;
    Activity mActivity;

    public EraseAllAsyncTask(Activity activity){
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.setMessage(String.format(mActivity.getString(R.string.msg_deleting), mActivity.getString(R.string.str_data)));
        pd.show();
        ;
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Services services = Services.getInstance(mActivity);
        return services.eraseAll(mActivity);
    }

    @Override
    protected void onPostExecute(Boolean success) {
        pd.cancel();
        String msg = String.format((success ? mActivity.getString(R.string.msg_finished) : mActivity.getString(R.string.msg_failed))
                , mActivity.getString(R.string.str_erase_all));
        UtilsView.alert(mActivity, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.recreate(); //update the view
                mActivity.finish();
            }
        });
        super.onPostExecute(success);
    }
}
