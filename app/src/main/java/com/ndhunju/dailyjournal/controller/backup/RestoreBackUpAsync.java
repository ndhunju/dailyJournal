package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.File;

/**
 * Restores the data from zipped/backup file
 * This class is a child class of {@link AsyncTask} that creates
 * deletes existing data and restores from a backup file selected
 * by the user. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class RestoreBackUpAsync extends AsyncTask<String, Void, Boolean> {

    private static final String TAG = RestoreBackUpAsync.class.getSimpleName();
    public enum Action {DELETE_SOURCE_FILE}

    //variables
    private Activity mActivity;
    private ProgressDialog pd;
    private Action action;
    private Exception ex;

    public RestoreBackUpAsync(Activity context){
        mActivity = context;
    }

    public RestoreBackUpAsync setAction(Action action) {
        this.action = action;
        return this;
    }

    @Override
    protected void onPreExecute() {
        String msg = String.format(mActivity.getString(R.string.msg_importing), mActivity.getString(R.string.str_backup));
        pd = new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        //End progress bar
        pd.cancel();
        mActivity.setResult(result ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        String msg = result
                ? String.format(mActivity.getString(R.string.msg_restored), mActivity.getString(R.string.str_backup))
                : String.format(mActivity.getString(R.string.msg_importing), mActivity.getString(R.string.str_failed));

        if (ex != null) {
            msg += "\n\n(" + ex.getMessage() + ")";
        }

        UtilsView.alert(mActivity, msg);//Display msg
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String sourceFilePath = params[0];

        try {
            //Delete existing files, objects, database
            Services.getInstance(mActivity).recreateDB();
            Services.getInstance(mActivity).clearCompanyInfo();

            //Get the app folder where the data are stored
            File appFolder = UtilsFile.getAppFolder(mActivity);

            //Delete old files, attachments
            UtilsFile.deleteDirectory(appFolder);

            //Unzip the files into app folder
            UtilsZip.unzip(new File(sourceFilePath), appFolder);

            //load .json and .properties files
            File[] files = appFolder.listFiles();
            Services.getInstance(mActivity).loadFromJsonAndPropertiesFile(files, JsonConverterString.getInstance(mActivity));

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mActivity, new String[]{appFolder.getAbsolutePath()}
                                            , null, null);

            if (action != null && action == Action.DELETE_SOURCE_FILE) {
                UtilsFile.deleteFile(sourceFilePath);
            }

            return true;
        } catch (Exception e) {
            this.ex = e;
            Log.e(TAG, "Error restoring backup file: " + e.getMessage());
            return false;
        }
    }
}
