package com.smartalerts.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.IntentSender;
import android.location.Location;
import android.location.LocationManager;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.smartalerts.AppEntry;
import com.smartalerts.AsyncResponse;

public class GpsUtils {

    private static final long INTERVAL = 1000 * 30; // 30 sec
    private static final long FASTEST_INTERVAL = 1000 * 15;// 15 sec
    public static boolean preventLocationCheck = false;
    public static boolean preventedCurrentLocation = false;
    private static final String LOG_TAG = "LOG_TAG=>" + GpsUtils.class.getSimpleName();
    private final Context context;
    private SettingsClient mSettingsClient;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationManager locationManager;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback mLocationCallback;

    public GpsUtils(Context context, int numUpdates) {

        this.context = context;

        try {

            mSettingsClient = LocationServices.getSettingsClient(context);
            locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            mLocationRequest = new LocationRequest();

            mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            mLocationRequest.setInterval(INTERVAL);
            mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

            if (numUpdates > 0) {
                mLocationRequest.setNumUpdates(numUpdates);
            }

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(mLocationRequest);
            mLocationSettingsRequest = builder.build();
            builder.setAlwaysShow(true); //this is the key ingredient

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean isProviderEnabled(String provider) {
        try {
            if (locationManager.isProviderEnabled(provider)) {
                Log.d(LOG_TAG, provider + " is enable");
                return true;
            }
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error : " + e.getMessage());
        }
        return false;
    }


    public void turnGPSOn(GpsStatusListener gpsStatusListener) {

        try {

            if (isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Log.d(LOG_TAG, "gps is on already");
                GpsStatusListener.updateGpsStatus(gpsStatusListener, true);
                return;
            }

            Log.d(LOG_TAG, "gps is not enable, showing popup");
            GpsStatusListener.updateGpsStatus(gpsStatusListener, false);

            mSettingsClient
                    .checkLocationSettings(mLocationSettingsRequest)
                    .addOnCompleteListener(task -> {
                        Log.d(LOG_TAG, "called: onComplete ");
                    })
                    .addOnSuccessListener((Activity) context, locationSettingsResponse -> {//  GPS is already enable, callback GPS status through listener
                        Log.d(LOG_TAG, "called: onSuccess ");
                        GpsStatusListener.updateGpsStatus(gpsStatusListener, true);
                    })
                    .addOnFailureListener((Activity) context, e -> {
                        try {
                            Log.d(LOG_TAG, "called: onFailure");

                            Log.d(LOG_TAG, "Error : " + e.getMessage());

                            int statusCode = ((ApiException) e).getStatusCode();

                            switch (statusCode) {

                                case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                    try {
                                        ResolvableApiException rae = (ResolvableApiException) e;
                                        rae.startResolutionForResult((Activity) context, AppEntry.REQUEST_CODE_GPS_PERMISSION);
                                    } catch (IntentSender.SendIntentException sie) {
                                        Log.d(LOG_TAG, "PendingIntent unable to execute request.");
                                    }

                                    break;

                                case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                    String errorMessage = "Location settings are inadequate, and cannot be " +
                                            "fixed here. Fix in Settings.";
                                    Log.d(LOG_TAG, errorMessage);
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    public void getLocation(AsyncResponse asyncResponse) {

        try {

            requestNewLocationData(asyncResponse);

            mFusedLocationClient.getLastLocation().addOnSuccessListener(lastKnownLocation -> {
                try {

                    if (lastKnownLocation != null) {
                        Log.d(LOG_TAG, "last location found: " + lastKnownLocation);
                        AsyncResponse.onResponse(asyncResponse, lastKnownLocation);
                        return;
                    }
                    Log.d(LOG_TAG, "last location not found");

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).addOnFailureListener(e -> {
                Log.d(LOG_TAG, "Error trying to get last GPS location");
                Log.d(LOG_TAG, "Error : " + e.getMessage());
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData(AsyncResponse asyncResponse) {

        try {

            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {

                    try {

                        if (locationResult == null) {
                            Log.d(LOG_TAG, "Location result not found");
                            return;
                        }

                        Log.d(LOG_TAG, "Location result found: " + locationResult);

                        Location lastKnownLocation = locationResult.getLastLocation();

                        if (lastKnownLocation == null) {
                            Log.d(LOG_TAG, "requested location not found");
                            return;
                        }

                        AsyncResponse.onResponse(asyncResponse, lastKnownLocation);
                        Log.d(LOG_TAG, "requested location found: " + lastKnownLocation);

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        } catch (Exception e) {
            Log.d(LOG_TAG, "Error : " + e.getMessage());
        }
    }

    public interface GpsStatusListener {

        static void updateGpsStatus(GpsStatusListener gpsStatusListener, boolean status) {
            if (gpsStatusListener != null) {
                gpsStatusListener.gpsStatus(status);
            }
        }

        void gpsStatus(boolean isGPSEnable);
    }
}