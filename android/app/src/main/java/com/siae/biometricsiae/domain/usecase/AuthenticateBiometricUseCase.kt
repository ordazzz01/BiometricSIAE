package com.siae.biometricsiae.domain.usecase

import com.siae.biometricsiae.security.BiometricHelper
import com.siae.biometricsiae.security.BiometricHelper.BiometricResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class AuthenticateBiometricUseCase @Inject constructor(
    private val biometricHelper: BiometricHelper
) {
    operator fun invoke(): Flow<BiometricResult> {
        return biometricHelper.resultFlow
    }

    fun canAuthenticate(): Boolean {
        return biometricHelper.canAuthenticate()
    }

    fun getAvailableBiometricType(): String {
        return biometricHelper.getAvailableBiometricType()
    }
}
