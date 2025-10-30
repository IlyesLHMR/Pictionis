package com.pictionis.ap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictionis.ap.auth.AuthViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    onCreateGame: () -> Unit = {},
    onJoinGame: () -> Unit = {}
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Bienvenue, ${currentUser?.email ?: "Utilisateur"} !",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onCreateGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Créer une partie")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onJoinGame,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Rejoindre une partie")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { authViewModel.logout() },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Text("Se déconnecter", color = MaterialTheme.colorScheme.onError)
        }
    }
}
