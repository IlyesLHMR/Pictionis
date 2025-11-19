package com.pictionis.ap.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.pictionis.ap.utils.WordsLibrary

@Composable
fun WordSelectionScreen(
    gameId: String,
    onWordSelected: (String) -> Unit
) {
    // Sélection automatique d'un mot aléatoire
    LaunchedEffect(Unit) {
        val randomWord = WordsLibrary.getRandomWord()
        onWordSelected(randomWord)
    }

    // Écran de chargement simple
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(16.dp))
            Text("Sélection du mot...")
        }
    }
}

