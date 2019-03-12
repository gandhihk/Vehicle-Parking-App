package com.example.vehicleparkingapp;

public class ConfigConstants
{
    //URL to our php file
    public static final String ROOT_URL = "http://192.168.2.104/vehicleparkingapp/";

    //Keys for email and password as defined in our $_POST['key'] in login.php
    public static final String KEY_USERNAME = "username";
    public static final String KEY_PASSWORD = "password";

    //Keys for Sharedpreferences
    //This would be the name of our shared preferences
    public static final String SHARED_PREF_NAME = "User";
    // All Shared Preferences Keys
    public static final String IS_LOGIN = "isLoggedIn";
    public static final String IS_VERIFIED = "isVerified";
    public static final String IS_GUEST = "isGuest";
    public static final String KEY_TITLE = "title";
    public static final String KEY_FNAME = "first_name";
    public static final String KEY_LNAME = "last_name";
    public static final String KEY_PHONE = "phone";
    public static final String KEY_ID = "id";
    public static final String KEY_PROOFTYPE = "proof_type";
    public static final String KEY_ADDRESS = "address";

    //URLs
    public static final String LOGIN_REGISTER_URL = ROOT_URL + "login_register.php";
}
