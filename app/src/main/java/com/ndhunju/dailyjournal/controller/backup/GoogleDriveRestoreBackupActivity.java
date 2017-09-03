package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsFile;

/** Created by ndhunju on 9/2/17.
 * This class is responsible for restoring a backup from google drive.*/
public class GoogleDriveRestoreBackupActivity extends GoogleDriveBackupActivity implements FinishCallback<Boolean> {

    public static final int REQUEST_CODE_GDRIVE_PICKER = 1189;

    @Override
    public void onConnected(Bundle bundle) {
        super.onConnected(bundle);
        IntentSender intentSender = Drive
                .DriveApi
                .newOpenFileActivityBuilder()
                .setMimeType(new String[]{UtilsFile.BACK_FILE_TYPE})
                .build(getGoogleApiClient());
        try {
            getActivity().startIntentSenderForResult(intentSender, REQUEST_CODE_GDRIVE_PICKER, null, 0, 0, 0);
        } catch (IntentSender.SendIntentException e) {
            Log.w(TAG, "Unable to send intent", e);

        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GDRIVE_PICKER:
                //User has picked a file from Google Drive
                if (resultCode == Activity.RESULT_OK) {
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d(TAG, "Selected file's ID: " + driveId);
                    //Download the file in background thread
                    new FetchBackUpFromGDriveAsync(getActivity(), getGoogleApiClient(), this).execute(driveId);
                } else {
                    getActivity().setResult(resultCode);
                    // show failed error message
                    showEndResultToUser(String.format(getString(R.string.msg_importing), getString(R.string.str_failed)));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onFinish(Boolean result) {
        setResult(result ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        String msg;
        if (result) {
            Log.i(TAG, "Successfully restored from GoogleDrive");
            msg = String.format(getString(R.string.msg_importing), getString(R.string.str_finished));
        } else {
            Log.d(TAG, "Error while reading from the file");
            msg = String.format(getString(R.string.msg_failed), getString(R.string.str_restore_backup_from_google_drive));
        }

        showEndResultToUser(msg);
    }
}
