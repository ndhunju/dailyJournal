package com.ndhunju.dailyjournal.controller.ImportExport;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.FolderPicker.FolderPickerDialogFragment;
import com.ndhunju.dailyjournal.controller.FolderPicker.OnDialogBtnClickedListener;
import com.ndhunju.dailyjournal.controller.LockScreenActivity;
import com.ndhunju.dailyjournal.service.Constants;
import com.ndhunju.dailyjournal.service.JsonConverter;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFile;
import com.ndhunju.dailyjournal.service.UtilsView;

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


    private GoogleApiClientManager mGoogleClientMgr;
    private android.os.Handler UIHandler; //Used to update UI

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
                mGoogleClientMgr = GoogleApiClientManager.getInstance(ImportExportActivity.this);
                //Initaite the connection to create a file in drive
                mGoogleClientMgr.connectGoogleApiClient(new GoogleApiClient.ConnectionCallbacks() {
                @Override
                public void onConnected(Bundle bundle) {
                    new UploadBackUpToGDriveAsync(ImportExportActivity.this,
                        mGoogleClientMgr.getGoogleApiClient(), REQUEST_CODE_GDRIVE_CREATOR).execute();
                    }
                        @Override
                        public void onConnectionSuspended(int i) {}
                    }, REQUEST_CODE_GDRIVE_RESOLUTION);
                }
            });

        //Restore from google drive
        ((Button) findViewById(R.id.activity_import_export_restore__google_drive_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mGoogleClientMgr = GoogleApiClientManager.getInstance(ImportExportActivity.this);
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

        //Disabling Import JSON File feature as it need some technical knowledge. Not user friendly
        ((Button) findViewById(R.id.activity_import_export_restore_json_from_sd_btn)).setVisibility(View.INVISIBLE);
        /*.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/.json");
                startActivityForResult(intent, REQUEST_CODE_PICK_JSON);
            }
        });*/

        //So far it is used only for update progressbar
        UIHandler = new android.os.Handler() {

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
        LockScreenActivity.updatePasscodeTime();
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
        if (importOldData) new ImportOldDataAsyncTask(ImportExportActivity.this).execute();

        //check pass code
        LockScreenActivity.checkPassCode(ImportExportActivity.this);

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
                JsonConverter converter = new JsonConverter(Services.getInstance(getBaseContext()));
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

        }

    }
}
