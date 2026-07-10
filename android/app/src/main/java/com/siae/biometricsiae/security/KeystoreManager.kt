package com.siae.biometricsiae.security

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.MessageDigest
import java.security.PrivateKey
import java.security.PublicKey
import java.security.Signature
import java.security.spec.ECGenParameterSpec

/**
 * KeystoreManager - Generación y gestión de claves criptográficas en Android Keystore.
 *
 * IMPORTANTE: Esta clase NO almacena ni procesa huellas digitales.
 * Solo genera claves criptográficas (EC) que se usan para firmar datos
 * después de una autenticación biométrica exitosa.
 *
 * El credentialId (SHA-256 de la clave pública) es una referencia criptográfica,
 * NO es la huella digital del usuario.
 */
class KeystoreManager {

    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply {
        load(null)
    }

    /**
     * Genera una clave EC en Android Keystore para una persona y dispositivo específico.
     *
     * @param personId ID de la persona
     * @param deviceId ID del dispositivo
     * @return KeyPair generado
     */
    fun generateKey(personId: String, deviceId: String): KeyPair {
        val keyAlias = "bio_${personId}_${deviceId}"

        val keyPairGenerator = KeyPairGenerator.getInstance(
            KeyProperties.KEY_ALGORITHM_EC,
            "AndroidKeyStore"
        )

        val spec = KeyGenParameterSpec.Builder(
            keyAlias,
            KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY
        )
            .setDigests(KeyProperties.DIGEST_SHA256)
            .setUserAuthenticationRequired(true)
            .setInvalidatedByBiometricEnrollment(true)
            .setKeySize(256)
            .build()

        keyPairGenerator.initialize(spec)
        return keyPairGenerator.generateKeyPair()
    }

    /**
     * Obtiene el fingerprint SHA-256 de la clave pública.
     * Este es el "credentialId" que se guarda en Firestore.
     *
     * IMPORTANTE: Esto NO es la huella digital. Es un hash de la clave pública
     * generada por el Keystore, usado solo como referencia criptográfica.
     */
    fun getPublicKeyFingerprint(keyAlias: String): String? {
        return try {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            val publicKey = entry?.certificate?.publicKey ?: return null

            val digest = MessageDigest.getInstance("SHA-256")
            val fingerprint = digest.digest(publicKey.encoded)

            fingerprint.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Firma un payload con la clave privada.
     * Solo funciona después de autenticación biométrica exitosa.
     */
    fun signPayload(keyAlias: String, payload: String): ByteArray? {
        return try {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            val privateKey = entry?.privateKey ?: return null

            val signature = Signature.getInstance("SHA256withECDSA")
            signature.initSign(privateKey)
            signature.update(payload.toByteArray())
            signature.sign()
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifica una firma con la clave pública.
     */
    fun verifySignature(keyAlias: String, payload: String, signature: ByteArray): Boolean {
        return try {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            val publicKey = entry?.certificate?.publicKey ?: return false

            val verifier = Signature.getInstance("SHA256withECDSA")
            verifier.initVerify(publicKey)
            verifier.update(payload.toByteArray())
            verifier.verify(signature)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Obtiene la clave privada para crear CryptoObject.
     */
    fun getPrivateKey(keyAlias: String): PrivateKey? {
        return try {
            val entry = keyStore.getEntry(keyAlias, null) as? KeyStore.PrivateKeyEntry
            entry?.privateKey
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Verifica si ya existe una clave para esa persona y dispositivo.
     */
    fun keyExists(personId: String, deviceId: String): Boolean {
        val keyAlias = "bio_${personId}_${deviceId}"
        return keyStore.containsAlias(keyAlias)
    }

    /**
     * Elimina una clave del Keystore.
     */
    fun deleteKey(personId: String, deviceId: String) {
        val keyAlias = "bio_${personId}_${deviceId}"
        if (keyStore.containsAlias(keyAlias)) {
            keyStore.deleteEntry(keyAlias)
        }
    }

    /**
     * Genera un alias único para la clave.
     */
    fun generateKeyAlias(personId: String, deviceId: String): String {
        return "bio_${personId}_${deviceId}"
    }
}
