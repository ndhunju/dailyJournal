package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilDownloadManager;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
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
    private FinishCallback<String> mCallback;

    public BackUpAsyncTask(Activity context, FinishCallback<String> callback) {
        mActivity = context;
        mCallback = callback;
        // lock the orientation, otherwise we will lose reference to callback
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
    }

    @Override
    protected void onPreExecute() {
        String msg = String.format(mActivity.getString(R.string.msg_creating), mActivity.getString(R.string.str_backup));
        pd= new ProgressDialog(mActivity);
        pd.setIndeterminate(false);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected String doInBackground(String... String) {
        String filePath = "";
        Services mServices = Services.getInstance(mActivity);
        try {
            filePath = mServices.createBackUp(String[0], (percentage, message) -> {
                mActivity.runOnUiThread(() -> {
                    pd.setProgress((int) percentage);
                    pd.setMessage(message);
                });
            });

            // Notify user that we created a file
            File file = new File(filePath);
            UtilDownloadManager.INSTANCE.notifyUserAboutFileCreation(
                    mActivity,
                    file,
                    mActivity.getString(
                            R.string.msg_backup_created_title,
                            mActivity.getString(R.string.app_name)
                    ),
                    UtilsFile.BACK_FILE_TYPE
            );

        } catch (IOException e) {
            Log.w(TAG, "Error creating backup file: " + e.getMessage());
        }
        return filePath;

    }

    @Override
    protected void onPostExecute(String filePath) {
        //End progress bar
        pd.cancel();
        mCallback.onFinish(filePath);
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
    }

}
