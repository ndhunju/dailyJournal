package com.ndhunju.dailyjournal.controller.preference.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.MetadataChangeSet;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Created by dhunju on 9/24/2015.
 * This class is a child class of {@link AsyncTask} that creates
 * back up in temp folder, uploads it to google drive and
 * deletes the file from temp folder. This
 * class displays Progress dialog before starting the operation
 * and notifies user once the operation is completed
 */
public class UploadBackUpToGDriveAsync extends AsyncTask<Void,Void,Void> {

    private static final String TAG = UploadBackUpToGDriveAsync.class.getSimpleName();

    //variables
    private GoogleApiClient mGoogleApiClient;
    private int mReturnRequestCode;
    private Activity mActivity;
    private ProgressDialog pd;

    public UploadBackUpToGDriveAsync(Activity activity, GoogleApiClient googleApiClient, int requestCode){
        mGoogleApiClient = googleApiClient;
        mReturnRequestCode = requestCode;
        mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        pd = new ProgressDialog(mActivity);
        pd.setMessage(String.format(mActivity.getString(R.string.msg_creating), mActivity.getString(R.string.str_backup)));
        pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        pd.setCanceledOnTouchOutside(false);
        pd.setCancelable(false);
        pd.setProgress(10);
        pd.setMax(100);
        pd.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        //Create a new Content in google drive and set a callback
        Drive.DriveApi.newDriveContents(mGoogleApiClient)
                .setResultCallback(new ResultCallback<DriveApi.DriveContentsResult>() {
                    @Override
                    public void onResult(DriveApi.DriveContentsResult result) {
                        if (!result.getStatus().isSuccess()) { //Failed
                            Log.i(TAG, "Failed to create new backup: " + result.getStatus().getStatusMessage());
                            return;
                        }

                        // If successful write the data to the new contents.
                        Log.i(TAG, "Content created.");

                        // Get an output stream for the contents.
                        OutputStream outputStream = result.getDriveContents().getOutputStream();

                        File backUpfile = null;

                        try {
                            //Create a new full backup of data into local drive
                            Services s = Services.getInstance(mActivity);
                            String filePath = s.createBackUp(UtilsFile.getCacheDir(mActivity));
                            backUpfile = new File(filePath);
                            outputStream.write(UtilsFile.read(backUpfile));

                            //delete the backup file from internal storage
                            UtilsFile.deleteFile(filePath);

                            // Create the initial metadata - MIME type and title.
                            // Note that the user will be able to change the title later.
                            MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
                                    .setTitle(backUpfile.getName())
                                    .setMimeType(UtilsFile.BACK_FILE_TYPE).build();


                            // Create an intent for the file chooser, and start it.
                            IntentSender intentSender = Drive.DriveApi
                                    .newCreateFileActivityBuilder()
                                    .setInitialMetadata(metadataChangeSet)
                                    .setInitialDriveContents(result.getDriveContents())
                                    .setActivityTitle(backUpfile.getName())
                                    .build(mGoogleApiClient);


                            //Show a Google Drive Picker where user can select the folder to save backup file in
                            //It usu takes a while for Drive Picker to show up. So cancel gDrivePD at onActivityResult();
                            mActivity.startIntentSenderForResult(intentSender, mReturnRequestCode, null, 0, 0, 0);

                        } catch (IOException e1) {Log.i(TAG, "Unable to write file contents.");
                        } catch (IntentSender.SendIntentException e) {Log.i(TAG, "Failed to launch file chooser.");}

                        pd.cancel();
                    }
                });
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        //this is called before the all the progress in background is complemeted
        //pd.cancel();
    }
}
