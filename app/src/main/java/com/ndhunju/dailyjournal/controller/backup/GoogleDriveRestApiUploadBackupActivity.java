package com.ndhunju.dailyjournal.controller.backup;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.util.Log;

import com.google.api.services.drive.Drive;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper;
import com.ndhunju.dailyjournal.service.AnalyticsService;

import java.util.concurrent.atomic.AtomicLong;

import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_FAIL;
import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_SUCCESS;
import static com.ndhunju.dailyjournal.util.ProgressListener.SHOW_INDETERMINATE_PROGRESS_PERCENTAGE;

public class GoogleDriveRestApiUploadBackupActivity extends GoogleDriveRestApiActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_google_drive_rest_upload);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("DriveRestApiUploadBackup");
    }

    @Override
    protected void onSignedInToGoogleDrive(Drive googleDriveService) {
        super.onSignedInToGoogleDrive(googleDriveService);
        uploadBackUpToGDrive();
    }

    private void uploadBackUpToGDrive() {
        if (getDriveServiceHelper() != null) {
            AtomicLong lastPostToUiThread = new AtomicLong(System.currentTimeMillis());
            ProgressDialog pd = showProgressDialog();
            getDriveServiceHelper().createBackup(
                    this,
                    (percentage, message) -> {
                        // Update UI only after 1 second has passed
                        long elapsedMillis = System.currentTimeMillis() - lastPostToUiThread.get();
                        if (elapsedMillis < 1000) {
                            return;
                        }

                        lastPostToUiThread.set(System.currentTimeMillis());

                        runOnUiThread(() -> {
                            // For some reason, the progress dialog is not being updated
                            if (percentage == SHOW_INDETERMINATE_PROGRESS_PERCENTAGE) {
                                pd.setIndeterminate(true);
                            } else {
                                pd.setIndeterminate(false);
                                pd.setProgress((int) percentage);
                            }

                            pd.setMessage(message);

                            Log.d(
                                    "uploadBackUpToGDrive",
                                    " percentage=" + percentage + " message=" + message
                            );
                        });
                    }
            ).addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                pd.dismiss();
                DriveServiceHelper.setLastOperationStatus(getContext(), OPERATION_STATUS_SUCCESS);
                onFinish(true, null);
            })).addOnFailureListener(e -> runOnUiThread(() -> {
                pd.dismiss();
                DriveServiceHelper.setLastOperationStatus(this, OPERATION_STATUS_FAIL);
                onFinish(false, e.getLocalizedMessage());
            }));
        }
    }

    public void onFinish(boolean success, String message) {
        // Format the message
        String resultMsg = String.format(
                getString(R.string.msg_exporting),
                success ? getString(R.string.str_finished) : getString(R.string.str_failed)
        );
        showEndResultToUser(resultMsg + "\n" + (message != null ? message : ""), success);
    }

    private ProgressDialog showProgressDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.msg_exporting, getString(R.string.str_backup)));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setCancelable(false);
        progressDialog.setMax(100);
        progressDialog.show();
        return progressDialog;
    }
}
