package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.io.IOException;

/**
 * Created by dhunju on 9/24/2015.
 * This class is a child class of {@link AsyncTask} that creates
 * back up in a user selected folder in the external storage. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class BackUpAsyncTask extends AsyncTask<String, Void, String> {

    private static final String TAG = BackUpAsyncTask.class.getSimpleName();

    private Activity mActivity;
    private ProgressDialog pd;

    public BackUpAsyncTask(Activity context) {
        mActivity = context;
    }

    @Override
    protected void onPreExecute() {
        String msg = String.format(mActivity.getString(R.string.msg_creating), mActivity.getString(R.string.str_backup));
        pd= new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected String doInBackground(String... String) {
        String filePath = "";
        Services mServices = Services.getInstance(mActivity);
        try { filePath = mServices.createBackUp(String[0]);}
        catch (IOException e) { Log.w(TAG, "Error creating backup file: " + e.getMessage());}
        return filePath;

    }

    @Override
    protected void onPostExecute(String filePath) {
        //End progress bar
        pd.cancel();
        boolean success = !filePath.equals("")  || filePath != null;
        mActivity.setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        String resultMsg =  success ?
                            String.format(mActivity.getString(R.string.msg_finished), mActivity.getString(R.string.str_backup))
                            : String.format(mActivity.getString(R.string.msg_failed), mActivity.getString(R.string.str_backup));

        resultMsg += String.format(mActivity.getString(R.string.msg_saved_in), filePath);
        //Display the result
        UtilsView.alert(mActivity, resultMsg);
    }

}
