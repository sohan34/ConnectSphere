package com.sohan.connectsphere;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth mAuth;
    private View loadingLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        loadingLayout = findViewById(R.id.loadingLayout);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Show loading animation after a short delay
        new Handler().postDelayed(() -> {
            loadingLayout.setVisibility(View.VISIBLE);
            
            // Check if user is already logged in
            if (mAuth.getCurrentUser() != null) {
                // User is logged in, go to appropriate dashboard
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            } else {
                // User is not logged in, go to login screen
                startActivity(new Intent(SplashActivity.this, MainActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
} 