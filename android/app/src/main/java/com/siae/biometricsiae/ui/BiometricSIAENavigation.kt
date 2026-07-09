package com.siae.biometricsiae.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.siae.biometricsiae.feature.auth.LoginScreen
import com.siae.biometricsiae.feature.checkin.CheckinScreen

@Composable
fun BiometricSIAENavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("checkin") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }

        composable("checkin") {
            CheckinScreen()
        }
    }
}
