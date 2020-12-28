package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.api.services.drive.Drive;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper;
import com.ndhunju.dailyjournal.controller.service.GoogleSignInHelper;
import com.ndhunju.dailyjournal.util.UtilsView;

import androidx.appcompat.widget.Toolbar;
import androidx.core.util.Pair;

import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_FAIL;
import static com.ndhunju.dailyjournal.controller.service.DriveServiceHelper.OPERATION_STATUS_SUCCESS;

/**
 * The main {@link Activity} for the Drive REST API functionality.
 */
public class GoogleDriveRestApiActivity extends BaseActivity {

    // Constants
    private static final String TAG = GoogleDriveRestApiActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 1;
    private static final int REQUEST_CODE_ERROR_RESOLUTION = 2;
    /** Pass true for this key to finish this activity upon successful sign in to google drive **/
    public static final String BUNDLE_SHOULD_FINISH_ON_SIGN_IN = "BUNDLE_SHOULD_FINISH_ON_SIGN_IN";

    // Member Variables
    private final GoogleSignInHelper googleSignInHelper = GoogleSignInHelper.get();
    private DriveServiceHelper mDriveServiceHelper;

    // View Variables
    private ProgressDialog connectionPd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // Wire views
        connectionPd = new ProgressDialog(getActivity());
        connectionPd.setMessage(String.format(
                getString(R.string.msg_connecting),
                getString(R.string.str_google_drive)
        ));
        connectionPd.setCanceledOnTouchOutside(true);
        connectionPd.setIndeterminate(true);
        connectionPd.setCancelable(true);
        connectionPd.show();

        Pair<GoogleSignInAccount, Integer> googleAccountAndConnectionResult
                = GoogleSignInHelper.get().getLastSignedInAccountAndConnectionResult(getContext());

        if (googleAccountAndConnectionResult.second == ConnectionResult.SUCCESS) {
            if (googleAccountAndConnectionResult.first != null) {
                onSignedInToGoogleAccount(googleAccountAndConnectionResult.first);
            } else {
                requestSignIn();
            }
        } else {
            GoogleApiAvailability.getInstance().getErrorDialog(
                    this,
                    googleAccountAndConnectionResult.second,
                    REQUEST_CODE_ERROR_RESOLUTION
            ).show();
        }
    }

    protected void showProgress(boolean showProgress, String message) {
        if (showProgress) {
            connectionPd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            connectionPd.setMessage(message);
            connectionPd.show();
        } else if (!TextUtils.isEmpty(message)) {
            connectionPd.setProgressDrawable(null);
            connectionPd.setMessage(message);
        } else {
            connectionPd.dismiss();
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

        GoogleSignInClient client = GoogleSignInHelper.get().getGoogleSigInClient(this);

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
        onSignedInToGoogleDrive(
                googleSignInHelper.signInToGoogleDrive(googleAccount, getContext())
        );
    }

    protected void onSignedInToGoogleDrive(Drive googleDriveService) {
        showProgress(false, null);

        if (getIntent().getBooleanExtra(BUNDLE_SHOULD_FINISH_ON_SIGN_IN, false)) {
            setResult(RESULT_OK);
            finish();
            return;
        }

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
