package com.ndhunju.dailyjournal.controller;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi.DriveContentsResult;
import com.google.android.gms.drive.DriveContents;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.OpenFileActivityBuilder;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.model.Party;
import com.ndhunju.dailyjournal.model.Storage;
import com.ndhunju.dailyjournal.model.Utils;

import org.json.JSONArray;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;

public class ImportExportActivity extends Activity {

    private static final String TAG = ImportExportActivity.class.getCanonicalName();
    private static final String BACK_FILE_TYPE = "application/zip";


    public static final int REQUEST_CODE_PICK_JSON = 1;
    public static final int REQUEST_CODE_PICK_BACKUP = 8;

    //For Google Drive Api
    private static final int REQUEST_CODE_GDRIVE_CREATOR = 1185;
    private static final int REQUEST_CODE_GDRIVE_PICKER = 1189;
    private static final int REQUEST_CODE_RESOLUTION = 1258;

    //Declare GoogleApiClient to use Google Drive
    private GoogleApiClient mGoogleApiClient;
    private int mRequestCode;
    ProgressDialog gDrivePd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_export_import);

        //Wire up widgets
        ((Button) findViewById(R.id.activity_import_export_full_backup_google_drive_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //Connect  to create a file in drive
                        connectGoogleApiClient(REQUEST_CODE_GDRIVE_CREATOR);
                    }
                });


        ((Button) findViewById(R.id.activity_import_export_restore__google_drive_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.alert(ImportExportActivity.this, getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        //connect to pick a file from drive
                                        connectGoogleApiClient(REQUEST_CODE_GDRIVE_PICKER);
                                    }
                                });
                    }
                });

        ((Button) findViewById(R.id.activity_import_export_full_backup_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        createBackUpToSdCard();
                    }
                });

        ((Button) findViewById(R.id.activity_import_export_restore_btn))
                .setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Utils.alert(ImportExportActivity.this, getString(R.string.warning_restore),
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {
                                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                                        intent.setType(BACK_FILE_TYPE);
                                        startActivityForResult(intent, REQUEST_CODE_PICK_BACKUP);
                                    }
                                });
                    }
                });

        ((Button) findViewById(R.id.activity_import_export_restore_json_from_sd_btn)).setVisibility(View.INVISIBLE);
        /*.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("file/.json");
                startActivityForResult(intent, REQUEST_CODE_PICK_JSON);
            }
        });*/
    }

    private void connectGoogleApiClient(int requestCode) {
        mRequestCode = requestCode; //this is used later at onConnected to distinguish
        mGoogleApiClient = new GoogleApiClient.Builder(ImportExportActivity.this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "API client connected.");
                        switch (mRequestCode) {
                            case REQUEST_CODE_GDRIVE_CREATOR:
                                gDrivePd = new ProgressDialog(ImportExportActivity.this);
                                gDrivePd.setIndeterminate(true);
                                gDrivePd.setMessage(String.format(getString(R.string.msg_creating), getString(R.string.str_backup)));
                                gDrivePd.setCancelable(false);
                                gDrivePd.setCanceledOnTouchOutside(false);
                                gDrivePd.show();
                                saveFileToGDrive();
                                break;

                            case REQUEST_CODE_GDRIVE_PICKER:
                                IntentSender intentSender = Drive.DriveApi
                                        .newOpenFileActivityBuilder()
                                        .setMimeType(new String[]{BACK_FILE_TYPE})
                                        .build(mGoogleApiClient);
                                try {
                                    startIntentSenderForResult(intentSender, REQUEST_CODE_GDRIVE_PICKER, null, 0, 0, 0);
                                } catch (IntentSender.SendIntentException e) {
                                    Log.w(TAG, "Unable to send intent", e);
                                }
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        Log.i(TAG, "GoogleApiClient connection suspended");
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        // Called whenever the API client fails to connect.
                        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
                        if (!result.hasResolution()) {
                            // show the localized error dialog.
                            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), ImportExportActivity.this, 0).show();
                            return;
                        }
                        // The failure has a resolution. Resolve it.
                        // Called typically when the app is not yet authorized, and an
                        // authorization
                        // dialog is displayed to the user.
                        try {
                            result.startResolutionForResult(ImportExportActivity.this, REQUEST_CODE_RESOLUTION);
                            Log.i(TAG, "Resolving resolution");
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Exception while starting resolution activity", e);
                        }
                    }
                })
                .build();

        mGoogleApiClient.connect();
    }

    @Override
    protected void onPause() {
        //commenting out because onPause is called when a dialog for Google Drive is called
        //which disconnects ApiClient. However it is needed later
        //  mGoogleApiClient.disconnect();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (mGoogleApiClient != null) mGoogleApiClient.disconnect();
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        String msg = "";
        switch (requestCode) {
            case REQUEST_CODE_GDRIVE_CREATOR:
                // Called after a file is saved to Drive.
                gDrivePd.cancel();
                msg = String.format(getString(R.string.msg_exporting),
                        resultCode == RESULT_OK ?getString(R.string.str_finished): getString(R.string.str_failed));
                Utils.alert(ImportExportActivity.this, msg);
                break;

            case REQUEST_CODE_RESOLUTION:
                //Called after the issue has been resolved
                Log.i(TAG, "Activity result request code resolution");
                // Make sure the app is not already connected or attempting to connect
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected())
                    mGoogleApiClient.connect();
                break;

            //Currently this feature is disabled
            case REQUEST_CODE_PICK_JSON:
                //Called when the user has picked a json file to restore data from
                if(resultCode != RESULT_OK) {
                    msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                    Utils.alert(ImportExportActivity.this, msg);
                    return;
                }

                Uri selectedJsonFile = data.getData();
                boolean success = parseJSONFile((selectedJsonFile.getPath()), false);
                //Prepare msg
                msg = success ? String.format(getString(R.string.msg_finished), getString(R.string.str_backup)) :
                        String.format(getString(R.string.msg_failed), getString(R.string.str_backup));

                Utils.alert(ImportExportActivity.this, msg);

                break;

            case REQUEST_CODE_PICK_BACKUP:
                //Called when the user has picked a backup (.dj/.zip) file
                if(resultCode != RESULT_OK || data.getData() == null) {
                    msg = String.format(getString(R.string.msg_importing), getString(R.string.str_failed));
                    Utils.alert(ImportExportActivity.this, msg);
                    return;
                }
                Uri selectedFile = data.getData();
                restoreBackUp(selectedFile.getPath());
                break;

            case REQUEST_CODE_GDRIVE_PICKER:

                if (resultCode == RESULT_OK) {
                    DriveId driveId = (DriveId) data.getParcelableExtra(
                            OpenFileActivityBuilder.EXTRA_RESPONSE_DRIVE_ID);
                    Log.d(TAG, "Selected file's ID: " + driveId);
                    new RestoreFromGoogleDriveAsyncTask(this).execute(driveId);
                }
                break;

        }
    }

    /**
     * Creates a new backup file on a local drive and saves it to Drive.
     */
    private void saveFileToGDrive() {

        new ApiClientAsyncTask<Void, Void, Boolean>(ImportExportActivity.this) {
            boolean success;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected Boolean doInBackgroundConnected(Void... params) {

                //the loading bar hangs up , so putting it up before saveFileToGDrive() is called
                /*runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        gDrivePd = new ProgressDialog(ImportExportActivity.this);
                        gDrivePd.setIndeterminate(true);
                        gDrivePd.setMessage(String.format(getString(R.string.msg_creating), getString(R.string.str_backup)));
                        gDrivePd.setCancelable(false);
                        gDrivePd.setCanceledOnTouchOutside(false);
                        gDrivePd.show();
                    }
                });*/

                //Create a new Content in google drive and set a callback
                Drive.DriveApi.newDriveContents(mGoogleApiClient)
                        .setResultCallback(new ResultCallback<DriveContentsResult>() {

                            @Override
                            public void onResult(DriveContentsResult result) {
                                // If the operation was not successful, notify user
                                if (!result.getStatus().isSuccess()) {
                                    Log.i(TAG, "Failed to create new backup: " + result.getStatus().getStatusMessage());
                                    return;
                                }
                                // Otherwise, write our data to the new contents.
                                Log.i(TAG, "Content created.");

                                // Get an output stream for the contents.
                                OutputStream outputStream = result.getDriveContents().getOutputStream();

                                File backUpfile = null;

                                try {
                                    //Create a new full backup of data into local drive
                                    String filePath = createBackUp(false);
                                    backUpfile = new File(filePath);
                                    outputStream.write(Utils.read(backUpfile));

                                    // Create the initial metadata - MIME type and title.
                                    // Note that the user will be able to change the title later.
                                    MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                            .setTitle(backUpfile.getName())
                                            .setMimeType(BACK_FILE_TYPE).build();


                                    // Create an intent for the file chooser, and start it.
                                    IntentSender intentSender = Drive.DriveApi
                                            .newCreateFileActivityBuilder()
                                            .setInitialMetadata(metadataChangeSet)
                                            .setInitialDriveContents(result.getDriveContents())
                                            .setActivityTitle(backUpfile.getName())
                                            .build(mGoogleApiClient);

                                    success = true;
                                    startIntentSenderForResult(intentSender, REQUEST_CODE_GDRIVE_CREATOR, null, 0, 0, 0);

                                } catch (IOException e1) {
                                    Log.i(TAG, "Unable to write file contents.");
                                } catch (IntentSender.SendIntentException e) {
                                    Log.i(TAG, "Failed to launch file chooser.");
                                    e.printStackTrace();
                                }
                            }
                        });

                return success;
            }

            @Override
            protected void onPostExecute(final Boolean result) {
                super.onPostExecute(success);
            }
        }.execute();
    }

    /**
     * Creates a JSON file containing the data into predefined (Utils.getAppFolder()) path
     * in sd card. <b>It deletes the old JSON file</b>
     * @return If successful, returns the absolute path of the file. Otherwise, null.
     */
    public String createJSONFile() throws IOException {

        try {
            // Create a app folder
            File appFolder = Utils.getAppFolder(ImportExportActivity.this);

            //Store old json files in an array so that it can be deleted once
            //new json file is created succesfully
            ArrayList<File> filesToDelete = new ArrayList<>();
            for(File f : appFolder.listFiles()){
                if(f.getName().endsWith(".json"))
                    filesToDelete.add(f);
            }

            String fileName = "dailyJournal-" + Utils.parseDate(new Date(), Utils.DATE_FORMAT_FOR_FILE) + ".json";
            File jsonFile = new File(appFolder.getAbsolutePath(),fileName );

            if(!jsonFile.createNewFile())
                Log.d(TAG, "Failed to create file " + jsonFile.getAbsolutePath());

            FileOutputStream fileOutputStream = new FileOutputStream(jsonFile.getAbsoluteFile());
            fileOutputStream.write(getJSONDb().toString().getBytes());
            fileOutputStream.close();

            // Let the device know that a new file has been created so that it appears in the computer when connected via usb
            MediaScannerConnection.scanFile(this,
                    new String[]{jsonFile.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i("ExternalStorage", "Scanned " + path + ":");
                            Log.i("ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Log.i("json backup", "JSON backup created");

            for(File f : filesToDelete){
                f.delete();
            }

            return jsonFile.getAbsolutePath();

        } catch (IOException e) {
            Log.w("TAG", "Error creating json backup file: " + e.getMessage());
            throw e;
        }
    }


    /**
     * Creates full backup (.json file + attachments) into the app folder
     * @return Full path of the file if successful otherwise null.
     */
    public void createBackUpToSdCard() {

        new AsyncTask<Void, Void, String>() {
            ProgressDialog pd;
            String filePath = null;

            @Override
            protected void onPreExecute() {
                String msg = String.format(getString(R.string.msg_creating), getString(R.string.str_backup));
                pd  = new ProgressDialog(ImportExportActivity.this);
                pd.setIndeterminate(true);
                pd.setMessage(msg);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();
            }

            @Override
            protected String doInBackground(Void... Void) {
                try {
                    filePath = createBackUp(true);
                } catch (IOException e) {
                    Log.w("TAG", "Error creating backup file: " + e.getMessage());
                } finally {
                    return filePath;
                }
            }

            @Override
            protected void onPostExecute(String result) {
                //End progress bar
                pd.cancel();

                String resultMsg =  result != "" ?
                        String.format(getString(R.string.msg_finished), getString(R.string.str_backup))
                        : String.format(getString(R.string.msg_failed), getString(R.string.str_backup));

                resultMsg += String.format(getString(R.string.msg_saved_in), getString(R.string.backup_dir));

                //Display the result
                Utils.alert(ImportExportActivity.this, resultMsg);
            }
        }.execute();


    }

    public String createBackUp(boolean inExtDir) throws IOException {
        //1.Create JSON with latest data
        createJSONFile();

        //2. Zip app folder  as all the attachments and json file are here
        //2.1 Get the app folder
        File directoryToZip = Utils.getAppFolder(ImportExportActivity.this);

        //2.2 get App Folder that is not hidden. Backup file will be created here
        File appFolder = inExtDir ? Utils.getAppFolder(false) : Utils.getCacheDir(ImportExportActivity.this);


        //2.3 create a zip file in not hidden app folder so that user can use it
        String fileName = "dailyJournal-" + Utils.parseDate(new Date(), Utils.DATE_FORMAT_FOR_FILE) + Utils.ZIP_EXT;
        File zipFile = new File(appFolder.getAbsoluteFile(), fileName);
        zipFile.createNewFile();

        //3 zip the directory file into zipFile
        Utils.zip(directoryToZip, zipFile);

        if(inExtDir) {
            //let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(ImportExportActivity.this, new String[]{appFolder.getAbsolutePath(), zipFile.getAbsolutePath()}, null, null);
        }

        Log.i("backup", "backup created");

        return zipFile.getAbsolutePath();
    }

    /**
     * Restores the data from zipped file
     * @param filePath
     */
    public void restoreBackUp(String filePath){

        //Check if the file extension is correct
        if(!(filePath.endsWith(Utils.ZIP_EXT) || filePath.endsWith(Utils.ZIP_EXT_OLD))){
            Utils.alert(this, getString(com.ndhunju.dailyjournal.R.string.warning_ext_mismatch));
            return;
        }

        new AsyncTask<String, Void, Boolean>() {
            //Show progress bar
            ProgressDialog pd;

            @Override
            protected void onPreExecute() {
                String msg = String.format(getString(R.string.msg_importing),getString(R.string.str_backup));
                pd  = new ProgressDialog(ImportExportActivity.this);
                pd.setIndeterminate(true);
                pd.setMessage(msg);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();
            }

            @Override
            protected void onPostExecute(Boolean result) {
                //End progress bar
                pd.cancel();
                //Display msg
                String msg = String.format(getString(R.string.msg_restored), getString(R.string.str_backup));
                Utils.alert(ImportExportActivity.this, msg);
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    //Get the app folder where the data are stored
                    File appFolder = Utils.getAppFolder(ImportExportActivity.this);

                    //Delete old files, attachments
                    Utils.cleanDirectory(appFolder);

                    //Delete existing objects
                    Storage.getInstance(ImportExportActivity.this).getParties().clear();

                    //Unzip the files into app folder
                    Utils.unzip(new File(params[0]), appFolder);

                    //search .json file
                    File[] files = appFolder.listFiles();
                    for(int i = files.length-1; i >= 0 ; i-- )
                        if(files[i].isFile() && files[i].getName().endsWith(".json")){
                            parseJSONFile(files[i].getAbsolutePath(), false); //false to preserve Id
                            //takes the first json file from the last
                            //name of json file has date on it so the latest json file
                            //wil likely be at the bottom of the list
                            break;
                        }

                    //to let know that a new file has been created so that it appears in the computer
                    MediaScannerConnection.scanFile(ImportExportActivity.this, new String[]{ appFolder.getAbsolutePath()}, null, null);


                    return true;

                } catch (Exception e) {
                    Log.w("TAG", "Error creating backup file: " + e.getMessage());
                    return false;
                }
            }
        }.execute(filePath);
    }

    /**
     * Imports the data from zipped file
     * @param filePath
     */
    public void importData(String filePath){

        //Check if the file extension is correct
        if(!(filePath.endsWith(Utils.ZIP_EXT) || filePath.endsWith(Utils.ZIP_EXT_OLD))){
            Utils.alert(this, getString(com.ndhunju.dailyjournal.R.string.warning_ext_mismatch));
            return;
        }

        new AsyncTask<String, Void, Boolean>() {
            //Show progress bar
            ProgressDialog pd;

            protected void onPreExecute() {
                String msg = String.format(getString(R.string.msg_importing),"");
                pd  = new ProgressDialog(ImportExportActivity.this);
                pd.setIndeterminate(true);
                pd.setMessage(msg);
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.show();
            }

            protected void onPostExecute(Boolean result) {
                //End progress bar
                pd.cancel();
                //Display msg
                String msg =  getString(R.string.str_finished);
                Utils.alert(ImportExportActivity.this, msg);
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    //Get the app folder where the data are stored
                    File appFolder = Utils.getAppFolder(ImportExportActivity.this);

                    //Unzip the files into app folder
                    Utils.unzip(new File(params[0]), appFolder);

                    //search .json file
                    File[] files = appFolder.listFiles();
                    for(int i = files.length-1; i >= 0 ; i-- )
                        if(files[i].isFile() && files[i].getName().endsWith(".json")){
                            parseJSONFile(files[i].getAbsolutePath(), true); //true to create new Id
                            //takes the first json file from the last
                            //name of json file has date on it so the latest json file
                            //wil likely be at the bottom of the list
                            break;
                        }

                    //to let know that a new file has been created so that it appears in the computer
                    MediaScannerConnection.scanFile(ImportExportActivity.this, new String[]{ appFolder.getAbsolutePath()}, null, null);

                    //TODO While importing data need to find and merge duplicate parties

                    return true;

                } catch (Exception e) {
                    Log.w("TAG", "Error creating backup file: " + e.getMessage());
                    return false;
                }
            }
        }.execute(filePath);
    }

    /**
     * Returns all the data in JSON format
     * @return
     */
    public JSONArray getJSONDb(){
        JSONArray partyJSONs = new JSONArray();
        for (Party m : Storage.getInstance(this).getParties()) {
            partyJSONs.put(m.toJSON());
        }

        return partyJSONs;

    }

    /**
     * Parses JSON data and creates corresponding objects (eg. Party, Journal etc)
     * @param filePath : path of the json file
     * @return
     */
    public boolean parseJSONFile(String filePath, boolean newId){

        //check if the file extension matches
        if(!filePath.endsWith(".json")){
            Utils.alert(this, getString(com.ndhunju.dailyjournal.R.string.warning_ext_mismatch));
            return false;
        }

        FileInputStream is = null;

        try {
            //Copy the content of file into byte[] and then to string
            //TO:DO Not an efficient way for large file
            is = new FileInputStream(new File(filePath));
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String json = new String(buffer, "UTF-8");
            JSONArray partyJSONArray = new JSONArray(json);
            Storage storage = Storage.getInstance(this);
            for (int i = 0; i < partyJSONArray.length(); i++) {
                Party newParty = Party.fromJSON(partyJSONArray.getJSONObject(i), newId);
                storage.addParty(newParty);
            }
            return true;
        } catch (Exception e) {}
        finally {
            try{
                if(is != null) is.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }

        return false;

    }

    /**
     * https://github.com/googledrive/android-demos/blob/master/src/com/google/android/gms/drive/sample/demo
     * This class retrieves DriveFileContent, make a local copy, uses it to restore the backed up data
     */
    final private class RestoreFromGoogleDriveAsyncTask extends ApiClientAsyncTask<DriveId, Boolean, String> {

        ProgressDialog fileRetrievePd;
        public RestoreFromGoogleDriveAsyncTask(Context context) {
            super(context);
        }

        @Override
        protected String doInBackgroundConnected(DriveId... params) {

            //Show the progress bar
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileRetrievePd = new ProgressDialog(ImportExportActivity.this);
                    fileRetrievePd.setIndeterminate(false); //TODO Still shows indeterminate progress bar
                    fileRetrievePd.setMessage(String.format(getString(R.string.msg_importing), ""));
                    fileRetrievePd.setCanceledOnTouchOutside(false);
                    fileRetrievePd.setMax(100);
                    fileRetrievePd.setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialogInterface) {
                            mGoogleApiClient.disconnect();
                            RestoreFromGoogleDriveAsyncTask.this.cancel(true);
                        }
                    });

                    // Reset progress dialog back to zero as we're
                    // initiating an opening request.
                    fileRetrievePd.setProgress(0);
                    fileRetrievePd.show();
                }
            });

            //Get the DriveFile with given instance of GoogleApiClient and DriveID
            DriveFile driveFile = Drive.DriveApi.getFile(mGoogleApiClient, params[0]);
            DriveContentsResult driveContentsResult = driveFile.open(mGoogleApiClient,
                    DriveFile.MODE_READ_ONLY,
                    new DriveFile.DownloadProgressListener() {
                    @Override
                    public void onProgress(long bytesDownloaded, long bytesExpected) {
                        // Update progress dialog with the latest progress.
                        //Only once called tho
                        final int progress = (int)(bytesDownloaded*100/bytesExpected);
                        Log.d(TAG, String.format("Loading progress: %d percent", progress));
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                fileRetrievePd.setProgress(progress);

                            }
                        });

                    }
                }).await(); //making the process synchronous

            //If unsuccessful, notify the user and return
            if (!driveContentsResult.getStatus().isSuccess()) {
                Utils.alert(ImportExportActivity.this, getString(R.string.str_failed) + ":"
                + driveContentsResult.getStatus().getStatusMessage());
                return null;
            }

            //Get DriveContents to retrieve data from
            DriveContents driveContents = driveContentsResult.getDriveContents();

            //create the file in local drive to store retrieved data
            String fileName = "dailyJournal-" + Utils.parseDate(new Date(), Utils.DATE_FORMAT_FOR_FILE) + Utils.ZIP_EXT;
            File backUpFileFromGDrive = new File(Utils.getCacheDir(ImportExportActivity.this).getAbsolutePath(), fileName );


            //Buffered input streams read data from a memory area known as a buffer;
            //the native input API is called only when the buffer is empty.
            BufferedOutputStream out = null;
            InputStream in = null;

            try {
                out = new BufferedOutputStream(new FileOutputStream(backUpFileFromGDrive));
                in = driveContents.getInputStream();
                int b;
                while ( (b = in.read()) >= 0) {
                    out.write(b);
                }
                //Imp: Close the resources to prevent leakage
                in.close();
                out.close();
                driveContents.discard(getGoogleApiClient());

                //Get the app folder where the data are stored
                File appFolder = Utils.getAppFolder(ImportExportActivity.this);

                //Delete old files, attachments
                Utils.cleanDirectory(appFolder);

                //Delete existing objects
                Storage.getInstance(ImportExportActivity.this).getParties().clear();

                //Unzip the backup file into app folder
                Utils.unzip(backUpFileFromGDrive, appFolder);

                //search .json file
                File[] files = appFolder.listFiles();
                for(int i = files.length-1; i >= 0 ; i-- )
                    if(files[i].isFile() && files[i].getName().endsWith(".json")){
                        parseJSONFile(files[i].getAbsolutePath(), false);
                        //takes the first json file from the last
                        //name of json file has date on it so the latest json file
                        //wil likely be at the bottom of the list
                        break;
                    }

            }catch(Exception ex){
                Log.e(TAG, "Error copying file to local drive");
                ex.printStackTrace();
            }finally {
                //delete the file once the data is restored; Google drive already has a copy
               boolean success =  Utils.deleteFile(backUpFileFromGDrive.getAbsolutePath());
                if(!success) Log.e(TAG, "Deleting backup file from google drive failed.");
                //backUpFileFromGDrive.delete();
            }

            return "success";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result == null) {
                Log.d(TAG, "Error while reading from the file");
                return;
            }
            Log.i(TAG, "Successfully restored from GoogleDrive");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileRetrievePd.cancel();
                    String msg = String.format(getString(R.string.msg_importing), getString(R.string.str_finished));
                    Utils.alert(ImportExportActivity.this, msg);
                }
            });
        }
    }
}
