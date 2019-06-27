package com.example.vehicleparkingapp;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ParkingHistory extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    View b,rootview;
    TextView booking_id_txt,entry_time_txt,fare_txt,date_txt,vehicle_txt;
    LinearLayout scrollView;

    private String mParam1;
    private String mParam2;

    String username;

    private RequestQueue requestQueue;
    ProgressDialog pDialog;
    private OnFragmentInteractionListener mListener;

    public ParkingHistory() {
        // Required empty public constructor
    }


    public static ParkingHistory newInstance(String param1, String param2) {
        ParkingHistory fragment = new ParkingHistory();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        MainActivity.toolbar.setTitle("Parking History");
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.parking_history, container, false);

        scrollView = rootview.findViewById(R.id.scrollView);

        SessionManager s = new SessionManager(getContext());
        SharedPreferences sp = s.pref;
        username = sp.getString(ConfigConstants.KEY_USERNAME,"");
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading...");
        pDialog.show();
        getHistory(inflater);

        return rootview;
    }

    void getHistory(final LayoutInflater inflater)
    {
        requestQueue = Volley.newRequestQueue(getActivity());
        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, username);
        params.put("operation", "get_history");

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.GET_MAP_DETAILS, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                JSONArray history = response.getJSONArray("history");
                                for(int i=0;i<history.length();i++)
                                {
                                    JSONObject j = history.getJSONObject(i);
                                    int booking_id = j.getInt("booking_id");
                                    String date = j.getString("date");
                                    String entry_time = j.getString("entry_time");
                                    double fare = j.getDouble("total_fare");
                                    String vehicle_category = j.getString("vehicle_category");
                                    createBookingFieldForHistory(booking_id,date,vehicle_category,fare,entry_time,inflater);
                                }
                                if(history.length()==0)
                                {
                                    scrollView.findViewById(R.id.no_history).setVisibility(View.VISIBLE);
                                }
                                pDialog.hide();
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(),
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

    void createBookingFieldForHistory(int booking_id, String date, String vehicle_category, double fare, String entry_time, LayoutInflater inflater)
    {
        b = inflater.inflate(R.layout.booking, null);
        booking_id_txt = b.findViewById(R.id.booking_id);
        entry_time_txt = b.findViewById(R.id.entry_time);
        fare_txt = b.findViewById(R.id.fare);
        date_txt = b.findViewById(R.id.date);
        vehicle_txt = b.findViewById(R.id.vehicle);

        entry_time = entry_time.substring(entry_time.length()-8);

        booking_id_txt.setText("Booking ID :   "+booking_id);
        entry_time_txt.setText("Entry Time :   "+entry_time);
        fare_txt.setText("Fare :   "+fare);
        date_txt.setText("Date :   "+date);
        vehicle_txt.setText("Vehicle :   "+vehicle_category+"\nCategory ");
        scrollView.addView(b);
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }
}
