package com.ndhunju.dailyjournal.controller.party;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.ImportContacts;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.List;

/**
 * Created by dhunju on 9/29/2015.
 * This class is a child class of {@link AsyncTask} that imports
 * contacts by calling method {@link ImportContacts#importContacts(Activity, List)}
 * This class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class ImportContactsAsync extends AsyncTask<List<ImportContacts.Contact>, Integer, Boolean> {

    Callback mCallback;
    Activity mActivity;
    ProgressDialog pd;

    interface Callback{
        public void onFinished();
    }

    public ImportContactsAsync(Activity activity, Callback callback){
        mActivity = activity;
        mCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        String msg = mActivity.getString(R.string.msg_importing,mActivity.getString(R.string.str_contact));
        pd = UtilsView.createProgressDialog(mActivity, msg);
    }

    @Override
    protected Boolean doInBackground(List<ImportContacts.Contact>... lists) {
        ImportContacts.importContacts(mActivity, lists[0]);
        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        pd.cancel();
        String msg = mActivity.getString(R.string.msg_finished, "");
        UtilsView.alert(mActivity, msg);
        if(mCallback != null) mCallback.onFinished();
    }
}
