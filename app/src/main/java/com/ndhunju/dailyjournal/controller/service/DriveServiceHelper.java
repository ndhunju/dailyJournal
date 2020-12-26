package com.ndhunju.dailyjournal.controller.service;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import java.io.InputStream;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.core.util.Pair;

/**
 * A utility for performing read/write operations on Drive files via the REST API
 */
public class DriveServiceHelper {
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
                mDriveService.files().list().setSpaces("drive").execute());
    }

}
