package com.noamls_amirbs.worklog;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.CalendarContract;
import android.util.Log;
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

    //==== gps ==========//

    private Button btn_start, btn_stop;
    private TextView textView;
    private BroadcastReceiver broadcastReceiver;

    //==================//

    public static final String MY_DB_NAME = "employeeRecord.db";
    private SQLiteDatabase employeeRecordDB = null;
    //===== timer setting ==============================//
    private Handler handler;
    private TextView textTimer;
    int second, minute, hours, milliscond;
    long milliseconfTime, startTime, TimeBuff, update = 0L;
    String startTimeShift = "----";
    //=================================================//
    Button insertButton, exitButton, employeeRecordBut;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textTimer = (TextView) findViewById(R.id.textTimer);
        insertButton = (Button) findViewById(R.id.insert_btn);
        exitButton = (Button) findViewById(R.id.exit_btn);
        employeeRecordBut = (Button) findViewById(R.id.employee_record_but);
        exitButton.setOnClickListener(this);
        insertButton.setOnClickListener(this);
        employeeRecordBut.setOnClickListener(this);
        //======= init timer variable ===================================================//
        handler = new Handler();
        milliseconfTime = 0L;
        startTime = 0L;
        TimeBuff = 0L;
        update = 0L;
        second = 0;
        minute = 0;
        hours = 0;
        //=============================================================================//
        btn_start = (Button) findViewById(R.id.button);
        btn_stop = (Button) findViewById(R.id.button2);
        textView = (TextView) findViewById(R.id.textView);

        if(!runtime_permissions())
            enable_buttons();

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
            milliscond = (int) (update % 1000);

            textTimer.setText(String.format("%02d:%02d", minute, second));
            handler.postDelayed(this, 0);
        }
    };

    //===== here is the click control, all the widget that will be clickable will be here ==//
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.insert_btn:
                Toast.makeText(this, "started shift!", Toast.LENGTH_SHORT).show();
                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(activeTimer, 0);
                startTimeShift = getCurrentTime();
                break;

            case R.id.exit_btn:
                createEvent();
                resetTimer();
                addEventToCalendar();
                break;

            case R.id.employee_record_but:
                Intent intent = new Intent(MainActivity.this, EmployeeRecord.class);
                startActivity(intent);
                break;

        }

    }

    //=== reset the timer once the employee click Exit ==//
    public void resetTimer() {
        textTimer.setText("00:00");
        handler.removeCallbacks(activeTimer);
    }

    public String getCurrentTime() {
        DateFormat now_clock = new SimpleDateFormat("HH:mm");
        now_clock.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curTime = now_clock.format(new Date());
        return curTime;
    }

    public String getCurrentDate() {
        DateFormat now_date = new SimpleDateFormat("yyyy-MM-dd");
        now_date.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curDate = now_date.format(new Date());
        return curDate;
    }


    public void createEvent() {
        createDB();
        String timerCount = textTimer.getText().toString();

        String sql = "INSERT INTO employeeRecord (total, exit, inter, date) " +
                "VALUES ('" + timerCount + "', '" + getCurrentTime() + "', '" + startTimeShift + "', '" + getCurrentDate() + "');";
        employeeRecordDB.execSQL(sql);
        Toast.makeText(this, "shift has been register!", Toast.LENGTH_SHORT).show();
    }


    public void createDB() {
        try {
            employeeRecordDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS employeeRecord (id integer primary key, total VARCHAR, exit VARCHAR, inter VARCHAR, date VARCHAR);";
            employeeRecordDB.execSQL(sql);
        } catch (Exception e) {
            Log.d("debug", "Error Creating Database");
        }
    }


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

        Log.d("debug","date "+arrOfStr[0]+"---"+arrOfStr[1]);

        Calendar beginTime = Calendar.getInstance();
        beginTime.set(yearInNum, monthInNum, dayInNum, beginHour, BeginMinutes);
        Calendar endTime = Calendar.getInstance();
        endTime.set(yearInNum, monthInNum, dayInNum, endHour, endMinutes);
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(CalendarContract.Events.CONTENT_URI)
                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, beginTime.getTimeInMillis())
                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime.getTimeInMillis())
                .putExtra(CalendarContract.Events.TITLE, "Work")
                .putExtra(CalendarContract.Events.DESCRIPTION, "Group class")
                .putExtra(CalendarContract.Events.EVENT_LOCATION, "The gym")
                .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
                .putExtra(Intent.EXTRA_EMAIL, "noamlasry02@gmail.com,noamlasry02@gmail.com");
        startActivity(intent);

    }
    private void enable_buttons()
    {

        btn_start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i =new Intent(getApplicationContext(),GpsService.class);
                startService(i);
            }
        });

        btn_stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent i = new Intent(getApplicationContext(),GpsService.class);
                stopService(i);

            }
        });

    }

    private boolean runtime_permissions()
    {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enable_buttons();
            }else {
                runtime_permissions();
            }
        }
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        if(broadcastReceiver == null){
            broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    textView.append("\n" +intent.getExtras().get("coordinates"));

                }
            };
        }
        registerReceiver(broadcastReceiver,new IntentFilter("location_update"));
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if(broadcastReceiver != null)
            unregisterReceiver(broadcastReceiver);
//        employeeRecordDB.close();
    }
}

