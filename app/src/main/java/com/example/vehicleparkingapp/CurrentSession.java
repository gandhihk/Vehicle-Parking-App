package com.example.vehicleparkingapp;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.DateFormat;
import java.text.SimpleDateFormat;
import android.net.ParseException;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
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

import java.util.Date;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static android.view.animation.Animation.RELATIVE_TO_SELF;


public class CurrentSession extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private static String NOTIFICATION_CHANNEL_ID = "com.example.vehicleparkingapp";
    private static int THRESHOLD=20;

    private String mParam1;
    static private String mParam2,username;
    int progress;
    CountDownTimer countDownTimer;
    ProgressDialog pDialog;
    View b,rootview;
    TextView booking_id_txt,entry_time_txt,fare_txt,date_txt,vehicle_txt,otp_txt,level_num_txt,total_fare_txt,time_left_txt,slot_num_txt,duration_txt;
    LinearLayout scrollView;
    Button online_pay,offline_pay;
    static private RequestQueue requestQueue;
    private OnFragmentInteractionListener mListener;
    int endTime=0;
    static int service_endTime;
    static boolean end=false,extend_parking_visible=true,extended=false;
    static FragmentTransaction ft;
    static Fragment frg;

    public CurrentSession() {
        // Required empty public constructor
    }


    public static CurrentSession newInstance(String param1, String param2) {
        CurrentSession fragment = new CurrentSession();
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

        MainActivity.toolbar.setTitle("Current Session");

        rootview = inflater.inflate(R.layout.current_session, container, false);

        scrollView = rootview.findViewById(R.id.scrollView);

        ft = getFragmentManager().beginTransaction();
        frg = getFragmentManager().findFragmentByTag("nav_current_session");

        SessionManager s = new SessionManager(getContext());
        SharedPreferences sp = s.pref;
        username = sp.getString(ConfigConstants.KEY_USERNAME,"");
        pDialog = new ProgressDialog(getContext());
        pDialog.setMessage("Loading...");
        pDialog.show();
        getCurrentBookings(inflater);
        extend_parking_visible=true;

        return rootview;
    }

    void getCurrentBookings(final LayoutInflater inflater)
    {
        requestQueue = Volley.newRequestQueue(getActivity());
        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, username);
        params.put("operation", "get_current_session");

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.CREATE_BOOKING, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(getContext(), response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                JSONArray current_bookings = response.getJSONArray("current_bookings");
                                for(int i=0;i<current_bookings.length();i++)
                                {
                                    JSONObject j = current_bookings.getJSONObject(i);
                                    int booking_id = j.getInt("booking_id");
                                    String date = j.getString("date");
                                    String entry_time = j.getString("entry_time");
                                    double fare = j.getDouble("total_fare");
                                    String vehicle_category = j.getString("vehicle_category");
                                    int OTP = j.getInt("otp");
                                    int level_num = j.getInt("level_num");
                                    int slot_num = j.getInt("slot_num");
                                    int duration = j.getInt("duration");
                                    createCurrentBookingField(booking_id,date,vehicle_category,fare,entry_time,OTP,level_num,slot_num,duration,inflater);
                                }
                                if(current_bookings.length()==0)
                                {
                                    scrollView.findViewById(R.id.no_current).setVisibility(View.VISIBLE);
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

    void createCurrentBookingField(final int booking_id, String date, String vehicle_category, double fare, String entry_time, int OTP, int level_num, int slot_num, final int duration, LayoutInflater inflater)
    {
        final ProgressBar progressBarView;
        progress = 1;

        b = inflater.inflate(R.layout.current_booking, null);
        booking_id_txt = b.findViewById(R.id.booking_id);
        entry_time_txt = b.findViewById(R.id.entry_time);
        fare_txt = b.findViewById(R.id.fare);
        date_txt = b.findViewById(R.id.date);
        vehicle_txt = b.findViewById(R.id.vehicle_category);
        otp_txt = b.findViewById(R.id.otp);
        level_num_txt = b.findViewById(R.id.level_num);
        slot_num_txt = b.findViewById(R.id.slot_num);
        duration_txt = b.findViewById(R.id.duration);
        progressBarView = b.findViewById(R.id.view_progress_bar);
        final TextView tv_time = b.findViewById(R.id.tv_timer);
        time_left_txt = b.findViewById(R.id.time_left);
        total_fare_txt = b.findViewById(R.id.total_fare);
        online_pay = b.findViewById(R.id.online_pay);
        offline_pay = b.findViewById(R.id.offline_pay);

        entry_time = entry_time.substring(entry_time.length()-8);
        String entry_time1 = entry_time.replaceAll(":","");
        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        int curr_hour = calendar.get(Calendar.HOUR_OF_DAY);
        int curr_minute = calendar.get(Calendar.MINUTE);
        int curr_second = calendar.get(Calendar.SECOND);
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date entrytime = new Date();
        Date curr_time = new Date();
        try {
            entrytime = sdf.parse(entry_time);
            curr_time = sdf.parse(String.valueOf(curr_hour)+":"+String.valueOf(curr_minute)+":"+String.valueOf(curr_second));
        } catch (Exception ex) {
            Log.v("Exception", ex.getLocalizedMessage());
        }//Toast.makeText(getContext(), hour+":"+minute+":"+second, Toast.LENGTH_LONG).show();
        long millis = curr_time.getTime() - entrytime.getTime();
        int hours = (int)millis/(1000 * 60 * 60);
        int mins = (int)(millis/(1000*60)) % 60;
        int sec = (int)(millis/1000)%60;
        int endtime = (int)millis/1000;
        //Toast.makeText(getContext(), curr_time+" "+entry_time, Toast.LENGTH_LONG).show();
        Log.i(TAG, curr_time+"\t"+entry_time+"\t"+millis);
        int duration1 = duration*60;
        endTime = duration1-endtime;

        booking_id_txt.setText("Booking ID :   "+booking_id);
        entry_time_txt.setText("Entry Time :   "+entry_time);
        fare_txt.setText("Estimated Fare :   "+fare+" Rs.");
        date_txt.setText("Date :   "+date);
        vehicle_txt.setText("Vehicle Category :   "+vehicle_category);
        otp_txt.setText("OTP :    "+OTP);
        level_num_txt.setText("Level Number :    "+level_num);
        slot_num_txt.setText("Slot Number :    "+slot_num);
        duration_txt.setText("Duration :    "+duration+" minutes");
        if(endTime==0)
            time_left_txt.setText("Time left for booking to start : 0 s");
        scrollView.addView(b);
        /*Animation*/
        RotateAnimation makeVertical = new RotateAnimation(0, -90, RELATIVE_TO_SELF, 0.5f, RELATIVE_TO_SELF, 0.5f);
        makeVertical.setFillAfter(true);
        progressBarView.startAnimation(makeVertical);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(0);
        //fn_countdown(progress,countDownTimer,endTime,progressBarView);

        //Toast.makeText(getContext(), String.valueOf(endTime), Toast.LENGTH_LONG).show();
        countDownTimer = new CountDownTimer(endTime * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                setProgress(progress, endTime, progressBarView,booking_id_txt);
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

                if(end){
                    countDownTimer.cancel();
                    ((View)total_fare_txt.getParent()).findViewById(R.id.extend_parking).setVisibility(View.GONE);
                }
                else
                    extend_parking_visible=true;

                if(millisUntilFinished<=THRESHOLD*60*1000)
                {
                    extend_parking_visible=false;
                    ((View)total_fare_txt.getParent()).findViewById(R.id.extend_parking).setVisibility(View.GONE);
                }

                service_endTime=(int)millisUntilFinished;

                //Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
            }

            @Override
            public void onFinish() {
                setProgress(progress, endTime, progressBarView,booking_id_txt);
                b.findViewById(R.id.time_layout).setVisibility(View.GONE);
                time_left_txt.setVisibility(View.GONE);
                tv_time.setVisibility(View.GONE);
                total_fare_txt.setVisibility(View.VISIBLE);
                online_pay.setVisibility(View.VISIBLE);
                offline_pay.setVisibility(View.VISIBLE);
                b.findViewById(R.id.extend_parking).setVisibility(View.GONE);
                end = true;
                calculateFare(getContext(),booking_id_txt.getText().toString(),fare_txt.getText().toString(),entry_time_txt.getText().toString(),vehicle_txt.getText().toString(),total_fare_txt);
                //sendNotification("Parking Time Ended","Tap for payment","4","End Notification",getContext());
            }
        };
        countDownTimer.start();
    }

    static void endParking(final View view, final Context context)
    {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        alertDialog.setTitle("End parking");
        alertDialog.setMessage("Are you sure you want to end parking ?");

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                end = true;
                view.setVisibility(View.GONE);
                View parent = (View)(((view.getParent()).getParent()).getParent()).getParent();
                View first_parent = (View)view.getParent();
                first_parent.findViewById(R.id.online_pay).setVisibility(View.VISIBLE);
                first_parent.findViewById(R.id.offline_pay).setVisibility(View.VISIBLE);
                first_parent.findViewById(R.id.total_fare).setVisibility(View.VISIBLE);
                first_parent.findViewById(R.id.time_left).setVisibility(View.GONE);
                first_parent.findViewById(R.id.extend_parking).setVisibility(View.GONE);
                first_parent.findViewById(R.id.time_layout).setVisibility(View.GONE);
                calculateFare(context,((TextView)parent.findViewById(R.id.booking_id)).getText().toString(),
                        ((TextView)parent.findViewById(R.id.fare)).getText().toString(),
                        ((TextView)parent.findViewById(R.id.entry_time)).getText().toString(),
                        ((TextView)parent.findViewById(R.id.vehicle_category)).getText().toString(),
                        (TextView) first_parent.findViewById(R.id.total_fare));
                //sendNotification("Parking Time Ended","Tap for payment","4","End Notification",context);
            }
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        alertDialog.show();
    }

    static void calculateFare(final Context context, String booking_id, String fare, String entry_time, String vehicle_category, final TextView total_fare_txt)
    {
        Date c = Calendar.getInstance().getTime();
        SimpleDateFormat df1 = new SimpleDateFormat("yyyy-MM-dd");
        String date = df1.format(c);
        int hr = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        int min = Calendar.getInstance().get(Calendar.MINUTE);
        if(hr<10)
            date = date+" 0"+hr+":"+min+":00";
        if(min<10)
            date = date+" "+hr+":0"+min+":00";
        if(hr>=10 && min>=10)
            date = date+" "+hr+":"+min+":00";

        Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, username);
        params.put("operation", "end_parking");
        booking_id = booking_id.substring(15);
        params.put("booking_id",booking_id);
        vehicle_category = vehicle_category.substring(21);
        params.put("vehicle_category",vehicle_category);
        params.put("date",date);

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.CREATE_BOOKING, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                int total_fare = response.getInt("total_fare");
                                int duration = response.getInt("duration");
                                //duration_txt.setText("Duration :    "+duration+" minutes");
                                total_fare_txt.setText("Total Fare :    "+total_fare+" Rs.");
                                View parent = (View)((total_fare_txt.getParent()).getParent());
                                ((TextView)parent.findViewById(R.id.duration)).setText("Duration :    "+duration+" minutes");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,
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

    static void extendParking(View view, final Context context)
    {
        View parent = (View)(((view.getParent()).getParent()).getParent()).getParent();
        final String booking_id = ((TextView)parent.findViewById(R.id.booking_id)).getText().toString();
        final String booking_id1 = booking_id.substring(15);

        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        alertDialog.setTitle("Extend parking");
        alertDialog.setMessage("Enter extension period in minutes");
        final EditText input = new EditText(context);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        alertDialog.setView(input);

        alertDialog.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int which) {
                final int extension = Integer.valueOf(input.getText().toString());
                Map<String,String> params = new HashMap<>();
                //Adding parameters to request
                params.put(ConfigConstants.KEY_USERNAME, username);
                params.put("extension",String.valueOf(extension));
                params.put("booking_id",booking_id1);
                params.put("operation","extend_parking");
                //Toast.makeText(context, extension+" "+booking_id1, Toast.LENGTH_LONG).show();

                //Creating a json request
                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, ConfigConstants.CREATE_BOOKING, new JSONObject(params), new Response.Listener<JSONObject>() {
                            @Override
                            public void onResponse(JSONObject response) {
                                try {
                                    //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
                                    if ((response.getString("message")).equals("success"))
                                    {
                                        Toast.makeText(context, "Parking has been extended by "+extension+" minutes.", Toast.LENGTH_LONG).show();
                                        extended = true;
                                        ft.replace(R.id.content_frame, new CurrentSession());
                                        ft.commit();
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    Toast.makeText(context,
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
        });

        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                //
            }
        });

        alertDialog.show();
    }

    static void makeOfflinePayment(View view, final Context context)
    {
        final View parent = (View)(((view.getParent()).getParent()).getParent()).getParent();
        final Map<String,String> params = new HashMap<>();
        //Adding parameters to request
        params.put(ConfigConstants.KEY_USERNAME, username);
        params.put("booking_id",((TextView)parent.findViewById(R.id.booking_id)).getText().toString().substring(15));
        params.put("operation","choose_offline_payment");
        //Toast.makeText(context, extension+" "+booking_id1, Toast.LENGTH_LONG).show();

        //Creating a json request
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, ConfigConstants.CREATE_BOOKING, new JSONObject(params), new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            //Toast.makeText(context, response.toString(), Toast.LENGTH_LONG).show();
                            if ((response.getString("message")).equals("success"))
                            {
                                parent.findViewById(R.id.online_pay).setVisibility(View.GONE);
                                parent.findViewById(R.id.offline_pay).setVisibility(View.GONE);
                                parent.findViewById(R.id.verified_img).setVisibility(View.VISIBLE);
                                parent.findViewById(R.id.successful_verify).setVisibility(View.VISIBLE);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                            Toast.makeText(context,
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

    public void setProgress(int startTime, int endTime, ProgressBar progressBarView, TextView booking_id_txt) {
        if(startTime==endTime)
        {
            View parent = booking_id_txt.getRootView();
            scrollView.removeView(parent);
        }
        progressBarView.setMax(endTime);
        progressBarView.setSecondaryProgress(endTime);
        progressBarView.setProgress(startTime);

    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Get extra data included in the Intent
            int time_left = (int)intent.getLongExtra("time_left",0);
            int duration = intent.getIntExtra("duration",0);
            endTime = time_left+duration;
            //Toast.makeText(context, String.valueOf(endTime), Toast.LENGTH_SHORT).show();
        }
    };

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

    @Override
    public void onDestroy() {
        extend_parking_visible=true;
        end=false;
        super.onDestroy();
    }

    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
