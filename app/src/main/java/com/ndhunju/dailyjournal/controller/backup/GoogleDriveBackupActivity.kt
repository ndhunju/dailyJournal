package com.ndhunju.dailyjournal.controller.backup

import android.content.Intent
import android.content.IntentSender
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.drive.Drive
import com.google.android.gms.drive.DriveClient
import com.google.android.gms.drive.DriveFolder
import com.google.android.gms.drive.DriveId
import com.google.android.gms.drive.DriveResourceClient
import com.google.android.gms.drive.OpenFileActivityOptions
import com.google.android.gms.drive.query.Filters
import com.google.android.gms.drive.query.SearchableField
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskCompletionSource
import com.ndhunju.dailyjournal.R

/** Created by ndhunju on 1/15/17.
 * This class encapsulates common operations when dealing with {@link DriveResourceClient}*/
abstract class GoogleDriveBackupActivity : GoogleDriveSignInActivity() {

    companion object {
        const val TAG = "GoogleDriveBackupActivity"

        /** Request code for Opening an item on Google Drive */
        const val REQUEST_CODE_OPEN_ITEM = 1189
    }

    /** Handles high-level drive functions like sync*/
    private lateinit var mDriveClient: DriveClient
    /**Handle access to Drive resources/files.*/
    private lateinit var mDriveResourceClient: DriveResourceClient
    /** Tracks completion of the drive picker */
    private lateinit var mOpenItemTaskSource: TaskCompletionSource<DriveId>

    private val openItemLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result: ActivityResult ->
        when (result.resultCode) {
            RESULT_OK -> {
                val data: Intent? = result.data
                if (data != null) {
                    // User has picked a file from Google Drive
                    val driveId: DriveId? = data.getParcelableExtra(OpenFileActivityOptions.EXTRA_RESPONSE_DRIVE_ID)
                    Log.d(TAG, "Selected file's ID: $driveId")
                    driveId?.let { mOpenItemTaskSource.setResult(it) } ?: mOpenItemTaskSource.setException(RuntimeException(getString(R.string.str_failed)))
                } else {
                    mOpenItemTaskSource.setException(RuntimeException(getString(R.string.str_failed)))
                }
            }
            else -> mOpenItemTaskSource.setException(RuntimeException(getString(R.string.str_failed)))
        }
    }

    override fun onSignedIn(googleSignInAccount: GoogleSignInAccount) {
        super.onSignedIn(googleSignInAccount)
        initializeDriveClient(googleSignInAccount)
    }

    /**
     * Continues the sign-in process, initializing the Drive clients with the current
     * user's account.
     */
    private fun initializeDriveClient(signInAccount: GoogleSignInAccount) {
        mDriveClient = Drive.getDriveClient(applicationContext, signInAccount)
        mDriveResourceClient = Drive.getDriveResourceClient(applicationContext, signInAccount)
        onDriveClientReady()
    }

    /**
     * Called after the user has signed in and the Drive client has been initialized.
     */
    protected open fun onDriveClientReady() {}

    protected fun getDriveResourceClient(): DriveResourceClient = mDriveResourceClient

    /**
     * Prompts the user to select a text file using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected fun pickFile(mimeType: String): Task<DriveId> {
        val openOptions = OpenFileActivityOptions.Builder()
            .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, mimeType))
            .build()
        return pickItem(openOptions)
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @return Task that resolves with the selected item's ID.
     */
    protected fun pickFolder(): Task<DriveId> {
        val openOptions = OpenFileActivityOptions.Builder()
            .setSelectionFilter(Filters.eq(SearchableField.MIME_TYPE, DriveFolder.MIME_TYPE))
            .build()
        return pickItem(openOptions)
    }

    /**
     * Prompts the user to select a folder using OpenFileActivity.
     *
     * @param openOptions Filter that should be applied to the selection
     * @return Task that resolves with the selected item's ID.
     */
    private fun pickItem(openOptions: OpenFileActivityOptions): Task<DriveId> {
        mOpenItemTaskSource = TaskCompletionSource()
        mDriveClient.newOpenFileActivityIntentSender(openOptions)
            .continueWith(Continuation<IntentSender, Void> { task ->
                try {
                    val intentSenderRequest = IntentSenderRequest.Builder(task.result).build()
                    openItemLauncher.launch(intentSenderRequest)
                } catch (e: IntentSender.SendIntentException) {
                    Log.w(TAG, "Unable to send intent", e)
                }
                null
            })
            .addOnFailureListener(OnFailureListener { e ->
                if (e.toString().contains("user must be signed in")) {
                    showSignInPage()
                } else {
                    showEndResultToUser("${getString(R.string.str_failed)}\n${e.localizedMessage}", false)
                }
            })
        return mOpenItemTaskSource.task
    }
}
