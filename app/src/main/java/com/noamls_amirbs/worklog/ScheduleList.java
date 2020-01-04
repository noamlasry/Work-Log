package com.noamls_amirbs.worklog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;


public class ScheduleList extends ArrayAdapter<EmployeeLine>
{
    ScheduleList (Activity context, ArrayList<EmployeeLine> employeeList)
    {
        super(context, 0, employeeList);
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {

        View listItemView = convertView;
        if(listItemView == null)
            listItemView = LayoutInflater.from(getContext()).inflate(R.layout.line, parent, false);

        EmployeeLine employeeLine = getItem(position);


        TextView nameTextView = listItemView.findViewById(R.id.textView1);
        nameTextView.setText(employeeLine.getInterClock());

        return listItemView;
    }

}
