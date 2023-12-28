package com.ndhunju.dailyjournal.controller.service;

import android.content.Context;

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

import static com.ndhunju.dailyjournal.util.ProgressListener.RESULT_AUTO_UPLOAD_TO_G_DRIVE_FAILED;
import static com.ndhunju.dailyjournal.util.ProgressListener.PROGRESS_INDETERMINATE;
import static com.ndhunju.dailyjournal.util.ProgressListener.publishProgress;
import static com.ndhunju.dailyjournal.util.ProgressListener.publishProgressWithFailedResult;

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
                        .setQ(
                                "mimeType = '" + UtilsFile.BACK_FILE_TYPE +"' " +
                                "and trashed = false"
                        )
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
        return getAppFolder(progressListener).continueWithTask(getAppFolderTask -> {

            // Get app folder first to store the backup file
            File appFolder = getAppFolderTask.getResult();

            if (appFolder == null) {
                String message = context.getString(
                        R.string.msg_fail,
                        context.getString(R.string.msg_error_google_drive_app_folder)
                );

                throw new RuntimeException(message);
            }

            publishProgress(
                    progressListener,
                    20,
                    context.getString(R.string.msg_google_drive_creating_backup_file)
            );

            return createFile(
                    progressListener,
                    appFolder,
                    UtilsFile.getZipFileName(),
                    UtilsFile.BACK_FILE_TYPE
            );

        }).continueWithTask(createFileTask -> {
            // Create a new file in Google Drive to store backup
            File file = createFileTask.getResult();

            if (file == null) {
                String message = context.getString(
                        R.string.msg_fail,
                        context.getString(R.string.msg_error_google_drive_backup_file)
                );

                throw new RuntimeException(message);
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
                        (progressType, percentage, message, resultCode) -> {
                            publishProgress(
                                    progressListener,
                                    progressType,
                                    // Cap lower limit to 30
                                    30 + (percentage / 2),
                                    context.getString(R.string.msg_copying, message),
                                    resultCode
                            );
                        }
                );

                localBackUpFile = new java.io.File(filePath);

                publishProgress(progressListener, 90, null);
                publishProgress(
                        progressListener,
                        PROGRESS_INDETERMINATE,
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
                        progressListener,
                        file.getId(),
                        metaData,
                        new FileContent(UtilsFile.BACK_FILE_TYPE, localBackUpFile)
                );
            } catch (Exception e1) {
                String message = "Failed to create backup in Google Drive: " + e1.getMessage();
                throw new RuntimeException(message);
            }
        }).addOnFailureListener(e -> {
            publishProgressWithFailedResult(
                    progressListener,
                    e.getMessage(),
                    RESULT_AUTO_UPLOAD_TO_G_DRIVE_FAILED
            );
        });
    }

    /**
     * Returns a pre-defined folder, {@link this#APP_ROOT_FOLDER}, that this app can use
     * to store backup files or any other files.
     */
    public Task<File> getAppFolder(ProgressListener progressListener) {

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
                String errorMessage = "Failed to get App Folder: " + e.getMessage();
                taskCompletionSource.setException(new Exception(
                        "Failed to get App Folder: " + e.getMessage()
                ));

                publishProgressWithFailedResult(
                        progressListener,
                        errorMessage,
                        RESULT_AUTO_UPLOAD_TO_G_DRIVE_FAILED
                );
            }

        });

        return taskCompletionSource.getTask();
    }

    /**
     * Creates a File in google drive inside {@code insideFolder} folder with name {@code fileName}
     * of type, {@code mimeType}
     */
    public Task<File> createFile(
            ProgressListener progressListener,
            File insideFolder,
            String fileName,
            String mimeType
    ) {

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
                String message =  "Failed to create backup file in Google Drive: " + e.getMessage();
                taskCompletionSource.setException(new IOException(message));
                publishProgressWithFailedResult(
                        progressListener,
                        message,
                        RESULT_AUTO_UPLOAD_TO_G_DRIVE_FAILED
                );
            }

        });

        return taskCompletionSource.getTask();
    }

    /**
     * Updates the file identified by {@code fileId} with the given {@code updatedMetadata}
     * and {@code content}.
     */
    public Task<File> saveFile(
            ProgressListener progressListener,
            String fileId,
            File updatedMetadata,
            FileContent content
    ) {

        TaskCompletionSource<File> taskCompletionSource = new TaskCompletionSource<>();

        mExecutor.execute(() -> {

            // Update the metadata and contents.
            try {
                File file = mDriveService.files().update(fileId, updatedMetadata, content).execute();
                taskCompletionSource.setResult(file);
            } catch (Exception e) {
                String message = "Failed to update created back up file in Google Drive: "
                        + e.getMessage();
                taskCompletionSource.setException(new RuntimeException(message));
                publishProgressWithFailedResult(
                        progressListener,
                        message,
                        RESULT_AUTO_UPLOAD_TO_G_DRIVE_FAILED
                );
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
