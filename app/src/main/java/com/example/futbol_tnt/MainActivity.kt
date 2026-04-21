package com.example.futbol_tnt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.futbol_tnt.presentation.navigation.AppNavigation
import com.example.futbol_tnt.presentation.ui.theme.FutbolTNTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FutbolTNTTheme {
                AppNavigation()
            }
        }
    }
}