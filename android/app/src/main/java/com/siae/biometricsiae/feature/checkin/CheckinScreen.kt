package com.siae.biometricsiae.feature.checkin

import androidx.compose.animation.*
import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.siae.biometricsiae.domain.model.Employee

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckinScreen(
    viewModel: CheckinViewModel = hiltViewModel()
) {
    val state by viewModel.state.collectAsState()
    var selectedType by remember { mutableStateOf(CheckinType.ENTRY) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Checador de Asistencias") },
                actions = {
                    // Offline indicator
                    if (state.isOffline) {
                        Icon(
                            Icons.Default.CloudOff,
                            contentDescription = "Sin conexión",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                    // Pending sync count
                    if (state.pendingSyncCount > 0) {
                        Badge(
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("${state.pendingSyncCount}")
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Status banner
            state.lastCheckinResult?.let { result ->
                ResultOverlay(
                    result = result,
                    onDismiss = { viewModel.clearResult() }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Error banner
            state.errorMessage?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = error,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Search bar
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                label = { Text("Buscar empleado por nombre o código") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Checkin type selector
            Text(
                text = "Tipo de registro",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CheckinType.entries.forEach { type ->
                    FilterChip(
                        selected = selectedType == type,
                        onClick = {
                            selectedType = type
                            viewModel.selectCheckinType(type)
                        },
                        label = { Text(type.displayName) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Employee list
            Text(
                text = "Seleccionar empleado",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.employees) { employee ->
                    EmployeeCard(
                        employee = employee,
                        isSelected = state.selectedEmployee?.id == employee.id,
                        onClick = { viewModel.selectEmployee(employee) }
                    )
                }
            }

            // Biometric button
            Button(
                onClick = {
                    // Get activity from context and start biometric auth
                    // viewModel.startBiometricAuthentication(activity)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled = state.selectedEmployee != null && !state.isLoading
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(
                        Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Confirmar asistencia",
                        fontSize = 18.sp
                    )
                }
            }

            // Biometric type info
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = viewModel.getBiometricType(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun EmployeeCard(
    employee: Employee,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Employee icon
            Icon(
                Icons.Default.Person,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Employee info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = employee.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = employee.code,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                employee.department?.let { dept ->
                    Text(
                        text = dept,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Status indicator
            Icon(
                if (employee.biometricEnrolled) Icons.Default.CheckCircle else Icons.Default.Warning,
                contentDescription = null,
                tint = if (employee.biometricEnrolled) Color.Green else Color Orange
            )
        }
    }
}
