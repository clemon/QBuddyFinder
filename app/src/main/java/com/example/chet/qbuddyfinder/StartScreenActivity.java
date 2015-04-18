package com.example.chet.qbuddyfinder;

import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
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
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.MobileServiceList;
import com.microsoft.windowsazure.mobileservices.table.MobileServiceTable;

import java.net.MalformedURLException;


public class StartScreenActivity extends ActionBarActivity implements
        GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {

    private GoogleApiClient mGoogleApiClient;   // dem Google APIs
    private MobileServiceClient mAzureClient;        // azure

    private static final int RC_SIGN_IN = 0;    // request code
    private boolean mIntentInProgress;          // flag to prevent starting intents if PendingIntent is in progress
    private boolean mSignInClicked = false;             // track if sign in button was clicked
    private static final String TAG = "StartScreenActivity";    // tag for logging
    public static final String EXTRA_EMAIL = "com.example.chet.qbuddyfinder.EMAIL";
    private static String email;

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

        try {
            mAzureClient = new MobileServiceClient(
                    "https://qbuddyfinder.azure-mobile.net/",
                    ApiKey.AZURE_API_KEY,
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        mGoogleApiClient.connect();

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
        if (!mIntentInProgress && result.hasResolution() && mSignInClicked) {
            mSignInClicked = false;
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
        this.email = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Toast.makeText(this, "User ("+email+") is connected!", Toast.LENGTH_LONG).show();

        // get a reference to the Azure DB Table
        final MobileServiceTable<User> mUserTable = mAzureClient.getTable(User.class);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Log.d(TAG, "azure doinbackground");
                    final MobileServiceList<User> result =
                            mUserTable.where().field("email").eq(StartScreenActivity.email).execute().get();

                    boolean match = false;
                    for (User item : result) {
                        Log.i(TAG, "Read object with email "+ item.email);
                        if(item.email.equals(email)) {
                            match = true;
                        }
                    }

                    pageNav(match ? "CardStack" : "AccountLink");

                } catch (Exception exception) {
                    Log.d(TAG, "Azure Exception: "+exception.toString());
                }
                return null;
            }
        }.execute();

    }

    private void pageNav(String where) {

        Intent intent = where.equals("CardStack")
                ? new Intent(this, CardStackActivity.class)
                : new Intent(this, AccountLinkActivity.class);

        intent.putExtra(EXTRA_EMAIL, this.email);
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
