package com.ndhunju.dailyjournal.controller.ImportExport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.JsonConverter;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.service.UtilsFile;
import com.ndhunju.dailyjournal.service.UtilsView;
import com.ndhunju.dailyjournal.service.UtilsZip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.InputMismatchException;

/**
 * Created by dhunju on 9/24/2015.
 */
public class ImportOldDataAsyncTask extends AsyncTask<Void,Integer,Boolean> {

    public static final String TAG = ImportOldDataAsyncTask.class.getSimpleName();

    ProgressDialog pd ;
    Activity mActivity;

    public ImportOldDataAsyncTask(Activity activity) {
        this.mActivity = activity;
    }

    @Override
    protected void onPreExecute() {
                pd = new ProgressDialog(mActivity);
                pd.setMessage(String.format(mActivity.getString(R.string.msg_importing), mActivity.getString(R.string.str_backup)));
                pd.setCancelable(false);
                pd.setCanceledOnTouchOutside(false);
                pd.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pd.setMax(100);
                pd.setProgress(10);
                pd.show();
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        pd.setProgress(values[0]);
        super.onProgressUpdate(values);

    }

    @Override
    protected Boolean doInBackground(Void... voids) {
        //1. Look for old app folder
        File directoryToZip = UtilsFile.getAppFolder(true);

        if(!directoryToZip.exists())
            return false;

        //2. Backup the old app folder

        //2.2 get App Folder that is not hidden. Backup file will be created here
        File extAppFolder = UtilsFile.getAppFolder(false) ;

        //2.3 create a zip file in external app folder so that user can use it in case anything goes wrong
        String fileName = UtilsFile.getZipFileName();
        File zipFile = new File(extAppFolder.getAbsoluteFile(), fileName);

        try{

            zipFile.createNewFile(); //create a new json file

            //Store old json files in an array so that it can be deleted once
            //new json file is created succesfully
            ArrayList<File> filesToDelete = new ArrayList<>();
            for(File f : extAppFolder.listFiles()){
                if(f.getName().endsWith(".json"))
                    filesToDelete.add(f);
            }


            String name = UtilsFile.getJSONFileName();
            File jsonFile = new File(directoryToZip.getAbsolutePath(),name );

            if(!jsonFile.createNewFile())
                Log.d(TAG, "Failed to create file at " + jsonFile.getAbsolutePath());

            publishProgress(30);

            JsonConverter converter = new JsonConverter(Services.getInstance(mActivity));

            //write data to json file
            FileOutputStream fileOutputStream = new FileOutputStream(jsonFile.getAbsoluteFile());
            fileOutputStream.write(converter.getJSONDb().toString().getBytes());
            fileOutputStream.close();

            publishProgress(50);

            // Let the device know that a new file has been created so that it appears in the computer when connected via usb
            MediaScannerConnection.scanFile(mActivity,
                    new String[]{jsonFile.toString()}, null,
                    new MediaScannerConnection.OnScanCompletedListener() {
                        @Override
                        public void onScanCompleted(String path, Uri uri) {
                            Log.i(TAG + "ExternalStorage", "Scanned " + path + ":");
                            Log.i(TAG + "ExternalStorage", "-> uri=" + uri);
                        }
                    });

            Log.i(TAG, "JSON backup for old data created");

            //once a new json file is created, delete old ones
            for(File f : filesToDelete){
                f.delete();
            }

            publishProgress(60);

            //3 zip the directory file into zipFile
            UtilsZip.zip(directoryToZip, zipFile);

            publishProgress(70);

            Log.i(TAG, "old file's backup created");

            //Delete existing files, objects, database
            Services.getInstance(mActivity).truncateAllTables();

            //3. Restore the backup file

            //Get the app folder where the new data are stored
            File newAppFolder = UtilsFile.getAppFolder(mActivity);

            //Delete old files, attachments
            UtilsFile.deleteDirectory(newAppFolder);

            publishProgress(80);

            //Unzip the files into new app folder
            UtilsZip.unzip(zipFile, newAppFolder);

            publishProgress(90);

            //search .json file
            File[] files = newAppFolder.listFiles();
            for(int i = files.length-1; i >= 0 ; i-- )
                if(files[i].isFile() && files[i].getName().endsWith(".json")){
                    try{
                        if(converter.parseJSONFile(files[i].getAbsolutePath()))
                            return false; //false to preserve Id
                    }catch (InputMismatchException e){
                        UtilsView.alert(mActivity, mActivity.getString(com.ndhunju.dailyjournal.R.string.warning_ext_mismatch));
                        return false;
                    }
                    //takes the first json file from the last
                    //name of json file has date on it so the latest json file
                    //wil likely be at the bottom of the list
                    break;
                }

            //to let know that a new file has been created so that it appears in the computer
            MediaScannerConnection.scanFile(mActivity, new String[]{ extAppFolder.getAbsolutePath()}, null, null);

        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        pd.cancel();
        //Record that the old data were imported so that it doesn't happen again
        Services.getInstance(mActivity).oldDataImportAttempted(true);

        mActivity.setResult(aBoolean ? Activity.RESULT_OK : Activity.RESULT_CANCELED);

        //Prepare message to notify user
        String msg = aBoolean ? mActivity.getString(R.string.str_finished) : mActivity.getString(R.string.str_failed);
        UtilsView.alert(mActivity, msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                mActivity.finish();  //Kill the activity once the import attempt is finished
            }
        }, null);

        super.onPostExecute(aBoolean);
    }
}
