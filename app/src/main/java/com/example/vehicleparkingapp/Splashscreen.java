package com.example.vehicleparkingapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import java.io.IOException;

public class Splashscreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.SplashTheme);
        super.onCreate(savedInstanceState);

        Intent intent;
        if(isOnline())
        {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        else
        {
            final AlertDialog.Builder alertDialog = new AlertDialog.Builder(Splashscreen.this);

            alertDialog.setTitle("No Internet");
            alertDialog.setMessage("Please make sure that you are connected to the internet.");

            alertDialog.setPositiveButton("RETRY", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int which) {
                    finish();
                    startActivity(getIntent());
                }
            });

            alertDialog.setNegativeButton("EXIT", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });

            alertDialog.show();
        }
    }


    public boolean isOnline() {
        Runtime runtime = Runtime.getRuntime();
        try {
            Process ipProcess = runtime.exec("/system/bin/ping -c 1 8.8.8.8");
            int     exitValue = ipProcess.waitFor();
            return (exitValue == 0);
        }
        catch (IOException e)          { e.printStackTrace(); }
        catch (InterruptedException e) { e.printStackTrace(); }

        return false;
    }
}

