package com.example.mytaskman;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class AlarmNotificationActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_notification);

        // Get event details from intent
        String eventName = getIntent().getStringExtra("eventName");
        String eventDetails = getIntent().getStringExtra("eventDetails");

        // Set event details to TextViews
        TextView textViewEventName = findViewById(R.id.textViewEventName);
        TextView textViewEventDetails = findViewById(R.id.textViewEventDetails);
        textViewEventName.setText(eventName);
        textViewEventDetails.setText(eventDetails);

        // Set stop button click listener
        Button buttonStopAlarm = findViewById(R.id.buttonStopAlarm);
        buttonStopAlarm.setOnClickListener(v -> {
            stopAlarm();
            finish();
        });
    }

    private void stopAlarm() {
        // Stop the AlarmService
        stopService(new Intent(this, AlarmService.class));
    }
}
