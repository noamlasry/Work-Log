package com.noamls_amirbs.worklog;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

public class GpsService extends Service
{
    //===== for GPS =================//
    private LocationListener listener;
    private LocationManager locationManager;
    //==== for notification =================//
    private NotificationManager notificationManager;
    private static String CHANNEL_ID = "channel";
    private static String CHANNEL_NAME = "Channel Work Log App";
    private static int notificationId = 1;
    boolean activeNotification = true;

    //=== have to implement as service aap, dos't do nothing
    public IBinder onBind(Intent intent) { return null; }

    @SuppressLint("MissingPermission")
    public void onCreate()
    {
        //=== check device version ======//
        setupNotificationChannel();
        listener = new LocationListener()
        {
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
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            public void onProviderEnabled(String s) { }

            public void onProviderDisabled(String s)
            {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }
    private void setupNotificationChannel()
    {
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
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
                .setSmallIcon(R.drawable.ic_notifications)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setSound(uri)
                //.setOngoing(true)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(notificationId, notification);
        notificationId++;
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if(locationManager != null)
           locationManager.removeUpdates(listener);

    }
}
