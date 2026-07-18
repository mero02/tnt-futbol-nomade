package com.example.futbol_tnt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.futbol_tnt.presentation.navigation.AppNavigation
import com.example.futbol_tnt.presentation.ui.theme.FutbolTNTTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Sincronizar Token FCM (HU-41)
        syncFcmToken()

        setContent {
            FutbolTNTTheme {
                AppNavigation()
            }
        }
    }

    private fun syncFcmToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) return@addOnCompleteListener
            val token = task.result
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null && token != null) {
                FirebaseFirestore.getInstance().collection("users").document(uid)
                    .update("fcmToken", token)
            }
        }
    }
}
