package com.ndhunju.dailyjournal.controller.service;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.core.util.Pair;

/**
 * A utility for performing read/write operations on Drive files via the REST API
 */
public class DriveServiceHelper {

    private static final String APP_ROOT_FOLDER = "Daily Journal Plus";
    private final Executor mExecutor = Executors.newSingleThreadExecutor();
    private final Drive mDriveService;

    public DriveServiceHelper(Drive driveService) {
        mDriveService = driveService;
    }

    /**
     * Opens the file identified by {@code fileId} and returns a {@link Pair} of its input stream
     * and meta data.
     */
    public Task<Pair<InputStream, File>> readFileAsync(String fileId) {
        return Tasks.call(mExecutor, () -> {
            // Retrieve the metaData as a File object.
            File metaData = mDriveService.files().get(fileId).execute();

            try {
                // Get and return input stream
                InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                return Pair.create(is, metaData);
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            return null;
        });
    }

    /**
     * Returns a {@link FileList} containing all the visible files in the user's My Drive.
     *
     * <p>The returned list will only contain files visible to this app, i.e. those which were
     * created by this app. To perform operations on files not created by the app, the project must
     * request Drive Full Scope in the <a href="https://play.google.com/apps/publish">Google
     * Developer's Console</a> and be submitted to Google for verification.</p>
     */
    public Task<FileList> queryFiles() {
        return Tasks.call(mExecutor, () ->
                mDriveService
                        .files()
                        .list()
                        // Request to return createdTime, modifiedTime, id and name
                        .setFields("files(createdTime,modifiedTime,id,name,size)")
                        // Return only backup files, filtering out folders
                        .setQ("mimeType='" + UtilsFile.BACK_FILE_TYPE +"'")
                        .setSpaces("drive")
                        .execute());
    }

    /**
     * Returns a pre-defined folder, {@link this#APP_ROOT_FOLDER}, that this app can use
     * to store backup files or any other files.
     */
    public Task<File> getAppFolder() {
        return Tasks.call(mExecutor, () -> {
            // First, check if app folder was already created
            List<File> fileList = mDriveService
                    .files()
                    .list()
                    .setFields("files(id,name)")
                    .setSpaces("drive")
                    .execute()
                    .getFiles();

            for (File file: fileList) {
                if (Objects.equals(file.getName(), APP_ROOT_FOLDER)) {
                    return file;
                }
            }

            // Second, create the app folder since it is not found
            File folderMetaData = new File()
                    .setName(APP_ROOT_FOLDER)
                    .setMimeType("application/vnd.google-apps.folder");

            return mDriveService
                    .files()
                    .create(folderMetaData)
                    .setFields("id")
                    .execute();
        });
    }

    /**
     * Creates a File in google drive inside {@code insideFolder} folder with name {@code fileName}
     * of type, {@code mimeType}
     * @param insideFolder
     * @param fileName
     * @param mimeType
     * @return
     */
    public Task<File> createFile(File insideFolder, String fileName, String mimeType) {
        return Tasks.call(mExecutor, () -> {

            File metadata = new File()
                    .setParents(Collections.singletonList(insideFolder.getId()))
                    .setMimeType(mimeType)
                    .setName(fileName);

            File googleFile = mDriveService.files().create(metadata).execute();
            if (googleFile == null) {
                throw new IOException("File creation failed.");
            }

            return googleFile;
        });
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code updatedMetadata}
     * and {@code content}.
     */
    public Task<Void> saveFile(String fileId, File updatedMetadata, FileContent content) {
        return Tasks.call(mExecutor, () -> {
            // Update the metadata and contents.
            mDriveService.files().update(fileId, updatedMetadata, content).execute();
            return null;
        });
    }

}
