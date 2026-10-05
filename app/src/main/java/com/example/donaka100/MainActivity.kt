package com.example.donaka100

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.donaka100.ui.layout.DonakaApp
import com.example.donaka100.ui.theme.DonakaTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // IMPORTANT : doit être appelé AVANT super.onCreate()
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DonakaTheme {
                DonakaApp()
            }
        }
    }
}