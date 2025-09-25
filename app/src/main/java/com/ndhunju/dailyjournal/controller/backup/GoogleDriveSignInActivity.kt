package com.ndhunju.dailyjournal.controller.backup

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.android.gms.drive.Drive
import com.google.android.gms.tasks.Tasks
import com.ndhunju.dailyjournal.R
import com.ndhunju.dailyjournal.util.UtilsView

class GoogleDriveSignInActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GoogleDriveSignInActivity"
        const val BUNDLE_SHOULD_FINISH_ON_SIGN_IN = "BUNDLE_SHOULD_FINISH_ON_SIGN_IN"

        fun makeSignInClient(context: Context): GoogleSignInClient {
            val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .build()
            return GoogleSignIn.getClient(context, signInOptions)
        }
    }

    private lateinit var connectionPd: ProgressDialog

    private val signInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = Tasks.await(task)
                onSignedIn(account)
            } catch (e: Exception) {
                Log.e(TAG, "Sign-in failed.", e)
                showEndResultToUser(
                    getString(
                        R.string.warning_common_google_play_services_unknown_issue,
                        getString(R.string.app_name)
                    ), false
                )
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        connectionPd = ProgressDialog(this).apply {
            setMessage(getString(R.string.msg_connecting, getString(R.string.str_google_drive)))
            setCanceledOnTouchOutside(true)
            isIndeterminate = true
            setCancelable(true)
            show()
        }
    }

    private fun signIn() {
        val requiredScopes = setOf(Drive.SCOPE_FILE, Drive.SCOPE_APPFOLDER)
        val signInAccount = GoogleSignIn.getLastSignedInAccount(this)
        if (signInAccount != null &&
            requiredScopes.all { signInAccount.grantedScopes.contains(it) } &&
            !signInAccount.isExpired
        ) {
            onSignedIn(signInAccount)
        } else {
            showSignInPage()
        }
    }

    private fun showSignInPage() {
        val googleSignInClient = makeSignInClient(this)
        signInLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun onSignedIn(googleSignInAccount: GoogleSignInAccount) {
        connectionPd.dismiss()

        if (intent.getBooleanExtra(BUNDLE_SHOULD_FINISH_ON_SIGN_IN, false)) {
            setResult(RESULT_OK)
            finish()
        }
    }

    private fun showEndResultToUser(message: String, success: Boolean) {
        setResult(if (success) Activity.RESULT_OK else Activity.RESULT_CANCELED)
        UtilsView.alert(this, message) { _, _ ->
            setResult(if (success) Activity.RESULT_OK else Activity.RESULT_CANCELED)
            finish()
        }
    }

    override fun onStart() {
        super.onStart()
        signIn()
    }

    fun getActivity(): Activity = this
}
