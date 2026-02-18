package com.ndhunju.dailyjournal.controller.backup

import android.accounts.Account
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
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.Scopes
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.controller.BaseActivity
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper
import com.ndhunju.dailyjournal.controller.service.GoogleAuthHelper
import com.ndhunju.dailyjournal.util.UtilsView
import kotlinx.coroutines.launch

/**
 * The main {@link Activity} for the Drive REST API functionality.
 */
open class GoogleDriveRestApiActivity : BaseActivity() {

    // Constants
    companion object {
        private const val TAG = "GoogleDriveRestApiActivity"
        private const val REQUEST_CODE_ERROR_RESOLUTION = "REQUEST_CODE_ERROR_RESOLUTION"

        /** Pass true for this key to finish this activity upon successful sign in to google drive **/
        const val BUNDLE_SHOULD_FINISH_ON_SIGN_IN = "BUNDLE_SHOULD_FINISH_ON_SIGN_IN"
    }

    // Member Variables
    private val googleSignInHelper = GoogleAuthHelper(this)
    private lateinit var mDriveServiceHelper: DriveServiceHelper

    // View Variables
    private lateinit var connectionPd: ProgressDialog

    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result ->
        onSignInResult(result)
    }

    private val resolutionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val newStatus = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this)
            if (newStatus == ConnectionResult.SUCCESS) {
                if (googleSignInHelper.getCredential()?.selectedAccount != null) {
                    onSignedInToGoogleAccount(googleSignInHelper.getCredential()?.selectedAccount)
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
        // TODO: Use AlertDialog to show progress
        connectionPd = ProgressDialog(this).apply {
            setMessage(getString(R.string.msg_connecting, getString(R.string.str_google_drive)))
            setCanceledOnTouchOutside(true)
            isIndeterminate = true
            setCancelable(true)
            show()
        }

        val account = googleSignInHelper.getCredential()?.selectedAccount

        if (account != null) {
            onSignedInToGoogleAccount(account)
        } else {
            val apiAvailability = GoogleApiAvailability.getInstance()
            if (googleSignInHelper.errorCode != null && apiAvailability.isUserResolvableError(googleSignInHelper.errorCode)) {
                val intent = apiAvailability.getErrorResolutionIntent(
                    this,
                    googleSignInHelper.errorCode,
                    REQUEST_CODE_ERROR_RESOLUTION
                )
                intent?.let { resolutionLauncher.launch(it) }
            } else {
                requestSignIn()
            }
        }
    }

    // TODO: Use AlertDialog to show progress
    fun showProgress(showProgress: Boolean, message: String?) {
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
    fun showEndResultToUser(message: String, success: Boolean) {
        setResult(if (success) RESULT_OK else RESULT_CANCELED)
        if (!isFinishing) {
            UtilsView.alert(this, message) { _, _ ->
                setResult(if (success) RESULT_OK else RESULT_CANCELED)
                finish()
            }
        }
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    fun requestSignIn() {
        Log.d(TAG, "Requesting sign-in")
        showProgress(true, getString(R.string.msg_requesting_sign_in))

        // Use FirebaseUI Auth to start the sign-in flow with Google provider
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder()
                .setScopes(
                    listOf(
                        DriveScopes.DRIVE_FILE,
                        Scopes.EMAIL,
                        Scopes.PROFILE
                    )
                )
                .build()
        )

        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setLogo(R.mipmap.ic_app)
            .setTheme(R.style.AppTheme)
            .build()
        signInLauncher.launch(signInIntent)
    }

    /**
     * Handles the result of the FirebaseUI Auth sign-in flow.
     */
    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                Log.d(TAG, "Sign in successful: ${user.email}")
                DriveServiceHelper.setLastOperationStatus(
                    this@GoogleDriveRestApiActivity,
                    DriveServiceHelper.OPERATION_STATUS_SUCCESS
                )
                val account = user.email?.let { Account(it, "com.google") }
                onSignedInToGoogleAccount(account)
            } else {
                Log.e(TAG, "Sign in succeeded but user is null")
                DriveServiceHelper.setLastOperationStatus(
                    this@GoogleDriveRestApiActivity,
                    DriveServiceHelper.OPERATION_STATUS_FAIL
                )
                showEndResultToUser(
                    getString(R.string.msg_error_g_drive_user_not_signed_in),
                    false
                )
            }
        } else {
            // Sign in failed
            val response = result.idpResponse
            if (response == null) {
                Log.d(TAG, "User cancelled sign-in")
            } else {
                Log.e(TAG, "Sign in error", response.error)
            }
            DriveServiceHelper.setLastOperationStatus(
                this@GoogleDriveRestApiActivity,
                DriveServiceHelper.OPERATION_STATUS_FAIL
            )
            showEndResultToUser(
                getString(R.string.msg_error_g_drive_user_not_signed_in),
                false
            )
        }
    }

    private fun onSignedInToGoogleAccount(googleAccount: Account?) {
        val googleDriveService = googleSignInHelper.getDriveService()
        if (googleDriveService != null) {
            Log.d(TAG, "Sign in successful")
            onSignedInToGoogleDrive(googleDriveService)
        } else {

            Log.d(TAG, "Sign in failed")
            DriveServiceHelper.setLastOperationStatus(
                this,
                DriveServiceHelper.OPERATION_STATUS_FAIL
            )
            showEndResultToUser(
                getString(R.string.msg_error_g_drive_user_not_signed_in),
                false
            )
        }
    }

    open fun onSignedInToGoogleDrive(googleDriveService: Drive) {

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
