package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.tasks.Task;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsView;

import java.util.HashSet;
import java.util.Set;

public class GoogleDriveSignInActivity extends AppCompatActivity {

    /** Request code for Google Sign-in */
    private static final int REQUEST_CODE_SIGN_IN = 0;

    public static final String TAG = GoogleDriveSignInActivity.class.getSimpleName();
    /** Pass true for this key to finish this activity upon successful sign in to google drive **/
    public static final String BUNDLE_SHOULD_FINISH_ON_SIGN_IN = "BUNDLE_SHOULD_FINISH_ON_SIGN_IN";

    public static GoogleSignInClient makeSignInClient(Context context) {
        GoogleSignInOptions signInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestScopes(Drive.SCOPE_FILE)
                .requestScopes(Drive.SCOPE_APPFOLDER)
                .build();
        return GoogleSignIn.getClient(context, signInOptions);
    }

    private ProgressDialog connectionPd;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        connectionPd = new ProgressDialog(getActivity());
        connectionPd.setMessage(String.format(getString(R.string.msg_connecting), getString(R.string.str_google_drive)));
        connectionPd.setCanceledOnTouchOutside(true);
        connectionPd.setIndeterminate(true);
        connectionPd.setCancelable(true);
        connectionPd.show();

    }

    /** Starts the sign-in process and initializes the Drive client. */
    protected void signIn() {
        Set<Scope> requiredScopes = new HashSet<>(2);
        requiredScopes.add(Drive.SCOPE_FILE);
        requiredScopes.add(Drive.SCOPE_APPFOLDER);
        GoogleSignInAccount signInAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (signInAccount != null && signInAccount.getGrantedScopes().containsAll(requiredScopes) && !signInAccount.isExpired()) {
            onSignedIn(signInAccount);
        } else {
            showSignInPage();
        }
    }

    public void showSignInPage() {
        GoogleSignInClient googleSignInClient = makeSignInClient(this);
        startActivityForResult(googleSignInClient.getSignInIntent(), REQUEST_CODE_SIGN_IN);
    }

    protected void onSignedIn(GoogleSignInAccount googleSignInAccount) {
        if (connectionPd != null) connectionPd.dismiss();

        if (getIntent().getBooleanExtra(BUNDLE_SHOULD_FINISH_ON_SIGN_IN, false)) {
            setResult(RESULT_OK);
            finish();
        }
    }

    protected void showEndResultToUser(String message, boolean success) {
        setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        if (getActivity() != null && !getActivity().isFinishing()) {
            UtilsView.alert(getActivity(), message, (dialog, which) -> {
                setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
                finish();
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        signIn();
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_SIGN_IN:
                Task<GoogleSignInAccount> getAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);
                if (getAccountTask.isSuccessful()) {
                    onSignedIn(getAccountTask.getResult());
                } else {
                    Log.e(TAG, "Sign-in failed.");
                    showEndResultToUser(getString(R.string.common_google_play_services_unknown_issue, getString(R.string.app_name)), false);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    public Activity getActivity() {
        return this;
    }
}
