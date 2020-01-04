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

public class MainActivity extends AppCompatActivity implements View.OnClickListener
{
    public static final String MY_DB_NAME = "contacts.db";
    private SQLiteDatabase contactsDB = null;
    //===== timer setting ==============================//
    private Handler handler;
    private TextView textTimer;
    int second,minute,hours,milliscond;
    long milliseconfTime,startTime,TimeBuff,update=0L;
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
                break;

            case R.id.exit_btn:
                Toast.makeText(this, " click!", Toast.LENGTH_SHORT).show();
                createEvent();
                break;

            case R.id.employee_record_but:
                Intent intent = new Intent(MainActivity.this,EmployeeRecord.class);
                startActivity(intent);
                break;

        }

    }

    public void createEvent()
    {
        createDB();

        String str1 = "1";
        String str2 = "2";
        String str3 = "3";
        String str4 = "4";
        
        String sql = "INSERT INTO contacts (total, exit, inter, date) " +
                "VALUES ('" + str1 + "', '" + str2 + "', '" + str3 + "', '" + str4 + "');";
        contactsDB.execSQL(sql);
        Toast.makeText(this, str1 + " was insert!", Toast.LENGTH_SHORT).show();
    }

    public void onResume() {

        super.onResume();

    }

    public void createDB()
    {
        try
        {
            contactsDB = openOrCreateDatabase(MY_DB_NAME, MODE_PRIVATE, null);
            String sql = "CREATE TABLE IF NOT EXISTS contacts (id integer primary key, total VARCHAR, exit VARCHAR, inter VARCHAR, date VARCHAR);";
            contactsDB.execSQL(sql);
        }
        catch (Exception e) { Log.d("debug", "Error Creating Database"); }
    }
}

