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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.siae.biometricsiae.data.EnrollmentRepository
import com.siae.biometricsiae.security.BiometricEnrollmentManager
import kotlinx.coroutines.launch

/**
 * EnrollmentScreen - Pantalla de registro biométrico.
 *
 * Usa BiometricEnrollmentManager que genera claves en Android Keystore
 * y firma un payload de verificación. El credentialId (SHA-256 de la
 * clave pública) se guarda en Firestore como referencia criptográfica.
 *
 * IMPORTANTE: Esta pantalla NO almacena huellas digitales.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnrollmentScreen(
    enrollmentRepository: EnrollmentRepository,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? androidx.fragment.app.FragmentActivity
    val scope = rememberCoroutineScope()

    val enrollmentManager = remember {
        BiometricEnrollmentManager(context, enrollmentRepository)
    }

    var fullName by remember { mutableStateOf("") }
    var rfc by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf("") }
    var statusType by remember { mutableStateOf<StatusType?>(null) }
    var isEnrolled by remember { mutableStateOf(false) }
    var credentialId by remember { mutableStateOf("") }
    var biometricStatus by remember { mutableStateOf("") }

    // Check biometric availability
    LaunchedEffect(Unit) {
        biometricStatus = enrollmentManager.getBiometricStatus()
    }

    // Listen for enrollment results
    LaunchedEffect(Unit) {
        enrollmentManager.resultFlow.collect { result ->
            when (result) {
                is BiometricEnrollmentManager.EnrollmentResult.Verifying -> {
                    statusMessage = "Verificando disponibilidad biométrica..."
                    statusType = null
                }
                is BiometricEnrollmentManager.EnrollmentResult.GeneratingKey -> {
                    statusMessage = "Generando clave criptográfica..."
                    statusType = null
                }
                is BiometricEnrollmentManager.EnrollmentResult.Authenticating -> {
                    statusMessage = "Esperando autenticación biométrica..."
                    statusType = null
                }
                is BiometricEnrollmentManager.EnrollmentResult.Success -> {
                    isLoading = false
                    statusMessage = "✓ Registro exitoso"
                    statusType = StatusType.SUCCESS
                    credentialId = result.credentialId
                    isEnrolled = true
                }
                is BiometricEnrollmentManager.EnrollmentResult.Error -> {
                    isLoading = false
                    statusMessage = "Error: ${result.message}"
                    statusType = StatusType.ERROR
                }
                is BiometricEnrollmentManager.EnrollmentResult.Cancelled -> {
                    isLoading = false
                    statusMessage = "Registro cancelado"
                    statusType = StatusType.WARNING
                }
                is BiometricEnrollmentManager.EnrollmentResult.BiometricNotAvailable -> {
                    isLoading = false
                    statusMessage = "Biometría no disponible. Configure huella en el dispositivo."
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

            Spacer(modifier = Modifier.height(8.dp))

            // Biometric status
            Text(
                text = biometricStatus,
                fontSize = 12.sp,
                color = if (biometricStatus.contains("disponible"))
                    Color(0xFF4CAF50) else Color(0xFFF44336)
            )

            Spacer(modifier = Modifier.height(24.dp))

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

            // Show credential ID if enrolled
            if (isEnrolled && credentialId.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFE3F2FD)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Credential ID generado:",
                            fontSize = 12.sp,
                            color = Color(0xFF1565C0)
                        )
                        Text(
                            text = credentialId.take(32) + "...",
                            fontSize = 10.sp,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                            color = Color(0xFF1976D2)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Este identificador es un hash SHA-256 de la clave pública generada por el Keystore. NO es la huella digital.",
                            fontSize = 10.sp,
                            color = Color(0xFF757575)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Enroll button
            Button(
                onClick = {
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
                    if (!enrollmentManager.canAuthenticate()) {
                        statusMessage = biometricStatus
                        statusType = StatusType.ERROR
                        return@Button
                    }
                    if (activity == null) {
                        statusMessage = "Error: no se pudo obtener la actividad"
                        statusType = StatusType.ERROR
                        return@Button
                    }

                    isLoading = true
                    isEnrolled = false
                    credentialId = ""

                    enrollmentManager.enroll(
                        activity = activity,
                        personId = "",
                        fullName = fullName.trim(),
                        rfc = rfc.trim()
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

            // Security note
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFF5F5F5)
                )
            ) {
                Text(
                    text = "⚠️ Esta app NO almacena huellas digitales. Solo guarda un identificador criptográfico (credentialId) generado por el Keystore del dispositivo, usado como referencia para vincular la persona a su huella en este dispositivo específico.",
                    fontSize = 11.sp,
                    color = Color(0xFF757575),
                    modifier = Modifier.padding(12.dp)
                )
            }

            // New enrollment button
            if (isEnrolled) {
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        fullName = ""
                        rfc = ""
                        statusMessage = ""
                        statusType = null
                        isEnrolled = false
                        credentialId = ""
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
