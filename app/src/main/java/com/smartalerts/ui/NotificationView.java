package com.smartalerts.ui;

import com.smartalerts.R;
import com.smartalerts.utils.DBHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class NotificationView extends AppCompatActivity {
    TextView textView;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = new DBHelper(this);
        setContentView(R.layout.popup);
        String message=getIntent().getStringExtra("message");
        Notification notification = db.getNotificationDetails(Integer.parseInt(message));

        EditText titleText = findViewById(R.id.title);
        EditText descriptionText = findViewById(R.id.description);
/*
        TextView category = findViewById()
*/
        final Spinner spinner = (Spinner) findViewById(R.id.eventtype);
        List<String> categories = new ArrayList<String>();
        categories.add(EventType.SHOW_NOTIFICATION.name());
        categories.add(EventType.SEND_MESSAGE.name());
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, categories);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);

       // EditText eventTypeText = findViewById(R.id.eventtype);
        EditText locationDetailsText = findViewById(R.id.location);

        titleText.setText(notification.getTitle());
        descriptionText.setText(notification.getDescription());
        spinner.setSelection(categories.indexOf(notification.getEventType()));

        locationDetailsText.setText(notification.getLocationDetail());

        Button saveButton = (Button) findViewById(R.id.saveButton);
        Button cancelButton = (Button) findViewById(R.id.cancelButton);
        Button deleteButton = (Button) findViewById(R.id.deleteButton);
        Button chooseContactsButton = (Button) findViewById(R.id.chooseContactsButton);
        saveButton.setVisibility(View.INVISIBLE);
        cancelButton.setVisibility(View.INVISIBLE);
        chooseContactsButton.setVisibility(View.INVISIBLE);
        spinner.setVisibility(View.INVISIBLE);
        deleteButton.setOnClickListener(view -> {
            db.deleteItem(notification.getId());
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        });

       // cancelButton.setOnClickListener(view -> dialog.dismiss());

       // textView = findViewById(R.id.textView);
        //getting the notification message

       // MainActivity mainActivity = new MainActivity();
        //mainActivity.createOldEventView(notification.getTitle(),notification.getDescription(),notification.getEventType(),notification.getLocationDetail());
        //textView.setText(notification.getDescription());
    }
}

