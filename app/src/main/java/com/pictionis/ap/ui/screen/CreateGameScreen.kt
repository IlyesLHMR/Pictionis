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

@Composable
fun CreateGameScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel(),
    onGameCreated: (String) -> Unit // Ajout du paramètre pour navigation
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var createdGameId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Créer une partie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val hostId = currentUser?.uid ?: return@Button
            gameViewModel.createGame(hostId) { gameId ->
                createdGameId = gameId
                onGameCreated(gameId) // Navigation vers le lobby
            }
        }) {
            Text("Créer la partie")
        }

        createdGameId?.let { id ->
            Spacer(modifier = Modifier.height(16.dp))
            Text("ID de la partie : $id")
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Retour") }
    }
}