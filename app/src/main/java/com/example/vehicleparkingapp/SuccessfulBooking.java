package com.example.vehicleparkingapp;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.view.animation.Animation.RELATIVE_TO_SELF;

public class SuccessfulBooking extends AppCompatActivity
{

    ProgressBar progressBarView;
    TextView tv_time,time_left,booking_id_txt,entry_time_txt,duration_txt,fare_txt;
    int endTime = 250,mins;
    int myProgress = 0;
    int progress,slot_number,level_number,fare,booking_id;
    CountDownTimer countDownTimer;
    String username,date,vehicle,duration_hrs,duration_mins,entry_time;

    static BoundService mBoundService;
    static boolean mServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.successful_booking);

        Intent i = getIntent();
        endTime = i.getIntExtra("time_left",0);
        endTime = endTime * 60;
        slot_number = i.getIntExtra("slot_number",0);
        level_number = i.getIntExtra("level_number",0);
        date = i.getStringExtra("date");
        vehicle = i.getStringExtra("vehicle");
        duration_hrs = i.getStringExtra("duration_hours");
        duration_mins = i.getStringExtra("duration_mins");
        entry_time = i.getStringExtra("entry_time");

        RequestQueue requestQueue = Volley.newRequestQueue(this);

        SessionManager s = new SessionManager(this);
        SharedPreferences sp = s.pref;
        username = sp.getString(ConfigConstants.KEY_USERNAME,"");

        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, username);
        params.put("level",String.valueOf(level_number));
        params.put("slot",String.valueOf(slot_number));
        params.put("duration_hrs",duration_hrs);
        params.put("duration_mins",duration_mins);
        params.put("vehicle",vehicle);
        params.put("entry_time",entry_time);
        params.put("date",date);

        params.put("operation", "create_booking");

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.CREATE_BOOKING, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                fare = response.getInt("fare");
                                booking_id = response.getInt("booking_id");
                                //Toast.makeText(SuccessfulBooking.this, endTime+" "+duration_hrs+" "+duration_mins+" "+entry_time, Toast.LENGTH_LONG).show();
                                progress();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(SuccessfulBooking.this,
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

    void progress()
    {
        progressBarView = (ProgressBar) findViewById(R.id.view_progress_bar);
        tv_time= (TextView)findViewById(R.id.tv_timer);
        time_left = findViewById(R.id.time_left);
        booking_id_txt = findViewById(R.id.booking_id);
        entry_time_txt = findViewById(R.id.entry_time);
        fare_txt = findViewById(R.id.fare);
        duration_txt = findViewById(R.id.duration);

        if(endTime==0)
            time_left.setText("Time left for booking to start : 0 s");

        booking_id_txt.setText(String.valueOf(booking_id));
        entry_time_txt.setText(entry_time);
        mins = Integer.valueOf(duration_hrs)*60 + Integer.valueOf(duration_mins);
        duration_txt.setText(mins+" minutes");
        fare_txt.setText(String.valueOf(fare)+" Rs");

        Intent intent = new Intent(getApplicationContext(), BoundService.class);
        intent.putExtra("time_left",(mins*60*1000));
        intent.putExtra("duration",(endTime*1000));
        startService(intent);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);

        /*Animation*/
        RotateAnimation makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(0);
        fn_countdown();
    }

    private void fn_countdown()
    {
            progress = 1;

            countDownTimer = new CountDownTimer(endTime * 1000, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    setProgress(progress, endTime);
                    progress = progress + 1;
                    int seconds = (int) (millisUntilFinished / 1000) % 60;
                    int minutes = (int) ((millisUntilFinished / (1000 * 60)) % 60);
                    int hours = (int) ((millisUntilFinished / (1000 * 60 * 60)) % 24);
                    String newtime = hours + ":" + minutes + ":" + seconds;

                    if (newtime.equals("0:0:0")) {
                        tv_time.setText("00:00:00");
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        tv_time.setText("0" + hours + ":0" + minutes + ":0" + seconds);
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(minutes).length() == 1)) {
                        tv_time.setText("0" + hours + ":0" + minutes + ":" + seconds);
                    } else if ((String.valueOf(hours).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        tv_time.setText("0" + hours + ":" + minutes + ":0" + seconds);
                    } else if ((String.valueOf(minutes).length() == 1) && (String.valueOf(seconds).length() == 1)) {
                        tv_time.setText(hours + ":0" + minutes + ":0" + seconds);
                    } else if (String.valueOf(hours).length() == 1) {
                        tv_time.setText("0" + hours + ":" + minutes + ":" + seconds);
                    } else if (String.valueOf(minutes).length() == 1) {
                        tv_time.setText(hours + ":0" + minutes + ":" + seconds);
                    } else if (String.valueOf(seconds).length() == 1) {
                        tv_time.setText(hours + ":" + minutes + ":0" + seconds);
                    } else {
                        tv_time.setText(hours + ":" + minutes + ":" + seconds);
                    }
                    tv_time.setText(tv_time.getText().toString()+" left");
                }

                @Override
                public void onFinish() {
                    setProgress(progress, endTime);
                }
            };
            countDownTimer.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

    }


    @Override
    protected void onStop() {
        super.onStop();
        if (mServiceBound) {
            // If a timer is active, foreground the service, otherwise kill the service
            if (mBoundService.isTimerRunning()) {
                mBoundService.foreground();
            }
            else {
                stopService(new Intent(this, BoundService.class));
            }
            // Unbind the service
            unbindService(mServiceConnection);
            mServiceBound = false;
        }
    }

    static ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mServiceBound = false;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            BoundService.MyBinder myBinder = (BoundService.MyBinder) service;
            mBoundService = myBinder.getService();

            mServiceBound = true;
            mBoundService.background();
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        //getActivity().registerReceiver(br, new IntentFilter(Timer_Service.COUNTDOWN_BR));
        //Log.i(TAG, "Registered broacast receiver");
    }

    @Override
    public void onPause() {
        super.onPause();
        //getActivity().unregisterReceiver(br);
        //Log.i(TAG, "Unregistered broacast receiver");
    }


    @Override
    public void onDestroy() {
        //getActivity().stopService(new Intent(getActivity(), Timer_Service.class));
        //Log.i(TAG, "Stopped service");
        super.onDestroy();
    }

    public void setProgress(int startTime, int endTime) {
        progressBarView.setMax(endTime);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(startTime);

    }

    @Override
    public void onBackPressed() {
        Toast.makeText(this, "Cannot go back !", Toast.LENGTH_LONG).show();
    }

    public void gohome(View view) {
        finish();
        /*Fragment f = new Home();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.content_frame, f);
        ft.commit();*/
        MainActivity.h.sendEmptyMessage(0);
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}
