package com.jiajiabingcheng.phonelog;

import java.util.HashMap;

class CallRecord {

    CallRecord() {}

    String formattedNumber;
    String number;
    String callType;
    String name;
    long date;
    long duration;


    HashMap<String, Object> toMap() {
        HashMap<String, Object> recordMap = new HashMap<>();
        recordMap.put("formattedNumber", formattedNumber);
        recordMap.put("number", number);
        recordMap.put("callType", callType);
        recordMap.put("name", name);
        recordMap.put("date", date);
        recordMap.put("duration", duration);

        return recordMap;
    }
}
