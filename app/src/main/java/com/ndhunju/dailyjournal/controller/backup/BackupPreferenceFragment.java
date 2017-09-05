package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v4.content.ContextCompat;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.FolderPickerDialogFragment;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

/**
 * Created by dhunju on 10/8/2015.
 * This fragment allows users to create/restore backup from Google drive/SD Card
 * as well as set Automatic Backup and erase all data
 */
public class BackupPreferenceFragment extends PreferenceFragment implements OnDialogBtnClickedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PreferenceFragment.class.getSimpleName();


    //Request codes with random values
    private static final int REQUEST_CODE_PICK_JSON = 1235;
    private static final int REQUEST_CODE_PICK_BACKUP = 8489;
    private static final int REQUEST_CODE_BACKUP_DIR = 3561;

    private PreferenceService preferenceService;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Without this android creates default xml file
        getPreferenceManager().setSharedPreferencesName(PreferenceService.DEF_NAME_SHARED_PREFERENCE);

        addPreferencesFromResource(R.xml.preference_backup);

        //load the preference file
        preferenceService = PreferenceService.from(getActivity());


        findPreference(getString(R.string.key_pref_backup_google_drive))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        startActivity(new Intent(getActivity(), GoogleDriveUploadBackupActivity.class));
                        return true;
                    }
                });

        findPreference(getString(R.string.key_pref_restore_google_drive))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        startActivity(new Intent(getActivity(), GoogleDriveRestoreBackupActivity.class));
                                    }
                                }, null);
                        return true;
                    }
                });

        findPreference(getString(R.string.key_pref_backup_sd))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR);
                        dpdf.setTargetFragment(BackupPreferenceFragment.this, 0);
                        dpdf.show(getFragmentManager(), TAG);
                        return true;
                    }
                });

        findPreference(getString(R.string.key_pref_restore_sd))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setType(UtilsFile.BACK_FILE_TYPE);
                                        startActivityForResult(intent, REQUEST_CODE_PICK_BACKUP);
                                    }
                                }, null);
                        return true;
                    }
                });

        findPreference(getString(R.string.key_pref_restore_auto_backup))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int pos) {
                                        new AutoBackupAlertDialog(getActivity())
                                                .create().show();
                                    }
                                }, null);
                        return  true;
                    }
                });

    }

    @Override
    public void onResume() {
        super.onResume();
        //block the previous view
        getView().setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.white));
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
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
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        String msg = "";
        getActivity().setResult(resultCode);
        switch (requestCode) {

            //Currently this feature is disabled. User needs some technical knowledge to use this
            case REQUEST_CODE_PICK_JSON:     //Called when the user has picked a json file to restore data from
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

                Uri selectedFile = data.getData();
                new RestoreBackUpAsync(getActivity()).execute(selectedFile.getPath());

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
                    new BackUpAsyncTask(getActivity()).execute(dir);
                }
                break;

        }

    }

}
