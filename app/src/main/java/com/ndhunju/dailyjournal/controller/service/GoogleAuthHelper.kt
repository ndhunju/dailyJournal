package com.ndhunju.dailyjournal.controller.service

import android.accounts.Account
import android.content.Context
import com.firebase.ui.auth.AuthUI
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.firebase.auth.FirebaseAuth

/**
 * Helper class to manage Google Sign-In for Drive API access.
 */
class GoogleAuthHelper(private val context: Context) {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    val errorCode: Int? = null

    /**
     * Checks if the user is currently signed in.
     */
    fun isSignedIn(): Boolean {
        return auth.currentUser != null
    }

    fun getDriveService(): Drive? {
        val credential = getCredential() ?: return null

        return Drive.Builder(
            NetHttpTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("Daily Journal")
            .build()
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
