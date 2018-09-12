package com.example.clyste.jfregistration;

import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Clyste on 4/24/2018.
 */

public class Event {

    private long startTime;
    private long endTime;

    private DateFormat date;
    private Calendar calDate;
    private static SimpleDateFormat dateFormatTime = new SimpleDateFormat("HH:mm", Locale.US);
    private static SimpleDateFormat dateFormatDate = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

    public Event(){

    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime() {
        this.startTime = new Date().getTime();
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime() {
        this.endTime = new Date().getTime();
    }

    public long getDuration(){
        return endTime - startTime;
    }

    public DateFormat getDate() {
        return date;
    }

    public void setDate(DateFormat date) {
        this.date = date;
    }

    public Calendar getCalDate() {
        return calDate;
    }

    public void setCalDate(Calendar calDate) {
        this.calDate = calDate;
    }

}
