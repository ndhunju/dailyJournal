package com.ndhunju.dailyjournal.util

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.checkSelfPermission
import androidx.fragment.app.Fragment

/**
 * This class will hold logic around permission that the app needs.
 */
object PermissionManager {

    /**
     * Returns true if the app has permission to manage files in the device.
     * On API 29+, the app uses app-scoped external storage which doesn't require
     * special permissions.
     */
    fun hasManageFilePermission(context: Context): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, we use app-scoped external storage
            // which doesn't require MANAGE_EXTERNAL_STORAGE
            return true
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // Check WRITE_EXTERNAL_STORAGE permission for Android OS older than Q
            return ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        }

        // For rest, once we define the the AndroidManifest file, we have the permission
        return true
    }

    /**
     * Returns true if permission is already granted.
     */
    fun askManageFilePermission(
        fragment: Fragment,
        requestCodeForManageFilePermission: Int,
        requestCodeForWriteExternalStoragePermission: Int
    ): Boolean {

        val context = fragment.context ?: return false

        if (hasManageFilePermission(context)) {
            return true
        }

        // Check WRITE_EXTERNAL_STORAGE permission for Android OS >= M and < Q
        if (checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request WRITE_EXTERNAL_STORAGE permission
            fragment.requestPermissions(
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                requestCodeForWriteExternalStoragePermission
            )
        }
        return false
        return true
    }

    fun canSaveImageOnDownloadsFolder(context: Context): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, we use app-scoped external storage
            // which doesn't require special write permission
            return true
        } else
            // Keep the existing logic for Android M and above
            return checkSelfPermission(
                context,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED

        return true
    }

    fun askPermissionForSavingImageOnDownloadsFolder(
        activity: Activity,
        requestCode: Int
    ): Boolean {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // On Android 10+, we use app-scoped external storage
            // which doesn't require special write permission
            return true
        } else
            // Keep the existing logic for Android M and above
            if (checkSelfPermission(
                    activity,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                activity.requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    requestCode
                )

                // Permission not granted yet
                return false
            }

        return true
    }
}