package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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

        ScheduleList flavorAdapter = new ScheduleList(this, employeeLines);


        ListView listView = findViewById(R.id.ListView1ID);
        listView.setAdapter(flavorAdapter);
    }
}
