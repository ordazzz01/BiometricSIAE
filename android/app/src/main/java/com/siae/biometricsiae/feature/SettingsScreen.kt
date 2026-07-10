package com.siae.biometricsiae.feature

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.siae.biometricsiae.data.Employee
import com.siae.biometricsiae.data.FirestoreRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: FirestoreRepository,
    onBack: () -> Unit
) {
    var employees by remember { mutableStateOf<List<Employee>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedEmployee by remember { mutableStateOf<Employee?>(null) }
    var showFingerprintDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        employees = repository.getEmployees()
        isLoading = false
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
            // Device info section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Información del Dispositivo",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Modelo: ${android.os.Build.MODEL}")
                        Text("Versión: 1.0.0")
                    }
                }
            }

            // Fingerprint enrollment section
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Asignación de Huellas",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
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
            onDismiss = {
                showFingerprintDialog = false
                selectedEmployee = null
            },
            onAssigned = {
                showFingerprintDialog = false
                selectedEmployee = null
                // Refresh list
            }
        )
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    onAssignFingerprint: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "RFC: ${employee.rfc}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Depto: ${employee.department} | Turno: ${employee.shift}",
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
                Button(
                    onClick = onAssignFingerprint
                ) {
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
    onDismiss: () -> Unit,
    onAssigned: () -> Unit
) {
    var isProcessing by remember { mutableStateOf(false) }
    var message by remember { mutableStateOf("Toque su huella para asignar a ${employee.name}") }

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
                    // In real app, trigger biometric enrollment
                    // For now, assign a placeholder ID
                    val fingerprintId = "fp_${System.currentTimeMillis()}"
                    // repository.updateEmployeeFingerprint(employee.id, fingerprintId)
                    message = "Huella asignada (simulado)"
                    isProcessing = false
                },
                enabled = !isProcessing
            ) {
                Text("Asignar Huella")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
