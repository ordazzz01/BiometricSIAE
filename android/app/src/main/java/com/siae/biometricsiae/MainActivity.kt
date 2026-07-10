package com.siae.biometricsiae

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.fragment.app.FragmentActivity
import com.siae.biometricsiae.data.FirestoreRepository
import com.siae.biometricsiae.feature.CheckinScreen
import com.siae.biometricsiae.feature.SettingsScreen
import com.siae.biometricsiae.ui.theme.BiometricSIAETheme
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {

    private lateinit var biometricHelper: BiometricHelper
    private val repository = FirestoreRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        biometricHelper = BiometricHelper(this)

        // Full screen mode
        enableFullScreen()

        // Keep screen on
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        setContent {
            BiometricSIAETheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val scope = rememberCoroutineScope()
                    var currentScreen by remember { mutableStateOf("checkin") }

                    when (currentScreen) {
                        "checkin" -> CheckinScreen(
                            biometricHelper = biometricHelper,
                            repository = repository,
                            onSettingsClick = {
                                // Request device fingerprint to access settings
                                biometricHelper.authenticate(
                                    activity = this@MainActivity,
                                    title = "Acceso a Configuración",
                                    subtitle = "Verifique su identidad con huella del dispositivo"
                                )
                                scope.launch {
                                    biometricHelper.resultFlow.collect { result ->
                                        when (result) {
                                            is BiometricHelper.BiometricResult.Success -> {
                                                currentScreen = "settings"
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            },
                            onBiometricRequest = {
                                biometricHelper.authenticate(
                                    activity = this@MainActivity,
                                    title = "Confirmar asistencia",
                                    subtitle = "Toque su huella"
                                )
                            },
                            onExit = {
                                // Request device fingerprint to exit
                                biometricHelper.authenticate(
                                    activity = this@MainActivity,
                                    title = "Salir de la aplicación",
                                    subtitle = "Solo puede salir con huella del dispositivo"
                                )
                                scope.launch {
                                    biometricHelper.resultFlow.collect { result ->
                                        when (result) {
                                            is BiometricHelper.BiometricResult.Success -> {
                                                finish()
                                            }
                                            else -> {}
                                        }
                                    }
                                }
                            }
                        )
                        "settings" -> SettingsScreen(
                            biometricHelper = biometricHelper,
                            repository = repository,
                            onBack = { currentScreen = "checkin" }
                        )
                    }
                }
            }
        }
    }

    private fun enableFullScreen() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.statusBars())
        controller.hide(WindowInsetsCompat.Type.navigationBars())
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Disabled - kiosk mode
    }
}
