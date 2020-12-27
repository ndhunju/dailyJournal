package com.ndhunju.dailyjournal.util;

public interface ProgressListener {

    public static int SHOW_INDETERMINATE_PROGRESS_PERCENTAGE = -1;
    /**
     * Invoked when progress {@code percentage} or {@code message} has changed.
     * @param percentage: Could be any positive value
     *                  or {@link ProgressListener#SHOW_INDETERMINATE_PROGRESS_PERCENTAGE}
     * @param message: message to show
     */
    void onProgress(float percentage, String message);

    static void publishProgress(
            ProgressListener progressListener,
            float percentage,
            String message
    ) {
        if (progressListener != null) {
            progressListener.onProgress(percentage, message);
        }
    }
}

