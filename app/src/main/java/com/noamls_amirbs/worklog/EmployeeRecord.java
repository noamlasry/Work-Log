package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class EmployeeRecord extends AppCompatActivity
{
    public static final String MY_DB_NAME = "employeeRecord.db";
    private SQLiteDatabase employeeRecordDB = null;
    private ArrayList<EmployeeLine> employeeLines;
    TextView reportTxt,hoursTxt;
    int report = 0,hours,minutes,seconds;
    long hoursInMillisecond = 0,minutesInMilliseconds = 0,secondsInMilliseconds = 0l,update;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_record);

        reportTxt = (TextView)findViewById(R.id.report_txt);
        hoursTxt = (TextView)findViewById(R.id.hours_txt);

        createDB();
        employeeLines = new ArrayList<EmployeeLine>();

        String sql = "SELECT * FROM employeeRecord";
        Cursor cursor = employeeRecordDB.rawQuery(sql, null);


        int idColumn = cursor.getColumnIndex("id");
        int totalColumn = cursor.getColumnIndex("total");
        int exitColumn = cursor.getColumnIndex("exit");
        int interColumn = cursor.getColumnIndex("inter");
        int dateColumn = cursor.getColumnIndex("date");

        if (cursor.moveToFirst())
        {
            do {
                report++;
                String id = cursor.getString(idColumn);
                String total = cursor.getString(totalColumn);
                String exit = cursor.getString(exitColumn);
                String inter = cursor.getString(interColumn);
                String date = cursor.getString(dateColumn);


                calculateHours(total);
                EmployeeLine employeeLine = new EmployeeLine(total,exit,inter,date);

                employeeLines.add(new EmployeeLine(employeeLine.getTotal(),employeeLine.getExitClock(),employeeLine.getInterClock(),employeeLine.getDate()));

            } while (cursor.moveToNext());

        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }

        hoursTxt.setText(String.format("%02d:%02d:%02d",hours, minutes, seconds));
        ScheduleList scheduleList = new ScheduleList(this, employeeLines);


        ListView listView = findViewById(R.id.ListView1ID);
        listView.setAdapter(scheduleList);
        String str = String.valueOf(report);
        reportTxt.setText(str);
    }

    public void calculateHours(String total)
    {
        String[] parts = total.split(":");

        hours = Integer.parseInt(parts[0]);
        minutes = Integer.parseInt(parts[1]);
        seconds = Integer.parseInt(parts[2]);
        hoursInMillisecond +=  getHoursInMilliseconds(hours);
        minutesInMilliseconds += getMinuteInMilliseconds(minutes);
        secondsInMilliseconds += getSecondInMilliseconds(seconds);
        long allMilliseconds = hoursInMillisecond+minutesInMilliseconds+secondsInMilliseconds;

        update =  allMilliseconds;
        seconds = (int) (update / 1000);
        minutes = seconds / 60;
        hours = minutes / 60;
        seconds = seconds % 60;
        minutes = minutes % 60;



    }
    public long getHoursInMilliseconds(int hours)
    {
        long milliseconds = hours * 60 * 60 *1000;
        return milliseconds;
    }
    public long getMinuteInMilliseconds(int minute)
    {
        long milliseconds = minute  * 60 *1000;
        return milliseconds;
    }
    public long getSecondInMilliseconds(int second)
    {
        long milliseconds = second  *1000;
        return milliseconds;
    }


    // use to override the original 'back button' that on the device, and get back to the previous activity
    public void onBackPressed()
    {
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("sign",10);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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
    public void onDestroy() {

        super.onDestroy();
        employeeRecordDB.close();
    }
}
