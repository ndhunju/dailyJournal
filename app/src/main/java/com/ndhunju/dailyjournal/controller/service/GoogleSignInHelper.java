package com.ndhunju.dailyjournal.controller.service;

import android.content.Context;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.common.collect.Sets;
import com.ndhunju.dailyjournal.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;

import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_FAIL;

/**
 * Helper class that groups relevant objects like {@link Scope} and provides helper methods
 */
public class GoogleSignInHelper {

    public static final GoogleSignInHelper INSTANCE = new GoogleSignInHelper();

    public static GoogleSignInHelper get() {
        return INSTANCE;
    }

    Scope[] requiredScopes = {
            // See https://github.com/vaquarkhan/Scoot-mobile-app/blob/master/ScootAppSource/com/google/android/gms/auth/api/signin/GoogleSignInOptions.java
//            GoogleSignInOptions.zat, // openId
//            GoogleSignInOptions.zar, // profile
            // Without email, gives "IllegalArgumentException: the name must not be empty: null"
            /** See {@link GoogleSignInOptions.Builder#requestEmail()} to know which var is for email */
            GoogleSignInOptions.zab, // email
            new Scope(DriveScopes.DRIVE_FILE)
    };

    private GoogleSignInHelper() {}

    public GoogleSignInOptions buildGoogleSigInOptions() {
        GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder();

        for (Scope scope: requiredScopes) {
            builder.requestScopes(scope);
        }

        return builder.build();
    }

    public GoogleSignInClient getGoogleSigInClient(Context context) {
        return GoogleSignIn.getClient(
                context,
                GoogleSignInHelper.get().buildGoogleSigInOptions()
        );
    }

    @NonNull
    public Drive signInToGoogleDrive(GoogleSignInAccount googleSignInAccount, Context context) {
        // Use the authenticated account to sign in to the Drive service.
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                context,
                requiredScopesAsStringList()
        );

        credential.setSelectedAccount(googleSignInAccount.getAccount());

        return new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                credential
        ).setApplicationName(context.getString(R.string.app_name)).build();
    }

    public Pair<GoogleSignInAccount, Integer> getLastSignedInAccountAndConnectionResult(
            Context context
    ) {

        int googleServiceStatus = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(context);

        switch (googleServiceStatus) {
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.API_UNAVAILABLE:
            case ConnectionResult.SERVICE_DISABLED:
                return Pair.create(null, googleServiceStatus);
            case ConnectionResult.SUCCESS:
                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(context);
                if (signInAccount != null
                        && !signInAccount.isExpired()
                        && (signInAccount.getGrantedScopes()
                        .containsAll(requiredScopesAsSet()))
                        && (DriveServiceHelper.getLastOperationStatus(context)
                        != OPERATION_STATUS_FAIL)) {
                    return Pair.create(signInAccount, googleServiceStatus);
                }
        }

        return Pair.create(null, googleServiceStatus);
    }

    public Set<Scope> requiredScopesAsSet() {
        return Sets.newHashSet(requiredScopes);
    }

    public List<String> requiredScopesAsStringList() {
        List<String> scopes = new ArrayList<>();
        for (Scope scope: requiredScopes) {
            scopes.add(scope.toString());
        }

        return scopes;
    }
}
