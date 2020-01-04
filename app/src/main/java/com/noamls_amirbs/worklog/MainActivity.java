package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
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
        employeeRecordBut = (Button)findViewById(R.id.employee_record_but);
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
            case R.id.employee_record_but:
                Intent intent=new Intent(MainActivity.this,EmployeeRecord.class);
                startActivity(intent);
                finish();
                break;

        }

    }

}

