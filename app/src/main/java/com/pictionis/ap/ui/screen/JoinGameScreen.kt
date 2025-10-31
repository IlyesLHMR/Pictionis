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
fun JoinGameScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel()
) {
    var gameIdInput by remember { mutableStateOf("") }
    var joinResult by remember { mutableStateOf<String?>(null) }
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rejoindre une partie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = gameIdInput,
            onValueChange = { gameIdInput = it },
            label = { Text("ID de la partie") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            val userId = currentUser?.uid
            if (gameIdInput.isNotBlank() && userId != null) {
                gameViewModel.joinGame(gameIdInput, userId) { success ->
                    joinResult = if (success) "Partie rejointe !" else "Impossible de rejoindre"
                }
            } else {
                joinResult = "ID ou utilisateur manquant"
            }
        }) {
            Text("Rejoindre")
        }

        joinResult?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Retour") }
    }
}