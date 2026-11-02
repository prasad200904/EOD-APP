package com.example

import android.app.Application
import com.example.util.Analytics
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class WorkCoreApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // Initialize Firebase
        FirebaseApp.initializeApp(this)

        // Initialize Analytics
        Analytics.init(this)

        // Enable Crashlytics
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
            log("WorkCore started - version ${BuildConfig.VERSION_NAME}")
        }
    }
}
