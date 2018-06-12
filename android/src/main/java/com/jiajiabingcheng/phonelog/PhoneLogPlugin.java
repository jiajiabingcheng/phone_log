package com.jiajiabingcheng.phonelog;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog;
import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.PluginRegistry;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/**
 * PhoneLogPlugin
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class PhoneLogPlugin implements MethodCallHandler, PluginRegistry.RequestPermissionsResultListener {
    PhoneLogPlugin(Activity activity, ContentResolver contentResolver) {
        this.activity = activity;
        this.contentResolver = contentResolver;
    }

    private Result pendingResult;
    private final Activity activity;
    private final ContentResolver contentResolver;

    public static void registerWith(Registrar registrar) {
        final MethodChannel channel =
                new MethodChannel(registrar.messenger(), "github.com/jiajiabingcheng/phone_log");
        PhoneLogPlugin phoneLogPlugin =
                new PhoneLogPlugin(registrar.activity(), registrar.context().getContentResolver());
        channel.setMethodCallHandler(phoneLogPlugin);
        registrar.addRequestPermissionsResultListener(phoneLogPlugin);
    }

    @Override
    public void onMethodCall(MethodCall call, Result result) {
        String startDate, duration;
        switch (call.method) {
            case "checkPermission":
                result.success(checkPermission());
                break;
            case "requestPermission":
                this.pendingResult = result;
                requestPermission();
                break;
            case "getPhoneLogs":
                startDate = call.argument("startDate");
                duration = call.argument("duration");
                this.pendingResult = result;
                fetchCallRecords(startDate, duration);
                break;
            default:
                result.notImplemented();
        }
    }

    private void requestPermission() {
        Log.i("PhoneLogPlugin",
                "Requesting permission : " + Manifest.permission.READ_CALL_LOG);
        String[] perm = {Manifest.permission.READ_CALL_LOG};
        activity.requestPermissions(perm, 0);
    }

    private boolean checkPermission() {
        Log.i("PhoneLogPlugin", "Checking permission : " +
                Manifest.permission.READ_CALL_LOG);
        return PackageManager.PERMISSION_GRANTED ==
                activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG);
    }

    @Override
    public boolean onRequestPermissionsResult(int requestCode, String[] strings, int[] grantResults) {
        boolean res = false;
        if (requestCode == 0 && grantResults.length > 0) {
            res = grantResults[0] == PackageManager.PERMISSION_GRANTED;
            Log.i("PhoneLogPlugin", "Requesting permission result : " + res);
            pendingResult.success(res);
        }
        return res;
    }

    private static final String[] PROJECTION =
            {
                    CallLog.Calls.CACHED_FORMATTED_NUMBER,
                    CallLog.Calls.CACHED_MATCHED_NUMBER,
                    CallLog.Calls.TYPE,
                    CallLog.Calls.DATE,
                    CallLog.Calls.DURATION,
            };

    @TargetApi(Build.VERSION_CODES.M)
    private void fetchCallRecords(String startDate, String duration) {
        if (activity.checkSelfPermission(Manifest.permission.READ_CALL_LOG)
                == PackageManager.PERMISSION_GRANTED) {
            String selectionCondition = null;
            if (startDate != null) {
                selectionCondition = CallLog.Calls.DATE + "> " + startDate;
            }
            if (duration != null) {
                String durationSelection = CallLog.Calls.DURATION + "> " + duration;
                if (selectionCondition != null) {
                    selectionCondition = selectionCondition + " AND " + durationSelection;
                } else {
                    selectionCondition = durationSelection;
                }
            }
            Cursor cursor = contentResolver.query(CallLog.Calls.CONTENT_URI, PROJECTION,
                    selectionCondition,
                    null, CallLog.Calls.DATE + " DESC");

            try {
                ArrayList<HashMap<String, Object>> records = getCallRecordMaps(cursor);
                pendingResult.success(records);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }

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

        while (cursor != null && cursor.moveToNext()) {
            CallRecord record = new CallRecord();
            record.formattedNumber = cursor.getString(0);
            record.number = cursor.getString(1);
            record.callType = getCallType(cursor.getInt(2));

            Date date = new Date(cursor.getLong(3));
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            record.dateYear = cal.get(Calendar.YEAR);
            record.dateMonth = cal.get(Calendar.MONTH);
            record.dateDay = cal.get(Calendar.DAY_OF_MONTH);
            record.dateHour = cal.get(Calendar.HOUR_OF_DAY);
            record.dateMinute = cal.get(Calendar.MINUTE);
            record.dateSecond = cal.get(Calendar.SECOND);
            record.duration = cursor.getLong(4);

            records.add(record.toMap());
            Log.i("PhoneLogPlugin", record.toString());
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
