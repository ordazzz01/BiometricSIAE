package com.siae.biometricsiae.feature

import android.app.Activity
import android.media.AudioManager
import android.media.ToneGenerator
import android.view.WindowManager
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siae.biometricsiae.BiometricHelper
import com.siae.biometricsiae.data.AttendanceRecord
import com.siae.biometricsiae.data.FirestoreRepository
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun CheckinScreen(
    biometricHelper: BiometricHelper,
    repository: FirestoreRepository,
    onSettingsClick: () -> Unit,
    onBiometricRequest: () -> Unit = {},
    onExit: () -> Unit = {}
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Registre su huella") }
    var statusColor by remember { mutableStateOf(Color.White) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var resultSuccess by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }
    var lastActivityTime by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var isScreenDimmed by remember { mutableStateOf(false) }

    // Animated background - SLOW (60 seconds)
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val colorIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(60000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorIndex"
    )

    val gradientColors = listOf(
        Color(0xFF1B5E20),
        Color(0xFF0D47A1),
        Color(0xFF4A148C),
        Color(0xFFE65100),
        Color(0xFF1B5E20),
    )

    val currentIndex = colorIndex.toInt().coerceIn(0, 3)
    val nextIndex = (currentIndex + 1).coerceAtMost(4)
    val fraction = colorIndex - currentIndex

    fun lerpColor(start: Color, end: Color, f: Float): Color {
        return Color(
            red = start.red + (end.red - start.red) * f,
            green = start.green + (end.green - start.green) * f,
            blue = start.blue + (end.blue - start.blue) * f
        )
    }

    val animatedColor1 = lerpColor(gradientColors[currentIndex], gradientColors[nextIndex], fraction)
    val animatedColor2 = lerpColor(
        gradientColors[(currentIndex + 1).coerceAtMost(4)],
        gradientColors[(currentIndex + 2).coerceAtMost(4)],
        fraction
    )

    // Play click sound on success
    fun playClickSound() {
        try {
            val toneGenerator = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGenerator.startTone(ToneGenerator.TONE_PROP_ACK, 150)
            toneGenerator.release()
        } catch (e: Exception) {
            // Ignore sound errors
        }
    }

    // Update clock - HH:mm only (no seconds)
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es")).format(now)
            delay(1000)
        }
    }

    // Dim screen after 30s inactivity to 10% brightness
    LaunchedEffect(lastActivityTime) {
        while (true) {
            delay(1000)
            val elapsed = System.currentTimeMillis() - lastActivityTime
            if (elapsed > 30000 && !isScreenDimmed) {
                isScreenDimmed = true
                activity?.window?.attributes = activity.window.attributes.apply {
                    screenBrightness = 0.10f
                }
            }
        }
    }

    // Restore brightness to 80% on activity
    fun onUserActivity() {
        lastActivityTime = System.currentTimeMillis()
        if (isScreenDimmed) {
            isScreenDimmed = false
            activity?.window?.attributes = activity.window.attributes.apply {
                screenBrightness = 0.80f
            }
        }
    }

    // Listen for biometric results
    LaunchedEffect(Unit) {
        biometricHelper.resultFlow.collect { result ->
            onUserActivity()
            when (result) {
                is BiometricHelper.BiometricResult.Success -> {
                    isProcessing = true
                    statusMessage = "Procesando..."

                    val record = AttendanceRecord(
                        employeeId = "pending",
                        employeeName = "Procesando...",
                        type = "check_in",
                        method = "fingerprint",
                        deviceId = android.os.Build.MODEL
                    )

                    val success = repository.saveAttendance(record)
                    isProcessing = false

                    if (success) {
                        playClickSound()
                        resultSuccess = true
                        resultMessage = "¡Éxito! Asistencia registrada"
                        statusMessage = "¡Éxito! Asistencia registrada"
                        statusColor = Color(0xFF4CAF50)
                    } else {
                        resultSuccess = false
                        resultMessage = "Error al guardar"
                        statusMessage = "Error al registrar"
                        statusColor = Color(0xFFF44336)
                    }
                    showResult = true
                    delay(3000)
                    showResult = false
                    statusMessage = "Registre su huella"
                    statusColor = Color.White
                }
                is BiometricHelper.BiometricResult.UserCancelled -> {
                    statusMessage = "Intente de nuevo"
                    statusColor = Color(0xFFFFC107)
                    delay(2000)
                    statusMessage = "Registre su huella"
                    statusColor = Color.White
                }
                is BiometricHelper.BiometricResult.Error -> {
                    statusMessage = "Error: ${result.message}"
                    statusColor = Color(0xFFF44336)
                    delay(2000)
                    statusMessage = "Registre su huella"
                    statusColor = Color.White
                }
                is BiometricHelper.BiometricResult.NotAvailable -> {
                    statusMessage = "Huella no disponible"
                    statusColor = Color(0xFFFF9800)
                }
            }
        }
    }

    // Full screen
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(animatedColor1, animatedColor2)
                )
            )
            .systemBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar with settings and exit
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = { onSettingsClick() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = "Configuración",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }

                Text(
                    text = "Checador Biométrico",
                    fontSize = 18.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )

                // Exit button - only works with device fingerprint
                IconButton(
                    onClick = { onExit() },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = "Salir (requiere huella)",
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Large clock
            Text(
                text = currentTime,
                fontSize = 80.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = currentDate.replaceFirstChar { it.uppercase() },
                fontSize = 18.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.weight(1f))

            // Status card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color.Black.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = when {
                            showResult && resultSuccess -> Icons.Default.CheckCircle
                            showResult && !resultSuccess -> Icons.Default.Error
                            else -> Icons.Default.Fingerprint
                        },
                        contentDescription = null,
                        modifier = Modifier.size(56.dp),
                        tint = when {
                            showResult && resultSuccess -> Color(0xFF4CAF50)
                            showResult && !resultSuccess -> Color(0xFFF44336)
                            else -> Color.White
                        }
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = if (showResult) resultMessage else statusMessage,
                        fontSize = 16.sp,
                        color = if (showResult) {
                            if (resultSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                        } else Color.White,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // SINGLE button - "Registre su huella"
            Button(
                onClick = {
                    onUserActivity()
                    if (!isProcessing) {
                        isProcessing = true
                        statusMessage = "Esperando huella..."
                        onBiometricRequest()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White.copy(alpha = 0.2f)
                ),
                shape = RoundedCornerShape(16.dp),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(48.dp),
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = "Registre su huella",
                        fontSize = 22.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
