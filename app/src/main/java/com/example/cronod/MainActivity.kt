package com.example.cronod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
    private val screenOffCounter = mutableStateOf(0)  // Solución: contador

    companion object {
        var shouldStopStopwatch = false  // Solución: variable compartida
    }

    private val screenOffReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_SCREEN_OFF) {
                shouldStopStopwatch = true  // Señal inmediata para detener
                screenOffCounter.value++    // Incrementa para que Compose reaccione
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        registerReceiver(screenOffReceiver, IntentFilter(Intent.ACTION_SCREEN_OFF))

        setContent {
            Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                TimerScreen(screenOffCounter)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(screenOffReceiver)
    }
}
