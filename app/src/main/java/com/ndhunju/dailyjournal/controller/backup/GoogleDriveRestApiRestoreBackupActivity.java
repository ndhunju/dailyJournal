package com.ndhunju.dailyjournal.controller.backup;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.widget.LinearLayout;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.AnalyticsService;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

public class GoogleDriveRestApiRestoreBackupActivity
        extends GoogleDriveRestApiActivity
        implements FileRecyclerViewAdapter.OnFileSelectListener {

    // Constants
    private static final String TAG = GoogleDriveRestApiRestoreBackupActivity.class.getSimpleName();

    private FileRecyclerViewAdapter fileAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_google_drive_rest_restore);
        super.onCreate(savedInstanceState);

        // Setup recycler view
        RecyclerView fileRecyclerView = findViewById(R.id.file_list);
        fileRecyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), LinearLayout.VERTICAL)
        );
        fileRecyclerView.setAdapter(fileAdapter = new FileRecyclerViewAdapter());
    }

    @Override
    protected void onResume() {
        super.onResume();
        AnalyticsService.INSTANCE.logScreenViewEvent("DriveRestApiRestoreBackup");
    }

    @Override
    protected void onSignedInToGoogleDrive(Drive googleDriveService) {
        super.onSignedInToGoogleDrive(googleDriveService);
        queryForBackupFiles();
    }

    /**
     * Queries the Drive REST API for files visible to this app and lists them in the content view.
     */
    private void queryForBackupFiles() {
        if (getDriveServiceHelper() != null) {
            Log.d(TAG, "Querying for backup files.");
            showProgress(true, getString(R.string.msg_loading_files));

            getDriveServiceHelper().queryFiles()
                    .addOnSuccessListener(fileList -> {
                        showProgress(false, null);
                        fileAdapter.setFileList(fileList.getFiles());
                        fileAdapter.setOnFileSelectListener(this);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Unable to query files.", exception);
                        if (exception instanceof UserRecoverableAuthIOException
                                || exception instanceof UserRecoverableAuthException) {
                            // User might have revoked access, request Sign In again
                            requestSignIn();
                        } else {
                            showProgress(
                                    false,
                                    getString(R.string.msg_error_google_drive_query_fail) +
                                            "\n(" + exception.getLocalizedMessage() + ")"
                            );
                        }
                    });
        }
    }

    @Override
    public void onFileSelect(File file) {
        new FetchBackUpFromGDriveRestApiAsync(
                GoogleDriveRestApiRestoreBackupActivity.this,
                getDriveServiceHelper(),
                result -> {
                    new AlertDialog.Builder(getContext())
                            .setMessage(result)
                            .create().show();
                }).execute(file);
    }

}
