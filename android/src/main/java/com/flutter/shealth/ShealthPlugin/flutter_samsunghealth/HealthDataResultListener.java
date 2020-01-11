package com.flutter.shealth.ShealthPlugin.flutter_samsunghealth;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthDataObserver;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthDevice;
import com.samsung.android.sdk.healthdata.HealthDeviceManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Date;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class HealthDataResultListener implements
    HealthResultHolder.ResultListener<ReadResult>
{
    private static final String FLUTTER_PLUGIN = "HealthListenerTAG";

    private Result mResult;
    private FlutterSamsunghealthPlugin mPlugin;

    public static final String[] TIME_COLUMNS = {"day_time","start_time","end_time"};

    public HealthDataResultListener(FlutterSamsunghealthPlugin plugin, Result result)
    {
        mResult = result;
        mPlugin = plugin;
    }

    private HashMap getDeviceInfo(String uuid)
    {
        HashMap<String, String> map = new HashMap<>();
        HealthDeviceManager deviceManager = new HealthDeviceManager(mPlugin.getStore());
        HealthDevice device = deviceManager.getDeviceByUuid(uuid);

        String deviceName = device == null ? null : device.getCustomName();
        String deviceManufacturer = device == null ? null : device.getManufacturer();
        String deviceModel = device == null ? null : device.getModel();
        Integer deviceGroup = device == null ? HealthDevice.GROUP_UNKNOWN : device.getGroup();

        String groupName = "";

        if (deviceName == null) {
            deviceName = "";
        }

        if (deviceManufacturer == null) {
            deviceManufacturer = "";
        }

        if (deviceModel == null) {
            deviceModel = "";
        }

        switch(deviceGroup){
            case HealthDevice.GROUP_MOBILE:
                groupName = "mobileDevice";
                break;
            case HealthDevice.GROUP_EXTERNAL:
                groupName = "peripheral";
                break;
            case HealthDevice.GROUP_COMPANION:
                groupName = "wearable";
                break;
            case HealthDevice.GROUP_UNKNOWN:
                groupName = "unknown";
                break;
        }

        Log.d(FLUTTER_PLUGIN, "Device: " + uuid + " Name: " + deviceName + " Model: " + deviceModel + " Group: " + groupName);

        map.put("name", deviceName);
        map.put("manufacturer", deviceManufacturer);
        map.put("model", deviceModel);
        map.put("group", groupName);
        map.put("uuid", uuid);
        return map;
    }

    @Override
    public void onResult(ReadResult result) {
        Map<String, ArrayList<HashMap<String, Object>>> devices;

        Cursor c = null;

        try {
            c = result.getResultCursor();

            Log.d("getResultCursorgetResultCursor ", result.getResultCursor().toString());

            if (c.moveToFirst()) {
                Log.d(FLUTTER_PLUGIN, "Column Names" + Arrays.toString(c.getColumnNames()));

                long r = 0;
                do {
                    int col_uuid = c.getColumnIndex(HealthConstants.Common.DEVICE_UUID);
                    String uuid = c.getString(col_uuid);
                    Log.d(FLUTTER_PLUGIN, "UUUUUIDDD" + uuid);
                    ArrayList<HashMap<String, Object>> arrays = new ArrayList<HashMap<String, Object>>();
                    if (!devices.containsKey(uuid)) {
                        devices.put(uuid, arrays);
                    }
                    ArrayList resultSet = devices.get(uuid);
                    HashMap<String, Object> map = new HashMap<>();

                    for (int col = 0; col < c.getColumnCount(); col++) {
                        if (col == col_uuid) continue;

                        String key = c.getColumnName(col);
                        if (key == HealthConstants.Common.DEVICE_UUID) continue;

                        int type = c.getType(col);
                        if (Arrays.asList(TIME_COLUMNS).contains(key)) {
                            type = Cursor.FIELD_TYPE_FLOAT;
                        }

                        switch (type)
                        {
                        case Cursor.FIELD_TYPE_BLOB:
                            //
                            break;
                        case Cursor.FIELD_TYPE_FLOAT:
                            map.put(key, c.getDouble(col));
                            break;
                        case Cursor.FIELD_TYPE_INTEGER:
                            map.put(key, c.getInt(col));
                            break;
                        case Cursor.FIELD_TYPE_NULL:
                            //
                            break;
                        case Cursor.FIELD_TYPE_STRING:
                        default:
                            map.put(key, c.getString(col));
                        }
                    }
                    resultSet.push(map);
                    r++;
                } while (c.moveToNext());

                Log.d(FLUTTER_PLUGIN, "Found rows " + Long.toString(r));
            } else {
                Log.d(FLUTTER_PLUGIN, "The cursor is null.");
            }
        }
        catch(Exception e) {
            Log.e(FLUTTER_PLUGIN, e.getClass().getName() + " - " + e.getMessage());
            mResult.error(FLUTTER_PLUGIN, e.getClass().getName() + " - " + e.getMessage(), e.getMessage());
        }
        finally {
            if (c != null) {
                c.close();
            }
        }

        // ArrayList<HashMap<String, Object>> results;
        // for(Map.Entry<String, Object> entry: devices.entrySet()) {
        //     HashMap<String, Object> map = new HashMap<>();
        //     map.put("source", getDeviceInfo(entry.getKey()));
        //     map.put("data", entry.getValue());
        //     results.push(map);
        // }
        // Log.d("resultsresultsresults  ",results.toString());
        mResult.success(true);
    }
}