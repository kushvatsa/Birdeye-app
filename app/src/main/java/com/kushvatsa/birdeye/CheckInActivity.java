package com.kushvatsa.birdeye;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import javax.net.ssl.HttpsURLConnection;

public class CheckInActivity extends AppCompatActivity {

    private final String TAG = CheckInActivity.class.getSimpleName();

    EditText tx_f_name;
    EditText tx_l_name;
    EditText tx_email;
    EditText tx_phone;
    Button btn_check;
    ProgressBar mBar;

    String d_fname;
    String d_lname;
    String d_name;
    String d_email;
    String d_phone;
    ConstraintLayout constraintLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        constraintLayout = findViewById(R.id.cl_checkIn);

        tx_f_name = findViewById(R.id.data_fname);
        tx_l_name = findViewById(R.id.data_lname);
        tx_email = findViewById(R.id.data_email);
        tx_phone = findViewById(R.id.data_phone);
        btn_check = findViewById(R.id.btn_check);

        mBar= findViewById(R.id.screen_wait_ci);

        tx_email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                d_email = s.toString().toLowerCase();
                //checking email is valid or not
                if(!Utils.isValidEmaillId(d_email))
                {
                    tx_email.setError("Invalid email type");
                }
                if(d_email==null)
                {
                    tx_email.setError("Please enter email");
                }
            }
        });
        tx_phone.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                d_phone = s.toString().toLowerCase();
            }
        });

        tx_f_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                d_fname = s.toString();
            }
        });

        tx_l_name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                d_lname = s.toString();
            }
        });


        btn_check.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                d_name = d_fname + " " + d_lname;
                new DataBackground().execute();
            }
        });

    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_none, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NavUtils.navigateUpFromSameTask(this);
    }

    public class DataBackground  extends
            AsyncTask<Void, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mBar.setVisibility(View.VISIBLE);
            constraintLayout.setVisibility(View.GONE);
        }


        // Sending data to server
        @Override
        protected Boolean doInBackground(Void... voids) {
            URL url;
            String response = "";
            try {

                url = new URL(Utils.BE_POST_URL);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(15000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("content-type", "application/json");
                conn.setRequestProperty("accept" , "application/json");

                JSONObject root = new JSONObject();
                root.put(Utils.BE_NAME, d_name);
                root.put(Utils.BE_EMAIL, d_email);
                root.put(Utils.BE_PHONE, d_phone);

                Log.d(TAG, "root : " + root.toString());

                String str = root.toString();
                byte[] outputBytes = str.getBytes("UTF-8");
                OutputStream os = conn.getOutputStream();
                os.write(outputBytes);

                int responseCode = conn.getResponseCode();

                Log.e(TAG, "responseCode : " + responseCode);

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Log.d(TAG, "HTTP_OK");

                    String line;
                    BufferedReader br = new BufferedReader(new InputStreamReader(
                            conn.getInputStream()));
                    while ((line = br.readLine()) != null) {
                        response += line;
                    }
                } else {
                    Log.d(TAG, "False - HTTP_OK");
                    response = "";
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mBar.setVisibility(View.GONE);
            //moving to main activity
            startActivity(new Intent(CheckInActivity.this, MainActivity.class));
        }
    }



}
