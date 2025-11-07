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
import kotlinx.coroutines.launch

@Composable
fun JoinGameScreen(
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel(),
    onGameJoined: (String) -> Unit // Ajout du paramètre pour navigation
) {
    var pseudoInput by remember { mutableStateOf("") }
    var joinResult by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val currentUser by authViewModel.currentUser.collectAsState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Rejoindre une partie", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        Text("Entre le pseudo du créateur de la partie", style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = pseudoInput,
            onValueChange = { pseudoInput = it },
            label = { Text("Pseudo du créateur") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val userId = currentUser?.uid
                if (pseudoInput.isNotBlank() && userId != null) {
                    isLoading = true
                    joinResult = null

                    scope.launch {
                        val gameId = gameViewModel.findGameByHostUsername(pseudoInput)
                        if (gameId != null) {
                            gameViewModel.joinGame(gameId, userId) { success ->
                                isLoading = false
                                if (success) {
                                    joinResult = "Partie rejointe !"
                                    onGameJoined(gameId)
                                } else {
                                    joinResult = "Impossible de rejoindre la partie"
                                }
                            }
                        } else {
                            isLoading = false
                            joinResult = "Aucune partie en attente trouvée pour ce pseudo. Le créateur doit être dans le lobby."
                        }
                    }
                } else {
                    joinResult = "Pseudo ou utilisateur manquant"
                }
            },
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Text("Rejoindre")
            }
        }

        joinResult?.let {
            Spacer(modifier = Modifier.height(16.dp))
            Text(it, color = if (it.contains("rejointe")) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onBack) { Text("Retour") }
    }
}