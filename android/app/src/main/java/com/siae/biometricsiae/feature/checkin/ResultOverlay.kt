package com.siae.biometricsiae.feature.checkin

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import kotlinx.coroutines.delay

@Composable
fun ResultOverlay(
    result: CheckinUiResult,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(true) }

    LaunchedEffect(result) {
        delay(3000)
        visible = false
        delay(300)
        onDismiss()
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (result) {
                    is CheckinUiResult.Success -> if (result.isDuplicate) Color(0xFFFFF3E0) else Color(0xFFE8F5E9)
                    is CheckinUiResult.Error -> Color(0xFFFFEBEE)
                    is CheckinUiResult.Offline -> Color(0xFFFFF8E1)
                    is CheckinUiResult.BiometricCancelled -> Color(0xFFF3E5F5)
                    is CheckinUiResult.BiometricFailed -> Color(0xFFFFEBEE)
                    is CheckinUiResult.RequiresPin -> Color(0xFFE3F2FD)
                }
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Icon
                Icon(
                    imageVector = when (result) {
                        is CheckinUiResult.Success -> if (result.isDuplicate) Icons.Default.Warning else Icons.Default.CheckCircle
                        is CheckinUiResult.Error -> Icons.Default.Error
                        is CheckinUiResult.Offline -> Icons.Default.CloudOff
                        is CheckinUiResult.BiometricCancelled -> Icons.Default.Cancel
                        is CheckinUiResult.BiometricFailed -> Icons.Default.ErrorOutline
                        is CheckinUiResult.RequiresPin -> Icons.Default.Lock
                    },
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = when (result) {
                        is CheckinUiResult.Success -> if (result.isDuplicate) Color(0xFFFF9800) else Color(0xFF4CAF50)
                        is CheckinUiResult.Error -> Color(0xFFF44336)
                        is CheckinUiResult.Offline -> Color(0xFFFFC107)
                        is CheckinUiResult.BiometricCancelled -> Color(0xFF9E9E9E)
                        is CheckinUiResult.BiometricFailed -> Color(0xFFF44336)
                        is CheckinUiResult.RequiresPin -> Color(0xFF2196F3)
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Title
                Text(
                    text = when (result) {
                        is CheckinUiResult.Success -> if (result.isDuplicate) "Registro Duplicado" else "Registro Exitoso"
                        is CheckinUiResult.Error -> "Error"
                        is CheckinUiResult.Offline -> "Guardado Localmente"
                        is CheckinUiResult.BiometricCancelled -> "Cancelado"
                        is CheckinUiResult.BiometricFailed -> "Autenticación Fallida"
                        is CheckinUiResult.RequiresPin -> "PIN Requerido"
                    },
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Details
                when (result) {
                    is CheckinUiResult.Success -> {
                        Text(
                            text = result.employeeName,
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = result.type,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = result.timestamp,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        if (result.isDuplicate) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Este registro ya fue capturado recientemente",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFFF9800),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    is CheckinUiResult.Error -> {
                        Text(
                            text = result.message,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    is CheckinUiResult.Offline -> {
                        Text(
                            text = "${result.employeeName} - ${result.type}",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Se sincronizará cuando haya conexión",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                    is CheckinUiResult.BiometricCancelled -> {
                        Text(
                            text = "La autenticación fue cancelada",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    is CheckinUiResult.BiometricFailed -> {
                        Text(
                            text = "No se pudo verificar la identidad",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    is CheckinUiResult.RequiresPin -> {
                        Text(
                            text = "Ingrese PIN de supervisor",
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}
