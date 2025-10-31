package com.pictionis.ap.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.*
import com.pictionis.ap.auth.AuthViewModel
import com.pictionis.ap.ui.screen.HomeScreen
import com.pictionis.ap.ui.screen.CreateGameScreen
import com.pictionis.ap.ui.screen.JoinGameScreen

@Composable
fun GameNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> HomeScreen(
            authViewModel = authViewModel,
            onCreateGame = { currentScreen = "createGame" },
            onJoinGame = { currentScreen = "joinGame" }
        )
        "createGame" -> CreateGameScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = "home" }
        )
        "joinGame" -> JoinGameScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = "home" }
        )
    }
}