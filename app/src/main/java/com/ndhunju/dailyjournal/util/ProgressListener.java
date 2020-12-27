package com.ndhunju.dailyjournal.util;

public interface ProgressListener {

    /**
     * Invoked when progress {@code percentage} or {@code message} has changed.
     * @param percentage
     * @param message
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

