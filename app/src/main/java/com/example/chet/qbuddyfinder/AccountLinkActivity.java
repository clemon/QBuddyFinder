package com.example.chet.qbuddyfinder;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Random;


public class AccountLinkActivity extends ActionBarActivity {

    private static final String TAG = "AccountLinkActivity";    // tag for logging
    private static final int MAX = 99999999;
    private static final int MIN = 10000000;
    private static int authNum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_link);
        Log.d(TAG, "onCreate");

        Intent intent = getIntent();
        String email = intent.getStringExtra(StartScreenActivity.EXTRA_EMAIL);
        Log.d(TAG, "email: "+email);

        Button linkButton = (Button) findViewById(R.id.linkButton);

        linkButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //get summonerID


                    Random rand = new Random();
                    int randomNum = rand.nextInt((MAX - MIN) + 1) + MIN;
                    AccountLinkActivity.authNum = randomNum;

                    View rootView = findViewById(R.id.linkActivity_root);
                    TextView randomDigits = (TextView) rootView.findViewById(R.id.randomDigits);
                    randomDigits.setText(Integer.toString(randomNum));
                    randomDigits.setAlpha(100);

                    // disable the username field and submit button
                    Button linkButton = (Button) rootView.findViewById(R.id.linkButton);
                    EditText username = (EditText) rootView.findViewById(R.id.username_input);
                    linkButton.setClickable(false);
                    username.setClickable(false);
                    username.setFocusable(false);
                    linkButton.setAlpha((float).25);
                    username.setAlpha((float).25);

                    // get outta my face, keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(username.getWindowToken(), 0);

                    // bring in the next step (rand number and auth button)
                    TextView authInstruct = (TextView) rootView.findViewById(R.id.linkInstructions);
                    Button authButton = (Button) rootView.findViewById(R.id.authButton);
                    authInstruct.setAlpha(1);
                    authButton.setAlpha(1);
                    authButton.setClickable(true);

                }
            }
        );


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_account_link, menu);
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
