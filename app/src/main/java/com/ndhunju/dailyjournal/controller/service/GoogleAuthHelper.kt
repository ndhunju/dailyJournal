package com.ndhunju.checkin.util

import android.accounts.Account
import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth

/**
 * Helper class to manage Google Sign-In for Drive API access.
 */
class GoogleAuthHelper(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    /**
     * Checks if the user is currently signed in.
     */
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    /**
     * Creates a GoogleAccountCredential for use with Drive API.
     */
    fun getCredential(): GoogleAccountCredential? {
        val email = auth.currentUser?.email ?: return null
        return GoogleAccountCredential.usingOAuth2(
            context,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccount = Account(email, "com.google")
        }
    }

    /**
     * Signs out the current user.
     */
    fun signOut() {
        AuthUI.getInstance().signOut(context)
    }
}
