package com.siae.biometricsiae.security

import android.content.Context
import android.os.Build
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
import java.security.Signature

/**
 * BiometricEnrollmentManager - Orquesta el flujo de enrolamiento biométrico.
 *
 * FLUJO CORRECTO:
 * 1. Verificar disponibilidad biométrica
 * 2. Generar clave EC en Keystore (setUserAuthenticationRequired=true)
 * 3. Obtener la clave privada del Keystore
 * 4. Crear Signature con la clave privada
 * 5. Lanzar BiometricPrompt con CryptoObject(Signature)
 * 6. Si autenticación exitosa → Signature se inicializa → firmar payload
 * 7. Guardar credentialId en Firestore
 *
 * IMPORTANTE: La clave EC no se puede usar hasta que BiometricPrompt
 * confirme la identidad. El CryptoObject contiene la Signature que
 * será inicializada después de la autenticación biométrica.
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
    }

    fun canAuthenticate(): Boolean {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun getBiometricStatus(): String {
        return when (BiometricManager.from(context).canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> "Biometría fuerte disponible"
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> "Sin hardware biométrico"
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> "Hardware no disponible"
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> "Sin biometría enrolada"
            else -> "Biometría no disponible"
        }
    }

    /**
     * Ejecuta el flujo completo de enrolamiento biométrico.
     *
     * FLUJO:
     * 1. Generar clave EC en Keystore (sin biometría aún)
     * 2. Obtener la clave privada
     * 3. Crear Signature con la clave privada
     * 4. BiometricPrompt firma con CryptoObject
     * 5. Después de autenticación → Signature inicializada → firmar
     * 6. Guardar credentialId en Firestore
     */
    fun enroll(
        activity: FragmentActivity,
        personId: String,
        fullName: String,
        rfc: String
    ) {
        if (!canAuthenticate()) {
            _resultChannel.trySend(EnrollmentResult.BiometricNotAvailable)
            return
        }

        val deviceId = Build.SERIAL ?: Build.MODEL
        val keyAlias = keystoreManager.generateKeyAlias(personId, deviceId)

        _resultChannel.trySend(EnrollmentResult.GeneratingKey)

        try {
            // Paso 1: Generar clave EC en Keystore (sin biometría)
            val keyPair = keystoreManager.generateKey(personId, deviceId)

            // Paso 2: Obtener la clave privada
            val privateKey = keystoreManager.getPrivateKey(keyAlias)
            if (privateKey == null) {
                _resultChannel.trySend(EnrollmentResult.Error("No se pudo obtener la clave privada"))
                return
            }

            // Paso 3: Crear Signature con la clave privada
            // La Signature se inicializará DESPUÉS de la autenticación biométrica
            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(privateKey)

            // Paso 4: Crear CryptoObject con la Signature
            val cryptoObject = BiometricPrompt.CryptoObject(signature)

            // Paso 5: Lanzar BiometricPrompt
            _resultChannel.trySend(EnrollmentResult.Authenticating)
            authenticateWithCrypto(activity, cryptoObject, fullName, rfc, deviceId, keyAlias)

        } catch (e: Exception) {
            _resultChannel.trySend(EnrollmentResult.Error("Error: ${e.message}"))
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

                try {
                    // La Signature ya está inicializada después de la autenticación
                    val signature = result.cryptoObject?.signature
                    if (signature != null) {
                        // Firmar el payload
                        val payload = "${rfc}|${deviceId}|${System.currentTimeMillis()}"
                        signature.update(payload.toByteArray())
                        val signedData = signature.sign()

                        // Calcular credentialId
                        val credentialId = keystoreManager.getPublicKeyFingerprint(keyAlias)

                        if (credentialId != null && signedData != null) {
                            processEnrollment(
                                fullName = fullName,
                                rfc = rfc,
                                deviceId = deviceId,
                                keyAlias = keyAlias,
                                credentialId = credentialId
                            )
                        } else {
                            _resultChannel.trySend(EnrollmentResult.Error("No se pudo generar credentialId"))
                        }
                    } else {
                        _resultChannel.trySend(EnrollmentResult.Error("Firma no disponible"))
                    }
                } catch (e: Exception) {
                    _resultChannel.trySend(EnrollmentResult.Error("Error al procesar: ${e.message}"))
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
        credentialId: String
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
}
