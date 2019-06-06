package com.example.vehicleparkingapp;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NextStep extends AppCompatActivity implements AddVehicle.OnFragmentInteractionListener{

    TextView level_txt,slot_txt,date_txt,time_txt;
    private int level_number,slot_number,hr,min;
    private String date,time,vehicle,username;
    EditText hours_txt,minutes_txt;

    int diff;
    ArrayList<String> vehicleList;

    RequestQueue requestQueue;

    static final int TIME_DIALOG_ID = 1111,THRESHOLD=20;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.next_step);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Booking");

        requestQueue = Volley.newRequestQueue(this);

        initializeViews();

        SessionManager s = new SessionManager(this);
        SharedPreferences sp = s.pref;
        username = sp.getString(ConfigConstants.KEY_USERNAME,"");

        Intent i = getIntent();
        level_number = i.getIntExtra("level_number",0);
        slot_number = i.getIntExtra("slot_number",0);
        vehicleList = (ArrayList<String>) i.getSerializableExtra("vehicleList");

        level_txt.setText("Level Number :   "+level_number);
        slot_txt.setText("Slot Number :   "+slot_number);

        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df1 = new SimpleDateFormat("dd-MM-yyyy");
        date = df1.format(c);
        SimpleDateFormat df2 = new SimpleDateFormat("hh-mm-ss");
        time = df2.format(c);
        date_txt.setText(date);

        hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        min = Calendar.getInstance().get(Calendar.MINUTE);
        updateTime(hr, min);

        hours_txt.setText("00");
        minutes_txt.setText("15");

        Spinner spinner = findViewById(R.id.vehicles);
        createSpinner(spinner);
    }

    void initializeViews()
    {
        level_txt = findViewById(R.id.level_txt);
        slot_txt = findViewById(R.id.slot_txt);
        date_txt = findViewById(R.id.date);
        time_txt = findViewById(R.id.time);
        hours_txt = findViewById(R.id.hours);
        minutes_txt = findViewById(R.id.minute);
    }

    protected void createSpinner(final Spinner spinner)
    {
        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                this,R.layout.support_simple_spinner_dropdown_item,vehicleList){
            @Override
            public boolean isEnabled(int position){
                return true;
            }
            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                tv.setTextColor(Color.BLACK);
                return view;
            }
        };
        spinnerArrayAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerArrayAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                vehicle = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected Dialog createdDialog(int id) {
        switch (id) {
            case TIME_DIALOG_ID:
                return new TimePickerDialog(this, timePickerListener, hr, min, false);
        }
        return null;
    }

    private TimePickerDialog.OnTimeSetListener timePickerListener = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minutes) {
            check(hourOfDay,minutes);
        }
    };

    boolean check(int new_hr, int new_min)
    {
        hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        min = Calendar.getInstance().get(Calendar.MINUTE);
        diff = Math.abs(new_min-min);
        if((diff<=THRESHOLD && new_min>min && new_hr==hr) || (diff>THRESHOLD && new_hr>hr))
        {
            min = new_min;
            hr = new_hr;
            updateTime(new_hr, new_min);
            return true;
        }
        else{
            Toast.makeText(NextStep.this, "Booking is allowed only "+THRESHOLD+" minutes prior.", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private static String utilTime(int value)
    {
        if (value < 10) return "0" + String.valueOf(value); else return String.valueOf(value);
    }

    private void updateTime(int hours, int mins)
    { String timeSet = "";
        String minutes = "";
        if (mins < 10)
            minutes = "0" + mins;
        else
            minutes = String.valueOf(mins);
        String aTime = new StringBuilder().append(hours).append(':').append(minutes).append(" ").append(timeSet).toString();
        time_txt.setText(aTime);
    }

    public void openDialog(View view)
    {
        createdDialog(1111).show();
    }

    public void proceed(View view) {
        int hour = Integer.valueOf(time_txt.getText().toString().substring(0,2));
        int minute = Integer.valueOf(time_txt.getText().toString().substring(3,5));
        hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        min = Calendar.getInstance().get(Calendar.MINUTE);
        if(hour>=hr && minute>=min)
        {
            Intent i = new Intent(this, SuccessfulBooking.class);
            i.putExtra("time_left",diff);
            i.putExtra("level_number",level_number);
            i.putExtra("slot_number",slot_number);
            i.putExtra("entry_time",time_txt.getText().toString());
            i.putExtra("duration_hours",hours_txt.getText().toString());
            i.putExtra("duration_mins",minutes_txt.getText().toString());
            i.putExtra("vehicle",vehicle);
            i.putExtra("date",date);
            startActivity(i);
            finish();
        }
        else
            Toast.makeText(NextStep.this, "Enter valid entry time.", Toast.LENGTH_LONG).show();

    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //
    }
}
