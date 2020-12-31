package com.ndhunju.dailyjournal.controller.service;

import android.app.Notification;
import android.content.Context;
import androidx.annotation.StringRes;
import androidx.core.util.Pair;

import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.api.services.drive.Drive;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.MyNotificationManager;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.util.concurrent.Executors;

import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_FAIL;
import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_SUCCESS;

public class AutoBackupWithRestApiHelper {

    private static final String TAG = AutoBackupWithRestApiHelper.class.getSimpleName();

    private static final int NOTIFICATION_ID_AUTO_BACK_UP = 1;
    private static final int NOTIFICATION_ID_GOOGLE_DRIVE_UPLOAD = 2;

    // Member Variables
    private final GoogleSignInHelper googleSignInHelper = GoogleSignInHelper.get();
    private DriveServiceHelper driveServiceHelper;

    private final Context context;
    private MyNotificationManager notifMgr;
    private String backUpFileDir;
    private EventListener eventListener;

    public AutoBackupWithRestApiHelper(Context context) {
        this.context = context;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void startBackup() {
        Log.i(TAG, "Service started to create backup");
        // Get needed objects
        notifMgr = MyNotificationManager.from(getContext());
        PreferenceService ps = PreferenceService.from(getContext());

        /*Check the selected interval for backup*/
        // Get the selected value for reminder interval eg, 3600000
        long selectedInterval = ps.getVal(
                R.string.key_pref_auto_backup_interval_lp,
                PreferenceService.DEF_AUTO_BACKUP_TIME
        );

        // Get respective Entry eg. 1 hours
        String selectedEntry = ps.getEntryForIntervalPL(selectedInterval);

        // Service object by default runs on Main Thread
        Executors.newCachedThreadPool().execute(() -> {
            try {
                // Make it unstoppable until the task is completed
                startForeground(1, notifMgr.createBackingUpNotif());
                backUpFileDir = Services.getInstance(getContext())
                        .createBackUp(UtilsFile.getAutoBackupDir(getContext()));

                if (ps.getVal(R.string.key_pref_auto_upload_backup_to_gdrive_cb, false)) {
                    signInToGoogleDrive();
                }

                // Notify that the backup has been created
                notifMgr.notify(
                        notifMgr.createBackupCreatedNotif(selectedEntry),
                        NOTIFICATION_ID_AUTO_BACK_UP
                );

                Log.i(TAG, "backup finished");
                eventListener.onFinishBackUp();
            } catch (Exception e) {
                Log.i(TAG, "backup failed" + e.getLocalizedMessage());
                // Notify that auto backup creation failed
                notifMgr.notify(notifMgr.createBackupCreationErrorNotif(
                        selectedEntry,
                        " Reason: " + e.getLocalizedMessage()
                ), NOTIFICATION_ID_AUTO_BACK_UP);

                e.printStackTrace();
            }
        });
    }

    /** Starts the sign-in process and initializes the Drive client. */
    protected void signInToGoogleDrive() {

        Pair<GoogleSignInAccount, Integer> googleSignInAccountAndConnectionResult
                = GoogleSignInHelper.get().getLastSignedInAccountAndConnectionResult(getContext());

        if (googleSignInAccountAndConnectionResult.first != null) {
            onSignedInToGoogleAccount(googleSignInAccountAndConnectionResult.first);
        } else {
            GoogleSignInHelper.get()
                    .getGoogleSigInClient(getContext())
                    .silentSignIn()
                    .addOnSuccessListener(
                            // Attempt Sign In again
                            googleSignInAccount -> signInToGoogleDrive()
                    )
                    .addOnFailureListener(e -> {
                        Log.i(TAG, "User not signed into google drive.");
                        notifyGDriveErrorToUser(
                                getString(R.string.msg_error_g_drive_user_not_signed_in)
                                + "(" + e.getLocalizedMessage() + ")"
                        );
                    });
        }
    }

    private void onSignedInToGoogleAccount(GoogleSignInAccount googleAccount) {
        Log.d(TAG, "Sign in successful");
        onSignedInToGoogleDrive(
                googleSignInHelper.signInToGoogleDrive(googleAccount, getContext())
        );
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    protected void onSignedInToGoogleDrive(Drive googleDriveService) {
        driveServiceHelper = new DriveServiceHelper(googleDriveService);
        driveServiceHelper
                .createBackup(getContext(), null)
                .addOnSuccessListener(aVoid -> {
                    // Notify user about success
                    notifMgr.notify(
                            notifMgr.createBackupUploadedToGDriveSuccessNotif(),
                            NOTIFICATION_ID_GOOGLE_DRIVE_UPLOAD
                    );
                    DriveServiceHelper.setLastOperationStatus(
                            getContext(),
                            OPERATION_STATUS_SUCCESS
                    );
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Unable to create file in google drive - ", exception);
                    // Notify user about failure
                    notifyGDriveErrorToUser(exception.getLocalizedMessage());
                    // Force user to grant access again
                    GoogleSignInHelper.get().getGoogleSigInClient(getContext()).revokeAccess();
                    DriveServiceHelper.setLastOperationStatus(getContext(), OPERATION_STATUS_FAIL);
                });
    }

    private void notifyGDriveErrorToUser(String errorMsg) {
        // disable auto upload to google drive option
        PreferenceService ps = PreferenceService.from(getContext());
        ps.putVal(getString(R.string.key_pref_auto_upload_backup_to_gdrive_cb), false);

        errorMsg += ". " + getContext().getString(R.string.msg_option_disabled, getString(R.string.pref_title_auto_upload_backup_to_gdrive));
        notifMgr.notify(
                notifMgr.createBackupUploadedToGDriveErrorNotif(errorMsg),
                NOTIFICATION_ID_GOOGLE_DRIVE_UPLOAD
        );
    }

    /** @see android.app.Service#startForeground(int, Notification)*/
    public final void startForeground(int id, Notification notification) {
        eventListener.onStartForeground(id, notification);
    }

    public Context getContext() {
        return context;
    }

    public String getString(@StringRes int resId) {
        return context.getString(resId);
    }

    interface EventListener {
        /** @see android.app.Service#startForeground(int, Notification)*/
        void onStartForeground(int id, Notification notification);

        void onFinishBackUp();
    }
}
