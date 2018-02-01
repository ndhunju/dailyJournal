package com.ndhunju.dailyjournal.controller.backup;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.ndhunju.dailyjournal.R;
import com.ndhunju.dailyjournal.util.UtilsView;

/** Created by ndhunju on 1/15/17.
 * This class encapsulates common operations when dealing with {@link GoogleApiClient}*/
public abstract class GoogleDriveBackupActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = GoogleDriveBackupActivity.class.getSimpleName();

    //For Google Drive Api
    private static final int REQUEST_CODE_GDRIVE_RESOLUTION = 1258;

    protected GoogleApiClient mGoogleApiClient;
    private AlertDialog suspendedAlertDialog;
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

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        connect();

    }

    public ConnectionResult getGDriveConnectionResult() {
        if (mGoogleApiClient.isConnected()) {
            return mGoogleApiClient.getConnectionResult(Drive.API);
        }
        return null;
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    public void connect(){
        if(mGoogleApiClient != null && !mGoogleApiClient.isConnected())
            mGoogleApiClient.connect();
    }

    public void disconnect(){
        if(mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    /** After calling connect(), this method will be invoked asynchronously when the connect request
     *  has successfully completed. After this callback, the application can make requests on other
     *  methods provided by the client and expect that no user intervention is required to call
     *  methods that use account and scopes provided to the client constructor**/
    @Override
    public void onConnected(Bundle bundle) {
        if (connectionPd != null) connectionPd.dismiss();
        if (suspendedAlertDialog != null) suspendedAlertDialog.dismiss();

    }

    /** Called when the client is temporarily in a disconnected state. This can happen if there is a
     *  problem with the remote service (e.g. a crash or resource problem causes it to be killed by
     *  the system). When called, all requests have been canceled and no outstanding listeners will
     *  be executed. GoogleApiClient will automatically attempt to restore the connection.
     *  Applications should disable UI components that require the service, and wait for a call to
     *  onConnected(Bundle) to re-enable them **/
    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "GoogleApiClient connection suspended. Cause = " + i);
        if (i == CAUSE_NETWORK_LOST || i == CAUSE_SERVICE_DISCONNECTED) {
            if (connectionPd != null) {
                connectionPd.dismiss();
            }

            suspendedAlertDialog = new AlertDialog.Builder(getActivity())
                    .setMessage(getString(R.string.msg_error_google_service))
                    .setTitle(getString(R.string.str_alert))
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            finish();
                        }
                    }).create();
            suspendedAlertDialog.show();
        }
    }

    @Override
    public void onConnectionFailed(final ConnectionResult connectionResult) {
        // Called whenever the API client fails to connect.
        if (!connectionResult.hasResolution()) {
            // there is no resolution for the failure, show message and finish the activity
            String msg = GoogleApiAvailability.getInstance().getErrorString(connectionResult.getErrorCode());
            Log.i(TAG, "GoogleApiClient connection failed: " + msg);
            showEndResultToUser(msg, false);
        }

        // If the failure has a resolution. Resolve it. Called typically when the app is
        // not yet authorized, and an authorization dialog is displayed to the user.
        try {
            connectionResult.startResolutionForResult(getActivity(), REQUEST_CODE_GDRIVE_RESOLUTION);
            Log.i(TAG, "Resolving resolution");
        } catch (IntentSender.SendIntentException e) {
            Log.e(TAG, "Exception while starting resolution activity", e);
            showEndResultToUser(e.getLocalizedMessage(), false);
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_GDRIVE_RESOLUTION:
                // google drive connection issue has been resolved
                Log.i(TAG, "Activity result request code resolution");
                if (resultCode == Activity.RESULT_OK) {
                    // try connecting now
                    connect();
                } else {
                    // first see if there was an issue with google drive connection
                    ConnectionResult connectionResult = getGDriveConnectionResult();
                    Dialog dialog = GoogleApiAvailability.getInstance().getErrorDialog(getActivity(),
                            connectionResult != null ? connectionResult.getErrorCode() : 0,
                            REQUEST_CODE_GDRIVE_RESOLUTION);

                    if (dialog == null) {
                        dialog = new AlertDialog.Builder(getActivity())
                                .setMessage(getString(R.string.common_google_play_services_unknown_issue, getString(R.string.app_name)))
                                .create();
                    }

                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            finish();
                        }
                    });
                    dialog.show();
                    getActivity().setResult(resultCode);
                }
                break;
            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    protected void showEndResultToUser(String message, boolean success) {
        setResult(success ? Activity.RESULT_OK : Activity.RESULT_CANCELED);
        UtilsView.alert(getActivity(), message, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                finish();
            }
        });
    }

    public Activity getActivity() {
        return this;
    }


    @Override
    protected void onStart() {
        super.onStart();
        connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }
}
