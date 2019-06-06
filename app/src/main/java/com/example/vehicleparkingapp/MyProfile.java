package com.example.vehicleparkingapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
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


public class MyProfile extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private static View rootview;
    private static RequestQueue requestQueue;

    private static String first_name,last_name,username,address,licence;

    private static EditText fn,ln,address_txt,licence_txt;
    private TextView username_txt,name;

    private OnFragmentInteractionListener mListener;

    public MyProfile() {
        // Required empty public constructor
    }

    public static MyProfile newInstance(String param1, String param2) {
        MyProfile fragment = new MyProfile();
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
        MainActivity.toolbar.setTitle("My Profile");

        rootview = inflater.inflate(R.layout.my_profile, container, false);

        requestQueue = Volley.newRequestQueue(getActivity());

        SessionManager s = new SessionManager(getContext());
        SharedPreferences sp = s.pref;
        username = sp.getString(ConfigConstants.KEY_USERNAME,"");
        first_name = sp.getString(ConfigConstants.KEY_FNAME,"");
        last_name = sp.getString(ConfigConstants.KEY_LNAME,"");
        licence = sp.getString(ConfigConstants.KEY_LICENCE,"");
        address = sp.getString(ConfigConstants.KEY_ADDRESS,"");

        setProfileDetails();

        // Inflate the layout for this fragment
        return rootview;
    }

    void setProfileDetails()
    {
        fn = rootview.findViewById(R.id.first_name_txt);
        ln = rootview.findViewById(R.id.last_name_txt);
        address_txt = rootview.findViewById(R.id.address);
        licence_txt = rootview.findViewById(R.id.licence);
        username_txt = rootview.findViewById(R.id.username);
        name = rootview.findViewById(R.id.name);
        fn.setText(first_name);
        ln.setText(last_name);
        username_txt.setText("Username :   "+username);
        name.setText(first_name+" "+last_name);
        address_txt.setText(address);
        licence_txt.setText(licence);
    }

    public static void edit(View view)
    {
        fn.setFocusableInTouchMode(true);
        ln.setFocusableInTouchMode(true);
        address_txt.setFocusableInTouchMode(true);
        licence_txt.setFocusableInTouchMode(true);
        Button b = rootview.findViewById(R.id.edit_profile);
        b.setVisibility(View.VISIBLE);
    }

    public static void updateProfile(final Context context)
    {
        String f_n,l_n,add,lic;
        f_n = fn.getText().toString();
        l_n = ln.getText().toString();
        add = address_txt.getText().toString();
        lic = licence_txt.getText().toString();

        if(f_n.equals("")){
            fn.setError("This field is required.");
            fn.requestFocus();
        }else if(l_n.equals("")){
            ln.setError("This field is required.");
            ln.requestFocus();
        }else if(add.equals("")){
            address_txt.setError("This field is required.");
            address_txt.requestFocus();
        }else if(lic.length()!=16){
            licence_txt.setError("Invalid licence number");
            licence_txt.requestFocus();
        }
        else
        {
            SessionManager s = new SessionManager(context);
            s.updateLoginSession(f_n,l_n,add,lic);

            Map<String,String> params = new HashMap<>();
            //Adding parameters to request
            params.put(ConfigConstants.KEY_USERNAME, username);
            params.put("first_name",f_n);
            params.put("last_name",l_n);
            params.put("address",add);
            params.put("licence",lic);
            params.put("operation", "set_profile_details");

            //Toast.makeText(context, f_n+l_n, Toast.LENGTH_LONG).show();

            //Creating a json request
            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                    (Request.Method.POST, ConfigConstants.SET_PROFILE_DETAILS, new JSONObject(params), new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            try {
                                //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
                                if ((response.getString("message")).equals("success"))
                                {
                                    Toast.makeText(context, "Profile successfully updated.", Toast.LENGTH_LONG).show();
                                    MainActivity.replaceFragments();
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
