package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.json.JsonConverterString;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;
import com.ndhunju.folderpicker.FolderPickerDialogFragment;
import com.ndhunju.folderpicker.OnDialogBtnClickedListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dhunju on 10/8/2015.
 * This Activity allows users to create/restore backup from Google drive/SD Card
 * as well as set Automatic Backup and erase all data
 */
public class BackupPreferenceFragment extends PreferenceFragment implements OnDialogBtnClickedListener, SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String TAG = PreferenceFragment.class.getSimpleName();


    //Request codes with random values
    private static final int REQUEST_CODE_PICK_JSON = 1235;
    private static final int REQUEST_CODE_PICK_BACKUP = 8489;
    private static final int REQUEST_CODE_BACKUP_DIR = 3561;

    //For Google Drive Api
    private static final int REQUEST_CODE_GDRIVE_CREATOR = 1185;
    public static final int REQUEST_CODE_GDRIVE_PICKER = 1189;
    public static final int REQUEST_CODE_GDRIVE_RESOLUTION = 1258;
    private static final int REQUEST_CODE_BACKUP_DIR_PRINTABLE = 1264;


    private GoogleApiClientManager mGoogleClientMgr;
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
                        mGoogleClientMgr = new GoogleApiClientManager(getActivity());
                        //Initiate the connection to create a file in drive
                        mGoogleClientMgr.connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
                            @Override
                            public void onConnected(Bundle bundle) {
                                new UploadBackUpToGDriveAsync(getActivity(),
                                        mGoogleClientMgr.getGoogleApiClient(), REQUEST_CODE_GDRIVE_CREATOR).execute();
                            }

                            @Override
                            public void onConnectionSuspended(int i) {
                            }
                        }, REQUEST_CODE_GDRIVE_RESOLUTION);

                        return true;
                    }

                });

        findPreference(getString(R.string.key_pref_restore_google_drive))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        mGoogleClientMgr = new GoogleApiClientManager(getActivity());
                        UtilsView.alert(getActivity(), getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //TODO It's taking too long to download file and progress bar is not working either
                                        //connect to pick a file from drive
                                        mGoogleClientMgr
                                                .connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
                                                    @Override
                                                    public void onConnected(Bundle bundle) {
                                                        IntentSender intentSender = Drive
                                                                .DriveApi
                                                                .newOpenFileActivityBuilder()
                                                                .setMimeType(new String[]{UtilsFile.BACK_FILE_TYPE})
                                                                .build(mGoogleClientMgr.getGoogleApiClient());
                                                        try {
                                                            getActivity().startIntentSenderForResult(intentSender, REQUEST_CODE_GDRIVE_PICKER, null, 0, 0, 0);
                                                        } catch (IntentSender.SendIntentException e) {
                                                            Log.w(TAG, "Unable to send intent", e);
                                                        }
                                                    }

                                                    @Override
                                                    public void onConnectionSuspended(int i) {
                                                        Log.i(TAG, "GoogleApiClient connection suspended. Cause = " + i);
                                                    }
                                                }, REQUEST_CODE_GDRIVE_RESOLUTION);
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

        findPreference(getString(R.string.key_pref_export_printable))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR_PRINTABLE);
                        dpdf.setTargetFragment(BackupPreferenceFragment.this, 0);
                        dpdf.show(getFragmentManager(), TAG);
                        return false;
                    }
                });

        findPreference(getString(R.string.key_pref_erase_all))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.msg_erase_all), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new EraseAllAsyncTask(getActivity()).execute();
                            }
                        }, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        });
                        return true;
                    }
                });

        findPreference(getString(R.string.key_pref_erase_journals))
                .setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        UtilsView.alert(getActivity(), getString(R.string.msg_erase_journals), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                new EraseJournalsAsyncTask(getActivity()).execute();
                            }
                        }, null);
                        return true;
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        //block the preview view
        getView().setBackgroundColor(getResources().getColor(android.R.color.white));
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
            case REQUEST_CODE_GDRIVE_CREATOR:   // Called after the file is created in the google drive.
                //Format the message
                msg = String.format(getString(R.string.msg_exporting), resultCode == Activity.RESULT_OK
                        ? getString(R.string.str_finished) : getString(R.string.str_failed));
                UtilsView.alert(getActivity(), msg);

                break;

            case REQUEST_CODE_GDRIVE_RESOLUTION:    //Called after google drive connection issue has been resolved
                Log.i(TAG, "Activity result request code resolution");
                // Make sure the app is not already connected or attempting to connect
                mGoogleClientMgr.connect();
                break;

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

            case REQUEST_CODE_GDRIVE_PICKER://User picks a file from Google Drive

                if (resultCode == Activity.RESULT_OK) {
                    DriveId driveId = (DriveId) data
                            .getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d(TAG, "Selected file's ID: " + driveId);
                    //Download the file in background thread
                    new FetchBackUpFromGDriveAsync(getActivity(), mGoogleClientMgr.getGoogleApiClient()).execute(driveId);
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
                    new BackUpAsyncTask(getActivity()).execute(dir);
                }
                break;

            case REQUEST_CODE_BACKUP_DIR_PRINTABLE:
                if (result != Activity.RESULT_OK)
                    UtilsView.toast(getActivity(), getString(R.string.str_failed));
                if (whichBtn == OnDialogBtnClickedListener.BUTTON_POSITIVE) {
                    data.getData();
                    final String dir = data.getStringExtra(FolderPickerDialogFragment.KEY_CURRENT_DIR);
                    final Services services = Services.getInstance(getActivity());

                    // let the user choose the type of printable she wants to export
                    String[] options = ExportPartiesReportAsync.getStrTypes();
                    new AlertDialog.Builder(getActivity()).setItems(options,
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int optionIndex) {
                                    createAllOrSelectPartyDialog(services, dir, optionIndex);
                                }
                            })
                            .create().show();
                }

                break;

        }

    }

    private void createAllOrSelectPartyDialog(final Services services, final String dir, final int optionIndex) {
        //Let the user choose the parties
        final List<Party> parties = services.getParties();

        CharSequence[] options = getResources().getStringArray(R.array.options_export_print);
        AlertDialog chooseDialog = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.str_choose))
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0: //All parties
                                new ExportPartiesReportAsync(getActivity(), dir, ExportPartiesReportAsync.Type.values()[optionIndex]).execute(parties);
                                break;

                            case 1: //Select parties
                                createPartySelectDialogToExport(services, parties, dir, optionIndex).show();
                                break;

                        }
                    }
                }).create();

        chooseDialog.show();
    }

    private AlertDialog createPartySelectDialogToExport(Services services, final List<Party> parties, final String dir, final int optionIndex) {
        final ArrayList<Party> selectedParties = new ArrayList<>();

        // create array of Parties' name
        String[] allParties = new String[parties.size()];
        for (int i = 0; i < parties.size(); i++)
            allParties[i] = parties.get(i).getName();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.msg_choose, getString(R.string.str_contact)));
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.setMultiChoiceItems(allParties, null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        //Add checked contacts into selectedParties list
                        if (b) selectedParties.add(parties.get(i));
                        else selectedParties.remove(parties.get(i));
                    }
                });
        builder.setPositiveButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new ExportPartiesReportAsync(getActivity(), dir, ExportPartiesReportAsync.Type.values()[optionIndex]).execute(selectedParties);
                    }
                });

        return builder.create();
    }

    @Override
    public void onDestroy() {
        if (mGoogleClientMgr != null) mGoogleClientMgr.disconnect();
        super.onDestroy();
    }
}
