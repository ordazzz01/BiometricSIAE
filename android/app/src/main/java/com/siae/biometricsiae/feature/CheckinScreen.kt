package com.siae.biometricsiae.feature

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siae.biometricsiae.data.AttendanceRecord
import com.siae.biometricsiae.data.Employee
import com.siae.biometricsiae.data.FirestoreRepository
import com.siae.biometricsiae.BiometricHelper
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(
    biometricHelper: BiometricHelper,
    repository: FirestoreRepository,
    onSettingsClick: () -> Unit,
    onBiometricRequest: () -> Unit = {}
) {
    var currentTime by remember { mutableStateOf("") }
    var currentDate by remember { mutableStateOf("") }
    var statusMessage by remember { mutableStateOf("Toque su huella para registrar") }
    var statusColor by remember { mutableStateOf(Color.White) }
    var lastCheckin by remember { mutableStateOf<Pair<String, String>?>(null) }
    var isProcessing by remember { mutableStateOf(false) }
    var showResult by remember { mutableStateOf(false) }
    var resultSuccess by remember { mutableStateOf(true) }
    var resultMessage by remember { mutableStateOf("") }

    // Animated background
    val infiniteTransition = rememberInfiniteTransition(label = "background")
    val colorIndex by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(20000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "colorIndex"
    )

    val gradientColors = listOf(
        Color(0xFF1B5E20), // Verde oscuro
        Color(0xFF0D47A1), // Azul oscuro
        Color(0xFF4A148C), // Morado
        Color(0xFFE65100), // Naranja
        Color(0xFF1B5E20), // Verde oscuro (loop)
    )

    val currentIndex = colorIndex.toInt().coerceIn(0, 3)
    val nextIndex = (currentIndex + 1).coerceAtMost(4)
    val fraction = colorIndex - currentIndex

    val animatedColor1 = lerp(gradientColors[currentIndex], gradientColors[nextIndex], fraction)
    val animatedColor2 = lerp(gradientColors[(currentIndex + 1).coerceAtMost(4)], gradientColors[(currentIndex + 2).coerceAtMost(4)], fraction)

    // Update clock
    LaunchedEffect(Unit) {
        while (true) {
            val now = Date()
            currentTime = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now)
            currentDate = SimpleDateFormat("EEEE, d 'de' MMMM", Locale("es")).format(now)
            delay(1000)
        }
    }

    // Listen for biometric results
    LaunchedEffect(Unit) {
        biometricHelper.resultFlow.collect { result ->
            when (result) {
                is BiometricHelper.BiometricResult.Success -> {
                    isProcessing = true
                    statusMessage = "Procesando..."

                    // For now, save with a placeholder fingerprint ID
                    // In real app, the fingerprint ID would come from the system
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
                        resultSuccess = true
                        resultMessage = "Asistencia registrada"
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
                    statusMessage = "Toque su huella para registrar"
                    statusColor = Color.White
                }
                is BiometricHelper.BiometricResult.UserCancelled -> {
                    statusMessage = "Cancelado"
                    statusColor = Color(0xFFFFC107)
                    delay(2000)
                    statusMessage = "Toque su huella para registrar"
                    statusColor = Color.White
                }
                is BiometricHelper.BiometricResult.Error -> {
                    statusMessage = "Error: ${result.message}"
                    statusColor = Color(0xFFF44336)
                    delay(2000)
                    statusMessage = "Toque su huella para registrar"
                    statusColor = Color.White
                }
                is BiometricHelper.BiometricResult.NotAvailable -> {
                    statusMessage = "Huella no disponible"
                    statusColor = Color(0xFFFF9800)
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checador Biométrico") },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "Configuración")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(animatedColor1, animatedColor2)
                    )
                )
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
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
                    fontSize = 20.sp,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(48.dp))

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
                            modifier = Modifier.size(64.dp),
                            tint = when {
                                showResult && resultSuccess -> Color(0xFF4CAF50)
                                showResult && !resultSuccess -> Color(0xFFF44336)
                                else -> Color.White
                            }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (showResult) resultMessage else statusMessage,
                            fontSize = 18.sp,
                            color = if (showResult) {
                                if (resultSuccess) Color(0xFF4CAF50) else Color(0xFFF44336)
                            } else Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Last checkin
                lastCheckin?.let { (name, time) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black.copy(alpha = 0.2f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "Última checada:",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                            Text(
                                text = "$name - $time",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Fingerprint button
                Button(
                    onClick = {
                        if (!isProcessing) {
                            isProcessing = true
                            statusMessage = "Esperando huella..."
                            onBiometricRequest()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(72.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    ),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isProcessing
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    } else {
                        Icon(
                            Icons.Default.Fingerprint,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Registrar Asistencia",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
