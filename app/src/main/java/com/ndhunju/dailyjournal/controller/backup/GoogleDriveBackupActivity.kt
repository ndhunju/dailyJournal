package com.ndhunju.dailyjournal.controller.backup;

import android.content.Intent;
import android.content.IntentSender;
import androidx.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveClient;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.OpenFileActivityOptions;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.SearchableField;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.ndhunju.dailyjournal.R;

/** Created by ndhunju on 1/15/17.
 * This class encapsulates common operations when dealing with {@link DriveResourceClient}*/
public abstract class GoogleDriveBackupActivity extends GoogleDriveSignInActivity {

    public static final String TAG = GoogleDriveBackupActivity.class.getSimpleName();

    /** Request code for Opening an item on Google Drive */
    public static final int REQUEST_CODE_OPEN_ITEM = 1189;

    /** Handles high-level drive functions like sync*/
    private DriveClient mDriveClient;
    /**Handle access to Drive resources/files.*/
    private DriveResourceClient mDriveResourceClient;
    /** Tracks completion of the drive picker */
    private TaskCompletionSource<DriveId> mOpenItemTaskSource;

    @Override
    protected void onSignedIn(GoogleSignInAccount signInAccount) {
        super.onSignedIn(signInAccount);
        initializeDriveClient(signInAccount);
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void initializeDriveClient(GoogleSignInAccount signInAccount) {
        mDriveClient = Drive.getDriveClient(getApplicationContext(), signInAccount);
        mDriveResourceClient = Drive.getDriveResourceClient(getApplicationContext(), signInAccount);
        onDriveClientReady();
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    protected void onDriveClientReady() {}


    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_OPEN_ITEM:
                if (resultCode == RESULT_OK && data != null) {
                    // User has picked a file from Google Drive
                    DriveId driveId = data.getParcelableExtra(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d(TAG, "Selected file's ID: " + driveId);
                    mOpenItemTaskSource.setResult(driveId);
                } else {
                    mOpenItemTaskSource.setException(new RuntimeException(getString(R.string.str_failed)));
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    /**
     * Prompts the user to select a text file using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected Task<DriveId> pickFile(String mimeType) {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, mimeType))
                        .build();
        return pickItem(openOptions);
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected Task<DriveId> pickFolder() {
        OpenFileActivityOptions openOptions =
                new OpenFileActivityOptions.Builder()
                        .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
                        .build();
        return pickItem(openOptions);
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private Task<DriveId> pickItem(OpenFileActivityOptions openOptions) {
        mOpenItemTaskSource = new TaskCompletionSource<>();
        mDriveClient
                .newOpenFileActivityIntentSender(openOptions)
                .continueWith(new Continuation<IntentSender, Void>() {
                    @Override
                    public Void then(@NonNull Task<IntentSender> task) {
                        try {
                            startIntentSenderForResult(task.getResult(), REQUEST_CODE_OPEN_ITEM, null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Log.w(TAG, "Unable to send intent", e);
                        }
                        return null;
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e.toString().contains("user must be signed in")) {
                            showSignInPage();
                        } else {
                            showEndResultToUser(getString(R.string.str_failed) + "\n" + e.getLocalizedMessage(), false);
                        }
                    }
                });
        return mOpenItemTaskSource.getTask();
    }
}
