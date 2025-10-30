package com.pictionis.ap.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.pictionis.ap.auth.AuthViewModel
import com.pictionis.ap.ui.screens.LoginScreen
import com.pictionis.ap.ui.screens.SignupScreen

@Composable
fun AuthNavHost(authViewModel: AuthViewModel, modifier: Modifier = Modifier) {
    var showSignup by remember { mutableStateOf(false) }

    if (showSignup) {
        SignupScreen(
            authViewModel = authViewModel,
            onSignupSuccess = { /* HomeScreen sera affiché automatiquement */ },
            onNavigateToLogin = { showSignup = false }
        )
    } else {
        LoginScreen(
            authViewModel = authViewModel,
            onLoginSuccess = { /* HomeScreen sera affiché automatiquement */ },
            onNavigateToSignup = { showSignup = true }
        )
    }
}
