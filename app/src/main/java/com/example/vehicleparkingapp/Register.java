package com.example.vehicleparkingapp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class Register extends AppCompatActivity {

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
    }

    public void createAccount(View view)
    {

    }

    public void addVehicle(View view)
    {

    }
}
