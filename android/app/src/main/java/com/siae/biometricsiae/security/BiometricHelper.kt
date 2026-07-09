package com.siae.biometricsiae.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val biometricManager = BiometricManager.from(context)
    private val _resultChannel = Channel<BiometricResult>()
    val resultFlow = _resultChannel.receiveAsFlow()

    sealed class BiometricResult {
        data object Success : BiometricResult()
        data class Error(val message: String) : BiometricResult()
        data object UserCancelled : BiometricResult()
        data object NotAvailable : BiometricResult()
        data object NotEnrolled : BiometricResult()
    }

    fun canAuthenticate(): Boolean {
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun getAvailableBiometricType(): String {
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometría disponible"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Sin hardware biométrico"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware no disponible"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Sin biometría registrada"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Actualización de seguridad requerida"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "No soportado"
            else -> "Error desconocido"
        }
    }

    fun authenticate(
        activity: FragmentActivity,
        title: String = "Confirmar asistencia",
        subtitle: String = "Validar identidad",
        negativeButtonText: String = "Cancelar"
    ) {
        val executor = ContextCompat.getMainExecutor(context)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                _resultChannel.trySend(BiometricResult.Success)
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        _resultChannel.trySend(BiometricResult.UserCancelled)
                    }
                    BiometricPrompt.ERROR_HW_NOT_PRESENT -> {
                        _resultChannel.trySend(BiometricResult.NotAvailable)
                    }
                    BiometricPrompt.ERROR_NO_BIOMETRICS -> {
                        _resultChannel.trySend(BiometricResult.NotEnrolled)
                    }
                    else -> {
                        _resultChannel.trySend(BiometricResult.Error(errString.toString()))
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                // Don't send result here - it's called on each failed attempt
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo)
    }
}
