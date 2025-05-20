package com.app.chatori.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

import com.app.chatori.R;
import com.app.chatori.ui.home.MainActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Splash screen activity that displays the app logo and name
 * before redirecting to either the login screen or main screen.
 */
public class SplashActivity extends AppCompatActivity {

    private static final long SPLASH_DELAY = 2000; // 2 seconds
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        auth = FirebaseAuth.getInstance();

        // Delay for the splash screen
        new Handler(Looper.getMainLooper()).postDelayed(this::checkUserAndRedirect, SPLASH_DELAY);
    }

    /**
     * Checks if a user is already logged in and redirects accordingly
     */
    private void checkUserAndRedirect() {
        FirebaseUser currentUser = auth.getCurrentUser();
        
        if (currentUser != null) {
            // User is already logged in, go to MainActivity
            startActivity(new Intent(SplashActivity.this, MainActivity.class));
        } else {
            // No user logged in, go to LoginActivity
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
        }
        
        // Close this activity
        finish();
    }
}
