package com.example.cronod

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(screenOffCounter: State<Int>) {
    val context = LocalContext.current
    val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    var countdownInput by remember { mutableStateOf("15") }
    var countdown by remember { mutableStateOf(15) }
    var running by remember { mutableStateOf(false) }
    var countdownFinished by remember { mutableStateOf(false) }
    var stopwatchRunning by remember { mutableStateOf(false) }
    var stopwatchMs by remember { mutableStateOf(0) }
    var finalTimeMs by remember { mutableStateOf<Int?>(null) }

    var mediaPlayer: MediaPlayer? by remember { mutableStateOf(null) }

    // 🚨 Reacciona cada vez que cambia screenOffCounter
    LaunchedEffect(screenOffCounter.value) {
        if (MainActivity.shouldStopStopwatch && stopwatchRunning && finalTimeMs == null) {
            stopwatchRunning = false
            finalTimeMs = stopwatchMs
            MainActivity.shouldStopStopwatch = false  // Resetear
        }
    }

    LaunchedEffect(running) {
        if (running && !countdownFinished) {
            try {
                // Iniciar beep al comenzar la cuenta atrás
                mediaPlayer = MediaPlayer.create(context, R.raw.beep)
                mediaPlayer?.isLooping = true
                mediaPlayer?.start()

                while (countdown > 0) {
                    delay(1000)
                    countdown--
                }

                // Parar beep al terminar cuenta atrás
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null

                // ▶️ Reproducir shot.mp3 cuando inicia el cronómetro
                MediaPlayer.create(context, R.raw.shot)?.apply {
                    setOnCompletionListener { it.release() }
                    start()
                }

                countdownFinished = true
                stopwatchRunning = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    LaunchedEffect(stopwatchRunning) {
        while (stopwatchRunning) {
            delay(10)
            stopwatchMs += 10
        }
    }

    fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val centiseconds = (ms % 1000) / 10
        return "%02d:%02d".format(seconds, centiseconds)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (!running) {
            OutlinedTextField(
                value = countdownInput,
                onValueChange = { countdownInput = it.filter { c -> c.isDigit() } },
                label = { Text("Cuenta atrás (segundos)") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        when {
            !running -> Text("Listo para empezar")
            !countdownFinished -> Text("Cuenta atrás: $countdown s")
            finalTimeMs != null -> Text("Tiempo: ${formatTime(finalTimeMs!!)}")
            else -> Text("Cronómetro: ${formatTime(stopwatchMs)}")
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            !running -> {
                Button(onClick = {
                    countdown = countdownInput.toIntOrNull() ?: 15
                    running = true
                    stopwatchMs = 0
                    countdownFinished = false
                    stopwatchRunning = false
                    finalTimeMs = null
                }) {
                    Text("Iniciar")
                }
            }

            countdownFinished && finalTimeMs == null -> {
                Row {
                    Button(onClick = {
                        stopwatchRunning = false
                        finalTimeMs = stopwatchMs
                    }) {
                        Text("Parar Cronómetro")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        running = false
                        stopwatchMs = 0
                        countdownFinished = false
                        stopwatchRunning = false
                        finalTimeMs = null
                        countdownInput = "15"
                    }) {
                        Text("Reiniciar")
                    }
                }
            }

            finalTimeMs != null -> {
                Button(onClick = {
                    running = false
                    stopwatchMs = 0
                    countdownFinished = false
                    stopwatchRunning = false
                    finalTimeMs = null
                    countdownInput = "15"
                }) {
                    Text("Reiniciar")
                }
            }
        }
    }
}
