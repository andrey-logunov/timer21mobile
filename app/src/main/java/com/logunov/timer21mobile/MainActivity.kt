package com.logunov.timer21mobile

import android.media.Ringtone
import android.media.RingtoneManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Locale

class MainActivity : ComponentActivity() {
    private var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var ringtone: Ringtone

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val uri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        ringtone = RingtoneManager.getRingtone(this, uri)

        enableEdgeToEdge()

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TimerScreen(ringtone, handler)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimerScreen(ringtone: Ringtone, handler: Handler) {
    var expanded by remember { mutableStateOf(false) }
    var selectedTime by remember { mutableStateOf("1:00") }
    var currentSeconds by remember { mutableIntStateOf(0) }
    var totalSeconds by remember { mutableIntStateOf(60) }
    var isRunning by remember { mutableStateOf(false) }

    val times = listOf("1:00", "5:00", "10:00", "30:00", "45:00", "60:00")

    LaunchedEffect(selectedTime) {
        totalSeconds = selectedTime.substringBefore(":").toInt() * 60
        stopTimer(handler, isRunningSetter = { isRunning = it }, resetSeconds = { currentSeconds = it })
    }

    val backgroundColor = remember(currentSeconds, totalSeconds) {
        derivedStateOf {
            when {
                currentSeconds <= 5 -> Color(0xFF006400) // Dark Green
                currentSeconds >= totalSeconds - 5 -> Color.Gray
                else -> Color.Black
            }
        }
    }.value

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                TextField(
                    readOnly = true,
                    value = selectedTime,
                    onValueChange = { },
                    label = { Text("Select Time", fontSize = 32.sp, color = Color.White) },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = backgroundColor,
                        unfocusedContainerColor = backgroundColor,
                        disabledContainerColor = backgroundColor,
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.Gray,
                        disabledIndicatorColor = Color.Gray,
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth(),
                    textStyle = TextStyle(fontSize = 64.sp, color = Color.White)
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false },
                    modifier = Modifier.background(backgroundColor) // Dropdown background
                ) {
                    times.forEach { time ->
                        DropdownMenuItem(
                            text = { Text(time, fontSize = 64.sp, color = Color.White) },
                            onClick = {
                                selectedTime = time
                                expanded = false
                            },
                            modifier = Modifier.background(backgroundColor) // Item background
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            val min = currentSeconds / 60
            val sec = currentSeconds % 60
            Text(
                text = String.format(Locale.ROOT,"%d:%02d", min, sec),
                fontSize = 128.sp,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column {
                Button(
                    onClick = {
                        startTimer(
                            handler,
                            isRunningSetter = { isRunning = it },
                            isRunningGetter = { isRunning },
                            currentSeconds = { currentSeconds },
                            totalSeconds = totalSeconds,
                            updateSeconds = { currentSeconds = it },
                            ringtone = ringtone
                        )
                    },
                    enabled = !isRunning && totalSeconds > 0,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("Start", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        stopTimer(handler, isRunningSetter = { isRunning = it }, resetSeconds = { currentSeconds = it })
                    },
                    enabled = isRunning,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(64.dp)
                ) {
                    Text("Stop", fontSize = 32.sp)
                }
            }
        }
    }
}

private fun startTimer(
    handler: Handler,
    isRunningSetter: (Boolean) -> Unit,
    isRunningGetter: () -> Boolean,
    currentSeconds: () -> Int,
    totalSeconds: Int,
    updateSeconds: (Int) -> Unit,
    ringtone: Ringtone
) {
    isRunningSetter(true)
    handler.postDelayed(object : Runnable {
        override fun run() {
            if (isRunningGetter()) {
                val seconds = currentSeconds()
                if (seconds == totalSeconds) {
                    ringBell(ringtone, handler)
                    updateSeconds(0)
                } else {
                    updateSeconds(seconds + 1)
                }
                handler.postDelayed(this, 1000)
            }
        }
    }, 1000)
}

private fun stopTimer(
    handler: Handler,
    isRunningSetter: (Boolean) -> Unit,
    resetSeconds: (Int) -> Unit
) {
    isRunningSetter(false)
    handler.removeCallbacksAndMessages(null)
    resetSeconds(0)
}

private fun ringBell(ringtone: Ringtone, handler: Handler) {
    handler.post { ringtone.play() }
    handler.postDelayed({ ringtone.play() }, 2000)
    handler.postDelayed({ ringtone.play() }, 4000)
    handler.postDelayed({ ringtone.play() }, 6000)
    handler.postDelayed({ ringtone.play() }, 8000)
}
