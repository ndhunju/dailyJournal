package com.ndhunju.dailyjournal.controller.service

import android.content.Context
import android.util.Log
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.util.Pair
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.common.collect.Sets
import com.ndhunju.dailyjournal.R

/**
 * Helper class that groups relevant objects like [Scope] and provides helper methods
 */
object GoogleSignInHelper {

    const val TAG = "GoogleSignInHelper"

    private val requiredScopes = setOf(Scope(DriveScopes.DRIVE_FILE))

    fun buildGoogleSigInOptions(): GoogleSignInOptions {
        return GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
    }

    fun getGoogleSigInClient(context: Context): GoogleSignInClient {
        return GoogleSignIn.getClient(context, buildGoogleSigInOptions())
    }

    fun signInToGoogleDrive(
        @Nullable googleSignInAccount: GoogleSignInAccount?,
        context: Context
    ): Drive? {
        googleSignInAccount ?: return null

        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        )

        credential.selectedAccount = googleSignInAccount.account
        return try {
            Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential
            )
                .setApplicationName(context.getString(R.string.app_name))
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "signInToGoogleDrive: ", e)
            null
        }
    }

    @NonNull
    fun getLastSignedInAccountAndConnectionResult(context: Context): Pair<GoogleSignInAccount?, Int> {
        val googleServiceStatus = GoogleApiAvailability.getInstance()
            .isGooglePlayServicesAvailable(context)

        return when (googleServiceStatus) {
            ConnectionResult.SERVICE_MISSING,
            ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED,
            ConnectionResult.API_UNAVAILABLE,
            ConnectionResult.SERVICE_DISABLED -> Pair(null, googleServiceStatus)

            ConnectionResult.SUCCESS -> {
                val signInAccount = GoogleSignIn.getLastSignedInAccount(context)
                if (signInAccount != null
                    && !signInAccount.isExpired
                    && requiredScopes.all { signInAccount.grantedScopes.contains(it) }
                    && DriveServiceHelper.getLastOperationStatus(context) != DriveServiceHelper.OPERATION_STATUS_FAIL
                ) {
                    Pair(signInAccount, googleServiceStatus)
                } else {
                    Pair(null, googleServiceStatus)
                }
            }

            else -> Pair(null, googleServiceStatus)
        }
    }

    fun requiredScopesAsSet(): Set<Scope> = Sets.newHashSet(*requiredScopes.toTypedArray())

    fun requiredScopesAsStringList(): List<String> = requiredScopes.map { it.toString() }
}
