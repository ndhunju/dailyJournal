package com.ndhunju.dailyjournal.controller.backup;

import android.util.Log;

import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;

/** Created by ndhunju on 9/2/17.
 * This class is responsible for creating a backup file and uploading it to the google drive.*/
public class GoogleDriveUploadBackupActivity extends GoogleDriveBackupActivity implements FinishCallback<String> {

    @Override
    protected void onDriveClientReady() {
        super.onDriveClientReady();
        pickFolder()
                .addOnSuccessListener(this, driveId -> {
                    // If successful write the data to the new contents.
                    Log.i(TAG, "Content created.");
                    new UploadBackUpToGDriveAsync(getActivity(), getDriveResourceClient(), GoogleDriveUploadBackupActivity.this).execute(driveId.asDriveFolder());
                })
                .addOnFailureListener(this, e -> {
                    Log.e(TAG, "No folder selected?", e);
                    showEndResultToUser(e.getLocalizedMessage(), false);
                });
    }

    @Override
    public void onFinish(String result) {
        boolean success = result != null && result.contains("Success");
        //Format the message
        String msg = String.format(getString(R.string.msg_exporting), success
                ? getString(R.string.str_finished) : getString(R.string.str_failed));
        showEndResultToUser(msg, success);
    }

}
