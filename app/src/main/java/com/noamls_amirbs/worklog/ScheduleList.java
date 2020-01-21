package com.noamls_amirbs.worklog;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import java.util.ArrayList;

// use to adapt employee list
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


        TextView totalText = listItemView.findViewById(R.id.total_txt);
        TextView exitText = listItemView.findViewById(R.id.exit_txt);
        TextView interText = listItemView.findViewById(R.id.inter_txt);
        TextView dateText = listItemView.findViewById(R.id.date_txt);

        totalText.setText(employeeLine.getTotal());
        exitText.setText(employeeLine.getExitClock());
        interText.setText(employeeLine.getInterClock());
        dateText.setText(employeeLine.getDate());

        return listItemView;
    }

}
