package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by dhunju on 9/24/2015.
 * This class is a child class of {@link AsyncTask} that creates
 * back up in temp folder, uploads it to google drive and
 * deletes the file from temp folder. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class UploadBackUpToGDriveAsync extends AsyncTask<DriveApi.DriveContentsResult, Integer, IntentSender> {

    private static final String TAG = UploadBackUpToGDriveAsync.class.getSimpleName();

    //variables
    private FinishCallback<IntentSender> mFinishCallback;
    private GoogleApiClient mGoogleApiClient;
    private Activity mActivity;
    private ProgressDialog pd;

    public UploadBackUpToGDriveAsync(Activity activity, GoogleApiClient googleApiClient, FinishCallback<IntentSender> resultCallback){
        mGoogleApiClient = googleApiClient;
        mFinishCallback = resultCallback;
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(mActivity);
        pd.setMessage(String.format(mActivity.getString(R.string.msg_creating), mActivity.getString(R.string.str_backup)));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.setMax(100);
        pd.show();
        pd.setProgress(10);
    }

    @Override
    protected IntentSender doInBackground(DriveApi.DriveContentsResult... results) {
        //Create a new Content in google drive and set a callback

        // Get an output stream for the contents.
        OutputStream outputStream = results[0].getDriveContents().getOutputStream();

        File backUpfile = null;

        try {
            //Create a new full backup of data into local drive
            Services s = Services.getInstance(mActivity);
            String filePath = s.createBackUp(UtilsFile.getCacheDir(mActivity));
            backUpfile = new File(filePath);
            publishProgress(20);

            InputStream ios = null;
            try {
                byte[] buffer = new byte[4096];
                ios = new FileInputStream(backUpfile);
                int read;
                while ((read = ios.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, read);
                }
            } finally {
                try {
                    if (outputStream != null) outputStream.close();
                } catch (IOException ignore) {
                }

                try {
                    if (ios != null) ios.close();
                } catch (IOException ignore) {
                }
            }
            publishProgress(60);

            //delete the backup file from internal storage
            UtilsFile.deleteFile(filePath);
            publishProgress(80);

            // Create the initial metadata - MIME type and title.
            // Note that the user will be able to change the title later.
            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                    .setTitle(backUpfile.getName())
                    .setMimeType(UtilsFile.BACK_FILE_TYPE).build();
            publishProgress(90);


            // Create an intent for the file chooser, and start it.
            IntentSender intentSender = Drive.DriveApi
                    .newCreateFileActivityBuilder()
                    .setInitialMetadata(metadataChangeSet)
                    .setInitialDriveContents(results[0].getDriveContents())
                    .setActivityTitle(backUpfile.getName())
                    .build(mGoogleApiClient);
            publishProgress(100);

            return intentSender;
        } catch (IOException e1) {
            Log.i(TAG, "Unable to write file contents.");
        }

        return null;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        pd.setProgress(values[0]);
        //Log.d(TAG, "progress " + values[0]);
    }

    @Override
    protected void onPostExecute(IntentSender intentSender) {
        pd.cancel();
        if (mFinishCallback != null) {
            mFinishCallback.onFinish(intentSender);
        }
    }
}
