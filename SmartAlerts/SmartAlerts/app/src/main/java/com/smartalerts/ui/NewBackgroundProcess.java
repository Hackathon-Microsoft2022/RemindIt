package com.smartalerts.ui;

import static android.content.Context.LOCATION_SERVICE;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.util.Pair;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.smartalerts.R;
import com.smartalerts.utils.DBHelper;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class NewBackgroundProcess extends BroadcastReceiver implements LocationListener {

    Map<String, Pair<Double, Double>> mp = new HashMap<>();
    final int RR = 6371;
    private static final String TAG = "MyService";
    private DBHelper db;
    private static final String CHANNEL_ID = "20";
    private int id = 0;
    @SuppressLint("MissingPermission")
    @Override
    public void onReceive(Context context, Intent intent) {

        boolean isGPSEnable;
        boolean isNetworkEnable;
        LocationManager locationManager;


       // populateLocationMap(context);

        locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        isGPSEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (isGPSEnable)
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 0, this);
            if (locationManager!=null){
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (location!=null){
                    checkInVicinity(context ,21.1458004,79.0881546);
                }
            }
        }
    }

    private void checkInVicinity(Context context ,double currentLat, double currentLon)
    {
        db = new DBHelper(context);
        SimpleCursorAdapter simpleCursorAdapter = db.populateListFromDB();
        int i = 0;
        while(simpleCursorAdapter.getCursor().getCount()>0 && !simpleCursorAdapter.getCursor().isLast())
        {
            Cursor cursor = (Cursor) simpleCursorAdapter.getItem(i++);
            String id = String.valueOf(cursor.getInt(0));
            double targetLat = cursor.getDouble(7);
            double targetLon = cursor.getDouble(8);
            String eventType = cursor.getString(3);
            String phoneNumber = cursor.getString(5);
            String message = cursor.getString(6);
           /* double targetLat = mp.get(k).first;
            double targetLon = mp.get(k).second;*/

            double latDistance = Math.toRadians(targetLat - currentLat);
            double lonDistance = Math.toRadians(targetLon - currentLon);
            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(currentLat)) * Math.cos(Math.toRadians(targetLat))
                    * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
            double distance = RR * c * 1000;

            if(distance <= 500) {
                if(eventType.equals(EventType.SHOW_NOTIFICATION.name()))
                {
                    addNotification(context, Integer.parseInt(id));
                }
                else if(eventType.equals(EventType.SEND_MESSAGE.name()) )
                {
                        sendMessage(context, phoneNumber, message, Integer.parseInt(id));
                    Log.e(TAG, "Test Message ");

                }

            }
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

    }

    private void sendMessage(Context context, String phoneNumber, String message, int id)
    {
        SmsManager smsManager = SmsManager.getDefault();
        String [] phoneNumbers = phoneNumber.split(",");

        for(String s:phoneNumbers) {
            smsManager.sendTextMessage(s, null, message, null, null);
            Toast.makeText(context, "SMS sent!", Toast.LENGTH_SHORT).show();

        }
        db.deleteItem(id);
    }

    private void addNotification(Context context , int notificationID) {
        Notification notification = db.getNotificationDetails(notificationID);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(notification.getTitle())
                        .setContentText(notification.getDescription())
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(notification.getDescription()))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setDefaults(NotificationCompat.DEFAULT_VIBRATE)
                        .setAutoCancel(true);

        Intent notificationIntent = new Intent(context, NotificationView.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //notification message will get at NotificationView
        String strId = String.valueOf(notification.getId());
        notificationIntent.putExtra("message", strId);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Random random = new Random();
        PendingIntent contentIntent = PendingIntent.getActivity(context,random.nextInt() , notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        builder.setContentIntent(contentIntent);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(id++, builder.build());
       // Toast.makeText(this, "Sent a notification", Toast.LENGTH_SHORT).show();
    }
}
