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
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class PermissionListener implements
    HealthResultHolder.ResultListener<PermissionResult>
{
    private FlutterSamsunghealthPlugin mPlugin;
    private Result mResult;

    private static final String FLUTTER_PLUGIN = "RNSamsungHealth";

    public PermissionListener(FlutterSamsunghealthPlugin plugin, Result result)
    {
        mPlugin = plugin;
        mResult = result;
    }

    @Override
    public void onResult(PermissionResult result) {
        Log.d(FLUTTER_PLUGIN, "Permission callback is received.");
        Map<PermissionKey, Boolean> resultMap = result.getResultMap();

        if (resultMap.containsValue(Boolean.FALSE)) {
            Log.e(FLUTTER_PLUGIN, "NOT CONNECTED YET");
            mResult.error("sHealth", "Permisson canceled", null);
        } else {
            Log.d(FLUTTER_PLUGIN, "COUNT THE STEPS!");
            mResult.success(true);
        }
    }
};