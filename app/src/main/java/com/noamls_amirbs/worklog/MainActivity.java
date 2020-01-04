package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

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

    Button insertButton,exitButton;
    private Handler handler;
    private TextView textTimer;
    int second,minute,hours,milliscond;
    long milliseconfTime,startTime,TimeBuff,update=0L;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textTimer = (TextView)findViewById(R.id.textTimer);
        insertButton = (Button)findViewById(R.id.insert_btn);
        insertButton.setOnClickListener(this);
        handler = new Handler();
        milliseconfTime = 0L ;
        startTime = 0L ;
        TimeBuff = 0L ;
        update = 0L ;
        second = 0 ;
        minute = 0 ;
        hours = 0 ;
        
    }
    private Runnable updateTimerMethod = new Runnable()
    {

        public void run() { // game timer , active than the screen on
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


    @Override
    public void onClick(View v)
    {
        switch (v.getId())
        {
            case R.id.insert_btn:
                Log.d("debug","debug");
                Toast.makeText(MainActivity.this,"dsdsd",Toast.LENGTH_SHORT).show();

                startTime = SystemClock.uptimeMillis();
                handler.postDelayed(updateTimerMethod,0);
                break;

        }

    }

}

