package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;

/** Created by ndhunju on 9/2/17.
 * This class is responsible for creating a backup file and uploading it to the google drive.*/
public class GoogleDriveUploadBackupActivity extends GoogleDriveBackupActivity implements FinishCallback<IntentSender> {

    private static final int REQUEST_CODE_GDRIVE_CREATOR = 1185;

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        Drive.DriveApi.newDriveContents(getGoogleApiClient())
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    /** Called on Main Thread.**/
                    @Override
                    public void onResult(final DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) {
                            Log.i(TAG, "Failed to create new backup: " + result.getStatus().getStatusMessage());
                            // Failed, show message to the user
                            showEndResultToUser(result.getStatus().getStatusMessage(), false);
                        }
                        // If successful write the data to the new contents.
                        Log.i(TAG, "Content created.");
                        new UploadBackUpToGDriveAsync(getActivity(), getGoogleApiClient(), GoogleDriveUploadBackupActivity.this).execute(result);
                    }
                });
    }

    @Override
    public void onFinish(IntentSender result) {
        //Show a Google Drive Picker where user can select the folder to save backup file in
        try {
            startIntentSenderForResult(result, REQUEST_CODE_GDRIVE_CREATOR, null, 0, 0, 0);
        } catch (Exception e) {
            Log.i(TAG, "Failed to launch file chooser.");
            e.printStackTrace();

            showEndResultToUser(String.format(getString(R.string.msg_exporting), getString(R.string.str_failed)), false);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GDRIVE_CREATOR:   // Called after the file is created in the google drive.
                //Format the message
                String msg = String.format(getString(R.string.msg_exporting), resultCode == Activity.RESULT_OK
                        ? getString(R.string.str_finished) : getString(R.string.str_failed));
                showEndResultToUser(msg, resultCode == Activity.RESULT_OK);

                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

}
