package com.pictionis.ap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pictionis.ap.auth.AuthViewModel
import com.pictionis.ap.viewModel.GameViewModel
import com.pictionis.ap.model.Game

@Composable
fun LobbyScreen(
    gameId: String,
    authViewModel: AuthViewModel,
    onStartGame: () -> Unit, // Appelé quand la partie démarre
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var gameState by remember { mutableStateOf<Game?>(null) }

    // Ecoute en temps réel les changements de la partie
    LaunchedEffect(gameId) {
        gameViewModel.listenToGame(gameId) { game ->
            gameState = game
            // Si la partie démarre, on appelle onStartGame
            if (game?.started == true) onStartGame()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Lobby de la partie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text("ID : $gameId")
        Spacer(modifier = Modifier.height(16.dp))

        Text("Joueurs :")
        gameState?.players?.forEach { playerId ->
            Text(playerId)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bouton démarrer visible uniquement pour le host
        if (gameState?.hostId == currentUser?.uid && gameState?.started == false) {
            Button(onClick = {
                gameViewModel.startGame(gameId)
            }) {
                Text("Démarrer la partie")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Quitter le lobby") }
    }
}