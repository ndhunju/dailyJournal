package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.model.File;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.ProgressListener;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

/**
 * @deprecated Use {@link DriveServiceHelper#createBackup(Activity, ProgressListener)}
 * Created by dhunju on 12/26/2020.
 * This class is a child class of {@link AsyncTask} that creates
 * back up in temp folder, uploads it to google drive and
 * deletes the file from temp folder. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class UploadBackUpToGDriveRestApiAsync extends AsyncTask<Void, Integer, String> {

    // Constants
    private static final String TAG = UploadBackUpToGDriveRestApiAsync.class.getSimpleName();
    private final static int SHOW_INDETERMINATE_PROGRESS = -1983;

    // Member variables
    private final FinishCallback<String> finishCallback;
    private final DriveServiceHelper driveServiceHelper;
    private final WeakReference<Activity> activityRef;
    private ProgressDialog pd;

    public UploadBackUpToGDriveRestApiAsync(
            Activity activity,
            DriveServiceHelper driveServiceHelper,
            FinishCallback<String> resultCallback
    ){
        this.activityRef = new WeakReference<>(activity);
        this.driveServiceHelper = driveServiceHelper;
        this.finishCallback = resultCallback;
    }

    @Override
    protected void onPreExecute() {
        Activity activity = activityRef.get();

        if (activity == null) {
            // Activity has been destroyed
            return;
        }

        // Show progress to user
        pd = new ProgressDialog(activity);
        pd.setMessage(String.format(activity.getString(R.string.msg_creating), activity.getString(R.string.str_backup)));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.setMax(100);
        pd.show();
        pd.setProgress(10);
    }

    @Override
    protected String doInBackground(Void... voids) {

        final String[] result = {"Fail"};
        Activity activity = activityRef.get();
        if (activity == null) {
            // Activity has been destroyed
            return result[0];
        }

        CountDownLatch countDownLatch = new CountDownLatch(1);

        driveServiceHelper.getAppFolder(null).continueWithTask(getAppFolderTask -> {

            // Get app folder first to store the backup file
            File appFolder = getAppFolderTask.getResult();

            if (appFolder == null) {
                result[0] = "Fail: Unable to get App Folder in Google Drive.";
                return null;
            }

            publishProgress(20);

            return driveServiceHelper.createFile(
                    null,
                    appFolder,
                    UtilsFile.getZipFileName(),
                    UtilsFile.BACK_FILE_TYPE
            );

        }).continueWithTask(createFileTask -> {
            // Create a new file in Google Drive to store backup
            File file = createFileTask.getResult();

            if (file == null) {
                result[0] = "Fail: Unable to create backup file in Google Drive.";
                return null;
            }

            java.io.File localBackUpFile;

            try {
                // Create a new full backup of data into local drive
                Services s = Services.getInstance(activity);
                String filePath = s.createBackUp(
                        UtilsFile.getCacheDir(activity),
                        (progressType, percentage, message, resultCode) -> {
                            publishProgress(
                                    percentage,
                                    activity.getString(R.string.msg_copying, message)
                            );
                        }
                );
                localBackUpFile = new java.io.File(filePath);

                publishProgress(
                        SHOW_INDETERMINATE_PROGRESS,
                        activity.getString(
                                R.string.msg_creating,
                                activity.getString(R.string.str_backup)
                        )
                );

                // Metadata for the backup file in Google Drive
                File metaData = new File()
                        .setName(localBackUpFile.getName())
                        .setMimeType(UtilsFile.BACK_FILE_TYPE);

                // Save the local backupFile in Google Drive
                return driveServiceHelper.saveFile(
                        null,
                        file.getId(),
                        metaData,
                        new FileContent(UtilsFile.BACK_FILE_TYPE, localBackUpFile)
                );
            } catch (IOException e1) {
                Log.i(TAG, "Unable to write file appFolder.");
                result[0] = "Fail: " + e1.getLocalizedMessage();
            }

            return null;
        }).addOnSuccessListener(activity, driveFile -> {
            result[0] = "Success";
            countDownLatch.countDown();
        }).addOnFailureListener(activity, e -> {
            Log.e(TAG, "Unable to create file?", e);
            result[0] = "Fail: " + e.getLocalizedMessage();
            countDownLatch.countDown();
        });

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return result[0];

    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        Log.d(TAG, "progress " + values[0]);
        if (values[0] == SHOW_INDETERMINATE_PROGRESS) {
            pd.setIndeterminate(true);
        } else {
            pd.setIndeterminate(false);
            pd.setProgress(values[0]);
        }
    }

    private long lastProgressUpdate;

    private void publishProgress(float percentage, String message) {
        Log.d(TAG, "progress percentage = [" + percentage + "], message = [" + message + "]");
        if (activityRef.get() != null) {
            // Update progress only every 1 second
            if (System.currentTimeMillis() - lastProgressUpdate < 1000) {
                return;
            }

            lastProgressUpdate = System.currentTimeMillis();
            activityRef.get().runOnUiThread(() -> {
                if (percentage == SHOW_INDETERMINATE_PROGRESS) {
                    pd.setIndeterminate(true);
                } else {
                    pd.setIndeterminate(false);
                    pd.setProgress((int) percentage);
                }

                pd.setMessage(message);
            });
        }
    }

    @Override
    protected void onPostExecute(String result) {
        pd.cancel();
        if (finishCallback != null) {
            finishCallback.onFinish(result);
        }
    }
}
