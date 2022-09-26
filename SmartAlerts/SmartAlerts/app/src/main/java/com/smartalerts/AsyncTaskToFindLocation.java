package com.smartalerts;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AsyncTaskToFindLocation extends AsyncTask<Object, Void, Object> {

    private static final String TAG = "TAG=>AsyncTaskToFindLocation";
    AsyncResponse asyncResponse;
    Context context;

    public AsyncTaskToFindLocation(Context context, AsyncResponse asyncResponse) {
        this.context = context;
        this.asyncResponse = asyncResponse;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Object doInBackground(Object... params) {
        try {
            LatLng latLng = (LatLng) params[0];
            List<Address> addressList = null;
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
                if (addressList != null) {

                    try {
                        Gson gson = new Gson();
                        String addressStr = gson.toJson(addressList);
                        Log.d(TAG, "addresses are: " + addressStr);
                    } catch (Exception e) {
                        AppEntry.printError(TAG, e);
                    }
                }

                if (addressList.size() > 0) {

                    Address address = addressList.get(0);

                    if (address != null && address.hasLatitude() && address.hasLongitude()) {
                        return addressList;
                    } else {
                        Log.d(TAG, "geocoder.getFromLocation, address not found");
                    }
                }

            } catch (Exception e) {
                AppEntry.printError(TAG, e);
            }
        } catch (Exception e) {
            AppEntry.printError(TAG, e);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Object result) {
        if (asyncResponse != null) {
            asyncResponse.processFinish(result);
        }
    }
}
