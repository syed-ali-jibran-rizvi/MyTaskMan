package com.example.mytaskman;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.ai.client.generativeai.GenerativeModel;
import com.google.ai.client.generativeai.java.GenerativeModelFutures;
import com.google.ai.client.generativeai.type.BlockThreshold;
import com.google.ai.client.generativeai.type.Content;
import com.google.ai.client.generativeai.type.GenerateContentResponse;
import com.google.ai.client.generativeai.type.GenerationConfig;
import com.google.ai.client.generativeai.type.HarmCategory;
import com.google.ai.client.generativeai.type.SafetySetting;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.io.FileOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerViewToday;
    private RecyclerView recyclerViewUpcoming;
    private EventAdapter todayEventAdapter;
    private EventAdapter upcomingEventAdapter;
    private List<Event> todayEventList;
    private List<Event> upcomingEventList;
    private DatabaseReference databaseEvents;
    private GenerativeModelFutures model;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Event Lists
        todayEventList = new ArrayList<>();
        upcomingEventList = new ArrayList<>();
        databaseEvents = FirebaseDatabase.getInstance().getReference("events");

        // Initialize RecyclerView for today's events
        recyclerViewToday = findViewById(R.id.recyclerviewtoday);
        recyclerViewToday.setLayoutManager(new LinearLayoutManager(this));
        todayEventAdapter = new EventAdapter(todayEventList);
        recyclerViewToday.setAdapter(todayEventAdapter);

        // Initialize RecyclerView for upcoming events
        recyclerViewUpcoming = findViewById(R.id.recyclerviewupcoming);
        recyclerViewUpcoming.setLayoutManager(new LinearLayoutManager(this));
        upcomingEventAdapter = new EventAdapter(upcomingEventList);
        recyclerViewUpcoming.setAdapter(upcomingEventAdapter);

        // Initialize Generative Model
        initializeGenerativeModel();

        // Fetch data from Firebase
        databaseEvents.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                todayEventList.clear();
                upcomingEventList.clear();
                long currentTime = System.currentTimeMillis();
                StringBuilder eventDetails = new StringBuilder();

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Event event = snapshot.getValue(Event.class);
                    if (event != null) {
                        if (event.getDate() <= currentTime) {
                            todayEventList.add(event);
                        } else {
                            upcomingEventList.add(event);
                        }
                        // Prepare details for prioritization
                        eventDetails.append("Event: {name: '").append(event.getName()).append("', date: ").append(event.getDate()).append("}\n");
                    }
                }

                // Prioritize events using Generative Model API
                prioritizeEvents(eventDetails.toString());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });

        // Bottom Navigation Setup
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_add) {
                startActivity(new Intent(MainActivity.this, AddTaskActivity.class));
                return true;
            } else if (itemId == R.id.navigation_profile) {
                // Handle profile action
                return true;
            }
            return false;
        });
    }

    private void initializeGenerativeModel() {
        GenerationConfig.Builder configBuilder = new GenerationConfig.Builder();
        configBuilder.temperature = 0.15f;
        configBuilder.topK = 32;
        configBuilder.topP = 1f;
        configBuilder.maxOutputTokens = 4096;

        ArrayList<SafetySetting> safetySettings = new ArrayList<>();
        safetySettings.add(new SafetySetting(HarmCategory.HARASSMENT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.HATE_SPEECH, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.SEXUALLY_EXPLICIT, BlockThreshold.MEDIUM_AND_ABOVE));
        safetySettings.add(new SafetySetting(HarmCategory.DANGEROUS_CONTENT, BlockThreshold.MEDIUM_AND_ABOVE));

        GenerativeModel gm = new GenerativeModel(
                "gemini-1.5-flash-001",
                BuildConfig.API_KEY,
                configBuilder.build(),
                safetySettings
        );

        model = GenerativeModelFutures.from(gm);
        executor = Executors.newSingleThreadExecutor();
    }

    private void prioritizeEvents(String eventDetails) {
        Content content = new Content.Builder()
                .addText("Rank the following based on their priority. If the event's deadline has passed, assign rank=3, if within 2 days, rank=1, else, rank=2.\nProvide only the rank and name, no extra details or text.\n\n" + eventDetails)
                .build();

        ListenableFuture<GenerateContentResponse> response = model.generateContent(content);

        Futures.addCallback(response, new FutureCallback<GenerateContentResponse>() {
            @Override
            public void onSuccess(GenerateContentResponse result) {
                try {
                    // Parse the response as a JSON array
                    String responseText = result.getText();
                    Log.d("MainActivity", "API Response: " + responseText);
                    saveResponseToFile(responseText); // Save the response to a file

                    // Assuming the response format is an array of strings
                    String[] lines = responseText.split("\n");
                    Map<String, Integer> priorityMap = new HashMap<>();

                    // Extract priorities
                    for (String line : lines) {
                        String[] parts = line.split("\\. ", 2);
                        if (parts.length == 2) {
                            String rankString = parts[0].replaceAll("\\D+", "");
                            int rank = Integer.parseInt(rankString);
                            String description = parts[1];
                            priorityMap.put(description, rank);
                        }
                    }

                    // Update events with priorities
                    for (Event event : todayEventList) {
                        Integer rank = priorityMap.get(event.getName());
                        if (rank != null) {
                            event.setRank(rank);
                        }
                    }
                    for (Event event : upcomingEventList) {
                        Integer rank = priorityMap.get(event.getName());
                        if (rank != null) {
                            event.setRank(rank);
                        }
                    }

                    runOnUiThread(() -> {
                        todayEventAdapter.notifyDataSetChanged();
                        upcomingEventAdapter.notifyDataSetChanged();
                    });
                } catch (Exception e) {
                    Log.e("MainActivity", "Error parsing API response", e);
                    runOnUiThread(() -> Toast.makeText(MainActivity.this, "Failed to parse API response.", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onFailure(Throwable t) {
                runOnUiThread(() -> {
                    t.printStackTrace();
                    Toast.makeText(MainActivity.this, "API call failed. Setting default colors.", Toast.LENGTH_SHORT).show();
                    // Set all events to white if API fails
                    for (Event event : todayEventList) {
                        event.setRank(Integer.MAX_VALUE); // Set high rank to make it white
                    }
                    for (Event event : upcomingEventList) {
                        event.setRank(Integer.MAX_VALUE); // Set high rank to make it white
                    }
                    todayEventAdapter.notifyDataSetChanged();
                    upcomingEventAdapter.notifyDataSetChanged();
                });
            }
        }, executor);
    }

    private void saveResponseToFile(String responseText) {
        try {
            FileOutputStream fos = openFileOutput("api_response.json", MODE_PRIVATE);
            fos.write(responseText.getBytes());
            fos.close();
            Log.d("MainActivity", "Response saved to file");
        } catch (IOException e) {
            Log.e("MainActivity", "Failed to save response to file", e);
        }
    }
}
