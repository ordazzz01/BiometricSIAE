package com.siae.biometricsiae.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.siae.biometricsiae.data.EnrollmentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import java.security.KeyPair
import java.security.Signature
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * BiometricEnrollmentManager - Orquesta el flujo de enrolamiento biométrico.
 *
 * IMPORTANTE: Esta clase NO almacena huellas digitales.
 * Usa Android Keystore + BiometricPrompt con CryptoObject para:
 * 1. Generar una clave criptográfica en el Keystore
 * 2. Exigir autenticación biométrica para usar la clave
 * 3. Firmar un payload de verificación
 * 4. Guardar el credentialId (hash de la clave pública) en Firestore
 *
 * El credentialId es una referencia criptográfica, NO la huella digital.
 */
class BiometricEnrollmentManager(
    private val context: Context,
    private val enrollmentRepository: EnrollmentRepository
) {
    private val keystoreManager = KeystoreManager()
    private val _resultChannel = Channel<EnrollmentResult>()
    val resultFlow = _resultChannel.receiveAsFlow()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    sealed class EnrollmentResult {
        data object Verifying : EnrollmentResult()
        data object GeneratingKey : EnrollmentResult()
        data object Authenticating : EnrollmentResult()
        data class Success(
            val credentialId: String,
            val personId: String,
            val eventType: String
        ) : EnrollmentResult()
        data class Error(val message: String) : EnrollmentResult()
        data object Cancelled : EnrollmentResult()
        data object BiometricNotAvailable : EnrollmentResult()
        data object BiometricNotEnrolled : EnrollmentResult()
    }

    /**
     * Verifica si el dispositivo soporta biometría fuerte.
     */
    fun canAuthenticate(): Boolean {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Obtiene el estado de disponibilidad biométrica.
     */
    fun getBiometricStatus(): String {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometría fuerte disponible"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Sin hardware biométrico"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware no disponible"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Sin biometría enrolada en el dispositivo"
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> "Actualización de seguridad requerida"
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> "No soportado"
            else -> "Error desconocido"
        }
    }

    /**
     * Ejecuta el flujo completo de enrolamiento biométrico.
     *
     * @param activity FragmentActivity para mostrar BiometricPrompt
     * @param personId ID de la persona (o vacío si es nueva)
     * @param fullName Nombre completo
     * @param rfc RFC de la persona
     */
    fun enroll(
        activity: FragmentActivity,
        personId: String,
        fullName: String,
        rfc: String
    ) {
        // Paso 1: Verificar disponibilidad biométrica
        if (!canAuthenticate()) {
            _resultChannel.trySend(EnrollmentResult.BiometricNotAvailable)
            return
        }

        val deviceId = Build.SERIAL ?: Build.MODEL
        val keyAlias = keystoreManager.generateKeyAlias(personId, deviceId)

        // Paso 2: Generar clave en Keystore
        _resultChannel.trySend(EnrollmentResult.GeneratingKey)

        try {
            val keyPair = keystoreManager.generateKey(personId, deviceId)

            // Paso 3: Preparar CryptoObject
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
            val cryptoObject = BiometricPrompt.CryptoObject(cipher)

            // Paso 4: Lanzar BiometricPrompt con CryptoObject
            _resultChannel.trySend(EnrollmentResult.Authenticating)
            authenticateWithCrypto(activity, cryptoObject, fullName, rfc, deviceId, keyAlias)

        } catch (e: Exception) {
            _resultChannel.trySend(EnrollmentResult.Error("Error al generar clave: ${e.message}"))
        }
    }

    private fun authenticateWithCrypto(
        activity: FragmentActivity,
        cryptoObject: BiometricPrompt.CryptoObject,
        fullName: String,
        rfc: String,
        deviceId: String,
        keyAlias: String
    ) {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)

                // Paso 5: Generar payload y firmar
                val payload = "${rfc}|${deviceId}|${System.currentTimeMillis()}"
                val signature = keystoreManager.signPayload(keyAlias, payload)

                if (signature != null) {
                    // Paso 6: Calcular credentialId
                    val credentialId = keystoreManager.getPublicKeyFingerprint(keyAlias)

                    if (credentialId != null) {
                        // Paso 7: Guardar en Firestore
                        processEnrollment(
                            fullName = fullName,
                            rfc = rfc,
                            deviceId = deviceId,
                            keyAlias = keyAlias,
                            credentialId = credentialId,
                            payload = payload,
                            signature = signature
                        )
                    } else {
                        _resultChannel.trySend(EnrollmentResult.Error("No se pudo generar credentialId"))
                    }
                } else {
                    _resultChannel.trySend(EnrollmentResult.Error("Error al firmar payload"))
                }
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errorCode, errString)
                when (errorCode) {
                    BiometricPrompt.ERROR_USER_CANCELED,
                    BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                        _resultChannel.trySend(EnrollmentResult.Cancelled)
                    }
                    else -> {
                        _resultChannel.trySend(EnrollmentResult.Error(errString.toString()))
                    }
                }
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
            }
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Registrar biometría")
            .setSubtitle("Confirme su identidad para registrar")
            .setNegativeButtonText("Cancelar")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
            )
            .build()

        val biometricPrompt = BiometricPrompt(activity, executor, callback)
        biometricPrompt.authenticate(promptInfo, cryptoObject)
    }

    private fun processEnrollment(
        fullName: String,
        rfc: String,
        deviceId: String,
        keyAlias: String,
        credentialId: String,
        payload: String,
        signature: ByteArray
    ) {
        scope.launch {
            try {
                val (personId, eventType) = enrollmentRepository.enrollPerson(
                    fullName = fullName,
                    rfc = rfc,
                    deviceId = deviceId,
                    keyAlias = keyAlias,
                    credentialId = credentialId,
                    publicKeyFingerprint = credentialId,
                    algorithm = "EC"
                )

                _resultChannel.trySend(
                    EnrollmentResult.Success(
                        credentialId = credentialId,
                        personId = personId,
                        eventType = eventType
                    )
                )
            } catch (e: Exception) {
                _resultChannel.trySend(
                    EnrollmentResult.Error("Error al guardar: ${e.message}")
                )
            }
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_AES,
            "AndroidKeyStore"
        )
        val spec = KeyGenParameterSpec.Builder(
            "enrollment_key",
            KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
        )
            .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
            .setUserAuthenticationRequired(false)
            .build()
        keyGenerator.init(spec)
        return keyGenerator.generateKey()
    }
}
