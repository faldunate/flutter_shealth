package com.flutter.shealth.ShealthPlugin.flutter_samsunghealth;

import android.util.Log;
import android.database.Cursor;
import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import android.app.Activity;
import android.content.Context;
import java.text.SimpleDateFormat;

import org.json.JSONArray;
import org.json.JSONObject;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

import com.samsung.android.sdk.healthdata.HealthData;
import com.samsung.android.sdk.healthdata.HealthConnectionErrorResult;
import com.samsung.android.sdk.healthdata.HealthConstants;
import com.samsung.android.sdk.healthdata.HealthConstants.StepCount;
import com.samsung.android.sdk.healthdata.HealthConstants.Sleep;
import com.samsung.android.sdk.healthdata.HealthDataResolver;
import com.samsung.android.sdk.healthdata.HealthDataResolver.Filter;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadRequest;
import com.samsung.android.sdk.healthdata.HealthDataResolver.ReadResult;
import com.samsung.android.sdk.healthdata.HealthDataService;
import com.samsung.android.sdk.healthdata.HealthDataStore;
import com.samsung.android.sdk.healthdata.HealthPermissionManager;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionKey;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionResult;
import com.samsung.android.sdk.healthdata.HealthPermissionManager.PermissionType;
import com.samsung.android.sdk.healthdata.HealthResultHolder;

/** FlutterSamsunghealthPlugin */
public class FlutterSamsunghealthPlugin implements MethodCallHandler {
  public static final String TAG = "SimpleHealth";
  public static final String DAY_TIME = "day_time";
  public static final String STEP_DAILY_TREND_TYPE = "com.samsung.shealth.step_daily_trend";

  private HealthDataStore mStore;
  private Context context;
  private Activity actContext;
  private HealthDataResolver mResolver;

  FlutterSamsunghealthPlugin(Registrar registrar) {
    context = registrar.context();
    actContext = registrar.activity();
    HealthDataService healthDataService = new HealthDataService();
    try {
      healthDataService.initialize(actContext);
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public static void registerWith(Registrar registrar) {
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "flutter_samsunghealth");
    channel.setMethodCallHandler(new FlutterSamsunghealthPlugin(registrar));
  }

  @Override
  public void onMethodCall(MethodCall call, Result result) {
    if (call.method.equals("connect")) {
      connect(result);
    } else if (call.method.equals("get_data")) {
      readStepCount(call.argument("startTime"), call.argument("startTime"), result);
    } else {
      result.notImplemented();
    }
  }

  public void readStepCount(long startDate, long endDate, Result _result) {
    HealthDataResolver mResolver = new HealthDataResolver(mStore, null);

    Filter filter = Filter.and(Filter.greaterThanEquals("day_time", (long)startDate),
    Filter.eq("source_type", -2));

    HealthDataResolver.ReadRequest request = new ReadRequest.Builder()
          .setDataType(FlutterSamsunghealthPlugin.STEP_DAILY_TREND_TYPE)
          .setSort("day_time", HealthDataResolver.SortOrder.DESC)
          .setFilter(filter)
          .build();

      try {
        mResolver.read(request).setResultListener(new HealthResultHolder.ResultListener<HealthDataResolver.ReadResult>(){

        @Override
        public void onResult(HealthDataResolver.ReadResult result) {

          int totalCount = 0;

          ArrayList<HashMap<String, Object>> stepResponse = new ArrayList<>();
          List<StepBinningData> binningDataList = Collections.emptyList();
          Cursor c = null;
          try {
            c = result.getResultCursor();
            if (c != null) {
              while(c.moveToNext()) {
                long dayTime = c.getLong(c.getColumnIndex("day_time"));
                int stepCount = c.getInt(c.getColumnIndex("count"));
                int distance = c.getInt(c.getColumnIndex("distance"));
                int calorie = c.getInt(c.getColumnIndex("calorie"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                HashMap<String, Object> daySteps = new HashMap<String, Object>();
                daySteps.put("date", dayTime);
                daySteps.put("stepCount", stepCount);
                daySteps.put("distance", distance);
                daySteps.put("calorie", calorie);
                stepResponse.add(daySteps);
              }
            } else {
                Log.d("cursor", "The cursor is null.");
            }
          }

          catch(Exception e) {
            Log.e("message", e.getClass().getName() + " - " + e.getMessage());
          }

          if(_result != null) {
            c.close();
            _result.success(stepResponse);
          }
        }});
    } catch (Exception e) {
      JSONArray stepResponse = new JSONArray();
      Log.e("StepCounterReader", "Getting daily step trend fails.", e);
      _result.success(stepResponse);
    }
  }

  public void connect(Result result) {
    ConnectionListener listener = new ConnectionListener(this, result);
    listener.addReadPermission();
    mStore = new HealthDataStore(actContext, listener);
    mStore.connectService();
  }

  public HealthDataStore getStore() {
    return mStore;
  }

  public Activity getContext() {
    return actContext;
  }

  public static class StepBinningData {
    public String time;
    public final int count;

    public StepBinningData(String time, int count) {
      this.time = time;
      this.count = count;
    }
  }
}
