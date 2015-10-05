package com.ndhunju.dailyjournal.controller.importExport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.JsonConverter;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Retrieves DriveFileContent, make a local copy, uses it to restore the backed up data and
 * deletes the downloaded backup file
 * https://github.com/googledrive/android-demos/blob/master/src/com/google/android/gms/drive/sample/demo
 */
public class FetchBackUpFromGDriveAsync extends AsyncTask<DriveId, Integer, String> {

    public final static String TAG = FetchBackUpFromGDriveAsync.class.getSimpleName();

    //Variables
    private GoogleApiClient mGoogleApiClient;
    ProgressDialog fileRetrievePd;
    private Activity mActivity;


    public FetchBackUpFromGDriveAsync(Activity context, GoogleApiClient googleApiClient) {
        mGoogleApiClient = googleApiClient;
        mActivity = context;
    }

    @Override
    protected void onPreExecute() {
        fileRetrievePd = new ProgressDialog(mActivity);
        fileRetrievePd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fileRetrievePd.setMessage(String.format(mActivity.getString(R.string.msg_importing), ""));
        fileRetrievePd.setCanceledOnTouchOutside(false);
        fileRetrievePd.setIndeterminate(true);
        fileRetrievePd.setMax(100);

        fileRetrievePd.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                mGoogleApiClient.disconnect();
                cancel(true);
            }
        });

        fileRetrievePd.show();
    }

    @Override
    protected String doInBackground(DriveId... params) {

        //Get the DriveFile with given instance of GoogleApiClient and DriveID
        DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, params[0]);
        DriveApi.DriveContentsResult driveContentsResult = driveFile.open(mGoogleApiClient,
                DriveFile.MODE_READ_ONLY,
                new DriveFile.DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long bytesExpected) {
                        // Update progress dialog with the latest progress.
                        //Only once called tho so far
                        final int progress = (int) (bytesDownloaded * 100 / bytesExpected);
                        Log.d(TAG, String.format("Loading progress: %d percent", progress));
                        publishProgress(progress + 10);
                    }
                }).await(); //making the process synchronous


        //If unsuccessful, notify the user and return
        if (!driveContentsResult.getStatus().isSuccess()) {
            UtilsView.alert(mActivity, mActivity.getString(R.string.str_failed)+":"+ driveContentsResult.getStatus().getStatusMessage());
            return null;
        }

        //Get DriveContents to retrieve data from
        DriveContents driveContents = driveContentsResult.getDriveContents();

        //create the file in local drive to store retrieved data
        String fileName = UtilsFile.getZipFileName();
        File backUpFileFromGDrive = new File(UtilsFile.getCacheDir(mActivity).getAbsolutePath(), fileName);


        //Buffered input streams read data from a memory area known as a buffer;
        //the native input API is called only when the buffer is empty.
        BufferedOutputStream out = null;
        InputStream in = null;

        try {
            out = new BufferedOutputStream(new FileOutputStream(backUpFileFromGDrive));
            in = driveContents.getInputStream();
            out.write(UtilsFile.read(in));//Faster Read

            //Imp: Close the resources to prevent leakage*/
            in.close();
            out.close();
            driveContents.discard(mGoogleApiClient);

            //Delete existing objects
            Services.getInstance(mActivity).truncateAllTables();

            //Get the app folder where the data are stored
            File appFolder = UtilsFile.getAppFolder(mActivity);

            //Delete old files, attachments
            UtilsFile.deleteDirectory(appFolder);

            //Unzip the backup file into app folder
            UtilsZip.unzip(backUpFileFromGDrive, appFolder);

            //search .json file
            File[] files = appFolder.listFiles();

            JsonConverter converter = JsonConverter.getInstance(Services.getInstance(mActivity));

            for (int i = files.length - 1; i >= 0; i--)
                if (files[i].isFile() && files[i].getName().endsWith(".json")) {
                    if(!converter.parseJSONFile(files[i].getAbsolutePath()))
                        return null;
                    //takes the first json file from the last
                    //name of json file has date on it so the latest json file
                    //wil likely be at the bottom of the list
                    break;
                }

        } catch (Exception ex) {
            Log.e(TAG, "Error copying file to local drive");
            ex.printStackTrace();
        } finally {
            //delete the file once the data is restored; Google drive already has a copy
            boolean success = UtilsFile.deleteFile(backUpFileFromGDrive.getAbsolutePath());
            if (!success) Log.e(TAG, "Deleting backup file from google drive failed.");
            //backUpFileFromGDrive.delete();
        }

        return "success";
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        //sticks at 0. Maybe because of small file size
        fileRetrievePd.setProgress(values[0] + 10);
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        final String msg;
        boolean success = result != null;
        mActivity.setResult(success ? mActivity.RESULT_OK : Activity.RESULT_CANCELED);
        if (success) {
            Log.i(TAG, "Successfully restored from GoogleDrive");
            msg = String.format(mActivity.getString(R.string.msg_importing), mActivity.getString(R.string.str_finished));
        } else {
            Log.d(TAG, "Error while reading from the file");
            msg = String.format(mActivity.getString(R.string.msg_failed), mActivity.getString(R.string.str_restore_backup_from_google_drive));
        }

        fileRetrievePd.cancel();
        UtilsView.alert(mActivity, msg);
    }
}
