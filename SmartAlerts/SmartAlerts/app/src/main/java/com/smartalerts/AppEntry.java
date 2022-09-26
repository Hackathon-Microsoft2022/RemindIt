package com.smartalerts;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.libraries.places.api.Places;

public class AppEntry extends Application {

    private final static String TAG = "TAG=>CityTeensApp";
    public static Application instance;
    public static int REQUEST_CODE_LOCATION_PERMISSION = 103;
    public static int REQUEST_CODE_GPS_PERMISSION = 105;

    public static synchronized Application getInstance() {
        return instance;
    }

    public static void printError(String TAG, Exception e) {
        try {
            if (e == null || e.getMessage() == null || e.getMessage().equals("java.lang.NullPointerException")) {
                Log.d(TAG + " Error in: ", "No Msg");
            } else {
                Log.d(TAG + " Error in: ", e.getMessage());
            }
            e.printStackTrace();
        } catch (Exception ex) {
            Log.d(TAG + " Error in: ", ex.toString());
        }
    }

    public static boolean checkLocationPermission(Activity activity) {

        boolean isPermissionGranted = false;
        if (ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission(activity);
        } else {
            isPermissionGranted = true;
        }
        return isPermissionGranted;
    }

    private static void requestLocationPermission(final Activity activity) {

            ActivityCompat.requestPermissions(activity,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            android.Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    REQUEST_CODE_LOCATION_PERMISSION
            );
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            instance = this;
            String apiKey = getString(R.string.google_maps_key);;
            if (!Places.isInitialized()) Places.initialize(getApplicationContext(), apiKey);
        }catch (Exception e){e.printStackTrace();}
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
    }


}
