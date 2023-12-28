package com.ndhunju.dailyjournal.util;

import androidx.annotation.IntRange;
import androidx.annotation.LongDef;

public interface ProgressListener {

    public static int PROGRESS_INDETERMINATE = 1;
    public static int PROGRESS_DETERMINATE = 2;
    @LongDef({PROGRESS_INDETERMINATE, PROGRESS_DETERMINATE})
    @interface ProgressType{};

    public static int RESULT_OK = 1;
    public static int RESULT_AUTO_UPLOAD_TO_G_DRIVE_FAILED = -1;

    /**
     * Invoked when progress {@code percentage} or {@code message} has changed.
     * @param percentage: Could be any positive value from 0 to 100
     * @param message: message to show
     */
    void onProgress(
            @ProgressType int progressType,
            @IntRange(from = 0, to = 100) int percentage,
            String message,
            int resultCode
    );

    static void publishProgress(
            ProgressListener progressListener,
            @IntRange(from = 0, to = 100) int percentage,
            String message
    ) {
        if (progressListener != null) {
            progressListener.onProgress(PROGRESS_DETERMINATE, percentage, message, RESULT_OK);
        }
    }

    static void publishProgress(
            ProgressListener progressListener,
            @ProgressType int progressType,
            @IntRange(from = 0, to = 100) int percentage,
            String message,
            int resultCode
    ) {
        if (progressListener != null) {
            progressListener.onProgress(progressType, percentage, message, resultCode);
        }
    }

    static void publishProgressWithFailedResult(
            ProgressListener progressListener,
            String message,
            int resultCode
    ) {
        if (progressListener != null) {
            progressListener.onProgress(PROGRESS_DETERMINATE, 100, message, resultCode);
        }
    }
}

