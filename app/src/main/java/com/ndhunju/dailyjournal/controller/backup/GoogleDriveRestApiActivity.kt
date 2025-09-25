package com.ndhunju.dailyjournal.controller.backup

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.Toolbar
import androidx.core.util.Pair
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.services.drive.Drive
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.BaseActivity
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper
import com.ndhunju.dailyjournal.controller.service.GoogleSignInHelper
import com.ndhunju.dailyjournal.util.UtilsView

/**
 * The main {@link Activity} for the Drive REST API functionality.
 */
class GoogleDriveRestApiActivity : BaseActivity() {

    // Constants
    companion object {
        private const val TAG = "GoogleDriveRestApiActivity"
        private const val REQUEST_CODE_SIGN_IN = 1
        private const val REQUEST_CODE_ERROR_RESOLUTION = 2
        /** Pass true for this key to finish this activity upon successful sign in to google drive **/
        const val BUNDLE_SHOULD_FINISH_ON_SIGN_IN = "BUNDLE_SHOULD_FINISH_ON_SIGN_IN"
    }

    // Member Variables
    private val googleSignInHelper = GoogleSignInHelper
    private lateinit var mDriveServiceHelper: DriveServiceHelper

    // View Variables
    private lateinit var connectionPd: ProgressDialog

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            handleSignInResult(result.data)
        } else {
            DriveServiceHelper.setLastOperationStatus(this, DriveServiceHelper.OPERATION_STATUS_FAIL)
            showEndResultToUser(
                getString(R.string.msg_error_g_drive_user_not_signed_in),
                false
            )
        }
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val newStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if (newStatus == ConnectionResult.SUCCESS) {
                val accountPair = googleSignInHelper.getLastSignedInAccountAndConnectionResult(this)
                if (accountPair.first != null) {
                    onSignedInToGoogleAccount(accountPair.first)
                } else {
                    requestSignIn()
                }
            } else {
                DriveServiceHelper.setLastOperationStatus(this, DriveServiceHelper.OPERATION_STATUS_FAIL)
                showEndResultToUser(
                    getString(R.string.msg_error_g_drive_user_not_signed_in),
                    false
                )
            }
        } else {
            DriveServiceHelper.setLastOperationStatus(this, DriveServiceHelper.OPERATION_STATUS_FAIL)
            showEndResultToUser(
                getString(R.string.msg_error_g_drive_user_not_signed_in),
                false
            )
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup Toolbar
        val toolbar: Toolbar? = findViewById(R.id.toolbar)
        toolbar?.let {
            setSupportActionBar(it)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
        }

        // Wire views
        connectionPd = ProgressDialog(this).apply {
            setMessage(getString(R.string.msg_connecting, getString(R.string.str_google_drive)))
            setCanceledOnTouchOutside(true)
            isIndeterminate = true
            setCancelable(true)
            show()
        }

        val accountPair = googleSignInHelper.getLastSignedInAccountAndConnectionResult(this)

        if (accountPair.second == ConnectionResult.SUCCESS) {
            accountPair.first?.let { onSignedInToGoogleAccount(it) } ?: requestSignIn()
        } else {
            val apiAvailability = GoogleApiAvailability.getInstance()
            if (apiAvailability.isUserResolvableError(accountPair.second)) {
                val intent = apiAvailability.getErrorResolutionIntent(this, accountPair.second, REQUEST_CODE_ERROR_RESOLUTION)
                intent?.let { resolutionLauncher.launch(it) }
            } else {
                DriveServiceHelper.setLastOperationStatus(this, DriveServiceHelper.OPERATION_STATUS_FAIL)
                showEndResultToUser(
                    getString(R.string.msg_error_g_drive_user_not_signed_in),
                    false
                )
            }
        }
    }

    protected fun showProgress(showProgress: Boolean, message: String?) {
        if (showProgress) {
            connectionPd.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            connectionPd.setMessage(message)
            connectionPd.show()
        } else if (!TextUtils.isEmpty(message)) {
            connectionPd.setProgressDrawable(null)
            connectionPd.setMessage(message)
        } else {
            connectionPd.dismiss()
        }
    }

    /**
     * Shows {@code message} to the user in a dialog. When user acknowledges the message, finishes
     * current activity and passes {@code success} to previous activity.
     */
    protected fun showEndResultToUser(message: String, success: Boolean) {
        setResult(if (success) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        if (!isFinishing) {
            UtilsView.alert(this, message) { _, _ ->
                setResult(if (success) Activity.RESULT_OK else Activity.RESULT_CANCELED)
                finish()
            }
        }
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    protected fun requestSignIn() {
        Log.d(TAG, "Requesting sign-in")
        showProgress(true, getString(R.string.msg_requesting_sign_in))

        val client: GoogleSignInClient = googleSignInHelper.getGoogleSigInClient(this)

        // The result of the sign-in Intent is handled in signInLauncher.
        signInLauncher.launch(client.signInIntent)
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private fun handleSignInResult(result: Intent?) {
        result ?: return
        GoogleSignIn.getSignedInAccountFromIntent(result)
            .addOnSuccessListener { googleSignInAccount ->
                DriveServiceHelper.setLastOperationStatus(this@GoogleDriveRestApiActivity, DriveServiceHelper.OPERATION_STATUS_SUCCESS)
                onSignedInToGoogleAccount(googleSignInAccount)
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "Failed sign in.", exception)
                DriveServiceHelper.setLastOperationStatus(this@GoogleDriveRestApiActivity, DriveServiceHelper.OPERATION_STATUS_FAIL)
                showEndResultToUser(
                    getString(R.string.msg_error_g_drive_user_not_signed_in),
                    false
                )
            }
    }

    private fun onSignedInToGoogleAccount(googleAccount: GoogleSignInAccount) {
        val googleDriveService = googleSignInHelper.signInToGoogleDrive(googleAccount, this)
        if (googleDriveService != null) {
            Log.d(TAG, "Sign in successful")
            onSignedInToGoogleDrive(googleDriveService)
        } else {
            Log.d(TAG, "Sign in failed")
            DriveServiceHelper.setLastOperationStatus(this, DriveServiceHelper.OPERATION_STATUS_FAIL)
            showEndResultToUser(
                getString(R.string.msg_error_g_drive_user_not_signed_in),
                false
            )
        }
    }

    protected fun onSignedInToGoogleDrive(googleDriveService: Drive) {

        showProgress(false, null)

        if (intent.getBooleanExtra(BUNDLE_SHOULD_FINISH_ON_SIGN_IN, false)) {
            setResult(RESULT_OK)
            finish()
            return
        }

        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = DriveServiceHelper(googleDriveService)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    fun getDriveServiceHelper(): DriveServiceHelper = mDriveServiceHelper

    fun getContext(): Context = this

    fun getActivity(): Activity = this
}
