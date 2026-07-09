package com.siae.biometricsiae.feature.settings

import androidx.lifecycle.ViewModel
import com.siae.biometricsiae.data.remote.firebase.FirestoreManager
import com.siae.biometricsiae.security.BiometricHelper
import com.siae.biometricsiae.security.KeystoreManager
import com.siae.biometricsiae.security.RootDetector
import com.siae.biometricsiae.security.TamperDetector
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val keyStoreManager: KeystoreManager,
    private val biometricHelper: BiometricHelper,
    private val rootDetector: RootDetector,
    private val tamperDetector: TamperDetector,
    private val firestoreManager: FirestoreManager
) : ViewModel() {

    private val _state = MutableStateFlow(SettingsState())
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _state.value = SettingsState(
            currentBranch = keyStoreManager.getBranchId() ?: "No configurada",
            deviceId = keyStoreManager.getDeviceId() ?: "No disponible",
            biometricType = biometricHelper.getAvailableBiometricType(),
            isKioskMode = true,
            faceRequired = false,
            geofenceEnabled = false,
            duplicateWindowMinutes = 5
        )
    }

    fun onKioskModeChanged(enabled: Boolean) {
        _state.value = _state.value.copy(isKioskMode = enabled)
    }

    fun onFaceRequiredChanged(required: Boolean) {
        _state.value = _state.value.copy(faceRequired = required)
    }

    fun onGeofenceEnabledChanged(enabled: Boolean) {
        _state.value = _state.value.copy(geofenceEnabled = enabled)
    }

    fun onDuplicateWindowChanged(minutes: Int) {
        _state.value = _state.value.copy(duplicateWindowMinutes = minutes)
    }

    fun getSecurityStatus(): String {
        val rootStatus = rootDetector.getSecurityStatus()
        val tamperStatus = tamperDetector.getTamperStatus()
        
        return buildString {
            appendLine("Estado de Seguridad:")
            appendLine("Root: ${if (rootStatus.isRooted) "Detectado" else "No detectado"}")
            appendLine("Emulador: ${if (rootStatus.isEmulator) "Sí" else "No"}")
            appendLine("Debuggable: ${if (rootStatus.isDebuggable) "Sí" else "No"}")
            appendLine("Debugger: ${if (tamperStatus.isDebuggerAttached) "Conectado" else "Desconectado"}")
            appendLine("Hora manipulada: ${if (tamperStatus.isBadTime) "Sí" else "No"}")
        }
    }
}
