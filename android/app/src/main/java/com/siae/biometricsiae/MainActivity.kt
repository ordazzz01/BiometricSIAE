package com.siae.biometricsiae

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.siae.biometricsiae.data.FirestoreRepository
import com.siae.biometricsiae.feature.CheckinScreen
import com.siae.biometricsiae.feature.SettingsScreen
import com.siae.biometricsiae.ui.theme.BiometricSIAETheme

class MainActivity : FragmentActivity() {

    private lateinit var biometricHelper: BiometricHelper
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        biometricHelper = BiometricHelper(this)

        setContent {
            BiometricSIAETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var currentScreen by remember { mutableStateOf("checkin") }

                    when (currentScreen) {
                        "checkin" -> CheckinScreen(
                            biometricHelper = biometricHelper,
                            repository = repository,
                            onSettingsClick = { currentScreen = "settings" },
                            onBiometricRequest = {
                                biometricHelper.authenticate(
                                    activity = this@MainActivity,
                                    title = "Confirmar asistencia",
                                    subtitle = "Toque su huella"
                                )
                            }
                        )
                        "settings" -> SettingsScreen(
                            repository = repository,
                            onBack = { currentScreen = "checkin" }
                        )
                    }
                }
            }
        }
    }
}
