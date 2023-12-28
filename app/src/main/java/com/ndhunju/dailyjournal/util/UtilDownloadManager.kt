package com.ndhunju.dailyjournal.util

import android.app.DownloadManager
import android.content.Context
import java.io.File

object UtilDownloadManager {

    fun notifyUserAboutFileCreation(
        context: Context,
        downloadedFile: File,
        description: String,
        mimeType: String
    ) {

        // Show it in Downloads app and in notification bar
        val downloadManager = context.getSystemService(
            Context.DOWNLOAD_SERVICE
        ) as DownloadManager

        downloadManager.addCompletedDownload(
            // OS is using this title to name the downloaded image file
            downloadedFile.name,
            description,
            true,
            mimeType,
            downloadedFile.absolutePath,
            downloadedFile.length(),
            true
        )
    }
}