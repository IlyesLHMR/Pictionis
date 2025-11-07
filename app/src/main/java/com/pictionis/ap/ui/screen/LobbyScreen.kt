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
    onStartGame: () -> Unit,
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var gameState by remember { mutableStateOf<Game?>(null) }
    var hasNavigated by remember { mutableStateOf(false) }

    // Ecoute en temps réel les changements de la partie
    LaunchedEffect(gameId) {
        gameViewModel.listenToGame(gameId) { game ->
            val previousStarted = gameState?.started
            gameState = game

            // Si la partie démarre (transition false -> true), naviguer vers le jeu
            if (game?.started == true && previousStarted == false && !hasNavigated) {
                hasNavigated = true
                onStartGame()
            }
        }
    }

    DisposableEffect(gameId) {
        onDispose {
            gameViewModel.stopListeningToGame(gameId)
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

        gameState?.let { game ->
            Text("Joueurs : ${game.players.size}", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            game.players.forEach { playerId ->
                var playerUsername by remember { mutableStateOf<String?>(null) }

                LaunchedEffect(playerId) {
                    authViewModel.getUsernameById(playerId) { username ->
                        playerUsername = username
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = playerUsername ?: playerId,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (playerId == game.hostId) {
                        Text(" (Créateur)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Message d'attente
            if (!game.started && game.players.size < 2) {
                Text(
                    "En attente d'un autre joueur...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Bouton démarrer visible uniquement pour le host
            if (game.hostId == currentUser?.uid && !game.started) {
                val canStart = game.players.size >= 2

                Button(
                    onClick = { gameViewModel.startGame(gameId) },
                    enabled = canStart
                ) {
                    Text(if (canStart) "Démarrer la partie" else "Attente d'un joueur...")
                }
            } else if (!game.started) {
                Text(
                    "En attente que le créateur démarre la partie...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } ?: run {
            CircularProgressIndicator()
            Text("Chargement de la partie...")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Quitter le lobby") }
    }
}