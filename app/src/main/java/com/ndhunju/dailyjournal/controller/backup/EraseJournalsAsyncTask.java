package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Journal;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsView;

/**
 * Created by dhunju on 9/24/2015.
 * * This class is a child class of {@link AsyncTask} that erases
 * all journal data by calling {@link Services#eraseAllJournals()}. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class EraseJournalsAsyncTask extends AsyncTask<Void,Integer,Boolean> {

    private ProgressDialog pd;
    private Activity mActivity;

    public EraseJournalsAsyncTask(Activity activity){
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        pd = new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setCancelable(false);
        pd.setMessage(String.format(mActivity.getString(R.string.msg_deleting), mActivity.getString(R.string.str_journal)));
        pd.show();
    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        Services services = Services.getInstance(mActivity);
        return services.eraseAllJournals();
    }

    @Override
    protected void onPostExecute(Boolean success) {
        pd.cancel();
        String msg = String.format((success ? mActivity.getString(R.string.msg_finished) : mActivity.getString(R.string.msg_failed))
                , mActivity.getString(R.string.activity_backup_erase_journals));
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
