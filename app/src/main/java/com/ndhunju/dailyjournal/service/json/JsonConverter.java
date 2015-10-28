package com.ndhunju.dailyjournal.service.json;

import android.content.Context;
import android.util.Log;

import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by dhunju on 10/15/2015.
 * This class entails methods for reading from JSON and inserting to
 * database and reading from database and writing to JSON
 */
public abstract class JsonConverter implements JsonKeys{

    private static final String TAG = JsonConverter.class.getSimpleName();

    //Variables
    protected Services mServices;


    protected JsonConverter(Context context){
        mServices = Services.getInstance(context);
    }


    /**
     * This method with read the passed json file and stores the data
     * into the database
     * @param jsonFilePath : Absolute file path of JSON file
     * @return true if success
     */
    public abstract boolean readFromJSON(String jsonFilePath);

    /**
     * This method will read the data from the database and stores it
     * into the passed json file
     * @param jsonFilePath : Absolute file path of JSON file
     * @return true if success
     */
    public abstract boolean writeToJSON(String jsonFilePath);


    /**
     * Creates a JSON file containing the data into predefined ({@link UtilsFile#getAppFolder(Context)} ) path
     * in sd card. <b>It deletes the old JSON file</b>
     * @return If successful, returns the absolute path of the file. Otherwise, null.
     */
    public String createJSONFile() throws IOException {

        try {
            // Create a app folder
            File appFolder = UtilsFile.getAppFolder(mServices.getContext());

            //Store old json files in an array so that it can be deleted once
            //new json file is created successfully
            ArrayList<File> filesToDelete = new ArrayList<>();
            for(File f : appFolder.listFiles()){
                if(f.getName().endsWith(".json"))
                    filesToDelete.add(f);
            }

            //Create new json file
            String fileName = UtilsFile.getJSONFileName();
            File jsonFile = new File(appFolder.getAbsolutePath(),fileName );

            if(!jsonFile.createNewFile())
                Log.d(TAG, "Fail to create file " + jsonFile.getAbsolutePath());

            writeToJSON(jsonFile.getAbsolutePath());

            //Delete Old files so that there is always one latest copy
            for(File f : filesToDelete){
                f.delete();
            }

            return jsonFile.getAbsolutePath();

        } catch (IOException e) {
            Log.w(TAG, "Error creating json backup file: " + e.getMessage());
            throw e;
        }
    }


}
