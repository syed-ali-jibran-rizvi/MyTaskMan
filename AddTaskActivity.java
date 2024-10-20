package com.example.mytaskman;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddTaskActivity extends AppCompatActivity {

    private EditText editTextEventName;
    private EditText editTextEventDate;
    private EditText editTextEventStatus;
    private EditText editTextEventDetails;
    private EditText editTextEventExpiry;
    private Button buttonAddEvent;

    private DatabaseReference databaseEvents;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize Firebase Database reference
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        // Initialize UI elements
        editTextEventName = findViewById(R.id.editTextEventName);
        editTextEventDate = findViewById(R.id.editTextEventDate); // Format: ddMMyyyy
        editTextEventStatus = findViewById(R.id.editTextEventStatus);
        editTextEventDetails = findViewById(R.id.editTextEventDetails);
        editTextEventExpiry = findViewById(R.id.editTextEventExpiry); // Format: ddMMyyyy-HHmm
        buttonAddEvent = findViewById(R.id.buttonAddEvent);

        buttonAddEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addEvent();
            }
        });
    }

    private void addEvent() {
        // Get user input
        String name = editTextEventName.getText().toString().trim();
        String dateStr = editTextEventDate.getText().toString().trim(); // Format: ddMMyyyy
        String status = editTextEventStatus.getText().toString().trim();
        String details = editTextEventDetails.getText().toString().trim();
        String expiryTimeStr = editTextEventExpiry.getText().toString().trim(); // Format: ddMMyyyy-HHmm

        // Check if any field is empty
        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(dateStr) || TextUtils.isEmpty(status) || TextUtils.isEmpty(details) || TextUtils.isEmpty(expiryTimeStr)) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        long date;
        long expiryTime;

        try {
            // Convert date and expiry time to long
            date = parseDate(dateStr);
            expiryTime = parseExpiryTime(expiryTimeStr);
        } catch (Exception e) {
            Toast.makeText(this, "Invalid date or expiry time format", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate unique key for the new event
        String id = databaseEvents.push().getKey();
        if (id != null) {
            // Create new Event object
            Event event = new Event(id, name, date, status, details, expiryTime);
            // Save event to Firebase database
            databaseEvents.child(id).setValue(event);
            // Set the alarm
            setAlarm(expiryTime);
            // Notify user and finish activity
            Toast.makeText(this, "Event added", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "Error generating event ID", Toast.LENGTH_SHORT).show();
        }
    }

    private void setAlarm(long triggerTime) {
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmManager != null) {
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
            Toast.makeText(this, "Alarm set", Toast.LENGTH_SHORT).show();
        }
    }

    private long parseDate(String dateStr) throws ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("ddMMyyyy", Locale.getDefault());
        Date date = dateFormat.parse(dateStr);
        if (date != null) {
            return date.getTime();
        } else {
            throw new ParseException("Date parsing error", 0);
        }
    }

    private long parseExpiryTime(String expiryTimeStr) throws ParseException {
        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("ddMMyyyy-HHmm", Locale.getDefault());
        Date date = dateTimeFormat.parse(expiryTimeStr);
        if (date != null) {
            return date.getTime();
        } else {
            throw new ParseException("Expiry time parsing error", 0);
        }
    }
}
