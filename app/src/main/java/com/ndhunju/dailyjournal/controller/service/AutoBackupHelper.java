package com.ndhunju.dailyjournal.controller.service;

import android.app.Notification;
import android.content.Context;
import android.support.annotation.StringRes;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFolder;
import com.google.android.gms.drive.DriveResourceClient;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.MyNotificationManager;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;

public class AutoBackupHelper {

    private static final String TAG = AutoBackupHelper.class.getSimpleName();

    private static final int NOTIFICATION_ID_AUTO_BACK_UP = 1;
    private static final int NOTIFICATION_ID_GOOGLE_DRIVE_UPLOAD = 2;

    /** Handle access to Drive resources/files.*/
    private DriveResourceClient mDriveResourceClient;

    private Context context;
    private MyNotificationManager notifMgr;
    private String backUpFileDir;
    private EventListener eventListener;

    public AutoBackupHelper(Context context) {
        this.context = context;
    }

    public void setEventListener(EventListener eventListener) {
        this.eventListener = eventListener;
    }

    public void startBackup() {
        Log.i(TAG, "Service started to create backup");
        // get needed objects
        notifMgr = MyNotificationManager.from(getContext());
        PreferenceService ps = PreferenceService.from(getContext());

        /*Check the selected interval for backup*/
        //get the selected value for reminder interval eg, 3600000
        long selectedInterval = ps.getVal(R.string.key_pref_auto_backup_interval_lp,
                PreferenceService.DEF_AUTO_BACKUP_TIME);
        //get respective Entry eg. 1 hours
        String selectedEntry = ps.getEntryForIntervalPL(selectedInterval);

        //Service object by default runs on Main Thread
        Executors.newCachedThreadPool().execute(() -> {
            try {
                //make it unstoppable until the task is completed
                startForeground(1, notifMgr.createBackingUpNotif());
                backUpFileDir = Services.getInstance(getContext())
                        .createBackUp(UtilsFile.getAutoBackupDir(getContext()));

                if (ps.getVal(R.string.key_pref_auto_upload_backup_to_gdrive_cb, false)) {
                    signInToGoogleDrive();
                }

                //notify that the backup has been created
                notifMgr.notify(notifMgr.createBackupCreatedNotif(selectedEntry), NOTIFICATION_ID_AUTO_BACK_UP);

                Log.i(TAG, "backup finished");
                eventListener.onFinishBackUp();
            } catch (Exception e) {
                Log.i(TAG, "backup failed" + e.getLocalizedMessage());
                // notify that auto backup creation failed
                notifMgr.notify(notifMgr.createBackupCreationErrorNotif(selectedEntry," Reason: " + e.getLocalizedMessage()), NOTIFICATION_ID_AUTO_BACK_UP);
                e.printStackTrace();
            }
        });
    }

    /** Starts the sign-in process and initializes the Drive client. */
    protected void signInToGoogleDrive() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes)) {
            onSignedInToGoogleDrive(signInAccount);
        } else {
            Log.i(TAG, "User not signed into google drive.");
            notifyGDriveErrorToUser(getString(R.string.msg_error_g_drive_user_not_signed_in));
        }
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private void onSignedInToGoogleDrive(GoogleSignInAccount signInAccount) {
        mDriveResourceClient = Drive.getDriveResourceClient(getContext(), signInAccount);
        if (backUpFileDir != null) {
            File backUpFileToUpload = new File(backUpFileDir);
            Task<DriveFolder> gDriveRootFolderTask = getDriveResourceClient().getRootFolder();
            Task<DriveContents> createContentsTask = getDriveResourceClient().createContents();
            Tasks.whenAll(gDriveRootFolderTask, createContentsTask).continueWithTask(task -> {
                DriveFolder gDriveRootFolder = gDriveRootFolderTask.getResult();
                DriveContents gDriveContents = createContentsTask.getResult();

                if (gDriveRootFolder == null || gDriveContents == null) {
                    notifyGDriveErrorToUser("");
                    return null;
                }

                File backUpFile = new File(backUpFileDir);
                UtilsFile.copy(new FileInputStream(backUpFile), gDriveContents.getOutputStream());

                MetadataChangeSet changeSet = new MetadataChangeSet.Builder()
                        .setTitle(backUpFileToUpload.getName())
                        .setMimeType(UtilsFile.BACK_FILE_TYPE)
                        .build();

                return getDriveResourceClient().createFile(gDriveRootFolder, changeSet, gDriveContents);
            }).addOnSuccessListener(driveFile -> {
                backUpFileDir = null;
                // notify user about success
                notifMgr.notify(notifMgr.createBackupUploadedToGDriveSuccessNotif(), NOTIFICATION_ID_GOOGLE_DRIVE_UPLOAD);
            }).addOnFailureListener(exception -> {
                Log.e(TAG, "Unable to create file in google drive - ", exception);
                // notify user about failure
                notifyGDriveErrorToUser(exception.getLocalizedMessage());
            });
        }
    }

    public DriveResourceClient getDriveResourceClient() {
        return mDriveResourceClient;
    }

    private void notifyGDriveErrorToUser(String errorMsg) {
        // disable auto upload to google drive option
        PreferenceService ps = PreferenceService.from(getContext());
        ps.putVal(getString(R.string.key_pref_auto_upload_backup_to_gdrive_cb), false);

        errorMsg += ". " + getContext().getString(R.string.msg_option_disabled, getString(R.string.pref_title_auto_upload_backup_to_gdrive));
        notifMgr.notify(notifMgr.createBackupUploadedToGDriveErrorNotif(errorMsg), NOTIFICATION_ID_GOOGLE_DRIVE_UPLOAD);
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
