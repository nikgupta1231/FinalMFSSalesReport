package com.myapplication.nik.mfssalesreport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Nik on 28-Aug-2017.
 */

public class MainDatabaseFields {
    private String startLoaction;
    private String stopLocaiton;
    private String startTime;
    private String stopTime;

    public MainDatabaseFields() {
    }

    public String getStartLoaction() {
        return startLoaction;
    }

    public void setStartLoaction(String startLoaction) {
        this.startLoaction = startLoaction;
    }

    public String getStopLocaiton() {
        return stopLocaiton;
    }

    public void setStopLocaiton(String stopLocaiton) {
        this.stopLocaiton = stopLocaiton;
    }

//    public Map<String,Object> toMap(){
//        HashMap<String,Object> result = new HashMap<>();
//        result.put("startLocation",startLoaction);
//        return result;
//    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }
}
