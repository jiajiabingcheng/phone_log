package com.jiajiabingcheng.phonelog;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

/**
 * PhoneLogPlugin
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PhoneLogPlugin implements MethodCallHandler,
        PluginRegistry.RequestPermissionsResultListener {
    private final Registrar registrar;
    private Result pendingResult;

    private PhoneLogPlugin(Registrar registrar) {
        this.registrar = registrar;
    }

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel =
                new MethodChannel(registrar.messenger(), "github.com/jiajiabingcheng/phone_log");
        PhoneLogPlugin phoneLogPlugin = new PhoneLogPlugin(registrar);
        channel.setMethodCallHandler(phoneLogPlugin);
        registrar.addRequestPermissionsResultListener(phoneLogPlugin);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        if (pendingResult != null) {
            pendingResult.error("multiple_requests", "Cancelled by a second request.", null);
            pendingResult = null;
        }
        pendingResult = result;
        switch (call.method) {
            case "checkPermission":
                pendingResult.success(checkPermission());
                pendingResult = null;
                break;
            case "requestPermission":
                requestPermission();
                break;
            case "getPhoneLogs":                
                fetchCallRecords();
                break;
            default:
                result.notImplemented();
        }
    }

    private void requestPermission() {
        Log.i("PhoneLogPlugin", "Requesting permission : " + Manifest.permission.READ_CALL_LOG);
        String[] perm = {Manifest.permission.READ_CALL_LOG};
        registrar.activity().requestPermissions(perm, 0);
    }

    private boolean checkPermission() {
        Log.i("PhoneLogPlugin", "Checking permission : " + Manifest.permission.READ_CALL_LOG);
        return PackageManager.PERMISSION_GRANTED
                == registrar.activity().checkSelfPermission(Manifest.permission.READ_CALL_LOG);
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode,
                                              String[] strings,
                                              int[] grantResults) {
        boolean res = false;
        if (requestCode == 0 && grantResults.length > 0) {
            res = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            pendingResult.success(res);
            pendingResult = null;
        }
        return res;
    }

    private static final String[] PROJECTION =
            {CallLog.Calls.CACHED_FORMATTED_NUMBER,
                    CallLog.Calls.CACHED_MATCHED_NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,};

    @TargetApi(Build.VERSION_CODES.M)
    private void fetchCallRecords() {
        if (registrar.activity().checkSelfPermission(Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            
            Cursor cursor = registrar.context().getContentResolver().query(
                    CallLog.Calls.CONTENT_URI, null, null, null, null);

            try {
                ArrayList<HashMap<String, Object>> records = getCallRecordMaps(cursor);
                pendingResult.success(records);
                pendingResult = null;
            } catch (Exception e) {
                Log.e("PhoneLog", "Error on fetching call record" + e);
                pendingResult.error("PhoneLog", e.getMessage(), null);
                pendingResult = null;
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

        } else {
            pendingResult.error("PhoneLog", "Permission is not granted", null);
            pendingResult = null;
        }
    }


    /**
     * Builds the list of call record maps from the cursor
     *
     * @param cursor
     * @return the list of maps
     */
    private ArrayList<HashMap<String, Object>> getCallRecordMaps(Cursor cursor) {
        ArrayList<HashMap<String, Object>> records = new ArrayList<>();

        int number = cursor.getColumnIndex( CallLog.Calls.NUMBER ); 
        int type = cursor.getColumnIndex( CallLog.Calls.TYPE );
        int date = cursor.getColumnIndex( CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex( CallLog.Calls.DURATION);

        while (cursor != null && cursor.moveToNext()) {
            CallRecord record = new CallRecord();

            String phNumber = cursor.getString( number );
            String callType = cursor.getString( type );
            String callDate = cursor.getString( date );
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString( duration );
            
            record.number = phNumber;
            record.callType = getCallType(Integer.parseInt( callType ));
           
            Calendar cal = Calendar.getInstance();
            cal.setTime(callDayTime);
            record.dateYear = cal.get(Calendar.YEAR);
            record.dateMonth = cal.get(Calendar.MONTH);
            record.dateDay = cal.get(Calendar.DAY_OF_MONTH);
            record.dateHour = cal.get(Calendar.HOUR_OF_DAY);
            record.dateMinute = cal.get(Calendar.MINUTE);
            record.dateSecond = cal.get(Calendar.SECOND);
            record.duration = cursor.getLong(4);

            records.add(record.toMap());
        }
        return records;
    }

    private String getCallType(int anInt) {
        switch (anInt) {
            case CallLog.Calls.INCOMING_TYPE:
                return "INCOMING_TYPE";
            case CallLog.Calls.OUTGOING_TYPE:
                return "OUTGOING_TYPE";
            case CallLog.Calls.MISSED_TYPE:
                return "MISSED_TYPE";
            default:
                break;
        }
        return null;
    }
}
