package com.example.futbol_tnt

import android.app.Application
import com.google.firebase.FirebaseApp

class FutbolTntApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}