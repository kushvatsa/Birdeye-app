package com.kushvatsa.birdeye.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;


public class CustContract {
    //content authority for SQLite
    public static final String CONTENT_AUTHORITY = "com.kushvatsa.birdeye";

     // base content uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    //path customer details
    public static final String PATH_CUST_DETAILS = "cust_details";

    //customer details entry
    public static final class CustDetailsEntry implements BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CUST_DETAILS).build();

       // content type
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CUST_DETAILS;

        // content item type
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CUST_DETAILS;

        // table details
        public static final String TABLE_NAME = "c_details";
        public static final String COLUMN_EMAIL = "email";
        public static final String COLUMN_F_NAME = "f_name";
        public static final String COLUMN_L_NAME = "l_name";
        public static final String COLUMN_PHONE= "phone";
        public static final String COLUMN_CUST_NUMBER = "cust_number";
        public static final String COLUMN_DATE = "date";


        public static Uri buildCustDetailsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }


}
