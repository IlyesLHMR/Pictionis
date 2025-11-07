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
    onGameCreated: (String) -> Unit
) {
    val currentUser by authViewModel.currentUser.collectAsState()
    var createdGameId by remember { mutableStateOf<String?>(null) }
    var username by remember { mutableStateOf<String?>(null) }

    // Récupérer le pseudo de l'utilisateur
    LaunchedEffect(currentUser) {
        if (currentUser != null) {
            authViewModel.getCurrentUsername { username = it }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Créer une partie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        username?.let {
            Text("Ton pseudo : $it", style = MaterialTheme.typography.bodyLarge)
            Text("Les autres pourront rejoindre avec ce pseudo", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
        } ?: run {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(8.dp))
            Text("Chargement de ton pseudo...", style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = {
                val hostId = currentUser?.uid ?: return@Button
                val hostUsername = username ?: ""

                if (hostUsername.isBlank()) return@Button

                gameViewModel.createGame(hostId, hostUsername) { gameId ->
                    createdGameId = gameId
                    onGameCreated(gameId)
                }
            },
            enabled = username != null && username!!.isNotBlank()
        ) {
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