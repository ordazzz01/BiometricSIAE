package com.siae.biometricsiae

import android.app.Application
import com.google.firebase.FirebaseApp
import com.google.firebase.crashlytics.FirebaseCrashlytics

class BiometricSIAEApp : Application() {
    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        FirebaseCrashlytics.getInstance().apply {
            setCrashlyticsCollectionEnabled(true)
        }
    }
}
