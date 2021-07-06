package com.samsung.android.sdk.accessory.example.filetransfer.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Belal on 12/30/2016.
 */

//public class RecvrService extends Service {
//    //creating a mediaplayer object
//    private MediaPlayer player;
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//        return null;
//    }
//
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//        //getting systems default ringtone
//        player = MediaPlayer.create(this,
//                Settings.System.DEFAULT_RINGTONE_URI);
//        //setting loop play to true
//        //this will make the ringtone continuously playing
//        player.setLooping(true);
//
//        //staring the player
//        player.start();
//
//        //we have some options for service
//        //start sticky means service will be explicity started and stopped
//        return START_STICKY;
//    }
//
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//        //stopping the player when service is destroyed
//        player.stop();
//    }
//
//
//}
public class RecvrService extends Service {
    public int counter=0;
    private MediaPlayer player;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void startMyOwnForeground()
    {
        String NOTIFICATION_CHANNEL_ID = "example.permanence";
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.d("intent", String.valueOf(intent));
        if (intent == null || intent.getAction().equals(String.valueOf( Constants.ACTION.STOPFOREGROUND_ACTION))) {
//            Log.i(LOG_TAG, "Received Stop Foreground Intent");
            //your end servce code
            stopForeground(true);
            stopSelfResult(startId);
            Log.d("tag", "Received Stop Foreground Intent");

            return START_NOT_STICKY;

        }

        else if (intent.getAction().equals(String.valueOf(Constants.ACTION.STARTFOREGROUND_ACTION) )) {
            Log.d("tag", "Received Start Foreground Intent ");

            startTimer();
            player = MediaPlayer.create(this,
                    Settings.System.DEFAULT_RINGTONE_URI);
            //setting loop play to true
            //this will make the ringtone continuously playing
            player.setLooping(true);

            //staring the player
            player.start();

            return START_STICKY;
        }


//        startTimer();
//        player = MediaPlayer.create(this,
//                Settings.System.DEFAULT_RINGTONE_URI);
//        //setting loop play to true
//        //this will make the ringtone continuously playing
//        player.setLooping(true);
//
//        //staring the player
//        player.start();

        return START_STICKY;

//        return START_NOT_STICKY;

    }







    @Override
    public void onDestroy() {
        super.onDestroy();
        stoptimertask();
        player.stop();
//        Intent broadcastIntent = new Intent();
//        broadcastIntent.setAction("restartservice");
//        broadcastIntent.setClass(this, Restarter.class);
//        this.sendBroadcast(broadcastIntent);
    }



    private Timer timer;
    private TimerTask timerTask;
    public void startTimer() {
        timer = new Timer();
        timerTask = new TimerTask() {
            public void run() {
                Log.i("Count", "=========  "+ (counter++));
            }
        };
        timer.schedule(timerTask, 1000, 1000); //
    }

    public void stoptimertask() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}