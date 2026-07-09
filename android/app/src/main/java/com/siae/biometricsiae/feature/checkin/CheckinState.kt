package com.siae.biometricsiae.feature.checkin

import com.siae.biometricsiae.domain.model.Employee

data class CheckinState(
    val isLoading: Boolean = false,
    val selectedEmployee: Employee? = null,
    val employees: List<Employee> = emptyList(),
    val searchQuery: String = "",
    val lastCheckinResult: CheckinUiResult? = null,
    val isOffline: Boolean = false,
    val pendingSyncCount: Int = 0,
    val errorMessage: String? = null,
    val biometricType: String = "",
    val currentBranchId: String = "",
    val currentBranchName: String = ""
)

sealed class CheckinUiResult {
    data class Success(
        val employeeName: String,
        val type: String,
        val timestamp: String,
        val isDuplicate: Boolean = false
    ) : CheckinUiResult()

    data class Error(
        val message: String
    ) : CheckinUiResult()

    data class Offline(
        val employeeName: String,
        val type: String
    ) : CheckinUiResult()

    data object BiometricCancelled : CheckinUiResult()
    data object BiometricFailed : CheckinUiResult()
    data object RequiresPin : CheckinUiResult()
}

enum class CheckinType(val displayName: String) {
    ENTRY("Entrada"),
    EXIT("Salida"),
    BREAK("Descanso"),
    BREAK_RETURN("Regreso")
}
