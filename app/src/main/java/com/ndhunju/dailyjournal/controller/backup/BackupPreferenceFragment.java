package com.ndhunju.dailyjournal.controller.backup;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.preference.CheckBoxPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import android.os.Environment;
import android.provider.Settings;
import android.text.TextUtils;

import com.ndhunju.dailyjournal.BuildConfig;
import com.ndhunju.dailyjournal.FinishCallback;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.AnalyticsService;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.FolderPickerDialogFragment;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import static android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION;

/**
 * Created by dhunju on 10/8/2015.
 * This fragment allows users to create/restore backup from Google drive/Local Storage
 * as well as set Automatic Backup and erase all data
 */
public class BackupPreferenceFragment
        extends PreferenceFragmentCompat
        implements OnDialogBtnClickedListener, OnSharedPreferenceChangeListener {

    public static final String TAG = BackupPreferenceFragment.class.getSimpleName();
    public static final String KEY_BACKUP_RESULT = TAG + ".BACKUP_RESULT";
    public static final String KEY_FINISH_ON_BACKUP_SUCCESS = TAG + ".KEY_FINISH_ON_BACKUP_SUCCESS";
    private static final int REQUEST_PERMISSIONS_WRITE_STORAGE = 2323;
    public static final String KEY_MSG = ".KEY_MSG";
    public static final String MSG_AUTO_BACKUP_FAILED = ".MSG_AUTO_BACKUP_FAILED";



    //Request codes with random values
    private static final int REQUEST_CODE_PICK_JSON = 1235;
    private static final int REQUEST_CODE_PICK_BACKUP = 8489;
    private static final int REQUEST_CODE_BACKUP_DIR = 3561;
    private static final int REQUEST_CODE_BACKUP_COMPLETE = 5465;
    private static final int REQUEST_CODE_G_DRIVE_SIGN_IN_COMPLETE = 3943;
    private static final int REQUEST_CODE_MANAGE_FILES_PERMISSIONS = 3944;

    private PreferenceService preferenceService;
    private CheckBoxPreference autoUploadBackupToGDriveCB;
    private Preference gDriveRestBackupPref;
    private boolean finishOnBackUpSuccess;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {

        // Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(
                PreferenceService.DEF_NAME_SHARED_PREFERENCE
        );

        addPreferencesFromResource(R.xml.preference_backup);

        if (getActivity() == null) {
            return;
        }

        // Load the preference file
        preferenceService = PreferenceService.from(getActivity());
        finishOnBackUpSuccess = getActivity().getIntent().getBooleanExtra(
                KEY_FINISH_ON_BACKUP_SUCCESS,
                false
        );


        findPreference(getString(R.string.key_pref_backup_google_drive))
                .setOnPreferenceClickListener(preference -> {
                    startActivityForResult(
                            new Intent(getActivity(), GoogleDriveUploadBackupActivity.class),
                            REQUEST_CODE_BACKUP_COMPLETE
                    );
                    return true;
                });

        findPreference(getString(R.string.key_pref_restore_google_drive))
                .setOnPreferenceClickListener(preference -> {
                    UtilsView.alert(getActivity(), getString(R.string.warning_restore),
                            (dialogInterface, i) -> startActivity(new Intent(
                                    getContext(),
                                    GoogleDriveRestoreBackupActivity.class)
                            ), null);
                    return true;
                });

        // Create Backup in Google Drive using REST API library
        gDriveRestBackupPref = findPreference(getString(R.string.key_pref_backup_google_drive_in_house));
        gDriveRestBackupPref.setOnPreferenceClickListener(preference -> {
                    startActivityForResult(new Intent(
                            getActivity(),
                            GoogleDriveRestApiUploadBackupActivity.class
                    ), REQUEST_CODE_BACKUP_COMPLETE);
                    return true;
                });

        // Restore Backup form Google Drive using REST API library
        findPreference(getString(R.string.key_pref_restore_google_drive_in_house))
                .setOnPreferenceClickListener(preference -> {
                    UtilsView.alert(getActivity(), getString(R.string.warning_restore),
                            (dialogInterface, i) -> startActivity(
                                    new Intent(
                                            getActivity(),
                                            GoogleDriveRestApiRestoreBackupActivity.class)
                            ), null);
                    return true;
                });

        findPreference(getString(R.string.key_pref_backup_local_storage))
                .setOnPreferenceClickListener(preference -> {

                    if (!checkWriteStoragePermission()) {
                        return true; // consume click
                    }

                    FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(
                            null,
                            REQUEST_CODE_BACKUP_DIR
                    );
                    dpdf.setTargetFragment(BackupPreferenceFragment.this, 0);
                    dpdf.show(getParentFragmentManager(), TAG);
                    return true;
                });

        findPreference(getString(R.string.key_pref_restore_local_storage))
                .setOnPreferenceClickListener((preference -> {

                    if (!checkWriteStoragePermission()) {
                        return true; // consume click
                    }

                    UtilsView.alert(
                            getActivity(),
                            getString(R.string.warning_restore),
                            (dialogInterface, i) -> {
                                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                intent.setType(UtilsFile.BACK_FILE_TYPE);
                                startActivityForResult(intent, REQUEST_CODE_PICK_BACKUP);
                            },
                            null
                    );
                    return true;
                }));

        findPreference(getString(R.string.key_pref_restore_auto_backup))
                .setOnPreferenceClickListener(preference -> {
                    UtilsView.alert(
                            getActivity(),
                            getString(R.string.warning_restore),
                            (dialogInterface, pos) -> new AutoBackupAlertDialog(getActivity())
                                    .create().show(),
                            null
                    );
                    return  true;
                });

        autoUploadBackupToGDriveCB = findPreference(
                getString(R.string.key_pref_auto_upload_backup_to_gdrive_cb)
        );

        // If user came to this fragment after clicking on a push notification for auto backup
        // failure, then ask user if they want to re-enable it if not already enabled
        Intent intent = getActivity().getIntent();
        if (MSG_AUTO_BACKUP_FAILED.equals(intent.getStringExtra(KEY_MSG))
                && !autoUploadBackupToGDriveCB.isChecked()
        ) {
            UtilsView.alert(
                    getContext(),
                    getString(R.string.msg_auto_backup_re_enable),
                    (dialog, which) -> autoUploadBackupToGDriveCB.performClick(),
                    (dialog, which) -> dialog.dismiss());

            // We just want to ask once. So clear the value for INTENT_MSG
            intent.putExtra(KEY_MSG, (String) null);
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        if (getView() != null && getContext() != null) {
            // Block the previous view
            getView().setBackgroundColor(
                    ContextCompat.getColor(getContext(), android.R.color.white)
            );
        }

        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        AnalyticsService.INSTANCE.logScreenViewEvent("BackupPreference");
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        //get the key for Reminder Checkbox and Reminder Interval Preference List
        String autoBackUpCB = getString(R.string.key_pref_auto_backup_cb);
        String autoBackUpIntervalPl = getString(R.string.key_pref_auto_backup_interval_lp);
        //Check if the changed preference is Reminder Checkbox or reminder interval preference
        if (key.equals(autoBackUpCB) || key.equals(autoBackUpIntervalPl)) {
            preferenceService.updateAutoBackup();
        }

        // get the key for Auto Upload Backup to Google Drive
        String autoUploadToGDriveCB = getString(R.string.key_pref_auto_upload_backup_to_gdrive_cb);
        if (key.equals(autoUploadToGDriveCB)) {
            boolean enableAutoUploadToGDriveCB = sharedPreferences.getBoolean(key, false);
            if (enableAutoUploadToGDriveCB) {
                // Check if user is logged into google drive using REST api
                startActivityForResult(
                        new Intent(getActivity(), GoogleDriveRestApiActivity.class).putExtra(
                                GoogleDriveRestApiActivity.BUNDLE_SHOULD_FINISH_ON_SIGN_IN,
                                true
                        ),
                        REQUEST_CODE_G_DRIVE_SIGN_IN_COMPLETE
                );
            }
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        String msg = "";
        if (getActivity() != null) {
            getActivity().setResult(resultCode);
        }

        switch (requestCode) {

            //Currently this feature is disabled. User needs some technical knowledge to use this
            case REQUEST_CODE_PICK_JSON:
                // Called when the user has picked a json file to restore data from
                if (resultCode != Activity.RESULT_OK) {
                    msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                    UtilsView.alert(getActivity(), msg);
                    return;
                }

                Uri selectedJsonFile = data.getData();
                JsonConverterString converter = JsonConverterString.getInstance(getActivity());
                boolean success = converter.readFromJSON((selectedJsonFile.getPath()));

                //Prepare msg
                msg = success ? String.format(getString(R.string.msg_finished), getString(R.string.str_backup)) :
                        String.format(getString(R.string.msg_failed), getString(R.string.str_backup));

                UtilsView.alert(getActivity(), msg);

                break;

            case REQUEST_CODE_PICK_BACKUP://User picks a backup file from mServices(.dj/.zip) file
                if (resultCode != Activity.RESULT_OK || data.getData() == null) {
                    msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                    UtilsView.alert(getActivity(), msg);
                    return;
                }

                String selectedFilePath = UtilsFile.getPath(getActivity(), data.getData());
                if (selectedFilePath != null) {
                    new RestoreBackUpAsync(getActivity()).execute(selectedFilePath);
                } else {
                    String internalCacheFile = UtilsFile.copyDataToInternalCacheFile(
                            getActivity(),
                            data.getData()
                    );
                    if (internalCacheFile != null) {
                        new RestoreBackUpAsync(getActivity())
                                // ask to delete the internal cache file after restoring backup
                                .setAction(RestoreBackUpAsync.Action.DELETE_SOURCE_FILE)
                                .execute(internalCacheFile);
                    } else {
                        // show restore failed error message
                        msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                        msg += "\n" + getString(R.string.msg_error_locating_file);
                        msg += "\n\n(" + data.getDataString() + ")";

                        AnalyticsService.INSTANCE.logEvent(
                                "failedLocalBackupRestore",
                                "data=" + data.getDataString()
                        );

                        UtilsView.alert(getActivity(), msg);
                    }
                }

                break;

            case REQUEST_CODE_BACKUP_COMPLETE:
                if (resultCode == Activity.RESULT_OK) {
                    setBackupSuccessResult();
                    if (finishOnBackUpSuccess) {
                        getActivity().finish();
                    }
                } else {
                    getActivity().setResult(Activity.RESULT_CANCELED);
                }
                break;

            case REQUEST_CODE_G_DRIVE_SIGN_IN_COMPLETE:
                // update the value of this preference based on the result
                autoUploadBackupToGDriveCB.setChecked(resultCode == Activity.RESULT_OK);
                if (resultCode == Activity.RESULT_OK) {
                    // Ask if user would like to back up now.
                    UtilsView.alert(
                            getContext(),
                            getString(R.string.msg_backup_now),
                            (dialog, which) -> gDriveRestBackupPref.performClick(),
                            (dialog, which) -> dialog.dismiss()
                    );
                } else {
                    // show connecting to msg "Connecting to Google Drive failed"
                    UtilsView.alert(
                            getActivity(),
                            getString(
                                    R.string.msg_connecting_failed,
                                    getString(R.string.str_google_drive)
                            )
                    );
                }
                break;

            case REQUEST_CODE_MANAGE_FILES_PERMISSIONS:
                // Although, user granted the permission, resultCode == RESULT_CANCELLED
                // so can't use result code to check if (resultCode == Activity.RESULT_OK)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                    if (Environment.isExternalStorageManager()) {
                        UtilsView.alert(
                                getContext(),
                                getString(R.string.msg_permission_granted)
                        );
                    } else {
                        UtilsView.alert(
                                getContext(),
                                getString(R.string.msg_permission_not_granted)
                        );
                    }
                }
                break;
        }
    }


    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        switch (requestCode) {
            case REQUEST_CODE_BACKUP_DIR:
                if (result != Activity.RESULT_OK)
                    UtilsView.toast(getActivity(), getString(R.string.str_failed));
                if (whichBtn == OnDialogBtnClickedListener.BUTTON_POSITIVE) {
                    data.getData();
                    String dir = data.getStringExtra(FolderPickerDialogFragment.KEY_CURRENT_DIR);
                    new BackUpAsyncTask(getActivity(), new FinishCallback<String>() {
                        @Override
                        public void onFinish(String filePath) {
                            boolean success = !TextUtils.isEmpty(filePath);
                            String resultMsg;
                            if (success) {
                                setBackupSuccessResult();
                                resultMsg = String.format(getString(R.string.msg_finished), getString(R.string.str_backup));
                                resultMsg += String.format(getString(R.string.msg_saved_in), filePath);
                                //Display the result
                                UtilsView.alert(getActivity(), resultMsg, (dialog, which) -> {
                                    if (finishOnBackUpSuccess) {
                                        getActivity().finish();
                                    }
                                });
                            } else {
                                if (getActivity() != null) {
                                    getActivity().setResult(Activity.RESULT_CANCELED);
                                    resultMsg = String.format(getString(R.string.msg_failed), getString(R.string.str_backup));
                                    // Display the result
                                    UtilsView.alert(getActivity(), resultMsg);
                                }
                            }

                        }
                    }).execute(dir);
                }
                break;

        }

    }

    private void setBackupSuccessResult() {
        if (getActivity() != null) {
            getActivity().setResult(
                    Activity.RESULT_OK,
                    new Intent().putExtra(KEY_BACKUP_RESULT, true)
            );
        }
    }

    /**
     * Returns true if the app has write storage permission.
     */
    private boolean checkWriteStoragePermission() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (Environment.isExternalStorageManager()) {
                return true;
            }

            UtilsView.alert(
                    getContext(),
                    getString(R.string.msg_permission_not_granted),
                    (dialog, which) -> {
                        AnalyticsService.INSTANCE
                                .logEvent("didClickOnOkToGrantFilePermission");
                        // Request manage all files permission at runtime
                        Uri uri = Uri.parse("package:" + BuildConfig.APPLICATION_ID);
                        startActivityForResult(
                                new Intent(ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, uri),
                                REQUEST_CODE_MANAGE_FILES_PERMISSIONS
                        );
                    },
                    (dialog, which) -> {
                        AnalyticsService.INSTANCE
                                .logEvent("didClickOnCancelToGrantFilePermission");
                        dialog.dismiss();
                    }
            );

            return false;
        } else if (
                // Check WRITE_EXTERNAL_STORAGE permission for Android OS older than R
                ActivityCompat.checkSelfPermission(
                        requireActivity(),
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                UtilsView.alert(
                        getContext(),
                        getString(R.string.msg_permission_not_granted),
                        // Pressed Positive Button
                        (dialog, which) -> {
                            AnalyticsService.INSTANCE
                                    .logEvent("didClickOnOkToGrantFilePermission");
                            // Request WRITE_EXTERNAL_STORAGE permission for Android OS >= M and <R
                            requireActivity().requestPermissions(
                                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                    REQUEST_PERMISSIONS_WRITE_STORAGE
                            );
                        },
                        // Pressed Negative Button
                        (dialog, which) -> {
                            AnalyticsService.INSTANCE
                                    .logEvent("didClickOnCancelToGrantFilePermission");
                            dialog.dismiss();
                        }
                );
            }

            return false;
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode,
            @NonNull String[] permissions,
            @NonNull int[] grantResults
    ) {
        if (requestCode == REQUEST_PERMISSIONS_WRITE_STORAGE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                UtilsView.alert(
                        getContext(),
                        getString(R.string.msg_permission_granted)
                );
            } else {
                UtilsView.alert(
                        getContext(),
                        getString(R.string.msg_permission_not_granted)
                );
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
