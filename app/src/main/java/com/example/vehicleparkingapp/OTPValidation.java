package com.example.vehicleparkingapp;

import android.Manifest;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class OTPValidation extends AppCompatActivity {

    Toolbar toolbar;
    Boolean otp_valid=false;
    static EditText otp_text;
    static int otp_from_database,otp_from_sms;
    RequestQueue requestQueue;
    JSONObject user;
    int MY_PERMISSIONS_REQUEST_RECEIVE_SMS=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.otpvalidation);

        //setting toolbar
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("OTP Validation");
        //toolbar.getNavigationIcon().setColorFilter(getResources().getColor(R.color.white));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        toolbar.setTitleTextColor(getResources().getColor(R.color.white));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!otp_valid)
                    deleteUser();
                //finish();
            }
        });

        //getting otp from intent
        Intent intent = getIntent();
        try {
            user = new JSONObject(intent.getStringExtra("user"));
            otp_from_database = user.getInt("OTP");
            //Toast.makeText(OTPValidation.this, String.valueOf(otp_from_database), Toast.LENGTH_LONG).show();
        }catch (JSONException j)
        {
            //
        }

        ActivityCompat.requestPermissions(OTPValidation.this,
                new String[]{Manifest.permission.RECEIVE_SMS},
                MY_PERMISSIONS_REQUEST_RECEIVE_SMS);

        requestQueue = Volley.newRequestQueue(this);


        otp_text = findViewById(R.id.otp);
        if(otp_text.getText().equals(""))
            otp_text.setText(String.valueOf(otp_from_sms));
    }


    @Override
    public void onBackPressed() {
        if(!otp_valid)
            deleteUser();
        //finish();
    }


    private void deleteUser()
    {
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        Map<String, String> params = new HashMap<>();
        try{
            params.put(ConfigConstants.KEY_USERNAME, user.getString("username"));
            params.put("operation","delete_user");
        }catch (JSONException j){
            //
        }


        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.LOGIN_REGISTER_URL, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(Register.this, response.toString(), Toast.LENGTH_SHORT).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                AlertDialog alertDialog = new AlertDialog.Builder(
                                        OTPValidation.this).create();
                                alertDialog.setTitle("Error");
                                alertDialog.setMessage("Invalid OTP. Please register again.");
                                alertDialog.setButton(Dialog.BUTTON_NEUTRAL,"OK",new DialogInterface.OnClickListener(){

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        finish();
                                    }
                                });
                                alertDialog.show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getApplicationContext(),
                                    "Error: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                //You can handle error here if you want
                            }
                        });

        //Adding the string request to the queue
        requestQueue.add(jsonObjectRequest);
    }


    public void checkOTP(View view)
    {
        String entered_otp = otp_text.getText().toString();
        if(otp_from_database == otp_from_sms || entered_otp.equals(String.valueOf(otp_from_database)))
            otp_valid = true;

        if(!otp_valid) {
            deleteUser();
            otp_text.requestFocus();
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(OTPValidation.this);
            alertDialog.setTitle("Invalid OTP");
            alertDialog.setMessage("Your entered OTP is invalid.\nPlease register again.");
            alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    // Write your code here to invoke YES event
                    finish();
                }
            });

            alertDialog.show();
        }
        else
        {
            final TextView otp_txt = findViewById(R.id.otp_txt);
            otp_txt.setText("OTP was successfully verified.");
            Button button = findViewById(R.id.button);
            button.setVisibility(View.GONE);
            EditText e = findViewById(R.id.otp);
            e.setVisibility(View.GONE);
            final ImageView img = findViewById(R.id.verified_img);
            img.setVisibility(View.VISIBLE);
            final TextView txt = findViewById(R.id.successful_booking);
            txt.setVisibility(View.VISIBLE);

            SessionManager s = new SessionManager(getApplicationContext());
            s.createLoginSession(user);
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }
    }
}
