package com.betsson.interviewtest



import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class InterviewTestApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // You can initialize other application-wide things here if needed
        // For Hilt, the @HiltAndroidApp annotation is the key part.
    }
}