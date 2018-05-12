package com.kushvatsa.birdeye;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialog;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.kushvatsa.birdeye.data.CustContract;
import com.kushvatsa.birdeye.sync.BirdEyeIntentService;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

import javax.net.ssl.HttpsURLConnection;

public class MainActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor>,CustAdapter.OnSelectedListener{

    private final String TAG = MainActivity.class.getSimpleName();

    BottomSheetDialog mBottomSheetDialog;
    LinearLayout f_delete;

    // Database Projections
    public static final String[] MAIN_CUSTOMER_PROJECTION = {
            CustContract.CustDetailsEntry._ID,
            CustContract.CustDetailsEntry.COLUMN_EMAIL,
            CustContract.CustDetailsEntry.COLUMN_F_NAME,
            CustContract.CustDetailsEntry.COLUMN_L_NAME,
            CustContract.CustDetailsEntry.COLUMN_PHONE,
            CustContract.CustDetailsEntry.COLUMN_CUST_NUMBER,
            CustContract.CustDetailsEntry.COLUMN_DATE,
    };

    /*
     * We store the indices of the values in the array of Strings above to more quickly be able to
     * access the data from our query. If the order of the Strings above changes, these indices
     * must be adjusted to match the order of the Strings.
     */
    public static final int INDEX_ID = 0;
    public static final int INDEX_EMAIL = 1;
    public static final int INDEX_F_NAME = 2;
    public static final int INDEX_L_NAME = 3;
    public static final int INDEX_PHONE = 4;
    public static final int INDEX_NUMBER = 5;
    public static final int INDEX_DATE = 6;

    private static final int ID_CUSTOMER_LOADER_ASC = 77;
    private static final int ID_CUSTOMER_LOADER_DSC = 11;

    private CustAdapter mCustAdapter;
    private RecyclerView mRecyclerView;
    private int mPosition = RecyclerView.NO_POSITION;

