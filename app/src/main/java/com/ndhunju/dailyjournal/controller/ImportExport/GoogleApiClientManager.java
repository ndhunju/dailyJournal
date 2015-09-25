package com.ndhunju.dailyjournal.controller.ImportExport;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.IntentSender;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.drive.Drive;
import com.ndhunju.dailyjournal.R;

/**
 * Created by dhunju on 9/24/2015.
 */
public class GoogleApiClientManager {

    private static final String TAG = GoogleApiClientManager.class.getSimpleName();
    private GoogleApiClient mGoogleApiClient;   //GoogleApiClient to use Google Drive
    private Activity mActivity;

    private static GoogleApiClientManager mGoogleApiClientMgr;

    public static GoogleApiClientManager getInstance(Activity activity){
        mGoogleApiClientMgr = new GoogleApiClientManager(activity);
        return mGoogleApiClientMgr;
    }

    private GoogleApiClientManager(Activity context){
        mActivity = context;
    }

    /**
     *Initiates the connection to Google Drive.
     */
    public void connectGoogleApiClient(GoogleApiClient.ConnectionCallbacks connectionCallbacks, final int requestCodeGDriveResolution) {
        final ProgressDialog connectionPd = new ProgressDialog(mActivity);
        connectionPd.setIndeterminate(true);
        connectionPd.setMessage(String.format(mActivity.getString(R.string.msg_connecting), mActivity.getString(R.string.str_google_drive)));
        connectionPd.setCancelable(false);
        connectionPd.setCanceledOnTouchOutside(false);
        connectionPd.show();

        mGoogleApiClient = new GoogleApiClient.Builder(mActivity)
                .addApi(Drive.API)
                .addScope(Drive.SCOPE_FILE)
                .addConnectionCallbacks(connectionCallbacks)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle bundle) {
                        Log.i(TAG, "API client connected.");
                        connectionPd.cancel();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        connectionPd.cancel();
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                        // Called whenever the API client fails to connect.
                        Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
                        if (!result.hasResolution()) {
                            // show the localized error dialog.
                            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), mActivity, 0).show();
                            return;
                        }
                        // If the failure has a resolution. Resolve it. Called typically when the app is
                        // not yet authorized, and an authorization dialog is displayed to the user.
                        try {
                            result.startResolutionForResult(mActivity, requestCodeGDriveResolution);
                            Log.i(TAG, "Resolving resolution");
                        } catch (IntentSender.SendIntentException e) {
                            Log.e(TAG, "Exception while starting resolution activity", e);
                        }
                    }
                })
                .build();

        mGoogleApiClient.connect();
    }

    public GoogleApiClient getGoogleApiClient(){
        return mGoogleApiClient;
    }

    public void connect(){
        if(!mGoogleApiClient.isConnected())
           mGoogleApiClient.connect();
    }

    public void disconnect(){
        if(!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
    }
}
