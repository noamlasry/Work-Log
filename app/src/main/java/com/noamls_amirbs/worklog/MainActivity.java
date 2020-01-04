package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;


public class MainActivity extends AppCompatActivity implements View.OnClickListener
{

    public static final String MY_DB_NAME = "employeeRecord.db";
    private SQLiteDatabase employeeRecordDB = null;
    //===== timer setting ==============================//
    private Handler handler;
    private TextView textTimer;
    int second,minute,hours,milliscond;
    long milliseconfTime,startTime,TimeBuff,update=0L;
    String startTimeShift = "----";
    //=================================================//
    Button insertButton,exitButton,employeeRecordBut;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        textTimer = (TextView)findViewById(R.id.textTimer);
        insertButton = (Button)findViewById(R.id.insert_btn);
        exitButton = (Button)findViewById(R.id.exit_btn);
        employeeRecordBut = (Button)findViewById(R.id.employee_record_but);
        exitButton.setOnClickListener(this);
        insertButton.setOnClickListener(this);
        employeeRecordBut.setOnClickListener(this);
        //======= init timer variable ===================================================//
        handler = new Handler();
        milliseconfTime = 0L ;
        startTime = 0L ;TimeBuff = 0L ;update = 0L ;second = 0 ;minute = 0 ;hours = 0 ;
        //=============================================================================//

    }
    //===== the timer function witch will be activate by INSERT button ================//
    private Runnable activeTimer = new Runnable()
    {
        public void run()
        {
            milliseconfTime = SystemClock.uptimeMillis() - startTime;
            update = TimeBuff + milliseconfTime;
            second = (int) (update / 1000);
            minute = second / 60;
            hours = minute / 60;
            second = second % 60;
            milliscond = (int)(update % 1000);

            textTimer.setText(String.format("%02d:%02d",minute,second));
            handler.postDelayed(this, 0);
        }
    };

    //===== here is the click control, all the widget that will be clickable will be here ==//
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.insert_btn:
                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(activeTimer,0);
                startTimeShift = getKnowTime();
                break;

            case R.id.exit_btn:
                createEvent();
                resetTimer();
                break;

            case R.id.employee_record_but:
                Intent intent = new Intent(MainActivity.this,EmployeeRecord.class);
                startActivity(intent);
                break;

        }

    }

    public void resetTimer()
    {
        textTimer.setText("00:00");
        handler.removeCallbacks(activeTimer);
    }

    public String getKnowTime()
    {
        DateFormat now_clock = new SimpleDateFormat("HH:mm");
        now_clock.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curTime = now_clock.format(new Date());
        return curTime;
    }

    public String getKnowDate()
    {
        DateFormat now_date = new SimpleDateFormat("yyyy-MM-dd");
        now_date.setTimeZone(TimeZone.getTimeZone("Asia/Jerusalem"));
        String curDate = now_date.format(new Date());
        return curDate;
    }


    public void createEvent()
    {
        createDB();

        String timerCount = textTimer.getText().toString();


        String sql = "INSERT INTO employeeRecord (total, exit, inter, date) " +
                "VALUES ('" + timerCount + "', '" + getKnowTime() + "', '" + startTimeShift + "', '" + getKnowDate() + "');";
        employeeRecordDB.execSQL(sql);
        Toast.makeText(this, "insert!", Toast.LENGTH_SHORT).show();
    }

    public void onResume() {

        super.onResume();

    }

    public void onDestroy() {

        super.onDestroy();
        employeeRecordDB.close();
    }

    public void createDB()
    {
        try
        {
            employeeRecordDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS employeeRecord (id integer primary key, total VARCHAR, exit VARCHAR, inter VARCHAR, date VARCHAR);";
            employeeRecordDB.execSQL(sql);
        }
        catch (Exception e) { Log.d("debug", "Error Creating Database"); }
    }
}

