package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsZip;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * Retrieves Google Drive File, makes a local copy, uses it to restore the backed up data and
 * deletes the downloaded backup file
 */
public class FetchBackUpFromGDriveRestApiAsync
        extends AsyncTask<com.google.api.services.drive.model.File, Integer, String> {

    // Constants
    private final static String TAG = FetchBackUpFromGDriveRestApiAsync.class.getSimpleName();
    private final static int SHOW_INDETERMINATE_PROGRESS = -1983;
    private final static int ARBITRARY_FILE_SIZE = 5000;

    // Variables
    private final FinishCallback<String> finishCallback;
    private final DriveServiceHelper driveServiceHelper;
    private final WeakReference<Activity>  activityRef;
    private ProgressDialog fileRetrievePd;


    public FetchBackUpFromGDriveRestApiAsync(
            Activity activity,
            DriveServiceHelper driveServiceHelper,
            FinishCallback<String> callback
    ) {
        this.activityRef = new WeakReference<>(activity);
        this.driveServiceHelper = driveServiceHelper;
        this.finishCallback = callback;
    }

    @Override
    protected void onPreExecute() {
        Activity mActivity = activityRef.get();

        if (mActivity == null) {
            // Activity has been destroyed
            return;
        }

        fileRetrievePd = new ProgressDialog(mActivity);
        fileRetrievePd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        fileRetrievePd.setMessage(String.format(mActivity.getString(R.string.msg_importing), ""));
        fileRetrievePd.setCanceledOnTouchOutside(false);
        fileRetrievePd.setIndeterminate(false);
        fileRetrievePd.setMax(100);
        fileRetrievePd.show();
    }

    @Override
    protected String doInBackground(com.google.api.services.drive.model.File... params) {

        final String[] success = { "Fail" };

        Activity mActivity = activityRef.get();

        if (mActivity == null) {
            success[0] = "Fail: Activity is already destroyed.";
            return success[0];
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);

        // Get file ID
        String fileId = params[0].getId();

        // Get InputStream for the given file ID
        InputStream[] inArray = new InputStream[1];
        com.google.api.services.drive.model.File[] metaDataArray
                = new com.google.api.services.drive.model.File[1];
        driveServiceHelper.readFileAsync(fileId)
                .addOnSuccessListener(inputStreamAndMetaDataPair -> {
                    inArray[0] = inputStreamAndMetaDataPair.first;
                    metaDataArray[0] = inputStreamAndMetaDataPair.second;
                    countDownLatch.countDown();
                }).addOnFailureListener(e -> {
                    success[0] = "Fail: " + e.getLocalizedMessage();
                    countDownLatch.countDown();
                });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        InputStream in = inArray[0];
        com.google.api.services.drive.model.File metaData = metaDataArray[0];
        long gDriveFileSize = ARBITRARY_FILE_SIZE;
        try {
            gDriveFileSize = metaData.getSize();
        } catch (Exception ignore) {}

        // Create the file in local drive to store retrieved data
        String fileName = UtilsFile.getZipFileName();
        File backUpFileFromGDrive = new File(UtilsFile.getCacheDir(mActivity), fileName);

        // Write to local file from InputStream
        BufferedOutputStream out;

        try {
            out = new BufferedOutputStream(new FileOutputStream(backUpFileFromGDrive));

            byte[] buffer = new byte[4096];
            int progress;
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
                 progress = (int) (read * 100 / gDriveFileSize);
                Log.d(TAG, String.format("Loading progress: %d percent", progress));
                // give file fetching 30% of the total progress
                publishProgress((int) (0.3 /*totalPerUnit*/ * progress));
            }

            // Imp: Close the resources to prevent leakage*/
            try {
                in.close();
                out.close();
            } catch ( IOException ignore) {}

            // Delete existing objects
            Services.getInstance(mActivity).recreateDB();
            Services.getInstance(mActivity).clearCompanyInfo();
            publishProgress(50);

            // Get the app folder where the data are stored
            File appFolder = UtilsFile.getAppFolder(mActivity);

            // Delete old files, attachments
            UtilsFile.deleteDirectory(appFolder);

            // Unzip the backup file into app folder
            UtilsZip.unzip(backUpFileFromGDrive, appFolder);
            publishProgress(80);

            //load .json and .properties files
            File[] files = appFolder.listFiles();
            publishProgress(SHOW_INDETERMINATE_PROGRESS);
            boolean successLoading = Services.getInstance(mActivity).loadFromJsonAndPropertiesFile(
                    files,
                    JsonConverterString.getInstance(mActivity)
            );
            success[0] = successLoading ? "Success" : "Fail";

        } catch (Exception ex) {
            Log.e(TAG, "Error copying file to local drive:" + ex.getMessage());
            ex.printStackTrace();
            success[0] = "Fail: " + ex.getLocalizedMessage();
        }

        // Delete the local backup file; Google drive already has a copy
        if (!UtilsFile.deleteFile(backUpFileFromGDrive.getAbsolutePath())) {
            Log.w(TAG, "Deleting backup file from google drive failed.");
        }

        return success[0];
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
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        fileRetrievePd.cancel();
        if (finishCallback != null) {
            finishCallback.onFinish(result);
        }

    }
}
