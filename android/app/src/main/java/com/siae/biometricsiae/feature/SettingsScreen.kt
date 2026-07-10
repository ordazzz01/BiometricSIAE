package com.siae.biometricsiae.feature

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siae.biometricsiae.BiometricHelper
import com.siae.biometricsiae.data.Employee
import com.siae.biometricsiae.data.FirestoreRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    biometricHelper: BiometricHelper,
    repository: FirestoreRepository,
    onBack: () -> Unit,
    onEnrollment: () -> Unit = {}
) {
    var isAuthorized by remember { mutableStateOf(false) }
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var showFingerprintDialog by remember { mutableStateOf(false) }

    // Request device fingerprint on entry
    LaunchedEffect(Unit) {
        biometricHelper.resultFlow.collect { result ->
            when (result) {
                is BiometricHelper.BiometricResult.Success -> {
                    isAuthorized = true
                    employees = repository.getEmployees()
                    isLoading = false
                }
                is BiometricHelper.BiometricResult.UserCancelled -> {
                    onBack()
                }
                is BiometricHelper.BiometricResult.Error -> {
                    onBack()
                }
                else -> {}
            }
        }
    }

    // Trigger biometric on first load
    LaunchedEffect(Unit) {
        // The biometric request will be triggered from MainActivity
        // For now, auto-authorize for testing
        isAuthorized = true
        employees = repository.getEmployees()
        isLoading = false
    }

    if (!isAuthorized) {
        // Show loading while waiting for biometric
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("Verificando identidad...")
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Device info
            item {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Dispositivo", style = MaterialTheme.typography.titleMedium)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Modelo: ${android.os.Build.MODEL}")
                        Text("Versión: 1.0.0")
                    }
                }
            }

            // Enrollment button
            item {
                Button(
                    onClick = onEnrollment,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Alta Biométrica")
                }
            }

            // Fingerprint section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Asignación de Huellas",
                    style = MaterialTheme.typography.titleLarge
                )
            }

            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                items(employees) { employee ->
                    EmployeeCard(
                        employee = employee,
                        onAssignFingerprint = {
                            selectedEmployee = employee
                            showFingerprintDialog = true
                        }
                    )
                }
            }
        }
    }

    if (showFingerprintDialog && selectedEmployee != null) {
        FingerprintAssignDialog(
            employee = selectedEmployee!!,
            repository = repository,
            biometricHelper = biometricHelper,
            onDismiss = {
                showFingerprintDialog = false
                selectedEmployee = null
            }
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onAssignFingerprint: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(employee.name, style = MaterialTheme.typography.titleMedium)
                Text("RFC: ${employee.rfc}", style = MaterialTheme.typography.bodySmall)
                Text(
                    "Depto: ${employee.department} | Turno: ${employee.shift}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            if (employee.fingerprintId != null) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Huella registrada",
                    tint = Color(0xFF4CAF50)
                )
            } else {
                Button(onClick = onAssignFingerprint) {
                    Text("Asignar")
                }
            }
        }
    }
}

@Composable
fun FingerprintAssignDialog(
    employee: Employee,
    repository: FirestoreRepository,
    biometricHelper: BiometricHelper,
    onDismiss: () -> Unit
) {
    var message by remember { mutableStateOf("Toque su huella para asignar a ${employee.name}") }
    var isProcessing by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        biometricHelper.resultFlow.collect { result ->
            when (result) {
                is BiometricHelper.BiometricResult.Success -> {
                    val fingerprintId = "fp_${System.currentTimeMillis()}"
                    val success = repository.updateEmployeeFingerprint(employee.id, fingerprintId)
                    if (success) {
                        message = "✓ Huella asignada exitosamente"
                    } else {
                        message = "Error al guardar"
                    }
                    isProcessing = false
                }
                is BiometricHelper.BiometricResult.UserCancelled -> {
                    message = "Cancelado"
                    isProcessing = false
                }
                is BiometricHelper.BiometricResult.Error -> {
                    message = "Error: ${result.message}"
                    isProcessing = false
                }
                else -> {}
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar Huella") },
        text = {
            Column {
                Text("Empleado: ${employee.name}")
                Text("RFC: ${employee.rfc}")
                Spacer(modifier = Modifier.height(16.dp))
                Text(message)
                if (isProcessing) {
                    Spacer(modifier = Modifier.height(8.dp))
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    isProcessing = true
                    message = "Esperando huella..."
                    // Biometric will be triggered from activity
                },
                enabled = !isProcessing
            ) {
                Text("Activar Sensor")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
