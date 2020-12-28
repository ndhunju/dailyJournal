package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.services.drive.Drive;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper;
import com.ndhunju.dailyjournal.controller.service.GoogleSignInHelper;
import com.ndhunju.dailyjournal.util.UtilsView;

import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_FAIL;

/**
 * The main {@link Activity} for the Drive REST API functionality.
 */
public abstract class GoogleDriveRestApiActivity extends BaseActivity {

    // Constants
    private static final String TAG = GoogleDriveRestApiActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_ERROR_RESOLUTION = 2;

    // Member Variables
    private final GoogleSignInHelper googleSignInHelper = GoogleSignInHelper.get();
    private DriveServiceHelper mDriveServiceHelper;

    // View Variables
    private ViewGroup progressContainer;
    private ProgressBar progressBar;
    private TextView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Wire views
        progressContainer = findViewById(R.id.progress_container);
        progressBar = findViewById(R.id.progress_circular);
        messageView = findViewById(R.id.message);

        int googleServiceStatus = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);
        switch (googleServiceStatus) {
            case ConnectionResult.SERVICE_MISSING:
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
            case ConnectionResult.API_UNAVAILABLE:
            case ConnectionResult.SERVICE_DISABLED:
                GoogleApiAvailability.getInstance().getErrorDialog(
                        this,
                        googleServiceStatus,
                        REQUEST_CODE_ERROR_RESOLUTION
                ).show();
                break;
            case ConnectionResult.SUCCESS:
                // Authenticate user if not already.
                GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
                if (signInAccount != null
                        && !signInAccount.isExpired()
                        && (signInAccount.getGrantedScopes()
                                         .containsAll(googleSignInHelper.requiredScopesAsSet()))
                        && (DriveServiceHelper.getLastOperationStatus(this)
                            != OPERATION_STATUS_FAIL)) {
                    onSignedInToGoogleAccount(signInAccount);
                } else {
                    requestSignIn();
                }
                break;
        }
    }

    protected void showProgress(boolean showProgress, String message) {
        if (showProgress) {
            progressContainer.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.VISIBLE);
            messageView.setText(message);
        } else if (!TextUtils.isEmpty(message)) {
            progressContainer.setVisibility(View.VISIBLE);
            messageView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            messageView.setText(message);
        } else {
            progressContainer.setVisibility(View.GONE);
        }
    }

    /**
     * Shows {@code message} to the user in a dialog. When user acknowledges the message, finishes
     * current activity and passes {@code success} to previous activity.
     */
    protected void showEndResultToUser(String message, boolean success) {
        setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        if (getActivity() != null && !getActivity().isFinishing()) {
            UtilsView.alert(getActivity(), message, (dialog, which) -> {
                setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
                finish();
            });
        }
    }

    /**
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    protected void requestSignIn() {
        Log.d(TAG, "Requesting sign-in");
        showProgress(true, getString(R.string.msg_requesting_sign_in));

        GoogleSignInOptions signInOptions = GoogleSignInHelper.get().buildGoogleSigInOptions();
        GoogleSignInClient client = GoogleSignIn.getClient(this, signInOptions);

        // The result of the sign-in Intent is handled in onActivityResult.
        startActivityForResult(client.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == Activity.RESULT_OK && resultData != null) {
                    handleSignInResult(resultData);
                } else {
                    DriveServiceHelper.setLastOperationStatus(this, OPERATION_STATUS_FAIL);
                    showEndResultToUser(
                            getString(R.string.msg_error_g_drive_user_not_signed_in),
                            false
                    );
                }
                break;
            case REQUEST_CODE_ERROR_RESOLUTION:
                if (resultCode == Activity.RESULT_OK) {
                    requestSignIn();
                } else {
                    DriveServiceHelper.setLastOperationStatus(this, OPERATION_STATUS_FAIL);
                    showEndResultToUser(
                            getString(R.string.msg_error_g_drive_user_not_signed_in),
                            false
                    );
                }
                break;
        }

        super.onActivityResult(requestCode, resultCode, resultData);
    }

    /**
     * Handles the {@code result} of a completed sign-in activity initiated from {@link
     * #requestSignIn()}.
     */
    private void handleSignInResult(Intent result) {
        GoogleSignIn.getSignedInAccountFromIntent(result)
                .addOnSuccessListener(googleSignInAccount -> {
                    DriveServiceHelper.setLastOperationStatus(getContext(), OPERATION_STATUS_SUCCESS);
                    onSignedInToGoogleAccount(googleSignInAccount);
                })
                .addOnFailureListener(exception -> {
                    Log.e(TAG, "Failed sign in.", exception);
                    DriveServiceHelper.setLastOperationStatus(this, OPERATION_STATUS_FAIL);
                    showEndResultToUser(
                            getString(R.string.msg_error_g_drive_user_not_signed_in),
                            false
                    );
                });
    }

    private void onSignedInToGoogleAccount(GoogleSignInAccount googleAccount) {
        Log.d(TAG, "Sign in successful");
        // Use the authenticated account to sign in to the Drive service.
        GoogleAccountCredential credential = GoogleAccountCredential.usingOAuth2(
                this,
                googleSignInHelper.requiredScopesAsStringList()
        );

        credential.setSelectedAccount(googleAccount.getAccount());
        Drive googleDriveService = new Drive.Builder(
                AndroidHttp.newCompatibleTransport(),
                new AndroidJsonFactory(),
                credential
        ).setApplicationName(getString(R.string.app_name)).build();

        onSignedInToGoogleDrive(googleDriveService);
    }

    protected void onSignedInToGoogleDrive(Drive googleDriveService) {
        showProgress(false, null);
        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public DriveServiceHelper getDriveServiceHelper() {
        return mDriveServiceHelper;
    }

    public Context getContext() {
        return this;
    }

    public Activity getActivity() {
        return this;
    }

}
