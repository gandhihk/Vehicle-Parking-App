package com.example.vehicleparkingapp;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddVehicle extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    static private String vehicle_category,vehicle_model,vehicle_company,plate_number,username;

    private static LinearLayout vehicles_layout;
    private static View rootview;

    static RequestQueue requestQueue;
    static JSONArray vehicles;
    ProgressDialog pDialog;
    static Activity activity;

    public AddVehicle() {
        // Required empty public constructor
    }


    public static AddVehicle newInstance(String param1, String param2) {
        AddVehicle fragment = new AddVehicle();
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
        // Inflate the layout for this fragment
        rootview = inflater.inflate(R.layout.add_vehicle, container, false);
        MainActivity.toolbar.setTitle("Add Vehicle");

        activity = getActivity();

        requestQueue = Volley.newRequestQueue(getActivity());
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading...");
        pDialog.show();

        SessionManager s = new SessionManager(getContext());
        SharedPreferences sp = s.pref;
        username = sp.getString(ConfigConstants.KEY_USERNAME,"");

        getAddedVehicles(rootview,inflater);
        Spinner spinner = rootview.findViewById(R.id.vehicle_category);
        createSpinner(spinner);
        //setVehicleDetails(rootview,inflater);

        return rootview;
    }

    static void setVehicleDetails(View view)
    {
        int pos = vehicles_layout.indexOfChild(view);
        try {
            TextView category = rootview.findViewById(R.id.vehicle_category_txt);
            EditText plate_num = rootview.findViewById(R.id.plate_number);
            Spinner s = rootview.findViewById(R.id.vehicle_category);
            EditText company = rootview.findViewById(R.id.vehicle_company);
            EditText model = rootview.findViewById(R.id.vehicle_model);
            TextView company_txt = rootview.findViewById(R.id.vehicle_company_txt);
            TextView model_txt = rootview.findViewById(R.id.vehicle_model_txt);
            TextView plate_num_txt = rootview.findViewById(R.id.plate_number_txt);
            Button b =rootview.findViewById(R.id.add_vehicle);
            String plate_no="",company_name="",model_name="";
            boolean valid = true;

            //Toast.makeText(getContext(), response.toString(), Toast.LENGTH_LONG).show();
            if(pos<vehicles.length())
            {
                JSONObject vehicle = vehicles.getJSONObject(pos);
                category.setText(vehicle.getString("vehicle_category"));
                category.setVisibility(View.VISIBLE);
                s.setVisibility(View.GONE);
                plate_no = vehicle.getString("plate_number");
                company_name = vehicle.getString("vehicle_company");
                model_name = vehicle.getString("vehicle_model");
                valid = false;
                b.setVisibility(View.GONE);

                company.setVisibility(View.GONE);
                model.setVisibility(View.GONE);
                plate_num.setVisibility(View.GONE);

                company_txt.setVisibility(View.VISIBLE);
                model_txt.setVisibility(View.VISIBLE);
                plate_num_txt.setVisibility(View.VISIBLE);

                plate_num_txt.setText("Plate Number :   "+plate_no);
                company_txt.setText("Company name :   "+company_name);
                model_txt.setText("Model name :   "+model_name);
            }
            else
            {
                Log.i("DEBUG",String.valueOf(pos));
                category.setVisibility(View.GONE);
                s.setVisibility(View.VISIBLE);
                b.setVisibility(View.VISIBLE);

                company.setVisibility(View.VISIBLE);
                model.setVisibility(View.VISIBLE);
                plate_num.setVisibility(View.VISIBLE);

                company_txt.setVisibility(View.GONE);
                model_txt.setVisibility(View.GONE);
                plate_num_txt.setVisibility(View.GONE);
            }
        }catch (JSONException j)
        {
            //
        }
    }

    static void addVehicle(final Context context)
    {
        TextView t = rootview.findViewById(R.id.vehicle_model);
        vehicle_model = t.getText().toString();
        TextView t1 = rootview.findViewById(R.id.plate_number);
        plate_number = t1.getText().toString();
        TextView t2 = rootview.findViewById(R.id.vehicle_company);
        vehicle_company = t2.getText().toString();

        if(vehicle_company.equals("")){
            t2.setError("This field is required.");
            t2.requestFocus();
        }else if(vehicle_model.equals("")){
            t.setError("This field is required.");
            t.requestFocus();
        }else if(plate_number.length()!=15){
            t1.setError("Invalid plate number");
            t2.requestFocus();
        }
        else
        {
            Map<String,String> params = new HashMap<>();
            //Adding parameters to request
            params.put(ConfigConstants.KEY_USERNAME, username);
            params.put("company",vehicle_company);
            params.put("model",vehicle_model);
            params.put("plate_num",plate_number);
            params.put("category",vehicle_category);
            params.put("operation", "add_vehicles");

            //Creating a json request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, ConfigConstants.GET_VEHICLES, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //Toast.makeText(Login.this, response.toString(), Toast.LENGTH_LONG).show();
                                if ((response.getString("message")).equals("success"))
                                {
                                    Toast.makeText(context, "Vehicle successfully added.", Toast.LENGTH_LONG).show();
                                    MainActivity.replaceFragments();
                                    //activity.onBackPressed();
                                    //Log.e("TP",String.valueOf(activity.getFragmentManager().getBackStackEntryCount()));
                                    //activity.getFragmentManager().popBackStack("Home", FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                }

                            } catch (JSONException e) {
                                e.printStackTrace();
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

    }

    void getAddedVehicles(final View rootview, final LayoutInflater inflater)
    {
        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, username);
        params.put("operation", "get_added_vehicles");

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.GET_VEHICLES, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(Login.this, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                vehicles = response.getJSONArray("vehicles");
                                setAddedVehicles(rootview,inflater);
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

    void setAddedVehicles(View rootview, LayoutInflater inflater)
    {
       vehicles_layout = rootview.findViewById(R.id.vehicles);
        for(int i=0;i<vehicles.length();i++)
        {
            try {
                JSONObject vehicle = vehicles.getJSONObject(i);
                View v1 = inflater.inflate(R.layout.vehicle, null);
                ImageView vehicle_img = v1.findViewById(R.id.vehicle_img);
                if(vehicle.getString("vehicle_category").equals("Two wheeler"))
                    vehicle_img.setImageResource(R.drawable.two_wheeler);
                else
                    vehicle_img.setImageResource(R.drawable.four_wheeler);
                vehicle_img.setBackground(getResources().getDrawable(R.drawable.vehicle_border));
                TextView t = v1.findViewById(R.id.vehicle_txt);
                t.setText(vehicle.getString("vehicle_model"));
                vehicles_layout.addView(v1);
            }catch (JSONException j)
            {
                //
            }
        }

        View v3 = inflater.inflate(R.layout.vehicle, null);
        ImageView vehicle_img3 = v3.findViewById(R.id.vehicle_img);
        vehicle_img3.setImageResource(R.drawable.add);
        vehicles_layout.addView(v3);

    }

    protected void createSpinner(final Spinner spinner)
    {
        String[] categories = {"Two wheeler","Four wheeler"};
        final List<String> categoryList = new ArrayList<>(Arrays.asList(categories));

        // Initializing an ArrayAdapter
        final ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(
                getActivity(),R.layout.support_simple_spinner_dropdown_item,categoryList){
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
                vehicle_category = (String) parent.getItemAtPosition(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
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
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
