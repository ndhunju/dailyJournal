package com.ndhunju.dailyjournal.controller.backup;

import android.os.Bundle;

import com.google.api.services.drive.Drive;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;

public class GoogleDriveRestApiUploadBackupActivity
        extends GoogleDriveRestApiActivity
        implements FinishCallback<String> {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_google_drive_rest_upload);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onSignedInToGoogleDrive(Drive googleDriveService) {
        super.onSignedInToGoogleDrive(googleDriveService);
        uploadBackUpToGDrive();
    }

    private void uploadBackUpToGDrive() {
        if (getDriveServiceHelper() != null) {
            new UploadBackUpToGDriveRestApiAsync(
                    this,
                    getDriveServiceHelper(),
                    this
            ).execute();
        }
    }

    @Override
    public void onFinish(String result) {
        boolean success = result != null && result.contains("Success");
        // Format the message
        String msg = String.format(getString(R.string.msg_exporting), success
                ? getString(R.string.str_finished) : getString(R.string.str_failed));
        showEndResultToUser(msg, success);
    }
}
