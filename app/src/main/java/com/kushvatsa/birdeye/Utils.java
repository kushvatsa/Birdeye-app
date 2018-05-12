package com.kushvatsa.birdeye;

import java.util.regex.Pattern;

public class Utils {

    //Utilities

    public final static String BE_NAME = "name";
    public final static String BE_EMAIL = "emailId";
    public final static String BE_PHONE = "phone";
    public final static String BE_API_KEY = "api_key";
    public final static String BE_B_KEY = "businessId";
    public final static String BE_POST_URL = "https://api.birdeye.com/resources/v1/customer/checkin?bid=151722397793976&api_key=vrbXq3jAflrtwDaUMcTmjf9TS135QAyd";
    public final static String BE_URL = "https://api.birdeye.com/resources/v1/customer/all?";

    public final static String BE_FIRST_NAME = "firstName";
    public final static String BE_LAST_NAME = "lastName";
    public final static String BE_NUMBER_CUSTOMER = "number";

    public static boolean isValidEmaillId(String email){

        return Pattern.compile("^(([\\w-]+\\.)+[\\w-]+|([a-zA-Z]{1}|[\\w-]{2,}))@"
                + "((([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\."
                + "([0-1]?[0-9]{1,2}|25[0-5]|2[0-4][0-9])\\.([0-1]?"
                + "[0-9]{1,2}|25[0-5]|2[0-4][0-9])){1}|"
                + "([a-zA-Z]+[\\w-]+\\.)+[a-zA-Z]{2,4})$").matcher(email).matches();
    }


    public static String returnName(String email)
    {
        String[] parts = email.split("@");
        return  parts[0];
    }




}
