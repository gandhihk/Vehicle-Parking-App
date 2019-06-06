package com.example.vehicleparkingapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        MyProfile.OnFragmentInteractionListener,
        Home.OnFragmentInteractionListener,
        AddVehicle.OnFragmentInteractionListener,
        ParkingHistory.OnFragmentInteractionListener,
        CurrentSession.OnFragmentInteractionListener,
        About.OnFragmentInteractionListener
{
    SessionManager s;
    User u;
    static Handler h;
    NavigationView navigationView;
    static Toolbar toolbar;
    static FragmentManager fm;
    ArrayList<String> vehicleList;
    RequestQueue requestQueue;
    int endTime;
    int MY_PERMISSIONS_REQUEST_RECEIVE_SMS = 1,NAV_ADD_VEHICLE = 2,NAV_CURRENT_SESSION = 4,NAV_PARKING_HISTORY = 3,NAV_ABOUT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //check if already logged in
        s = new SessionManager(getApplicationContext());
        s.checkLogin();

        if(!s.isLoggedIn())
            finish();

        SharedPreferences sp = s.pref;
        u = new User();
        u.fname = sp.getString(ConfigConstants.KEY_FNAME, "");
        u.lname = sp.getString(ConfigConstants.KEY_LNAME,"");
        u.username = sp.getString(ConfigConstants.KEY_USERNAME,"");
        u.contact = sp.getString(ConfigConstants.KEY_PHONE,"");
        u.licence = sp.getString(ConfigConstants.KEY_LICENCE,"");
        u.address = sp.getString(ConfigConstants.KEY_ADDRESS,"");

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        View header = navigationView.getHeaderView(0);
        TextView name = header.findViewById(R.id.textView);
        name.setText(u.fname+" "+u.lname);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();


        //To finish this activity if log in is successful
        h = new Handler() {

            public void handleMessage(Message msg) {
                super.handleMessage(msg);

                switch(msg.what) {

                    case 0:
                        finish();
                        break;

                }
            }

        };


    }

    private FragmentManager.OnBackStackChangedListener getListener() {
        FragmentManager.OnBackStackChangedListener result = new FragmentManager.OnBackStackChangedListener() {
            public void onBackStackChanged() {
                FragmentManager manager = getSupportFragmentManager();
                if (manager != null) {
                    int backStackEntryCount = manager.getBackStackEntryCount();
                    if (backStackEntryCount == 0) {
                        finish();
                    }
                    Fragment fragment = manager.getFragments()
                            .get(backStackEntryCount - 1);
                    fragment.onResume();
                }
            }
        };
        return result;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        //displaySelectedScreen(item.getItemId());

        Fragment f = null;

        if (id == R.id.nav_home) {
            f = new Home();
        } else if (id == R.id.nav_my_profile) {
            f = new MyProfile();
        } else if (id == R.id.nav_add_vehicle) {
            f = new AddVehicle();
        } else if (id == R.id.nav_parking_history) {
            f = new ParkingHistory();
        } else if (id == R.id.nav_current_session) {
            f = new CurrentSession();
        } else if (id == R.id.nav_logout) {
            logout();
        } else if (id == R.id.nav_about) {
            f = new About();
        }

        if (f != null) {
            fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.content_frame, f);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    public static void replaceFragments() {
        Fragment fragment = null;
        try {
            fragment = (Fragment) new Home();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Insert the fragment by replacing any existing fragment
        fm.beginTransaction().replace(R.id.content_frame, fragment)
                .commit();
    }

    void logout()
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        alertDialog.setTitle("Confirm Logout");
        alertDialog.setMessage("Are you sure you want to logout?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                s.logoutUser();
                finish();
                startActivity(getIntent());
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    void getVehicles()
    {
        requestQueue = Volley.newRequestQueue(this);
        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, u.username);
        params.put("operation", "get_added_vehicles");
        vehicleList = new ArrayList<>();

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.GET_VEHICLES, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(MainActivity.this, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                JSONArray vehicles = response.getJSONArray("vehicles");
                                for(int i=0;i<vehicles.length();i++)
                                {
                                    JSONObject j = vehicles.getJSONObject(i);
                                    String model = j.getString("vehicle_model");
                                    String company = j.getString("vehicle_company");
                                    vehicleList.add(company+" "+model);
                                }
                                if(vehicleList.isEmpty())
                                {
                                    Fragment f = new AddVehicle();
                                    FragmentManager fm = getSupportFragmentManager();
                                    FragmentTransaction ft = fm.beginTransaction();
                                    ft.replace(R.id.content_frame, f);
                                    ft.commit();
                                }else{
                                    Intent i = new Intent(MainActivity.this, NextStep.class);
                                    i.putExtra("level_number",Home.level_number);
                                    i.putExtra("slot_number",Home.slot_number);
                                    i.putExtra("vehicleList",vehicleList);
                                    startActivity(i);
                                }
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(MainActivity.this,
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

    private void displaySelectedScreen(int id)
    {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onResume() {
        super.onResume();
        navigationView.setCheckedItem(R.id.nav_home);
    }

    public void dummy_login(View view) {
        Intent intent = new Intent(this, Login.class);
        startActivity(intent);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {
        //
    }


    public void proceedAfterSelectingSlot(View view) {
        getVehicles();
    }

    public void addVehicle(View view) {
        AddVehicle.addVehicle(getApplicationContext());
    }

    public void setVehicleDetails(View view) {
        //view.setBackgroundColor(getResources().getColor(R.color.iron));
        AddVehicle.setVehicleDetails(view);
    }

    public void updateProfile(View view) {
        MyProfile.updateProfile(getApplicationContext());
    }

    public void edit(View view) {
        MyProfile.edit(view);
    }

    public void endParking(View view) {
        CurrentSession.endParking(view,this);
    }

    public void extendParking(View view) {
        CurrentSession.extendParking(view,this);
    }
}

class User
{
    String fname,lname,username,address,licence,contact;
}
