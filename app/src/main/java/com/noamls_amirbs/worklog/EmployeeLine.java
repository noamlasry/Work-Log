package com.noamls_amirbs.worklog;

import java.util.Date;

public class EmployeeLine
{
    String date,interClock,exitClock,total;
    EmployeeLine(String date,String interClock,String exitClock,String total)
    {
       this.date = date;
       this.interClock = interClock;
       this.exitClock = exitClock;
       this.total = total;
    }

    public void setDate(String date){this.date = date;}
    public String getDate(){return this.date;}

    public void setInterClock(String interClock){this.interClock = interClock;}
    public String getInterClock(){return this.interClock;}

    public void setExitClock(String exitClock){this.exitClock = exitClock;}
    public String getExitClock(){return this.exitClock;}


    public void setTotal(String total){this.total = total;}
    public String getTotal(){return this.total;}





}
