package com.noamls_amirbs.worklog;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    final Context context = this;
    private Button setGPS, enableGPS;//gps button, to activate and shut down
    //========= use for sqlite, read and write app data ==========//
    public static final String MY_DB_NAME = "employeeRecord.db";
    private SQLiteDatabase employeeRecordDB = null;
    //===== timer setting ==============================//
    private Handler handler;
    private TextView textTimer;
    int second, minute, hours, milliscond;
    long milliseconfTime, startTime, TimeBuff, update = 0L,startTimeShiftInMilli;
    String startTimeShift = "----";
    //==== this button control the employee in, out, and record ================//
    Button insertButton, exitButton, employeeRecordBut,deleteRecord;
    boolean wasInsert,setGPSbutton ,in_area;

    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTimer = (TextView) findViewById(R.id.textTimer);
        insertButton = (Button) findViewById(R.id.insert_btn);
        exitButton = (Button) findViewById(R.id.exit_btn);
        employeeRecordBut = (Button) findViewById(R.id.employee_record_but);
        setGPS = (Button) findViewById(R.id.button);
        enableGPS = (Button) findViewById(R.id.button2);
        deleteRecord = (Button)findViewById(R.id.delete_record);
        exitButton.setOnClickListener(this);
        insertButton.setOnClickListener(this);
        deleteRecord.setOnClickListener(this);

        employeeRecordBut.setOnClickListener(this);
        //======= init timer variable ===================================================//
        handler = new Handler();
        milliseconfTime = 0L;startTime = 0L;TimeBuff = 0L;update = 0L;second = 0;minute = 0;hours = 0;
        //== check GPS permission on runtime =============================================//
        if(!runtimePermissions())
            GpsButtons();
        //======== set back Preff to defult ==================================================//
        SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        in_area = sp.getBoolean("inArea", true);
        insertButton.setEnabled(in_area);
        //===== use this thread to enable and unanable the inter and exit button==============//
        handler.postDelayed(setAndUnSetButton, 0);

    }

    //===== the timer function witch will be activate by INSERT button ================//
    private Runnable activeTimer = new Runnable() {
        public void run() {
            milliseconfTime = SystemClock.uptimeMillis() - startTime;
            update = TimeBuff + milliseconfTime;
            second = (int) (update / 1000);
            minute = second / 60;
            hours = minute / 60;
            second = second % 60;
            minute = minute % 60;
            milliscond = (int) (update % 100);

            textTimer.setText(String.format("%02d:%02d:%02d",hours, minute, second));

            handler.postDelayed(this, 0);
        }
    };
    private Runnable setAndUnSetButton = new Runnable() {
        public void run()
        {
            //======= check in loop if the GPS on the current area ==================================//
            SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
            boolean inArea = sp.getBoolean("inArea",false);
            wasInsert = sp.getBoolean("wasInsert",false);


            //=== check:
            // 1 - if we are in the correct area
            // 2 - if the employee was check in
            // 3 - if set GPS button was clicked

            setGPSbutton = sp.getBoolean("set_GPS_button",false);
            // in case the employee wasn't sign in yet he in the current area
            if(inArea && !wasInsert && setGPSbutton)// make the insert button become available
                insertButton.setEnabled(true);
            else if(wasInsert || !inArea)// in case the employee isn't in the area and he wasn't sign in
                insertButton.setEnabled(false);
            exitButton.setEnabled(wasInsert);// if i was sign in, enable the exit button
            //==== i use this thread to listen the buttons status change ======//
            handler.postDelayed(this, 0);
        }
    };

    //===== here is the click control, all the widget that will be clickable will be here ==//
    public void onClick(View v)
    {
        SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        switch (v.getId()) {
            case R.id.insert_btn:
                activateTimer();
                lastBeginTime();
                insertButton.setEnabled(false);
                editor.putBoolean("wasInsert", true);//indicate that insert button was clicked
                editor.commit();
                break;

            case R.id.exit_btn:
                createEvent();
                resetTimer();
                addEventToCalendar();
                editor.putString("beginTime",null);
                editor.putBoolean("wasInsert", false);//indicate that now we can click again on the insert button
                editor.commit();
                if(in_area)// if the employee in the area. now that he click Exit enable back the insert button
                    insertButton.setEnabled(true);
                break;

            case R.id.employee_record_but:

                Intent intent = new Intent(MainActivity.this, EmployeeRecord.class);
                startActivity(intent);
                break;
            case R.id.delete_record:
                deleteRecordData();
                break;
        }
    }
    public void deleteRecordData()
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);

        alertDialogBuilder.setTitle("Delete Record");// set title

        // set dialog message
        alertDialogBuilder
                .setMessage("Do you really want to delete all?")
                .setCancelable(false)
                .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id)
                    {
                        Toast.makeText(MainActivity.this, "All employee record has deleted", Toast.LENGTH_SHORT).show();
                        deleteDatabase(MY_DB_NAME);
                    }
                })
                .setNegativeButton("No",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) { dialog.cancel(); }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();

        alertDialog.show();

    }
    public void activateTimer()
    {
        Toast.makeText(this, "started shift!", Toast.LENGTH_SHORT).show();
        startTime = SystemClock.uptimeMillis();
        handler.postDelayed(activeTimer, 0);
        startTimeShift = getCurrentTime();
    }

    public void lastBeginTime()
    {
        SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putLong("beginTimeInMilli", System.currentTimeMillis());
        editor.putString("beginTime",startTimeShift);
        editor.commit();
    }
    protected void onResume()
    {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        long beginTimeInMilli = sp.getLong("beginTimeInMilli",-1);
        String beginTime = sp.getString("beginTime",null);
        //=== use this intent flag to know if i get back from 'employeeRecord'
        Intent intent = getIntent();
        int sign = intent.getIntExtra("sign",-1);

        if(sign == 10 && beginTime != null)// that mean that i back from 'employeeRecord' and i sign in
        {
            long timerDiff = System.currentTimeMillis() - beginTimeInMilli;
            startTime = SystemClock.uptimeMillis() - timerDiff;
            handler.postDelayed(activeTimer, 0);
            startTimeShift = sp.getString("beginTime",null);
        }
        //===== check if i was in onDestroy and i start the shift,
        //===== by that i can active back the timer after coming back from onDestroy
        boolean wasDestroy = sp.getBoolean("wasDestroy",false);
        if(wasDestroy && beginTime!= null)
        {
            long timerDiff = System.currentTimeMillis() - beginTimeInMilli;
            startTime = SystemClock.uptimeMillis() - timerDiff;
            handler.postDelayed(activeTimer, 0);
            startTimeShift = sp.getString("beginTime",null);
        }

    }
    //=== reset the timer once the employee click Exit ==//
    public void resetTimer()
    {
        textTimer.setText("00:00:00");
        handler.removeCallbacks(activeTimer);
    }
    //==== return the current time on clock HH:MM =================//
    public String getCurrentTime()
    {
        DateFormat now_clock = new SimpleDateFormat("HH:mm");
        now_clock.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curTime = now_clock.format(new Date());
        return curTime;
    }
    //=========== return the current date, YYY-MM-DD================//
    public String getCurrentDate()
    {
        DateFormat now_date = new SimpleDateFormat("yyyy-MM-dd");
        now_date.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curDate = now_date.format(new Date());
        return curDate;
    }

    //==== once the employee click EXIT button he will get here and the shift will be save in the dada sqlite ==//
    public void createEvent()
    {
        createDB();
        String timerCount = textTimer.getText().toString();

        String sql = "INSERT INTO employeeRecord (total, exit, inter, date) " +
                "VALUES ('" + timerCount + "', '" + getCurrentTime() + "', '" + startTimeShift + "', '" + getCurrentDate() + "');";
        employeeRecordDB.execSQL(sql);
        Toast.makeText(this, "shift has been register!", Toast.LENGTH_SHORT).show();
    }

    //== create data bace at fist, and open it in the other time ==================================//
    public void createDB() {
        try {
            employeeRecordDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS employeeRecord (id integer primary key, total VARCHAR, exit VARCHAR, inter VARCHAR, date VARCHAR);";
            employeeRecordDB.execSQL(sql);
        } catch (Exception e) {
            Log.d("debug", "Error Creating Database");
        }
    }

    //===== once the employee exit the shift, he can choose ti sigh in the shift on Calendar ===========//
    public void addEventToCalendar()
    {
        DateFormat year = new SimpleDateFormat("yyyy");
        year.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curYear = year.format(new Date());
        int yearInNum = Integer.parseInt(curYear);

        DateFormat month = new SimpleDateFormat("MM");
        month.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curMonth = month.format(new Date());
        int monthInNum = Integer.parseInt(curMonth);
        monthInNum--;

        DateFormat day = new SimpleDateFormat("dd");
        day.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curDay = day.format(new Date());
        int dayInNum = Integer.parseInt(curDay);


        String startTime = startTimeShift;
        String[] arrOfStr = startTime.split(":", 2);
        int beginHour = Integer.parseInt(arrOfStr[0]);
        int BeginMinutes = Integer.parseInt(arrOfStr[1]);

        String endTimeClock = getCurrentTime();
        arrOfStr = endTimeClock.split(":", 2);
        int endHour = Integer.parseInt(arrOfStr[0]);
        int endMinutes = Integer.parseInt(arrOfStr[1]);


        Calendar beginTime = Calendar.getInstance();
        beginTime.set(yearInNum, monthInNum, dayInNum, beginHour, BeginMinutes);
        Calendar endTime = Calendar.getInstance();
        endTime.set(yearInNum, monthInNum, dayInNum, endHour, endMinutes);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "Work")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, "noamlasry02@gmail.com,noamlasry02@gmail.com");
        startActivity(intent);

    }
    //===== two buttons here, 1- to set the GPS, 2- enable the GPS ============//
    private void GpsButtons()
    {
        setGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("set_GPS_button",true);
                editor.commit();
                Toast.makeText(getApplicationContext(), "Start service GPS", Toast.LENGTH_SHORT).show();
                Intent i =new Intent(getApplicationContext(),GpsService.class);startService(i);

            }
        });
        enableGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                insertButton.setEnabled(false);
                SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("inArea", false);//indicate that insert button was clicked
                editor.putBoolean("set_GPS_button",false);
                editor.commit();
                Toast.makeText(getApplicationContext(), "Stop service GPS", Toast.LENGTH_SHORT).show();
                Intent i = new Intent(getApplicationContext(),GpsService.class);
                stopService(i);

            }
        });
    }
    //====== getting permission on runtime ====================================//
    private boolean runtimePermissions()
    {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);
            return true;
        }
        return false;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                GpsButtons();
            }else { runtimePermissions(); }
        }
    }

    protected void onDestroy()
    {
        super.onDestroy();
//        employeeRecordDB.close();
        SharedPreferences sp = getSharedPreferences("MyPref", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("wasDestroy", true);
        editor.commit();
    }
    public boolean onCreateOptionsMenu(Menu menu)
    {
        super.onCreateOptionsMenu(menu);
        MenuItem menuItem1 = menu.add("Setting");
        MenuItem menuItem2 = menu.add("Exit");

        menuItem1.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(MenuItem item)
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                alertDialogBuilder.setTitle("Work Log Setting");// set title
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + BuildConfig.APPLICATION_ID));
                startActivity(i);


                // create alert dialog
                AlertDialog alertDialog = alertDialogBuilder.create();

                // show it
                alertDialog.show();
                return true;
            }
        });
        menuItem2.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            public boolean onMenuItemClick(MenuItem item)
            {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                        context);

                alertDialogBuilder.setTitle("Exit Work log");// set title

                // set dialog message
                alertDialogBuilder
                        .setMessage("Do you really want to exit the app?")
                        .setCancelable(false)
                        .setPositiveButton("Yes",new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                            public void onClick(DialogInterface dialog, int id)
                            {
                                finishAndRemoveTask();
                                finishAffinity();


                            }
                        })
                        .setNegativeButton("No",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) { dialog.cancel(); }
                        });
                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();
                return true;
            }
        });
        return true;
    }
}

