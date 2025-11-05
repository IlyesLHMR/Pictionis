package com.pictionis.ap.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pictionis.ap.auth.AuthViewModel
import com.pictionis.ap.ui.screen.HomeScreen
import com.pictionis.ap.ui.screen.CreateGameScreen
import com.pictionis.ap.ui.screen.JoinGameScreen
import com.pictionis.ap.ui.screen.LobbyScreen
import com.pictionis.ap.ui.screen.GameScreen
import com.pictionis.ap.viewModel.GameViewModel

@Composable
fun GameNavHost(
    authViewModel: AuthViewModel,
    modifier: Modifier = Modifier
) {
    var currentScreen by remember { mutableStateOf("home") }
    var currentGameId by remember { mutableStateOf<String?>(null) }
    val gameViewModel: GameViewModel = viewModel()

    when (currentScreen) {
        "home" -> HomeScreen(
            authViewModel = authViewModel,
            onCreateGame = { currentScreen = "createGame" },
            onJoinGame = { currentScreen = "joinGame" }
        )
        "createGame" -> CreateGameScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = "home" },
            gameViewModel = gameViewModel,
            onGameCreated = { gameId ->
                currentGameId = gameId
                currentScreen = "lobby"
            }
        )
        "joinGame" -> JoinGameScreen(
            authViewModel = authViewModel,
            onBack = { currentScreen = "home" },
            gameViewModel = gameViewModel,
            onGameJoined = { gameId ->
                currentGameId = gameId
                currentScreen = "lobby"
            }
        )
        "lobby" -> currentGameId?.let { gameId ->
            LobbyScreen(
                gameId = gameId,
                authViewModel = authViewModel,
                gameViewModel = gameViewModel,
                onStartGame = { currentScreen = "game" }, // Navigue vers l'écran du jeu
                onBack = { currentScreen = "home"; currentGameId = null }
            )
        }
        "game" -> currentGameId?.let { gameId ->
            GameScreen(
                gameId = gameId,
                authViewModel = authViewModel,
                onBack = {
                    currentScreen = "home"
                    currentGameId = null
                },
                gameViewModel = gameViewModel
            )
        }
    }
}