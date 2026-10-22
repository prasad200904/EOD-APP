package com.example

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class WorkCoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Enable Crashlytics — automatically collects crash reports from all users
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            // Log app start so we know app launched successfully
            log("WorkCore started - version ${BuildConfig.VERSION_NAME}")
        }
    }
}
