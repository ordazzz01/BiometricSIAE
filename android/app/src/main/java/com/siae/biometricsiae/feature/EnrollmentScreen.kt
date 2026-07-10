package com.siae.biometricsiae.feature

import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siae.biometricsiae.BiometricHelper
import com.siae.biometricsiae.data.EnrollmentRepository
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    biometricHelper: BiometricHelper,
    repository: EnrollmentRepository,
    onBack: () -> Unit
) {
    var fullName by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var statusType by remember { mutableStateOf<StatusType?>(null) }
    var isEnrolled by remember { mutableStateOf(false) }

    // Listen for biometric results
    LaunchedEffect(Unit) {
        biometricHelper.resultFlow.collect { result ->
            when (result) {
                is BiometricHelper.BiometricResult.Success -> {
                    isLoading = true
                    statusMessage = "Procesando registro..."

                    try {
                        val deviceId = Build.SERIAL ?: Build.MODEL
                        val appVersion = "1.0.0"

                        val (personId, eventType) = repository.upsertPersonByRfc(
                            fullName = fullName.trim(),
                            rfc = rfc.trim(),
                            deviceId = deviceId,
                            appVersion = appVersion
                        )

                        repository.updateDeviceLastSeen(deviceId)

                        statusMessage = when (eventType) {
                            "create" -> "✓ Persona registrada exitosamente"
                            "update" -> "✓ Persona actualizada exitosamente"
                            "enroll" -> "✓ Huella vinculada exitosamente"
                            else -> "✓ Registro exitoso"
                        }
                        statusType = StatusType.SUCCESS
                        isEnrolled = true
                    } catch (e: Exception) {
                        statusMessage = "Error: ${e.message}"
                        statusType = StatusType.ERROR
                    }

                    isLoading = false
                }
                is BiometricHelper.BiometricResult.UserCancelled -> {
                    statusMessage = "Registro cancelado"
                    statusType = StatusType.WARNING
                }
                is BiometricHelper.BiometricResult.Error -> {
                    statusMessage = "Error biométrico: ${result.message}"
                    statusType = StatusType.ERROR
                }
                is BiometricHelper.BiometricResult.NotAvailable -> {
                    statusMessage = "Biometría no disponible en este dispositivo"
                    statusType = StatusType.ERROR
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Registrar Biometría") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Icon(
                Icons.Default.Fingerprint,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Alta Biométrica",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Registre una persona con su huella",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Form fields
            OutlinedTextField(
                value = fullName,
                onValueChange = { fullName = it },
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading && !isEnrolled
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = rfc,
                onValueChange = { rfc = it.uppercase() },
                label = { Text("RFC") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !isLoading && !isEnrolled
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Status message
            if (statusMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = when (statusType) {
                            StatusType.SUCCESS -> Color(0xFFE8F5E9)
                            StatusType.ERROR -> Color(0xFFFFEBEE)
                            StatusType.WARNING -> Color(0xFFFFF3E0)
                            null -> MaterialTheme.colorScheme.surface
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = when (statusType) {
                                StatusType.SUCCESS -> Icons.Default.CheckCircle
                                StatusType.ERROR -> Icons.Default.Error
                                StatusType.WARNING -> Icons.Default.Error
                                null -> Icons.Default.Fingerprint
                            },
                            contentDescription = null,
                            tint = when (statusType) {
                                StatusType.SUCCESS -> Color(0xFF4CAF50)
                                StatusType.ERROR -> Color(0xFFF44336)
                                StatusType.WARNING -> Color(0xFFFF9800)
                                null -> MaterialTheme.colorScheme.primary
                            }
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = statusMessage,
                            color = when (statusType) {
                                StatusType.SUCCESS -> Color(0xFF2E7D32)
                                StatusType.ERROR -> Color(0xFFC62828)
                                StatusType.WARNING -> Color(0xFFE65100)
                                null -> MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Enroll button
            Button(
                onClick = {
                    // Validate fields
                    if (fullName.isBlank()) {
                        statusMessage = "Ingrese el nombre completo"
                        statusType = StatusType.ERROR
                        return@Button
                    }
                    if (rfc.isBlank() || rfc.length < 10) {
                        statusMessage = "Ingrese un RFC válido (mínimo 10 caracteres)"
                        statusType = StatusType.ERROR
                        return@Button
                    }
                    if (!biometricHelper.canAuthenticate()) {
                        statusMessage = "Biometría no disponible. Configure huella en el dispositivo."
                        statusType = StatusType.ERROR
                        return@Button
                    }

                    // Reset and trigger biometric
                    statusMessage = ""
                    statusType = null
                    isEnrolled = false
                    isLoading = true

                    biometricHelper.authenticate(
                        activity = androidx.fragment.app.FragmentActivity::class.java.cast(
                            context as? androidx.fragment.app.FragmentActivity
                        ),
                        title = "Registrar biometría",
                        subtitle = "Confirme su identidad"
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = !isLoading && !isEnrolled && fullName.isNotBlank() && rfc.isNotBlank()
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                } else {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Registrar con huella",
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info text
            Text(
                text = "La huella se usa solo para validar la identidad. No se almacena la huella digital.",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            // Success actions
            if (isEnrolled) {
                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        fullName = ""
                        rfc = ""
                        statusMessage = ""
                        statusType = null
                        isEnrolled = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Registrar otra persona")
                }
            }
        }
    }
}

enum class StatusType {
    SUCCESS, ERROR, WARNING
}
