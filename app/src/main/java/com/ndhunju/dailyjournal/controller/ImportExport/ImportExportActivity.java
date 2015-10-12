package com.ndhunju.dailyjournal.controller.importExport;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.LockService;
import com.ndhunju.dailyjournal.controller.folderPicker.FolderPickerDialogFragment;
import com.ndhunju.dailyjournal.controller.folderPicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.JsonConverter;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.ArrayList;
import java.util.List;

public class ImportExportActivity extends Activity implements OnDialogBtnClickedListener {

    private static final String TAG = ImportExportActivity.class.getSimpleName();


    //Request codes with random values
    private static final int REQUEST_CODE_PICK_JSON = 1235;
    private static final int REQUEST_CODE_PICK_BACKUP = 8489;
    private static final int REQUEST_CODE_BACKUP_DIR = 3561;

    //For Google Drive Api
    private static final int REQUEST_CODE_GDRIVE_CREATOR = 1185;
    private static final int REQUEST_CODE_GDRIVE_PICKER = 1189;
    private static final int REQUEST_CODE_GDRIVE_RESOLUTION = 1258;
    private static final int REQUEST_CODE_BACKUP_DIR_PRINTABLE = 1264;


    private GoogleApiClientManager mGoogleClientMgr;
    private static Handler UIHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_import);

        //Wire up widgets
        //Full Back to Google Drive Button
        ((Button) findViewById(R.id.activity_import_export_full_backup_google_drive_btn))
        .setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });mGoogleClientMgr = new GoogleApiClientManager(ImportExportActivity.this);
        //Initaite the connection to create a file in drive
        mGoogleClientMgr.connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
            @Override
            public void onConnected(Bundle bundle) {
                new UploadBackUpToGDriveAsync(ImportExportActivity.this,
                        mGoogleClientMgr.getGoogleApiClient(), REQUEST_CODE_GDRIVE_CREATOR).execute();
            }

            @Override
            public void onConnectionSuspended(int i) {
            }
        }, REQUEST_CODE_GDRIVE_RESOLUTION);

        //Restore from google drive
        ((Button) findViewById(R.id.activity_import_export_restore__google_drive_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mGoogleClientMgr =  new GoogleApiClientManager(ImportExportActivity.this);
                        UtilsView.alert(ImportExportActivity.this, getString(R.string.warning_restore),
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
                                                            startIntentSenderForResult(intentSender, REQUEST_CODE_GDRIVE_PICKER, null, 0, 0, 0);
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
                    }
                });

        ((Button) findViewById(R.id.activity_import_export_full_backup_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR);
                        dpdf.show(getFragmentManager(), TAG);
                    }
                });

        ((Button) findViewById(R.id.activity_import_export_restore_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        UtilsView.alert(ImportExportActivity.this, getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setType(UtilsFile.BACK_FILE_TYPE);
                                        startActivityForResult(intent, REQUEST_CODE_PICK_BACKUP);
                                    }
                                }, null);
                    }
                });

        ((Button)findViewById(R.id.activity_import_export_restore_from_auto_backup_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        UtilsView.alert(ImportExportActivity.this, getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int pos) {
                                        new AutoBackupAlertDialog(ImportExportActivity.this)
                                                .create().show();
                                    }
                                }, null);
                    }
                });

        ((Button) findViewById(R.id.activity_import_export_export_printable_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FolderPickerDialogFragment dpdf = FolderPickerDialogFragment.newInstance(null, REQUEST_CODE_BACKUP_DIR_PRINTABLE);
                dpdf.show(getFragmentManager(), TAG);
            }
        });

        //Disabling Import JSON File feature as it need some technical knowledge. Not user friendly
        ((Button) findViewById(R.id.activity_import_export_restore_json_from_sd_btn)).setVisibility(View.GONE);

        //So far it is used only for update progressbar
         UIHandler = new Handler() {

            @Override
            public void handleMessage(Message msg) {
            }
        };


    }


    @Override
    protected void onPause() {
        super.onPause();

        //onPause() is called when a dialog for Google Drive is open
        //which disconnects ApiClient. However it is needed later
        // mGoogleClientMgr.disconnect();

        //Update lock time
        LockService.updatePasscodeTime();
    }

    @Override
    protected void onDestroy() {
        if (mGoogleClientMgr != null) mGoogleClientMgr.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Check if this activity was called to import the old data ( data saved by v3.1)
        boolean importOldData = getIntent().getBooleanExtra(Constants.KEY_IMPORT_OLD_DATA, false);
        if (importOldData) new TransferOldDataAsyncTask(ImportExportActivity.this).execute();

        //check pass code
        LockService.checkPassCode(ImportExportActivity.this);

    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        String msg = "";
        setResult(resultCode);
        switch (requestCode) {
            case REQUEST_CODE_GDRIVE_CREATOR:   // Called after the file is created in the google drive.
                //Format the message
                msg = String.format(getString(R.string.msg_exporting), resultCode == RESULT_OK
                        ? getString(R.string.str_finished) : getString(R.string.str_failed));
                UtilsView.alert(ImportExportActivity.this, msg);

                break;

            case REQUEST_CODE_GDRIVE_RESOLUTION:    //Called after google drive connection issue has been resolved
                Log.i(TAG, "Activity result request code resolution");
                // Make sure the app is not already connected or attempting to connect
                mGoogleClientMgr.connect();
                break;

            //Currently this feature is disabled. User needs some technical knowledge to use this
            case REQUEST_CODE_PICK_JSON:     //Called when the user has picked a json file to restore data from
                if (resultCode != RESULT_OK) {
                    msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                    UtilsView.alert(ImportExportActivity.this, msg);
                    return;
                }

                Uri selectedJsonFile = data.getData();
                JsonConverter converter = JsonConverter.getInstance(Services.getInstance(getBaseContext()));
                boolean success = converter.parseJSONFile((selectedJsonFile.getPath()));

                //Prepare msg
                msg = success ? String.format(getString(R.string.msg_finished), getString(R.string.str_backup)) :
                        String.format(getString(R.string.msg_failed), getString(R.string.str_backup));

                UtilsView.alert(ImportExportActivity.this, msg);

                break;

            case REQUEST_CODE_PICK_BACKUP://User picks a backup file from mServices(.dj/.zip) file
                if (resultCode != RESULT_OK || data.getData() == null) {
                    msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                    UtilsView.alert(ImportExportActivity.this, msg);
                    return;
                }

                Uri selectedFile = data.getData();
                new RestoreBackUpAsync(ImportExportActivity.this).execute(selectedFile.getPath());

                break;

            case REQUEST_CODE_GDRIVE_PICKER://User picks a file from Google Drive

                if (resultCode == RESULT_OK) {
                    DriveId driveId = (DriveId)data
                            .getParcelableExtra(OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d(TAG, "Selected file's ID: " + driveId);
                    //Download the file in background thread
                    new FetchBackUpFromGDriveAsync(ImportExportActivity.this, mGoogleClientMgr.getGoogleApiClient()).execute(driveId);
                }
                break;

        }
    }


    @Override
    public void onDialogBtnClicked(Intent data, int whichBtn, int result, int requestCode) {

        switch (requestCode) {
            case REQUEST_CODE_BACKUP_DIR:
                if (result != Activity.RESULT_OK)
                    UtilsView.toast(getBaseContext(), getString(R.string.str_failed));
                if (whichBtn == OnDialogBtnClickedListener.BUTTON_POSITIVE) {
                    data.getData();
                    String dir = data.getStringExtra(FolderPickerDialogFragment.KEY_CURRENT_DIR);
                    new BackUpAsyncTask(ImportExportActivity.this).execute(dir);
                }
                break;

            case REQUEST_CODE_BACKUP_DIR_PRINTABLE:
                if (result != Activity.RESULT_OK)
                    UtilsView.toast(getBaseContext(), getString(R.string.str_failed));
                if (whichBtn == OnDialogBtnClickedListener.BUTTON_POSITIVE) {
                    data.getData();
                    final String dir = data.getStringExtra(FolderPickerDialogFragment.KEY_CURRENT_DIR);
                    final Services services = Services.getInstance(ImportExportActivity.this);

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
                                            new ExportPartiesReportAsync(ImportExportActivity.this, dir).execute(parties);
                                            break;

                                        case 1: //Select parties
                                            createPartySelectDialog(services, parties, dir).show();
                                            break;

                                    }
                                }
                            }).create();

                    chooseDialog.show();

                }

                break;

        }

    }

    private AlertDialog createPartySelectDialog(Services services, final List<Party> parties, final String dir) {
        final ArrayList<Party> selectedParties = new ArrayList<>();

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getString(R.string.msg_choose, getString(R.string.str_contact)));
        builder.setNegativeButton(getString(android.R.string.cancel), null);
        builder.setMultiChoiceItems(services.getPartyNameAsArray(), null,
                new DialogInterface.OnMultiChoiceClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i, boolean b) {
                        //Add checked contacts into importContacts list
                        if (b) selectedParties.add(parties.get(i));
                        else selectedParties.remove(parties.get(i));
                    }
                });
        builder.setPositiveButton(getString(android.R.string.ok),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        new ExportPartiesReportAsync(ImportExportActivity.this, dir).execute(selectedParties);
                    }
                });

        return builder.create();
    }

    private Activity getActivity(){
        return ImportExportActivity.this;
    }
}