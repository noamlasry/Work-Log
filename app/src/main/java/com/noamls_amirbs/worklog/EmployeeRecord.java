package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class EmployeeRecord extends AppCompatActivity
{
    public static final String MY_DB_NAME = "employeeRecord.db";
    private SQLiteDatabase employeeRecordDB = null;
    private ArrayList<EmployeeLine> employeeLines;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_record);
        createDB();

        employeeLines = new ArrayList<EmployeeLine>();

        String sql = "SELECT * FROM employeeRecord";
        Cursor cursor = employeeRecordDB.rawQuery(sql, null);


        int idColumn = cursor.getColumnIndex("id");
        int totalColumn = cursor.getColumnIndex("total");
        int exitColumn = cursor.getColumnIndex("exit");
        int interColumn = cursor.getColumnIndex("inter");
        int dateColumn = cursor.getColumnIndex("date");

        if (cursor.moveToFirst()) {
            do {

                String id = cursor.getString(idColumn);
                String total = cursor.getString(totalColumn);
                String exit = cursor.getString(exitColumn);
                String inter = cursor.getString(interColumn);
                String date = cursor.getString(dateColumn);

                EmployeeLine employeeLine = new EmployeeLine(total,exit,inter,date);

                employeeLines.add(new EmployeeLine(employeeLine.getTotal(),employeeLine.getExitClock(),employeeLine.getInterClock(),employeeLine.getDate()));


            } while (cursor.moveToNext());


        } else {

            Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show();

        }

        ScheduleList scheduleList = new ScheduleList(this, employeeLines);

        ListView listView = findViewById(R.id.ListView1ID);
        listView.setAdapter(scheduleList);
    }


    // use to override the original 'back button' that on the device, and get back to the previous activity
    public void onBackPressed()
    {
        Intent intent=new Intent(this,MainActivity.class);
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
}
