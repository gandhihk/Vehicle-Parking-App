package com.example.vehicleparkingapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Chronometer;

import static android.support.constraint.Constraints.TAG;

public class BoundService extends Service {
    private static String LOG_TAG = "BoundService";
    private IBinder mBinder = new MyBinder();
    private Chronometer mChronometer;
    private final static String TAG = "BroadcastService";

    private static final int NOTIFICATION_ID = 1;
    private static String NOTIFICATION_CHANNEL_ID = "com.example.vehicleparkingapp";

    int time_left,duration,endTime;
    boolean sendNotificationForExtend=true,sendNotificationForEnd=true;

    public static final String COUNTDOWN_BR = "com.example.vehicleparkingapp.countdown_br";
    Intent bi = new Intent(COUNTDOWN_BR);

    CountDownTimer cdt = null;

    // Start and end times in milliseconds
    private long startTime;

    // Is the service tracking time?
    boolean isTimerRunning;

    private static int THRESHOLD=20;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "Starting timer...");

        endTime = 0;
        isTimerRunning = true;
    }

    @Override
    public int onStartCommand(Intent intent, final int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Bundle extras = intent.getExtras();
        if(extras == null)
            Log.d("Service","null");
        else
        {
            Log.d("Service","not null");
            time_left = intent.getIntExtra("time_left",0);
            duration = intent.getIntExtra("duration",0);
            endTime = time_left+duration;

            createTimer(endTime);
        }
        return Service.START_STICKY;
    }

    void createTimer(int endTime)
    {
        cdt = new CountDownTimer(endTime, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {

                Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                bi.putExtra("countdown", millisUntilFinished);
                sendBroadcast(bi);

                if(millisUntilFinished<=duration){
                    sendNotification("Parking Time Starting...","Your parking time has started","5","Start Notification");
                    Log.i(TAG, "Start");
                }


                if(millisUntilFinished<((THRESHOLD+10)*60*1000) && sendNotificationForExtend) {
                    sendNotificationForExtend=false;
                    Log.i(TAG, "Less < 30");
                    sendNotification("Parking Time Ending...",(THRESHOLD+10)+" Minutes are left for your parking.\nTap to extend your parking","10","NotificationExtend");
                }

                if(millisUntilFinished<(THRESHOLD*60*1000) && sendNotificationForEnd) {
                    sendNotificationForEnd=false;
                    Log.i(TAG, "Less < 20");
                    sendNotification("Parking Time Ending...",THRESHOLD+" Minutes are left for your parking.\nCannot extend parking now","3","NotificationEnd");
                }

                    /*if(millisUntilFinished<=1000){
                        sendNotification("Parking Time Ended","Tap for payment","4","End Notification");
                        stopForeground(true);
                        endParking();
                    }*/

                if(CurrentSession.end) {
                    CurrentSession.end=false;
                    Log.i(TAG, "Done");
                    cdt.cancel();
                    endParking();
                }

                if(CurrentSession.extended)
                {
                    cdt.cancel();
                    createTimer(CurrentSession.service_endTime);
                    CurrentSession.extended=false;
                }
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "Timer finished");
                stopForeground(true);
                endParking();
            }
        };

        cdt.start();
    }

    void endParking()
    {
        sendNotification("Parking Time Ended","Tap for payment","4","End Notification");
        stopSelf();
        onUnbind(bi);
        onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {

        Log.v(LOG_TAG, "in onBind");
        return mBinder;
    }

    @Override
    public void onRebind(Intent intent) {
        Log.v(LOG_TAG, "in onRebind");
        super.onRebind(intent);
    }

    @Override
    public boolean onUnbind(Intent intent) {
        Log.v(LOG_TAG, "in onUnbind");
        return true;
    }

    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    public void foreground() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(NOTIFICATION_ID, createNotification());
    }

    public void background() {
        stopForeground(true);
    }

    @Override
    public void onDestroy() {
        //stopSelf();
        super.onDestroy();
        //cdt.cancel();
        Log.v(LOG_TAG, "in onDestroy");
        //mChronometer.stop();

    }

    private void sendNotification(String title, String message, String id, String channelname)
    {
        //sendNotificationForExtend=false;
        NotificationChannel chan = new NotificationChannel(id, channelname, NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle()
                        .bigText(message))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        builder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 });

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify(1000, builder.build());

    }

    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.vehicleparkingapp";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("Timer is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private Notification createNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle("Timer Active")
                .setContentText("Tap to return to the timer")
                .setSmallIcon(R.mipmap.ic_launcher);

        Intent resultIntent = new Intent(this, MainActivity.class);
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(this, 0, resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(resultPendingIntent);

        return builder.build();
    }


    public class MyBinder extends Binder {
        BoundService getService() {
            return BoundService.this;
        }
    }
}