    private ProgressBar mBar;
    private TextView empty_view;
    private SearchView searchView;
    String cursorFilter;
    Boolean checkValue;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        checkValue = sharedPref.getBoolean("main_action", false);
        if(checkValue)
        {
            getSupportLoaderManager().initLoader(ID_CUSTOMER_LOADER_DSC, null, this);
        }
        else
        {
            getSupportLoaderManager().initLoader(ID_CUSTOMER_LOADER_ASC, null, this);
        }

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, CheckInActivity.class));
            }
        });

        mBottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = this.getLayoutInflater().inflate(R.layout.b_sheet, null);
        f_delete = sheetView.findViewById(R.id.bottom_sheet_delete);
        mBottomSheetDialog.setContentView(sheetView);
        mBar = findViewById(R.id.screen_wait);
        mRecyclerView= findViewById(R.id.customers_recycler_view);
        empty_view = findViewById(R.id.empty_text_view);

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        /* setLayoutManager associates the LayoutManager we created above with our RecyclerView */
        mRecyclerView.setLayoutManager(layoutManager);

        /*
         * Use this setting to improve performance if you know that changes in content do not
         * change the child layout size in the RecyclerView
         */
        mRecyclerView.setHasFixedSize(true);


        mCustAdapter = new CustAdapter(this, this);

        mRecyclerView.setAdapter(mCustAdapter);

        showLoading();

        //syncing with service
        Intent intentToSyncImmediately = new Intent(this, BirdEyeIntentService.class);
        this.startService(intentToSyncImmediately);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Associate searchable configuration with the SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.action_search)
                .getActionView();
        searchView.setSearchableInfo(searchManager
                .getSearchableInfo(getComponentName()));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        // listening to search query text change
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // filter recycler view when query submitted
                cursorFilter = !TextUtils.isEmpty(query) ? query : null;
                if(checkValue)
                {
                    getSupportLoaderManager().restartLoader(ID_CUSTOMER_LOADER_DSC, null, MainActivity.this);
                }
                else
                {
                    getSupportLoaderManager().restartLoader(ID_CUSTOMER_LOADER_ASC, null, MainActivity.this);
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String query) {
                // filter recycler view when text is changed
                cursorFilter = !TextUtils.isEmpty(query) ? query : null;
                if(checkValue)
                {
                    getSupportLoaderManager().restartLoader(ID_CUSTOMER_LOADER_DSC, null, MainActivity.this);
                }
                else
                {
                    getSupportLoaderManager().restartLoader(ID_CUSTOMER_LOADER_ASC, null, MainActivity.this);
                }

                return false;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.sort_a_z:
                SharedPreferences sharedPref1 = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor1 = sharedPref1.edit();
                editor1.putBoolean("main_action", false);
                editor1.apply();
                Intent showIntent1 = new Intent(MainActivity.this, MainActivity.class);
                startActivity(showIntent1);
                finish();
                break;
            case R.id.sort_z_a:
                SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("main_action", true);
                editor.apply();
                Intent showIntent2 = new Intent(MainActivity.this, MainActivity.class);
                startActivity(showIntent2);
                finish();
                break;
            case R.id.refresh_data:
                Intent intentToSyncImmediately = new Intent(this, BirdEyeIntentService.class);
                this.startService(intentToSyncImmediately);
                mCustAdapter.swapCursor(null);
                break;
            default:
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {
        switch (id) {

            case ID_CUSTOMER_LOADER_ASC:
                /* URI for all rows of customer data in our database table in asc */
                String selection;
                String[] selectionArgs;
                if(cursorFilter!=null)
                {
                    selection = CustContract.CustDetailsEntry.COLUMN_F_NAME +
                            " LIKE'%" + cursorFilter + "%'" + " " + "OR" + " "
                            +  CustContract.CustDetailsEntry.COLUMN_L_NAME +" LIKE'%" + cursorFilter + "%'" ;
                }
                else
                {
                    selection = null;
                    selectionArgs = null;
                }
                Uri QueryUri = CustContract.CustDetailsEntry.CONTENT_URI;

                String sortOrder = CustContract.CustDetailsEntry.COLUMN_F_NAME + " ASC"+ "," + " " +
                        CustContract.CustDetailsEntry.COLUMN_L_NAME + " ASC";

                return new CursorLoader(this,
                        QueryUri,
                        MAIN_CUSTOMER_PROJECTION,
                        selection,
                        null,
                        sortOrder);
            case ID_CUSTOMER_LOADER_DSC:

                String selection1;
                String[] selectionArgs1;
                if(cursorFilter!=null)
                {
                    selection1 = CustContract.CustDetailsEntry.COLUMN_F_NAME +
                            " LIKE'%" + cursorFilter + "%'" + " " + "OR" + " "
                            +  CustContract.CustDetailsEntry.COLUMN_L_NAME +" LIKE'%" + cursorFilter + "%'" ;
                }
                else
                {
                    selection1 = null;
                }
                /* URI for all rows of customer data in our database table in desc */
                Uri QueryUri2 = CustContract.CustDetailsEntry.CONTENT_URI;

                String sortOrder2 = CustContract.CustDetailsEntry.COLUMN_F_NAME + " DESC" + "," + " " +
                        CustContract.CustDetailsEntry.COLUMN_L_NAME + " DESC";

                return new CursorLoader(this,
                        QueryUri2,
                        MAIN_CUSTOMER_PROJECTION,
                        selection1,
                        null,
                        sortOrder2);

            default:
                throw new RuntimeException("Loader Not Implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor data) {
        mCustAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showDataView();
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        mCustAdapter.swapCursor(null);
    }

    @Override
    public void onSheetClicked(final View view, final String number) {
        mBottomSheetDialog.show();
        f_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DeleteBackground().execute(number);
                mBottomSheetDialog.hide();
                view.getContext().getContentResolver().delete(CustContract.CustDetailsEntry.CONTENT_URI,
                        CustContract.CustDetailsEntry.COLUMN_CUST_NUMBER + "=" + number,
                        null);
                mCustAdapter.swapCursor(null);
            }
        });

    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        /* Finally, show the Progressbar */
        mBar.setVisibility(View.VISIBLE);
    }

    private void showDataView() {
        /* First, hide the Progressbar */
        mBar.setVisibility(View.INVISIBLE);
        /* Finally, make sure the data is visible */
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    // deleting background thread
    public class DeleteBackground  extends
            AsyncTask<String, Void, Boolean> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showLoading();

        }
        @Override
        protected Boolean doInBackground(String... params) {
            URL url;
            String Url_delete = "https://api.birdeye.com/resources/v1/customer/id/" + params[0] +"?" + Utils.BE_API_KEY +"="+ BuildConfig.apikey;
            try {

                url = new URL(Url_delete);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("DELETE");
                conn.setDoOutput(true);
                conn.setRequestProperty("content-type", "application/json");
                conn.setRequestProperty("accept" , "application/json");
                conn.connect();

                int responseCode = conn.getResponseCode();

                Log.e(TAG, "responseCode : " + responseCode);

                if (responseCode == HttpsURLConnection.HTTP_OK) {
                    Log.d(TAG, "HTTP_OK");

                } else {
                    Log.d(TAG, "False - HTTP_OK");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            showDataView();

        }
    }
    @Override
    public void onBackPressed() {

        Intent startMain = new Intent(Intent.ACTION_MAIN);
        startMain.addCategory(Intent.CATEGORY_HOME);
        startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(startMain);
    }
}
