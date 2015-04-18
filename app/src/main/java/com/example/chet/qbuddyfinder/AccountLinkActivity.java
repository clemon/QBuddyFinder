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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.microsoft.windowsazure.mobileservices.MobileServiceClient;
import com.microsoft.windowsazure.mobileservices.http.ServiceFilterResponse;
import com.microsoft.windowsazure.mobileservices.table.TableOperationCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.MalformedURLException;
import java.util.Random;


public class AccountLinkActivity extends ActionBarActivity {

    private MobileServiceClient mClient;
    private static final String TAG = "AccountLinkActivity";    // tag for logging
    private static final int MAX = 99999999;
    private static final int MIN = 10000000;
    private static String email;
    private static String region;
    private static int authNum;
    private static String summId;
    private static String summName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_link);
        Log.d(TAG, "onCreate");

        try {
            mClient = new MobileServiceClient(
                    "https://qbuddyfinder.azure-mobile.net/",
                    ApiKey.AZURE_API_KEY,
                    this
            );
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Intent intent = getIntent();
        this.email = intent.getStringExtra(StartScreenActivity.EXTRA_EMAIL);
        Log.d(TAG, "email: "+email);

        // Spinner setup and choice listener
        Spinner regionsSpin = (Spinner) findViewById(R.id.regions_spinner);
        ArrayAdapter<CharSequence> spinAdapter = ArrayAdapter.createFromResource(this,
                R.array.regions_array, android.R.layout.simple_spinner_item);
        spinAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        regionsSpin.setAdapter(spinAdapter);

        regionsSpin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                // An item was selected. You can retrieve the selected item using
                Log.d(TAG, (String) parent.getItemAtPosition(pos));
                AccountLinkActivity.region = ((String) parent.getItemAtPosition(pos)).toLowerCase();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // because it bitches if i don't
            }
        });

        // Submit Button and click listener
        Button linkButton = (Button) findViewById(R.id.linkButton);
        linkButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Random rand = new Random();
                    int randomNum = rand.nextInt((MAX - MIN) + 1) + MIN;
                    AccountLinkActivity.authNum = randomNum;

                    View rootView = findViewById(R.id.linkActivity_root);
                    TextView randomDigits = (TextView) rootView.findViewById(R.id.randomDigits);
                    randomDigits.setText(Integer.toString(randomNum));
                    randomDigits.setAlpha(100);

                    // disable the username field, spinner and submit button
                    Button linkButton = (Button) rootView.findViewById(R.id.linkButton);
                    EditText username = (EditText) rootView.findViewById(R.id.username_input);
                    Spinner regionSpin = (Spinner) rootView.findViewById(R.id.regions_spinner);
                    linkButton.setClickable(false);
                    username.setClickable(false);
                    username.setFocusable(false);
                    regionSpin.setClickable(false);
                    linkButton.setAlpha((float).25);
                    username.setAlpha((float).25);
                    regionSpin.setAlpha((float).25);

                    AccountLinkActivity.summName = username.getText().toString();

                    // get outta my face, keyboard
                    InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(username.getWindowToken(), 0);

                    // Instantiate the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(view.getContext());

                    // url to get summonerID
                    String url = "https://"+AccountLinkActivity.region+".api.pvp.net/api/lol/"
                            +AccountLinkActivity.region+"/v1.4/summoner/by-name/"
                            +username.getText().toString()+"?api_key="+ApiKey.RIOT_API_KEY;

                    // Request a JSON response from the provided URL.
                    JsonObjectRequest summonderIDrequest = new JsonObjectRequest(Request.Method.GET, url,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, "Response is: "+ response.toString());
                                    EditText sumname_field = (EditText) findViewById(R.id.username_input);
                                    String sumname = sumname_field.getText().toString().toLowerCase();

                                    // parse the volley response
                                    try {
                                        JSONObject summoner = response.getJSONObject(sumname);
                                        AccountLinkActivity.summId = summoner.getString("id");
                                        Log.d(TAG, "id: "+AccountLinkActivity.summId);

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error.networkResponse != null && error.networkResponse.data != null) {
                                VolleyError volleyError = new VolleyError(new String(error.networkResponse.data));
                                Log.d(TAG, "error yo: " + volleyError);
                            }
                            // TODO actually handle errors?
                        }
                    });
                    // Add the request to the RequestQueue.
                    queue.add(summonderIDrequest);

                    // bring in the next step (rand number and auth button)
                    TextView authInstruct = (TextView) rootView.findViewById(R.id.linkInstructions);
                    Button authButton = (Button) rootView.findViewById(R.id.authButton);
                    authInstruct.setAlpha(1);
                    authButton.setAlpha(1);
                    authButton.setClickable(true);
                }
            }
        );

        // Auth Button and click listener
        Button authButton = (Button) findViewById(R.id.authButton);
        authButton.setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Instantiate the RequestQueue.
                    RequestQueue queue = Volley.newRequestQueue(view.getContext());
                    // url to get rune pages
                    String url = "https://"+AccountLinkActivity.region+".api.pvp.net/api/lol/"
                            +AccountLinkActivity.region+"/v1.4/summoner/"
                            +AccountLinkActivity.summId+"/runes?api_key="+ApiKey.RIOT_API_KEY;

                    // Request a JSON response from the provided URL.
                    JsonObjectRequest runePageRequest = new JsonObjectRequest(Request.Method.GET, url,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d(TAG, "Response is: "+ response.toString());

                                    // parse the response
                                    try {
                                        JSONObject summID = response.getJSONObject(AccountLinkActivity.summId);
                                        JSONArray runePages = summID.getJSONArray("pages");

                                        for(int x = 0; x < runePages.length(); x++) {
                                            JSONObject page = runePages.getJSONObject(x);
                                            String pageTitle = page.getString("name");
                                            Log.d(TAG, "runepage "+x+": "+pageTitle);

                                            // if match found
                                            if(Integer.toString(AccountLinkActivity.authNum).equals(pageTitle)) {
                                                // TODO put all that shit in the database

                                                getTier();
                                            }
                                        }

                                        // didnt find a match. Try again?
                                        Button authButton = (Button) findViewById(R.id.authButton);
                                        authButton.setText("Try Again");

                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            if(error.networkResponse != null && error.networkResponse.data != null) {
                                VolleyError volleyError = new VolleyError(new String(error.networkResponse.data));
                                Log.d(TAG, "error yo: " + volleyError);
                            }
                            // TODO actually handle errors?
                        }
                    });
                    // Add the request to the RequestQueue.
                    queue.add(runePageRequest);

                }
            }
        );


    }


    public void getTier() {
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);


        // API url for ranked stats
        String url = "https://"+region+".api.pvp.net/api/lol/"+region+"/v2.5/league/by-summoner/"
                +summId+"/entry?api_key="+ApiKey.RIOT_API_KEY;

        // Request a JSON response from the provided URL.
        JsonObjectRequest tierRequest = new JsonObjectRequest(Request.Method.GET, url,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "tier Response is: "+ response.toString());
                        try {
                            JSONArray arr = response.getJSONArray(summId);

                            for(int x=0; x<arr.length(); x++) {
                                JSONObject mode = arr.getJSONObject(x);
                                if(mode.getString("queue").equals("RANKED_SOLO_5x5")) {

                                    toTheDB(mode.getString("tier")
                                    +" "+mode.getJSONArray("entries").getJSONObject(0).getString("division"));

                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null && error.networkResponse.data != null) {
                    VolleyError volleyError = new VolleyError(new String(error.networkResponse.data));
                    Log.d(TAG, "error yo: " + volleyError);
                }
                // TODO actually handle errors?
            }
        });
        // Add the request to the RequestQueue.
        queue.add(tierRequest);

    }

    public void toTheDB(String tier) {
        User user = new User();
        user.email = AccountLinkActivity.email;
        user.summonerId = AccountLinkActivity.summId;
        user.region = AccountLinkActivity.region;
        user.summonerName = AccountLinkActivity.summName;
        user.tier = tier;
        user.candidates = "";
        user.matches = "";
        Log.d(TAG, "Authenticated!");
        Log.d(TAG, "email: "+user.email
                +"\nsummId: "+user.summonerId
                +"\nregion: "+user.region
                +"\nsummName: "+user.summonerName
                +"\ntier: "+user.tier);

        mClient.getTable(User.class).insert(user, new TableOperationCallback<User>() {
            public void onCompleted(User entity, Exception exception, ServiceFilterResponse response) {
                if (exception == null) {
                    // Insert succeeded
                    Log.d(TAG, "azure success");
                    
                } else {
                    // Insert failed
                    Log.d(TAG, "azure failed us..."+exception.toString());
                }
            }
        });
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
