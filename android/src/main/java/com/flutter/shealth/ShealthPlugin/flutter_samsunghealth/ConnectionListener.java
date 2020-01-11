package com.flutter.shealth.ShealthPlugin.flutter_samsunghealth;

import android.database.Cursor;
import android.util.Log;
import android.widget.Toast;
import android.app.AlertDialog;
import android.content.DialogInterface;

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

public class ConnectionListener implements
    HealthDataStore.ConnectionListener
{
    private Result mResult;
    private FlutterSamsunghealthPlugin mPlugin;
    private HealthConnectionErrorResult mConnError;

    private static final String FLUTTER_MODULE = "RNSamsungHealth";

    public Set<PermissionKey> mKeySet;

    public ConnectionListener(FlutterSamsunghealthPlugin plugin, Result result)
    {
        mPlugin = plugin;
        mResult = result;
        mKeySet = new HashSet<PermissionKey>();
    }

    public void addReadPermission()
    {
        // mKeySet.add(new PermissionKey(name, PermissionType.READ));
        // mKeySet.add(new PermissionKey(SamsungHealthModule.STEP_DAILY_TREND_TYPE, PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.health.step_count", PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.shealth.step_daily_trend", PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.health.weight", PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.health.height", PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.health.heart_rate", PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.health.blood_pressure", PermissionType.READ));
        mKeySet.add(new PermissionKey("com.samsung.health.sleep", PermissionType.READ));

    }

    @Override
    public void onConnected() {
        if (mKeySet.isEmpty()) {
            Log.e(FLUTTER_MODULE, "Permission is empty");
            mResult.error(FLUTTER_MODULE, "Permission is empty", null);
            return;
        }

        Log.d(FLUTTER_MODULE, "Health data service is connected.");
        HealthPermissionManager pmsManager = new HealthPermissionManager(mPlugin.getStore());

        try {
            Map<PermissionKey, Boolean> resultMap = pmsManager.isPermissionAcquired(mKeySet);

            if (resultMap.containsValue(Boolean.FALSE)) {
                pmsManager.requestPermissions(mKeySet, mPlugin.getContext()).setResultListener(
                    new PermissionListener(mPlugin, mResult)
                );
            } else {
                Log.d(FLUTTER_MODULE, "COUNT THE STEPS!");
                mResult.success(true);
            }
        } catch (Exception e) {
            Log.e(FLUTTER_MODULE, e.getClass().getName() + " - " + e.getMessage());
            mResult.error(FLUTTER_MODULE,"Permission setting fails", e.getMessage());
        }
    }

    @Override
    public void onConnectionFailed(HealthConnectionErrorResult error) {
        AlertDialog.Builder alert = new AlertDialog.Builder(mPlugin.getContext());
        mConnError = error;
        String message = "Connection with Samsung Health is not available";

        if (error.hasResolution()) {
          switch(error.getErrorCode()) {
            case HealthConnectionErrorResult.PLATFORM_NOT_INSTALLED:
                message = "Please install Samsung Health";
                break;
            case HealthConnectionErrorResult.OLD_VERSION_PLATFORM:
                message = "Please upgrade Samsung Health";
                break;
            case HealthConnectionErrorResult.PLATFORM_DISABLED:
                message = "Please enable Samsung Health";
                break;
            case HealthConnectionErrorResult.USER_AGREEMENT_NEEDED:
                message = "Please agree with Samsung Health policy";
                break;
            default:
                message = "Please make Samsung Health available";
                break;
            }
        }

        alert.setMessage(message);

        alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                if (mConnError.hasResolution()) {
                    mConnError.resolve(mPlugin.getContext());
                }
            }
        });

        if (error.hasResolution()) {
            alert.setNegativeButton("Cancel", null);
        }

        alert.show();
        //mErrorCallback.invoke(message);
    }

    @Override
    public void onDisconnected() {
        Log.d(FLUTTER_MODULE, "Health data service is disconnected.");
        //mErrorCallback.invoke("Health data service is disconnected.");
    }
};