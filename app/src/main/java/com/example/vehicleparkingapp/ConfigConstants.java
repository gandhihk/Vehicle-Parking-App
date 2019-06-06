package com.example.vehicleparkingapp;

public class ConfigConstants
{
    //URL to our php file
    public static final String ROOT_URL = "http://192.168.2.106/vehicleparkingapp/";

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
    public static final String KEY_LICENCE = "licence";
    public static final String KEY_ADDRESS = "address";

    //URLs
    public static final String LOGIN_REGISTER_URL = ROOT_URL + "login_register.php";
    public static final String GET_MAP_DETAILS = ROOT_URL + "get_map_details.php";
    public static final String GET_VEHICLES = ROOT_URL + "get_vehicles.php";
    public static final String SET_PROFILE_DETAILS = ROOT_URL + "set_profile_details.php";
    public static final String CREATE_BOOKING = ROOT_URL + "create_booking.php";
}
