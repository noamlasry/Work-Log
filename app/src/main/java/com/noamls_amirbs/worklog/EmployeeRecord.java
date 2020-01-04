package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import java.util.ArrayList;

public class EmployeeRecord extends AppCompatActivity
{
    private ArrayList<EmployeeLine> employeeLines;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_record);

        employeeLines = new ArrayList<EmployeeLine>();
        employeeLines.add(new EmployeeLine("24/7/19","8:00","20:00","12"));

        ScheduleList scheduleList = new ScheduleList(this, employeeLines);


        ListView listView = findViewById(R.id.ListView1ID);
        listView.setAdapter(scheduleList);
    }
    // use to override the original 'back button' that on the device, and get back to the previous activity
    public void onBackPressed()
    {
        Intent intent=new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
