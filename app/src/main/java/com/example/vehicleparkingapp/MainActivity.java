package com.example.vehicleparkingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

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
    boolean loggedIn;
    static Handler h;
    NavigationView navigationView;
    int NAV_MY_PROFILE = 1,NAV_ADD_VEHICLE = 2,NAV_CURRENT_SESSION = 4,NAV_PARKING_HISTORY = 3,NAV_ABOUT = 5;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //displaySelectedScreen(R.id.nav_home);
        onNavigationItemSelected(navigationView.getMenu().getItem(0));

        //check if already logged in
        s = new SessionManager(getApplicationContext());
        loggedIn = s.isLoggedIn();

        if(!loggedIn)
        {
            Intent i = new Intent(this, Login.class);
            startActivity(i);
            finish();
        }

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
            //
        } else if (id == R.id.nav_about) {
            f = new About();
        }

        if (f != null) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.content_frame, f);
            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        return true;
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
}
