package com.ndhunju.dailyjournal.controller.service;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.service.PreferenceService;
import com.ndhunju.dailyjournal.service.Services;
import com.ndhunju.dailyjournal.util.ProgressListener;
import com.ndhunju.dailyjournal.util.UtilsFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.LongDef;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;

import static com.ndhunju.dailyjournal.util.ProgressListener.SHOW_INDETERMINATE_PROGRESS_PERCENTAGE;
import static com.ndhunju.dailyjournal.util.ProgressListener.publishProgress;

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

        TaskCompletionSource<Pair<InputStream, File>> taskCompletionSource
                = new TaskCompletionSource<>();

        mExecutor.execute(() -> {
            try {
                // Retrieve the metaData as a File object.
                File metaData = mDriveService.files().get(fileId).execute();
                // Get and return input stream
                InputStream is = mDriveService.files().get(fileId).executeMediaAsInputStream();
                taskCompletionSource.setResult(Pair.create(is, metaData));
            } catch (Exception ex) {
                taskCompletionSource.trySetException(ex);
                ex.printStackTrace();
            }
        });

        return taskCompletionSource.getTask();
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

        TaskCompletionSource<FileList> taskCompletionSource = new TaskCompletionSource<>();

        mExecutor.execute(() -> {

            try {
                FileList fileList = mDriveService
                        .files()
                        .list()
                        // Request to return createdTime, modifiedTime, id and name
                        .setFields("files(createdTime,modifiedTime,id,name,size)")
                        // Return only backup files, filtering out folders
                        .setQ("mimeType='" + UtilsFile.BACK_FILE_TYPE +"'")
                        .setSpaces("drive")
                        .execute();
                taskCompletionSource.setResult(fileList);
            } catch (IOException e) {
                taskCompletionSource.trySetException(e);
                e.printStackTrace();
            }

        });

        return taskCompletionSource.getTask();
    }

    public Task<File> createBackup(Context context, @Nullable ProgressListener progressListener) {
        return getAppFolder().continueWithTask(getAppFolderTask -> {

            // Get app folder first to store the backup file
            File appFolder = getAppFolderTask.getResult();

            if (appFolder == null) {
                throw new RuntimeException(context.getString(
                        R.string.msg_fail,
                        context.getString(R.string.msg_error_google_drive_app_folder)
                ));
            }

            publishProgress(
                    progressListener,
                    20,
                    context.getString(R.string.msg_google_drive_creating_backup_file)
            );

            return createFile(
                    appFolder,
                    UtilsFile.getZipFileName(),
                    UtilsFile.BACK_FILE_TYPE
            );

        }).continueWithTask(createFileTask -> {
            // Create a new file in Google Drive to store backup
            File file = createFileTask.getResult();

            if (file == null) {
                throw new RuntimeException(context.getString(
                        R.string.msg_fail,
                        context.getString(R.string.msg_error_google_drive_backup_file)
                ));
            }

            java.io.File localBackUpFile;

            try {
                publishProgress(
                        progressListener,
                        30,
                        context.getString(R.string.msg_zipping)
                );

                // Create a new full backup of data into local drive
                Services services = Services.getInstance(context);
                // Looks like the backup file uploaded to google drive
                // is first created in Cache folder inside the app folder
                String filePath = services.createBackUp(
                        UtilsFile.getCacheDir(context),
                        (percentage, message) -> {
                            if (progressListener != null) {
                                progressListener.onProgress(
                                        // Cap lower limit to 30
                                        30 + (percentage / 2),
                                        context.getString(R.string.msg_copying, message)
                                );
                            }
                        }
                );

                localBackUpFile = new java.io.File(filePath);

                publishProgress(progressListener, 90, null);
                publishProgress(
                        progressListener,
                        SHOW_INDETERMINATE_PROGRESS_PERCENTAGE,
                        context.getString(
                                R.string.msg_uploading,
                                context.getString(R.string.str_backup)
                        )
                );

                // Metadata for the backup file in Google Drive
                File metaData = new File()
                        .setName(localBackUpFile.getName())
                        .setMimeType(UtilsFile.BACK_FILE_TYPE);

                // Save the local backupFile in Google Drive
                return saveFile(
                        file.getId(),
                        metaData,
                        new FileContent(UtilsFile.BACK_FILE_TYPE, localBackUpFile)
                );
            } catch (IOException e1) {
                Log.i("createBackup", "Unable to write file appFolder.");
                throw e1;
            }
        });
    }

    /**
     * Returns a pre-defined folder, {@link this#APP_ROOT_FOLDER}, that this app can use
     * to store backup files or any other files.
     */
    public Task<File> getAppFolder() {

        TaskCompletionSource<File> taskCompletionSource = new TaskCompletionSource<>();

        mExecutor.execute(() -> {
            try {
                // First, check if app folder was already created
                List<File> fileList = mDriveService
                        .files()
                        .list()
                        .setFields("files(id,name)")
                        .setSpaces("drive")
                        .execute()
                        .getFiles();

                for (File file : fileList) {
                    if (Objects.equals(file.getName(), APP_ROOT_FOLDER)) {
                        taskCompletionSource.setResult(file);
                        return;
                    }
                }

                // Second, create the app folder since it is not found
                File folderMetaData = new File()
                        .setName(APP_ROOT_FOLDER)
                        .setMimeType("application/vnd.google-apps.folder");

                File file =  mDriveService
                        .files()
                        .create(folderMetaData)
                        .setFields("id")
                        .execute();

                taskCompletionSource.setResult(file);
            } catch (Exception e) {
                taskCompletionSource.trySetException(e);
            }

        });

        return taskCompletionSource.getTask();
    }

    /**
     * Creates a File in google drive inside {@code insideFolder} folder with name {@code fileName}
     * of type, {@code mimeType}
     */
    public Task<File> createFile(File insideFolder, String fileName, String mimeType) {

        TaskCompletionSource<File> taskCompletionSource = new TaskCompletionSource<>();

        mExecutor.execute(() -> {

            File metadata = new File()
                    .setParents(Collections.singletonList(insideFolder.getId()))
                    .setMimeType(mimeType)
                    .setName(fileName);

            try {
                File googleFile = mDriveService.files().create(metadata).execute();
                taskCompletionSource.setResult(googleFile);
            } catch (IOException e) {
                taskCompletionSource.trySetException(new IOException("File creation failed."));
            }

        });

        return taskCompletionSource.getTask();
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code updatedMetadata}
     * and {@code content}.
     */
    public Task<File> saveFile(String fileId, File updatedMetadata, FileContent content) {

        TaskCompletionSource<File> taskCompletionSource = new TaskCompletionSource<>();

        mExecutor.execute(() -> {

            // Update the metadata and contents.
            try {
                File file = mDriveService.files().update(fileId, updatedMetadata, content).execute();
                taskCompletionSource.setResult(file);
            } catch (Exception e) {
                taskCompletionSource.trySetException(e);
            }

        });

        return taskCompletionSource.getTask();
    }

    private static final String KEY_LAST_OPERATION = "KEY_GOOGLE_SERVICE_LAST_OPERATION";
    public static final long OPERATION_STATUS_FAIL = -1;
    public static final long OPERATION_STATUS_UNKNOWN = 0;
    public static final long OPERATION_STATUS_SUCCESS = 1;
    @LongDef({OPERATION_STATUS_SUCCESS, OPERATION_STATUS_UNKNOWN, OPERATION_STATUS_FAIL})
    @interface OperationStatus {}

    public static void setLastOperationStatus(Context context, @OperationStatus long status) {
        PreferenceService.from(context).putVal(KEY_LAST_OPERATION, status);
    }

    public static long getLastOperationStatus(Context context) {
        return PreferenceService.from(context).getVal(KEY_LAST_OPERATION, OPERATION_STATUS_UNKNOWN);
    }

}
