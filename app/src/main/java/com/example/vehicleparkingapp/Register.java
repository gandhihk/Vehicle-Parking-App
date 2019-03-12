package com.example.vehicleparkingapp;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Register extends AppCompatActivity
{
    // UI references.
    private AutoCompleteTextView usernameView,fnameTextView,lnameTextView,phoneTextView,mPasswordView,mconfirmPassView;
    private AutoCompleteTextView licenceView, addressView;
    private View mProgressView;
    private View mRegisterFormView;
    private String username,password,fname,lname,phone,licence,address,conf_password;
    AlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try
        {
            this.getSupportActionBar().hide();
        }
        catch (NullPointerException e){
            //
        }
        setContentView(R.layout.register);

        initializeViews();
    }

    private void initializeViews()
    {
        usernameView = findViewById(R.id.username);
        mPasswordView = findViewById(R.id.password);
        mconfirmPassView = findViewById(R.id.confirm_password);
        fnameTextView = findViewById(R.id.first_name);
        lnameTextView = findViewById(R.id.last_name);
        phoneTextView = findViewById(R.id.phone);
        licenceView = findViewById(R.id.licence);
        addressView = findViewById(R.id.address);
        mProgressView = findViewById(R.id.register_progress);
        alertDialog = new AlertDialog.Builder(
                Register.this).create();
    }

    public void createAccount(View view)
    {
        username = usernameView.getText().toString();
        fname = fnameTextView.getText().toString();
        lname = lnameTextView.getText().toString();
        password = mPasswordView.getText().toString();
        conf_password = mconfirmPassView.getText().toString();
        phone = phoneTextView.getText().toString();
        licence = licenceView.getText().toString();
        address = addressView.getText().toString();
        if(!validate())
        {
            //
        }
    }

    private boolean validate()
    {
        boolean cancel=false;
        View focusView = null;
        Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(username);

        if(fname.equals("")){
            fnameTextView.setError(getString(R.string.error_field_required));
            focusView = fnameTextView;
            cancel = true;
        }
        else if(lname.equals("")){
            lnameTextView.setError(getString(R.string.error_field_required));
            focusView = lnameTextView;
            cancel = true;
        }
        else if(username.equals("")){
            usernameView.setError(getString(R.string.error_field_required));
            focusView = usernameView;
            cancel = true;
        }
        else if(m.find()){
            usernameView.setError(getString(R.string.error_invalid_username));
            focusView = usernameView;
            cancel = true;
        }
        else if(phone.length()!=10){
            phoneTextView.setError(getString(R.string.error_phone_too_short));
            focusView = phoneTextView;
            cancel = true;
        }
        else if(password.equals("")){
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
        }
        else if(password.length()<6){
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }
        else if(!conf_password.equals(password)){
            mconfirmPassView.setError(getString(R.string.error_conf_password));
            focusView = mconfirmPassView;
        }
        else if(licence.length()!=12){
            licenceView.setError(getString(R.string.error_invalid_licence));
            focusView = licenceView;
            cancel = true;
        }


        if (cancel)
            focusView.requestFocus();
        return cancel;
    }

    public void addVehicle(View view)
    {

    }
}
