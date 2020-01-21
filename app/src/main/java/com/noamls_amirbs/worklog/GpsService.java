package com.noamls_amirbs.worklog;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.math.BigDecimal;
import java.math.RoundingMode;

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
            public void onLocationChanged(Location location)
            {

                SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                double latitude = round(location.getLatitude(),4);
                double longitude = round(location.getLongitude(),4);
                //====== use sharedPreferences to save GPS button enable setting ====================//

                Log.d("debug","location :"+location.getLatitude()+" "+location.getLongitude());

                boolean GPS_on_or_of = sp.getBoolean("set_GPS_button",false);
                // Karme Tzure Gate location
                if((latitude > 31.6060 && latitude < 31.6070  && longitude > 35.0995 && longitude < 35.1005)&&GPS_on_or_of)
                {
                    editor.putBoolean("inArea", true);
                    editor.commit();
                    boolean activeNotification = sp.getBoolean("activeNotification",false);
                    if(activeNotification)
                    {
                        showNotification("Notification Message", "you get your work, click me to sign in");
                        editor.putBoolean("activeNotification", false);
                        editor.commit();
                    }
                }
                else
                {
                    editor.putBoolean("inArea", false);
                    editor.commit();
                    Log.d("debug","outside area");
                    editor.putBoolean("activeNotification", true);
                    editor.commit();
                }

            }
            //==== have to implement, not in use =======================//
            public void onStatusChanged(String s, int i, Bundle bundle) { }
            public void onProviderEnabled(String s) { }
            //==========================================================//
            //=== use to set up GPS component =========================//
            public void onProviderDisabled(String s)
            {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
            //===========================================================//
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,3000,0,listener);

    }
    //=========== use to set ou notification once the employee near his work spot ============================//
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
    //============= display the notification =======================================//
    public void showNotification(String notificationTitle, String notificationText)
    {
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,0,intent,0);
        Uri uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_alarm_on_black_24dp)
                .setContentTitle(notificationTitle)
                .setContentText(notificationText)
                .setContentIntent(pendingIntent)
                .setSound(uri)
                .setAutoCancel(true)
                .build();

        notificationManager.notify(notificationId, notification);
        notificationId++;
    }

    public void onDestroy()
    {
        super.onDestroy();
        if(locationManager != null)
           locationManager.removeUpdates(listener);

    }
    //get a double number and return the same double number with n 'places' after the point
    private static double round(double value, int places)
    {
          if (places < 0) throw new IllegalArgumentException();
          BigDecimal bd = new BigDecimal(Double.toString(value));
          bd = bd.setScale(places, RoundingMode.HALF_UP);
          return bd.doubleValue();
    }
}
