package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Retrieves DriveFileContent, make a local copy, uses it to restore the backed up data and
 * deletes the downloaded backup file
 * https://github.com/googledrive/android-demos/blob/master/src/com/google/android/gms/drive/sample/demo
 */
public class FetchBackUpFromGDriveAsync extends AsyncTask<DriveId, Integer, Boolean> {

    private final static String TAG = FetchBackUpFromGDriveAsync.class.getSimpleName();
    private final static int SHOW_INDETERMINATE_PROGRESS = -1983;

    //Variables
    private FinishCallback<Boolean> finishCallback;
    private GoogleApiClient mGoogleApiClient;
    private ProgressDialog fileRetrievePd;
    private Activity mActivity;


    public FetchBackUpFromGDriveAsync(Activity context, GoogleApiClient googleApiClient, FinishCallback<Boolean> callback) {
        mGoogleApiClient = googleApiClient;
        finishCallback = callback;
        mActivity = context;
    }

    @Override
    protected void onPreExecute() {
        fileRetrievePd = new ProgressDialog(mActivity);
        fileRetrievePd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fileRetrievePd.setMessage(String.format(mActivity.getString(R.string.msg_importing), ""));
        fileRetrievePd.setCanceledOnTouchOutside(false);
        fileRetrievePd.setIndeterminate(false);
        fileRetrievePd.setMax(100);
        fileRetrievePd.show();
    }

    @Override
    protected Boolean doInBackground(DriveId... params) {

        boolean success = true;

        //Get the DriveFile with given instance of GoogleApiClient and DriveID
        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, params[0]);
        final DriveApi.DriveContentsResult driveContentsResult = driveFile.open(mGoogleApiClient,
                DriveFile.MODE_READ_ONLY,
                new DriveFile.DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long bytesExpected) {
                        // Update progress dialog with the latest progress.
                        // Called properly for large files only if it was not fetched before.
                        // It seems google drive caches the file locally
                        final int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                        Log.d(TAG, String.format("Loading progress: %d percent", progress));
                        // give file fetching 30% of the total progress
                        publishProgress((int) (0.3 /*totalPerUnit*/ * progress));
                    }
                }).await(); //making the process synchronous


        //If unsuccessful, return
        if (!driveContentsResult.getStatus().isSuccess()) {
            return false;
        }

        //Get DriveContents to retrieve data from
        DriveContents driveContents = driveContentsResult.getDriveContents();
        publishProgress(40);

        //create the file in local drive to store retrieved data
        String fileName = UtilsFile.getZipFileName();
        File backUpFileFromGDrive = new File(UtilsFile.getCacheDir(mActivity), fileName);


        //Buffered input streams read data from a memory area known as a buffer;
        //the native input API is called only when the buffer is empty.
        BufferedOutputStream out;
        InputStream in;

        try {
            out = new BufferedOutputStream(new FileOutputStream(backUpFileFromGDrive));
            in = driveContents.getInputStream();

            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }

            //Imp: Close the resources to prevent leakage*/
            try {
                in.close();
                out.close();
            } catch ( IOException ignore) {}

            driveContents.discard(mGoogleApiClient);

            //Delete existing objects
            Services.getInstance(mActivity).recreateDB();
            publishProgress(50);

            //Get the app folder where the data are stored
            File appFolder = UtilsFile.getAppFolder(mActivity);

            //Delete old files, attachments
            UtilsFile.deleteDirectory(appFolder);

            //Unzip the backup file into app folder
            UtilsZip.unzip(backUpFileFromGDrive, appFolder);
            publishProgress(80);

            //search .json file
            File[] files = appFolder.listFiles();

            JsonConverterString converter = JsonConverterString.getInstance(mActivity);
            publishProgress(90);

            publishProgress(SHOW_INDETERMINATE_PROGRESS);
            for (int i = files.length - 1; i >= 0; i--)
                if (files[i].isFile() && files[i].getName().endsWith(".json")) {
                    if(!converter.readFromJSON(files[i].getAbsolutePath()))
                        return null;
                    //takes the first json file from the last
                    //name of json file has date on it so the latest json file
                    //wil likely be at the bottom of the list
                    break;
                }

        } catch (Exception ex) {
            Log.e(TAG, "Error copying file to local drive:" + ex.getMessage());
            ex.printStackTrace();
            success = false;
        }

        //delete the file once the data is restored; Google drive already has a copy
        if (!UtilsFile.deleteFile(backUpFileFromGDrive.getAbsolutePath())) {
            Log.w(TAG, "Deleting backup file from google drive failed.");
        }
        //backUpFileFromGDrive.delete();

        return success;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(TAG, "progress " + values[0]);
        if (values[0] == SHOW_INDETERMINATE_PROGRESS) {
            fileRetrievePd.setIndeterminate(true);
        } else {
            fileRetrievePd.setIndeterminate(false);
            fileRetrievePd.setProgress(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        fileRetrievePd.cancel();
        if (finishCallback != null) {
            finishCallback.onFinish(result);
        }

    }
}
