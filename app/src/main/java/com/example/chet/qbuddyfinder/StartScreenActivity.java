package com.example.chet.qbuddyfinder;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;


public class StartScreenActivity extends ActionBarActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;   // dem Google APIs

    private static final int RC_SIGN_IN = 0;    // request code
    private boolean mIntentInProgress;          // flag to prevent starting intents if PendingIntent is in progress
    private boolean mSignInClicked = false;             // track if sign in button was clicked
    private static final String TAG = "StartScreenActivity";    // tag for logging
    public static final String EXTRA_EMAIL = "com.example.chet.qbuddyfinder.EMAIL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_screen);
        Log.d(TAG, "top of onCreate");

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_PROFILE)
                .build();

        SignInButton gPlusButton = (SignInButton) findViewById(R.id.sign_in_button);
        gPlusButton.setSize(SignInButton.SIZE_WIDE);

        // set up a click listener for the sign in button
        gPlusButton.setOnClickListener(
            new View.OnClickListener() {
                 @Override
                 public void onClick(View view) {
                     if(!mGoogleApiClient.isConnecting()) {
                         Log.d(TAG, "signin button click");
                         StartScreenActivity.this.mSignInClicked = true;
                         mGoogleApiClient.connect();
                     }
                 }
            }
        );

        // click listener for the sign out button
        findViewById(R.id.sign_out_button).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if(mGoogleApiClient.isConnected()){
                        Log.d(TAG, "signout button click");
                        Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
                        Plus.AccountApi.revokeAccessAndDisconnect(mGoogleApiClient);
                        mGoogleApiClient.disconnect();
                        mGoogleApiClient.connect();
                    }
                }
            }
        );

    }

    protected void onStop() {
        Log.d(TAG, "enter onStop");
        super.onStop();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    public void onConnectionFailed(ConnectionResult result) {
        Log.d(TAG, "enter onConnectionFailed");
        if (!mIntentInProgress && result.hasResolution()) {
            try {
                mIntentInProgress = true;
                startIntentSenderForResult(result.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                Log.d(TAG, "onConnectionFailed google+ connect()");
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnected(Bundle connectionHint) {
        // We've resolved any connection errors.  mGoogleApiClient can be used to
        // access Google APIs on behalf of the user.
        Log.d(TAG, "onConnected!");
        String email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Toast.makeText(this, "User ("+email+") is connected!", Toast.LENGTH_LONG).show();
        // if its in the database, skip the account linking activity
        // else send email in an intent to the account linking activity
        Intent intent = new Intent(this, AccountLinkActivity.class);
        intent.putExtra(EXTRA_EMAIL, email);
        startActivity(intent);
    }

    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        Log.d(TAG, "enter onActivityResult");
        if (requestCode == RC_SIGN_IN) {
            if (responseCode != RESULT_OK)
                mSignInClicked = false;

            mIntentInProgress = false;

            if (!mGoogleApiClient.isConnecting() && this.mSignInClicked) {
                this.mSignInClicked = false;
                Log.d(TAG, "onActivityResult google+ connect()");
                mGoogleApiClient.connect();
            }
        }
    }

    public void onConnectionSuspended(int cause) {
        Log.d(TAG, "onConnectionSuspended google+ connect()");
        mGoogleApiClient.connect();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_start_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
