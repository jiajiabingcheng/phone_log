package com.example.phonelog;

import java.util.HashMap;

class CallRecord {

    CallRecord(){}

    String formattedNumber, number, callType;
    int durationHour, durationMinute, durationSecond, dateYear, dateMonth, dateDay, dateHour, dateMinute, dateSecond;
    long duration;

    HashMap<String,Object> toMap(){
        HashMap<String,Object> recordMap = new HashMap<>();
        recordMap.put("formattedNumber", formattedNumber);
        recordMap.put("number",number);
        recordMap.put("callType", callType);
        recordMap.put("durationHour",durationHour);
        recordMap.put("durationMinute",durationMinute);
        recordMap.put("durationSecond",durationSecond);
        recordMap.put("dateYear", dateYear);
        recordMap.put("dateMonth", dateMonth);
        recordMap.put("dateDay", dateDay);
        recordMap.put("dateHour",dateHour);
        recordMap.put("dateMinute",dateMinute);
        recordMap.put("dateSecond",dateSecond);
        recordMap.put("duration", duration);

        return recordMap;
    }
}
