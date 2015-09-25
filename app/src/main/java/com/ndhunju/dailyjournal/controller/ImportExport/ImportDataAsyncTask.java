package com.ndhunju.dailyjournal.controller.ImportExport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.media.MediaScannerConnection;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.JsonConverter;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFile;
import com.ndhunju.dailyjournal.service.UtilsView;
import com.ndhunju.dailyjournal.service.UtilsZip;

import java.io.File;

/**
 * Imports data from zipped/backup file
 * 1. Merging journals with duplicate id
 */
public class ImportDataAsyncTask extends AsyncTask<String, Void, Boolean> {


    Activity mActivity;
    //Show progress bar
    ProgressDialog pd;

    public ImportDataAsyncTask(Activity activity) {
        mActivity = activity;
    }

    protected void onPreExecute() {
        String msg = String.format(mActivity.getString(R.string.msg_importing), "");
        pd = new ProgressDialog(mActivity);
        pd.setIndeterminate(true);
        pd.setMessage(msg);
        pd.setCancelable(false);
        pd.setCanceledOnTouchOutside(false);
        pd.show();
    }

    protected void onPostExecute(Boolean result) {
        //End progress bar
        pd.cancel();

        //Display msg
        String msg = result ? String.format(mActivity.getString(R.string.msg_finished), mActivity.getString(R.string.str_import_json_from_sd))
                : String.format(mActivity.getString(R.string.msg_failed), mActivity.getString(R.string.str_import_json_from_sd));
        UtilsView.alert(mActivity, msg);
    }

    @Override
    protected Boolean doInBackground(String... params) {

        String filePath = params[0];

        //Check if the file extension is correct
        if (!(filePath.endsWith(UtilsFile.ZIP_EXT) || filePath.endsWith(UtilsFile.ZIP_EXT_OLD))) {
            UtilsView.alert(mActivity, mActivity.getString(com.ndhunju.dailyjournal.R.string.warning_ext_mismatch));
            return false;
        }
        try {
            //Get the app folder where the data are stored
            File appFolder = UtilsFile.getAppFolder(mActivity);

            //Unzip the files into app folder
            UtilsZip.unzip(new File(params[0]), appFolder);

            JsonConverter converter = new JsonConverter(Services.getInstance(mActivity));

            //search .json file
            File[] files = appFolder.listFiles();
            for (int i = files.length - 1; i >= 0; i--)
                if (files[i].isFile() && files[i].getName().endsWith(".json")) {
                    if(!converter.parseJSONFile(files[i].getAbsolutePath()))
                        return false; //true to create new Id
                    //takes the first json file from the last
                    //name of json file has date on it so the latest json file
                    //wil likely be at the bottom of the list
                    break;
                }

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mActivity, new String[]{appFolder.getAbsolutePath()}, null, null);

            //While importing data need to find and merge duplicate parties

            return true;

        } catch (Exception e) {
            Log.w("TAG", "Error creating backup file: " + e.getMessage());
            return false;
        }
    }
}
