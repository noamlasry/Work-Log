package com.noamls_amirbs.worklog;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class GpsService extends Service {
    //===== for GPS =================//
    private LocationListener listener;
    private LocationManager locationManager;
    //==== for notification =================//
    private NotificationManager notificationManager;
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "Channel 1 Demo";
    private static int notificationId = 1;
    boolean activeNotification = true;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressLint("MissingPermission")
    public void onCreate() {

        setupNotificationChannel();

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent i = new Intent("location_update");

                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
                sendBroadcast(i);

                if(location.getLatitude() > 31.25 && location.getLatitude() < 31.3)
                {
                    if(activeNotification)
                    {
                        Log.d("debug","in area");
                        showNotification("Notification Message", "you get your work, click me to sigh in");

                        Toast.makeText(getApplicationContext(),"in area",Toast.LENGTH_SHORT).show();
                        activeNotification = false;
                    }

                }
                else
                {
                    Log.d("debug","outside area");
                    activeNotification = true;

                }

            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }
    private void setupNotificationChannel()
    {
        // 1. Get reference to Notification Manager
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // 2. Create Notification Channel ONLY ONEs.
        //    Need for Android 8.0 (API level 26) and higher.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            Toast.makeText(this, "Notification Channel created!", Toast.LENGTH_LONG).show();
            //Create channel only if it is not already created
            if (notificationManager.getNotificationChannel(CHANNEL_ID) == null)
            {
                NotificationChannel notificationChannel = new NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT); // NotificationManager.IMPORTANCE_HIGH

                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
    }
    public void showNotification(String notificationTitle, String notificationText)
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.work)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setSound(uri)
                //.setOngoing(true)
                .setAutoCancel(true)
                .build();

        // Send the notification to the device Status bar.
        notificationManager.notify(notificationId, notification);

        notificationId++;  // for multiple(grouping) notifications on the same chanel
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
           locationManager.removeUpdates(listener);
        }
    }
}
