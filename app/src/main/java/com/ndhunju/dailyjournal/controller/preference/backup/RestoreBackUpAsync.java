package com.ndhunju.dailyjournal.controller.preference.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.service.Services;
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

    //variables
    private Activity mActivity;
    private ProgressDialog pd;

    public RestoreBackUpAsync(Activity context){
        mActivity = context;
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
        String msg = result ? String.format(mActivity.getString(R.string.msg_restored), mActivity.getString(R.string.str_backup))
                : String.format(mActivity.getString(R.string.msg_importing), mActivity.getString(R.string.str_failed));
        UtilsView.alert(mActivity, msg);//Display msg
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String filePath = params[0];

        try {
            //Delete existing files, objects, database
            Services.getInstance(mActivity).recreateDB();

            //Get the app folder where the data are stored
            File appFolder = UtilsFile.getAppFolder(mActivity);

            //Delete old files, attachments
            UtilsFile.deleteDirectory(appFolder);

            //Unzip the files into app folder
            UtilsZip.unzip(new File(params[0]), appFolder);

            //Get data from json file
            JsonConverterString converter = JsonConverterString.getInstance(mActivity);

            //search .json file
            File[] files = appFolder.listFiles();
            for (int i = files.length - 1; i >= 0; i--)
                if (files[i].isFile() && files[i].getName().endsWith(".json")) {
                    if (!converter.readFromJSON(files[i].getAbsolutePath()))
                        return false;
                    //takes the first json file from the last
                    //name of json file has date on it so the latest json file
                    //wil likely be at the bottom of the list
                    break;
                }

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mActivity, new String[]{appFolder.getAbsolutePath()}
                                            , null, null);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Error creating backup file: " + e.getMessage());
            return false;
        }
    }
}
