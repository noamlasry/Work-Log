package com.noamls_amirbs.worklog;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.Vector;

public class EmployeeRecord extends AppCompatActivity
{
    public static final String MY_DB_NAME = "employeeRecord.db";
    public static final String MY_DB_NAME_2 = "employeeRecord";
    private static final String ID = "id";
    private SQLiteDatabase employeeRecordDB = null;
    private ArrayList<EmployeeLine> employeeLines;
    TextView reportTxt,hoursTxt;
    int report = 0,hours,minutes,seconds;
    long hoursInMillisecond = 0,minutesInMilliseconds = 0,secondsInMilliseconds = 0l,update;
    Vector<String> vector = new Vector();


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_record);

        reportTxt = (TextView)findViewById(R.id.report_txt);
        hoursTxt = (TextView)findViewById(R.id.hours_txt);

        createDataList();//withdraw the data from sqlite and display it in list view
        //========== this three bottom lines here to enable the list to clickable=========================//
        ScheduleList scheduleList = new ScheduleList(this, employeeLines);
        ListView listView = findViewById(R.id.ListView1ID);
        listView.setAdapter(scheduleList);
        //===== one short click on single row on the list, and the user can remove the current item ====//
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { removeItem(position);
            }
        });

    }
    //========= create the list from sqlite database =========//
    public void createDataList()
    {
        createDB();// create database, if exist just open it
        employeeLines = new ArrayList<EmployeeLine>();// hold the Employee list object

        //=== going through the data and display it in list object =========================================================//
        String sql = "SELECT * FROM employeeRecord";
        Cursor cursor = employeeRecordDB.rawQuery(sql, null);
        int idColumn = cursor.getColumnIndex("id");int totalColumn = cursor.getColumnIndex("total");
        int exitColumn = cursor.getColumnIndex("exit");int interColumn = cursor.getColumnIndex("inter");
        int dateColumn = cursor.getColumnIndex("date");

        if (cursor.moveToFirst())
        {
            do {
                report++;//add report for any iteration that will come up
                String id = cursor.getString(idColumn); String total = cursor.getString(totalColumn);
                String exit = cursor.getString(exitColumn); String inter = cursor.getString(interColumn);
                String date = cursor.getString(dateColumn);

                vector.addElement(id);// i create this vector to enable to remove immediately row from list view
                calculateHours(total);// this function will calculate the total time shifts
                //== create a single Employee and added him to the list view =============================================================================//
                EmployeeLine employeeLine = new EmployeeLine(total,exit,inter,date);
                employeeLines.add(new EmployeeLine(employeeLine.getTotal(),employeeLine.getExitClock(),employeeLine.getInterClock(),employeeLine.getDate()));

            } while (cursor.moveToNext());

        } else { Toast.makeText(this, "No Results to Show", Toast.LENGTH_SHORT).show(); }

        hoursTxt.setText(String.format("%02d:%02d:%02d",hours, minutes, seconds));// display the total hours
        String str = String.valueOf(report); reportTxt.setText(str);// display num of report
        //==== create list and display it ======================================//
        ScheduleList scheduleList = new ScheduleList(this, employeeLines);
        ListView listView = findViewById(R.id.ListView1ID);
        listView.setAdapter(scheduleList);
    }
    //==== this function will delete single row from sqlite database =========//
    public void deleteName(int position)
    {
        String id_to_delete = vector.get(position);
        String query = "DELETE FROM " + MY_DB_NAME_2 + " WHERE "
                + ID + " = '" + id_to_delete + "'";
        vector.remove(position);
        employeeRecordDB.execSQL(query);
    }
    // === remove a single row ====================================================================//
    private void removeItem(final int position)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remove Item");
        builder.setMessage("Do you really want to remove it ?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                //===== this two line will delete the current row from the list view and display the new one ======//
                employeeLines.remove(position);
                deleteName(position);// call the function that will delete that row from the saving data
                vector.removeAllElements();// reset back the vector to get ready to set a new list
                //====== recet back the timer variable =============================================//
                report = 0;hoursInMillisecond = 0;minutesInMilliseconds = 0;secondsInMilliseconds = 0;
                hours = minutes = seconds = 0; update = 0;
                //==================================================================================//
                createDataList();//set the new list after delete and display it
                Toast.makeText(EmployeeRecord.this, "Item REMOVED!", Toast.LENGTH_LONG).show();
            }
        });
        builder.setNegativeButton("NO", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                Toast.makeText(EmployeeRecord.this, "Item NOT removed!", Toast.LENGTH_LONG).show();
            }
        });
        builder.show();
    }
    //=== calculate the total shift time ===========================================================//
    public void calculateHours(String total)
    {
        String[] parts = total.split(":");
        hours = Integer.parseInt(parts[0]);minutes = Integer.parseInt(parts[1]); seconds = Integer.parseInt(parts[2]);
        hoursInMillisecond +=  getHoursInMilliseconds(hours); minutesInMilliseconds += getMinuteInMilliseconds(minutes);
        secondsInMilliseconds += getSecondInMilliseconds(seconds);
        long allMilliseconds = hoursInMillisecond+minutesInMilliseconds+secondsInMilliseconds;

        update =  allMilliseconds;seconds = (int) (update / 1000);
        minutes = seconds / 60;hours = minutes / 60;
        seconds = seconds % 60;minutes = minutes % 60;
    }
    //=== get hour and return it in milliseconds =====//
    public long getHoursInMilliseconds(int hours)
    {
        long milliseconds = hours * 60 * 60 *1000;
        return milliseconds;
    }
    //=== get minute and return it in milliseconds =====//
    public long getMinuteInMilliseconds(int minute)
    {
        long milliseconds = minute  * 60 *1000;
        return milliseconds;
    }
    //=== get second and return it in milliseconds =====//
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
    public void onDestroy() { super.onDestroy();employeeRecordDB.close(); }
}
