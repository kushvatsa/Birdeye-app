package com.kushvatsa.birdeye.sync;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.format.Time;
import android.util.Log;

import com.kushvatsa.birdeye.BuildConfig;
import com.kushvatsa.birdeye.Utils;
import com.kushvatsa.birdeye.data.CustContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

public class BirdEyeSyncTask {


    synchronized public static void syncCustData(Context context) {
        Log.d("START", "START");

        Time dayTime = new Time();
        dayTime.setToNow();

        // we start at the day returned by local time. Otherwise this is a mess.
        int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

        // now we work exclusively in UTC
        dayTime = new Time();

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        try {
            Uri builtUri = Uri.parse(Utils.BE_URL).buildUpon()
                    .appendQueryParameter(Utils.BE_API_KEY, BuildConfig.apikey)
                    .appendQueryParameter(Utils.BE_B_KEY, BuildConfig.bid)
                    .build();
            URL url = new URL(builtUri.toString());

            Log.d("Built Uri", String.valueOf(builtUri));

            // Create the request and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("content-type", "application/json");
            urlConnection.setRequestProperty("accept" , "application/json");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuilder buffer = new StringBuilder();
            if (inputStream == null) {
                // Nothing to do.
                return;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line).append("\n");
            }

            Log.d("buffer length", String.valueOf(buffer.length()));

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.

                return;
            }

            String CustJsonStr = buffer.toString();

            Log.d("customersjson", CustJsonStr);


            JSONArray customersArray = new JSONArray(CustJsonStr);

            // Insert the new information into the database
            Vector<ContentValues> cVVector = new Vector<ContentValues>(customersArray.length());
            try {
                for (int i = 0; i < customersArray.length(); ++i) {
                    long dateTime;
                    dateTime = dayTime.setJulianDay(julianStartDay + i);
                    /*
                      handle JSON data.
                     */
                    JSONObject customerObject = customersArray.getJSONObject(i);

                    ContentValues customersValues = new ContentValues();

                    customersValues.put(CustContract.CustDetailsEntry.COLUMN_EMAIL,
                            customerObject.getString(Utils.BE_EMAIL));
                    customersValues.put(CustContract.CustDetailsEntry.COLUMN_F_NAME,
                            customerObject.getString(Utils.BE_FIRST_NAME));
                    customersValues.put(CustContract.CustDetailsEntry.COLUMN_L_NAME,
                            customerObject.getString(Utils.BE_LAST_NAME));
                    customersValues.put(CustContract.CustDetailsEntry.COLUMN_PHONE,
                            customerObject.getString(Utils.BE_PHONE));
                    customersValues.put(CustContract.CustDetailsEntry.COLUMN_CUST_NUMBER,
                            customerObject.getString(Utils.BE_NUMBER_CUSTOMER));
                    customersValues.put(CustContract.CustDetailsEntry.COLUMN_DATE, dateTime);
                    Log.d("data phone", customerObject.getString(Utils.BE_PHONE));


                    cVVector.add(customersValues);
                }
                Log.d("data", String.valueOf(cVVector));
                // add to database

                if (cVVector.size() > 0) {
                    //Bulk Data Deleted
                    /*
                    context.getContentResolver().delete(CustContract.CustDetailsEntry.CONTENT_URI,
                            null,
                            null);
                            */


                    //bulk Insert
                    ContentValues[] cvArray = new ContentValues[cVVector.size()];
                    cVVector.toArray(cvArray);
                    context.getContentResolver().bulkInsert(CustContract.CustDetailsEntry.CONTENT_URI, cvArray);

                    //Bulk Data Deleted
                    // delete old data so we don't build up an endless history
                    context.getContentResolver().delete(CustContract.CustDetailsEntry.CONTENT_URI,
                            CustContract.CustDetailsEntry.COLUMN_DATE + " <= ?",
                            new String[]{Long.toString(dayTime.setJulianDay(julianStartDay - 1))});

                }
                //sync Complete

            } catch (JSONException e) {
                //error message
                e.printStackTrace();
            }

        } catch (IOException e) {
            // io error
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    //error Closing Stream
                }
            }
        }

    }
}
