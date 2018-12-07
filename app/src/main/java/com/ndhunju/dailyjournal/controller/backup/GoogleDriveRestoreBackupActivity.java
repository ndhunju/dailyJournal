package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.drive.DriveId;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsFile;

/** Created by ndhunju on 9/2/17.
 * This class is responsible for restoring a backup from google drive.*/
public class GoogleDriveRestoreBackupActivity extends GoogleDriveBackupActivity implements FinishCallback<String> {

    @Override
    protected void onDriveClientReady() {
        super.onDriveClientReady();
        pickFile(UtilsFile.BACK_FILE_TYPE)
                .addOnSuccessListener(new OnSuccessListener<DriveId>() {
                    @Override
                    public void onSuccess(DriveId driveId) {
                        new FetchBackUpFromGDriveAsync(getActivity(),  getDriveResourceClient(), GoogleDriveRestoreBackupActivity.this).execute(driveId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // show failed error message
                        showEndResultToUser(String.format(getString(R.string.msg_importing), getString(R.string.str_failed)) + "\n" + e.getLocalizedMessage(), false);
                    }
                });
    }

    @Override
    public void onFinish(String result) {
        boolean success = result.contains("Success");
        setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        String msg;
        if (success) {
            Log.i(TAG, "Successfully restored from GoogleDrive");
            msg = String.format(getString(R.string.msg_importing), getString(R.string.str_finished));
        } else {
            Log.d(TAG, "Error while reading from the file [" + result + "]");
            msg = String.format(getString(R.string.msg_failed), getString(R.string.str_restore_backup_from_google_drive));
            msg += "\n[" + result + "]";
        }

        showEndResultToUser(msg, success);
    }
}
