package com.app.chatori;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Main Application class for the Chatori app.
 * Initializes Firebase and other app-wide configurations.
 */
public class ChatoriApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        
        // Initialize Firebase
        FirebaseApp.initializeApp(this);
    }
}
