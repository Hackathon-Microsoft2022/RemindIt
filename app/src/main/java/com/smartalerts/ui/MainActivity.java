package com.smartalerts.ui;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.smartalerts.AppEntry;
import com.smartalerts.AsyncResponse;
import com.smartalerts.R;
import com.smartalerts.StaticVariables;
import com.smartalerts.utils.DBHelper;
import com.smartalerts.utils.GpsUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;


import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = "LOG_TAG=>" + MainActivity.class.getSimpleName();
    private boolean locationVisible = false;
    private boolean isGpsEnabled = false;
    private GpsUtils gpsUtils;
    private DBHelper db;

    private int pendingIntentId = 0;
    private static final String CHANNEL_ID = "20";
    private Place selectedPlace;
    private View alertPopupView;
    private AlertDialog alertDialog;

    private static final int CONTACT_PICKER_REQUEST = 200;
    private EditText textMessage;
    private EditText textNumber;

    private Button smsButton;
    private Button whatsappButton;
    private Button chooseContactsButton;
    public List<ContactResult> results;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            setContentView(R.layout.activity_main);
            db = new DBHelper(this);
            FloatingActionButton createAlert = findViewById(R.id.fab);
            refreshAlertListData();
            createAlert.setOnClickListener(view -> createNewEventView());
            createNotificationChannel();
            /*textMessage = findViewById(R.id.message);
           // textNumber = findViewById(R.id.phonenumber);
            //smsButton = findViewById(R.id.smsbutton);
            chooseContactsButton = findViewById(R.id.chooseContactsButton);

            *//*smsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    try {
                        if (!results.isEmpty()) {
                            for (int j = 0; j < results.size(); j++) {
                                SmsManager smsManager = SmsManager.getDefault();
                                smsManager.sendTextMessage(results.get(j).getPhoneNumbers().get(0).getNumber(), null, textMessage.getText().toString(), null, null);
                                Toast.makeText(MainActivity.this, "SMS sent!", Toast.LENGTH_SHORT).show();
                            }
                        }

                    } catch (Exception e) {
                        Toast.makeText(MainActivity.this, "SMS sending failed", Toast.LENGTH_SHORT).show();
                    }
                }
            });*//*
            chooseContactsButton.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    new MultiContactPicker.Builder(MainActivity.this) //Activity/fragment context
                            .hideScrollbar(false) //Optional - default: false
                            .showTrack(true) //Optional - default: true
                            .searchIconColor(Color.WHITE) //Option - default: White
                            .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
//                        .handleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Azure Blue
//                        .bubbleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                            .bubbleTextColor(Color.WHITE) //Optional - default: White
                            .setTitleText("Select Contacts") //Optional - default: Select Contacts
                            .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                            .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                            .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                    android.R.anim.fade_in,
                                    android.R.anim.fade_out) //Optional - default: No animation overrides
                            .showPickerForResult(CONTACT_PICKER_REQUEST);
                }
            });*/


            if(ActivityCompat.checkSelfPermission(MainActivity.this,
                    Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(MainActivity.this,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},100);
            }
            else
            {
                callBackgroundService();
            }
            Dexter.withContext(this)
                    .withPermissions(
                            Manifest.permission.SEND_SMS,
                            Manifest.permission.READ_CONTACTS
                    ).withListener(new MultiplePermissionsListener() {
                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {/* ... */}

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {/* ... */}
                    }).check();
        } catch (Exception e) {
            Log.d(LOG_TAG, "Error : " + e.getMessage());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        textNumber = alertPopupView.findViewById(R.id.phonenumber);
        if (requestCode == CONTACT_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                results = MultiContactPicker.obtainResult(data);
                StringBuilder names = new StringBuilder(results.get(0).getPhoneNumbers().get(0).getNumber());
                for (int j = 1; j < results.size(); j++) {
                    names.append(", ").append(results.get(j).getPhoneNumbers().get(0).getNumber());
                }
                textNumber.setText(names);
                Log.d("MyTag", results.get(0).getDisplayName());
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("User closed the picker without selecting items.");
            }
        }

    }
    public void showWelcomeLocationDialog() {
        WelcomeLocationDialogFragment.newInstance().show(getSupportFragmentManager(), "WelcomeLocationDialogFragment");
    }

    private void findCurrentLocation() {

        if (locationVisible) {
            locationVisible = false;
            return;
        }

        if (GpsUtils.preventedCurrentLocation) return;

        gpsUtils.getLocation((AsyncResponse) responseObj -> {
            try {

                if (responseObj == null) {
                    Log.d(LOG_TAG, "response not found: gpsUtils.getLocation()");
                    return;
                }

                if (locationVisible) return;

                Location location = (Location) responseObj;

                StaticVariables.lat = location.getLatitude();
                StaticVariables.lng = location.getLongitude();

                showWelcomeLocationDialog();
                locationVisible = true;

            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void fetchCurrentLocation() {
        try {
            Log.d(LOG_TAG, "called: fetchCurrentLocation");
            if (GpsUtils.preventLocationCheck) return;
            if (!AppEntry.checkLocationPermission(this)) return;
            if (!isGpsEnabled) gpsUtils = new GpsUtils(this, 1);
        } catch (Exception e) {
            Log.d(LOG_TAG, "showCurrentLocation => Error : " + e.getMessage());
        }

        try {
            if (gpsUtils.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                isGpsEnabled = true;
                locationVisible = false;
                findCurrentLocation();
            } else {
                isGpsEnabled = false;
                GpsInfoDialogFragment.newInstance((AsyncResponse) responseObj -> fetchCurrentLocation()).show(getSupportFragmentManager(), "GpsInfoDialogFragment");
            }
        } catch (Exception e) {
            AppEntry.printError(LOG_TAG, e);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (alertDialog == null || !alertDialog.isShowing()) {
            fetchCurrentLocation();
        }
    }


    private void refreshAlertListData() {

        ListView alertList = findViewById(R.id.alertList);
        SimpleCursorAdapter simpleCursorAdapter = db.populateListFromDB();
        alertList.setAdapter(simpleCursorAdapter);
        alertList.setOnItemClickListener((adapterView, view, i, l) -> {
            Cursor cursor = (Cursor) simpleCursorAdapter.getItem(i);
            String title = cursor.getString(1);
            String description = cursor.getString(2);
            String eventType = cursor.getString(3);
            String locationDetail = cursor.getString(4);
            String phoneNumber = cursor.getString(5);
            String message = cursor.getString(6);
            if (eventType.equals(EventType.SEND_MESSAGE.name())) {
                createOldEventView(title, description, eventType, locationDetail, phoneNumber, message);
            } else if (eventType.equals(EventType.SHOW_NOTIFICATION.name())) {
                createOldEventView(title, description, eventType, locationDetail, null, null);
            }

        });
    }

    ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(), result -> {
                try {
                    selectedPlace = null;
                    Log.v(LOG_TAG, "activityResultLauncher => result : " + result.getResultCode());
                    if (result.getResultCode() != Activity.RESULT_OK) return;
                    Intent data = result.getData();
                    Place place = Autocomplete.getPlaceFromIntent(data);
                    Log.i(LOG_TAG, "Place: " + place.getName() + ", " + place.getId() + ", " + place.getAddress());

                    setPlaceData(place);

                } catch (Exception e) {
                    Log.v(LOG_TAG, "activityResultLauncher.Error : " + e.getMessage());
                }
            });

    private void setPlaceData(Place place){
        try {
            selectedPlace = place;
            EditText locationDetailsText = alertPopupView.findViewById(R.id.location);
            locationDetailsText.setText(selectedPlace.getName());
        }catch (Exception e){
            Log.v(LOG_TAG, "setPlaceData.Error : " + e.getMessage());
        }
    }

    private void createNewEventView(){

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        alertPopupView = getLayoutInflater().inflate(R.layout.popup, null);
        final Spinner spinner = (Spinner) alertPopupView.findViewById(R.id.eventtype);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                String selectedEventType = adapterView.getItemAtPosition(i).toString();
                View messageView = alertPopupView.findViewById(R.id.message);
                View phoneNumberView = alertPopupView.findViewById(R.id.phonenumber);
                chooseContactsButton =  alertPopupView.findViewById(R.id.chooseContactsButton);
                ConstraintLayout constraintLayout = (ConstraintLayout) alertPopupView;
                ConstraintSet constraintSet = new ConstraintSet();
                constraintSet.clone(constraintLayout);
                if (selectedEventType.equals(EventType.SEND_MESSAGE.name())) {
                    constraintSet.connect(R.id.location, ConstraintSet.TOP, R.id.message, ConstraintSet.BOTTOM);
                    constraintSet.applyTo(constraintLayout);
                    phoneNumberView.setVisibility(View.VISIBLE);
                    messageView.setVisibility(View.VISIBLE);
                    chooseContactsButton.setVisibility(View.VISIBLE);
                }
                else if (selectedEventType.equals(EventType.SHOW_NOTIFICATION.name())) {
                    constraintSet.connect(R.id.location, ConstraintSet.TOP, R.id.eventtype, ConstraintSet.BOTTOM);
                    constraintSet.applyTo(constraintLayout);
                    phoneNumberView.setVisibility(View.INVISIBLE);
                    messageView.setVisibility(View.INVISIBLE);
                    chooseContactsButton.setVisibility(View.INVISIBLE);
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        List<String> categories = new ArrayList<String>();
        categories.add(EventType.SHOW_NOTIFICATION.name());
        categories.add(EventType.SEND_MESSAGE.name());
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

        EditText titleText = alertPopupView.findViewById(R.id.title);
        EditText descriptionText = alertPopupView.findViewById(R.id.description);
        EditText locationDetailsText = alertPopupView.findViewById(R.id.location);
        EditText phoneNumberText = alertPopupView.findViewById(R.id.phonenumber);
        EditText messageText = alertPopupView.findViewById(R.id.message);

        Button cancelButton = (Button) alertPopupView.findViewById(R.id.cancelButton);
        Button saveButton = (Button) alertPopupView.findViewById(R.id.saveButton);
        Button deleteButton = (Button) alertPopupView.findViewById(R.id.deleteButton);
        chooseContactsButton =  (Button) alertPopupView.findViewById(R.id.chooseContactsButton);
        deleteButton.setVisibility(View.INVISIBLE);
        dialogBuilder.setView(alertPopupView);
        alertDialog = dialogBuilder.show();

        cancelButton.setOnClickListener(view -> alertDialog.dismiss());

        saveButton.setOnClickListener(view -> {

            String title = titleText.getText().toString();
            String description = descriptionText.getText().toString();
            String eventType = spinner.getSelectedItem().toString();
            String locationDetail = locationDetailsText.getText().toString();
            double latitude = selectedPlace.getLatLng().latitude;
            double longitude = selectedPlace.getLatLng().longitude;


            if (eventType.isEmpty()) {
                Toast.makeText(MainActivity.this, "Select Event Type", Toast.LENGTH_SHORT).show();
                return;
            }
            boolean checkInsertData = false;
            if (eventType.equals(EventType.SEND_MESSAGE.name())) {
                String phoneNumber = phoneNumberText.getText().toString();
                String message = messageText.getText().toString();
                if (phoneNumber.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Enter Phone Number", Toast.LENGTH_SHORT).show();
                    return;
                }
                checkInsertData = db.insertAlertData(title, description, eventType, locationDetail, phoneNumber, message, latitude, longitude);
            } else if (eventType.equals(EventType.SHOW_NOTIFICATION.name())) {
                checkInsertData = db.insertAlertData(title, description, eventType, locationDetail, null, null,latitude,longitude);
            }
            if (checkInsertData == true) {
                Toast.makeText(MainActivity.this, "New Alert Added", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "Error in inserting Alert", Toast.LENGTH_SHORT).show();
            }

            alertDialog.dismiss();
            refreshAlertListData();
        });

        //Adding here
        chooseContactsButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                new MultiContactPicker.Builder(MainActivity.this) //Activity/fragment context
                        .hideScrollbar(false) //Optional - default: false
                        .showTrack(true) //Optional - default: true
                        .searchIconColor(Color.WHITE) //Option - default: White
                        .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
//                        .handleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Azure Blue
//                        .bubbleColor(ContextCompat.getColor(MainActivity.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                        .bubbleTextColor(Color.WHITE) //Optional - default: White
                        .setTitleText("Select Contacts") //Optional - default: Select Contacts
                        .setLoadingType(MultiContactPicker.LOAD_ASYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                        .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                        .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                android.R.anim.fade_in,
                                android.R.anim.fade_out) //Optional - default: No animation overrides
                        .showPickerForResult(CONTACT_PICKER_REQUEST);
            }
        });

        //End Here
        locationDetailsText.setFocusable(false);
        locationDetailsText.setClickable(true);
        locationDetailsText.setOnClickListener(v -> {
            try {
                selectedPlace = null;
                List<Place.Field> fields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG);

                // Start the autocomplete intent.
                Intent intent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                        .setCountry("IN")
                        .build(this);

                activityResultLauncher.launch(intent);

            }catch (Exception e){
                Log.v(LOG_TAG, "locationDetailsText.Error : " + e.getMessage());
            }
        });

    }

    private void createOldEventView(String title, String description, String eventType, String locationDetail, String phoneNumber, String message) {

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        final View alertPopupView = getLayoutInflater().inflate(R.layout.popup, null);
        final Spinner spinner = (Spinner) alertPopupView.findViewById(R.id.eventtype);
        List<String> categories = new ArrayList<String>();
        categories.add(EventType.SHOW_NOTIFICATION.name());
        categories.add(EventType.SEND_MESSAGE.name());
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        EditText titleText = alertPopupView.findViewById(R.id.title);
        EditText descriptionText = alertPopupView.findViewById(R.id.description);
        EditText locationDetailsText = alertPopupView.findViewById(R.id.location);

        titleText.setText(title);
        descriptionText.setText(description);
        spinner.setSelection(categories.indexOf(eventType));
        locationDetailsText.setText(locationDetail);

        Button saveButton = (Button) alertPopupView.findViewById(R.id.saveButton);
        Button cancelButton = (Button) alertPopupView.findViewById(R.id.cancelButton);
        Button deleteButton = (Button) alertPopupView.findViewById(R.id.deleteButton);
        deleteButton.setVisibility(View.INVISIBLE);
        Button chooseContactsButton = (Button) alertPopupView.findViewById(R.id.chooseContactsButton);
        chooseContactsButton.setVisibility(View.INVISIBLE);

        ConstraintLayout constraintLayout = (ConstraintLayout) alertPopupView;
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(constraintLayout);
        View messageView = alertPopupView.findViewById(R.id.message);
        View phoneNumberView = alertPopupView.findViewById(R.id.phonenumber);


        if (eventType.equals(EventType.SEND_MESSAGE.name())) {

            constraintSet.connect(R.id.location, ConstraintSet.TOP, R.id.message, ConstraintSet.BOTTOM);
            constraintSet.connect(R.id.phonenumber, ConstraintSet.TOP, R.id.eventtype, ConstraintSet.BOTTOM);
        }
        constraintSet.setHorizontalBias(R.id.cancelButton, 0.5f);
        constraintSet.applyTo(constraintLayout);

        if (eventType.equals(EventType.SEND_MESSAGE.name())) {
            phoneNumberView.setVisibility(View.VISIBLE);
            messageView.setVisibility(View.VISIBLE);

            EditText phoneNumberText = (EditText) phoneNumberView;
            EditText messageText = (EditText) messageView;
            phoneNumberText.setText(phoneNumber);
            messageText.setText(message);

        }

        dialogBuilder.setView(alertPopupView);
        AlertDialog dialog = dialogBuilder.show();

        saveButton.setVisibility(View.INVISIBLE);

        cancelButton.setOnClickListener(view -> dialog.dismiss());

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater=getMenuInflater();
        inflater.inflate(R.menu.bottomappbar_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){

            case R.id.app_bar_settings:
                //addNotification(4);
                /*AudioManager am= (AudioManager) getBaseContext().getSystemService(Context.AUDIO_SERVICE);
                am.setRingerMode(AudioManager.RINGER_MODE_VIBRATE);*/
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                break;

            case R.id.app_bar_location:
                showWelcomeLocationDialog();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                notificationManager = getSystemService(NotificationManager.class);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }



    @SuppressLint("MissingSuperCall")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 100 && grantResults.length > 0 && (grantResults[0] + grantResults[1]
                == PackageManager.PERMISSION_GRANTED)) {

            callBackgroundService();
        } else {
            Toast.makeText(getApplicationContext(), "Permission denied."
                    , Toast.LENGTH_SHORT).show();
        }
    }

    public void callBackgroundService() {
        Intent intent = new Intent(getApplicationContext(), NewBackgroundProcess.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,0,intent,PendingIntent.FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, 1000*60, pendingIntent);
    }
}