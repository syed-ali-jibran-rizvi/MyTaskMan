package com.example.mytaskman;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

public class activity_splash extends AppCompatActivity {

    private static final int SPLASH_DURATION = 5000; // 5 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Initialize VideoView
        VideoView videoView = findViewById(R.id.splash_video_view);

        // Set up the video
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.beautyanimvid);
        videoView.setVideoURI(videoUri);

        // Start the video
        videoView.start();

        // Transition to MainActivity after the video finishes
        videoView.setOnCompletionListener(mp -> {
            Intent intent = new Intent(activity_splash.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Fallback in case the video is shorter than SPLASH_DURATION
        new Handler().postDelayed(() -> {
            if (videoView.isPlaying()) {
                videoView.stopPlayback();
                Intent intent = new Intent(activity_splash.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }, SPLASH_DURATION);
    }
}
