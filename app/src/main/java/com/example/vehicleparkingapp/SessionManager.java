package com.example.vehicleparkingapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class SessionManager
{
    // Shared Preferences
    SharedPreferences pref;

    // Editor for Shared preferences
    SharedPreferences.Editor editor;

    // Context
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Constructor
    public SessionManager(Context context){
        this._context = context;
        pref = _context.getSharedPreferences(ConfigConstants.SHARED_PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    /**
     * Create login session
     * */
    public void createLoginSession(JSONObject user)
    {
        // Storing login value as TRUE
        editor.putBoolean(ConfigConstants.IS_LOGIN, true);
        try
        {
            editor.putString(ConfigConstants.KEY_USERNAME, user.getString("username"));
            editor.putString(ConfigConstants.KEY_FNAME, user.getString("first_name"));
            editor.putString(ConfigConstants.KEY_LNAME, user.getString("last_name"));
            editor.putString(ConfigConstants.KEY_PHONE, user.getString("contact"));
            editor.putString(ConfigConstants.KEY_LICENCE, user.getString("licence_number"));
            editor.putString(ConfigConstants.KEY_ADDRESS, user.getString("address"));
        }catch (JSONException js)
        {
            Log.e("MYAPP", "unexpected JSON exception", js);
        }

        // commit changes
        editor.commit();
    }

    public void updateLoginSession(String first_name, String last_name, String address, String licence)
    {
        // Storing login value as TRUE
        editor.putBoolean(ConfigConstants.IS_LOGIN, true);
        editor.putString(ConfigConstants.KEY_FNAME, first_name);
        editor.putString(ConfigConstants.KEY_LNAME, last_name);
        editor.putString(ConfigConstants.KEY_LICENCE, licence);
        editor.putString(ConfigConstants.KEY_ADDRESS, address);

        // commit changes
        editor.commit();
    }


    public void checkLogin()
    {
        // Check login status
        if(!this.isLoggedIn()){
            // user is not logged in redirect him to Login Activity
            Intent i = new Intent(_context, Login.class);
            // Closing all the Activities
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            // Add new Flag to start new Activity
            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Staring Login Activity
            _context.startActivity(i);
        }

    }



    /**
     * Clear session details
     * */
    public void logoutUser(){
        // Clearing all data from Shared Preferences
        editor.clear();
        editor.commit();
    }

    /**
     * Quick check for login
     * **/
    // Get Login State
    public boolean isLoggedIn(){
        return pref.getBoolean(ConfigConstants.IS_LOGIN, false);
    }

    public boolean isGuestIn(){
        return pref.getBoolean(ConfigConstants.IS_GUEST, false);
    }

}

