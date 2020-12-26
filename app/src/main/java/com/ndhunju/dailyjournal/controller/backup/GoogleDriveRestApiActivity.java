package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.common.collect.Sets;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.controller.BaseActivity;
import com.ndhunju.dailyjournal.controller.service.DriveServiceHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

/**
 * The main {@link Activity} for the Drive REST API functionality.
 */
public class GoogleDriveRestApiActivity
        extends BaseActivity
        implements FileRecyclerViewAdapter.OnFileSelectListener {

    // Constants
    private static final String TAG = GoogleDriveRestApiActivity.class.getSimpleName();
    private static final int REQUEST_CODE_SIGN_IN = 1;

    // Member Variables
    private final GoogleSignInHelper googleSignInHelper = GoogleSignInHelper.get();
    private DriveServiceHelper mDriveServiceHelper;
    private FileRecyclerViewAdapter fileAdapter;

    // View Variables
    private ViewGroup progressContainer;
    private ProgressBar progressBar;
    private TextView messageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_drive);

        // Setup Toolbar
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Wire views
        progressContainer = findViewById(R.id.progress_container);
        progressBar = findViewById(R.id.progress_circular);
        messageView = findViewById(R.id.message);

        // Setup recycler view
        RecyclerView fileRecyclerView = findViewById(R.id.file_list);
        fileRecyclerView.addItemDecoration(
                new DividerItemDecoration(getContext(), LinearLayout.VERTICAL)
        );
        fileRecyclerView.setAdapter(fileAdapter = new FileRecyclerViewAdapter());

        // Authenticate user if not already.
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(getContext());
        if (signInAccount != null
                && signInAccount
                .getGrantedScopes()
                .containsAll(googleSignInHelper.requiredScopesAsSet())) {
            onSignedInToGoogleAccount(signInAccount);
        } else {
            requestSignIn();
        }
    }

    private void showProgress(boolean showProgress, String message) {
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
     * Starts a sign-in activity using {@link #REQUEST_CODE_SIGN_IN}.
     */
    private void requestSignIn() {
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
                .addOnSuccessListener(this::onSignedInToGoogleAccount)
                .addOnFailureListener(exception -> Log.e(TAG, "Failed sign in.", exception));
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

    private void onSignedInToGoogleDrive(Drive googleDriveService) {
        showProgress(false, null);
        // The DriveServiceHelper encapsulates all REST API and SAF functionality.
        // Its instantiation is required before handling any onClick actions.
        mDriveServiceHelper = new DriveServiceHelper(googleDriveService);
        queryForBackupFiles();
    }

    /**
     * Queries the Drive REST API for files visible to this app and lists them in the content view.
     */
    private void queryForBackupFiles() {
        if (mDriveServiceHelper != null) {
            Log.d(TAG, "Querying for backup files.");
            showProgress(true, getString(R.string.msg_loading_files));

            mDriveServiceHelper.queryFiles()
                    .addOnSuccessListener(fileList -> {
                        showProgress(false, null);
                        fileAdapter.setFileList(fileList.getFiles());
                        fileAdapter.setOnFileSelectListener(this);
                    })
                    .addOnFailureListener(exception -> {
                        Log.e(TAG, "Unable to query files.", exception);
                        if (exception instanceof UserRecoverableAuthIOException
                                || exception instanceof UserRecoverableAuthException) {
                            // User might have revoked access, request Sign In again
                            requestSignIn();
                        } else {
                            showProgress(
                                    false,
                                    getString(R.string.msg_error_google_drive_query_fail) +
                                            "\n(" + exception.getLocalizedMessage() + ")"
                            );
                        }
                    });
        }
    }

    @Override
    public void onFileSelect(File file) {
        new FetchBackUpFromGDriveRestApiAsync(
                GoogleDriveRestApiActivity.this,
                mDriveServiceHelper,
                result -> {
                    new AlertDialog.Builder(GoogleDriveRestApiActivity.this)
                            .setMessage(result)
                            .create().show();
                }).execute(file);
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

    private Context getContext() {
        return this;
    }

    /**
     * Helper class that groups relevant objects like {@link Scope} and provides helper methods
     */
    static class GoogleSignInHelper {

        public static final GoogleSignInHelper INSTANCE = new GoogleSignInHelper();

        public static GoogleSignInHelper get() {
            return INSTANCE;
        }

        Scope[] requiredScopes = { new Scope(DriveScopes.DRIVE_FILE) };

        private GoogleSignInHelper() {}

        public GoogleSignInOptions buildGoogleSigInOptions() {
            GoogleSignInOptions.Builder builder = new GoogleSignInOptions.Builder(
                    GoogleSignInOptions.DEFAULT_SIGN_IN
            ).requestEmail();

            for (Scope scope: requiredScopes) {
                builder.requestScopes(scope);
            }

            return builder.build();
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

}
