package com.pictionis.ap.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pictionis.ap.auth.AuthViewModel
import com.pictionis.ap.model.ChatMessage
import com.pictionis.ap.model.Game
import com.pictionis.ap.model.Stroke
import com.pictionis.ap.viewModel.ChatViewModel
import com.pictionis.ap.viewModel.DrawingViewModel
import com.pictionis.ap.viewModel.GameViewModel
import com.pictionis.ap.ui.components.DrawingCanvas
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GameScreen(
    gameId: String,
    authViewModel: AuthViewModel,
    onBack: () -> Unit,
    gameViewModel: GameViewModel = viewModel(),
    drawingViewModel: DrawingViewModel = viewModel(),
    chatViewModel: ChatViewModel = viewModel()
) {
    val currentUser by authViewModel.currentUser.collectAsState()

    var gameState by remember { mutableStateOf<Game?>(null) }
    val strokes = remember { mutableStateListOf<Stroke>() }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var guessText by remember { mutableStateOf("") }
    var chatText by remember { mutableStateOf("") }

    // Cache des pseudos
    val usernameCache = remember { mutableMapOf<String, String>() }
    var drawerUsername by remember { mutableStateOf("...") }

    // Récupérer le pseudo du dessinateur
    LaunchedEffect(gameState?.drawerId) {
        gameState?.drawerId?.let { drawerId ->
            if (usernameCache.containsKey(drawerId)) {
                drawerUsername = usernameCache[drawerId]!!
            } else {
                val username = gameViewModel.getUsernameFromUid(drawerId)
                usernameCache[drawerId] = username
                drawerUsername = username
            }
        }
    }

    // Attach listeners once when composable is launched for this gameId
    LaunchedEffect(gameId) {
        gameViewModel.listenToGame(gameId) { game -> gameState = game }
        drawingViewModel.listenToStrokes(
            gameId = gameId,
            onStroke = { stroke ->
                if (strokes.none { it.id == stroke.id }) {
                    strokes.add(stroke)
                }
            },
            onClear = {
                // Quand Firebase efface les strokes, on efface aussi localement
                strokes.clear()
            }
        )
        chatViewModel.listenToChat(gameId) { messages ->
            chatMessages = messages
        }
    }

    // Detach listeners when the composable leaves composition (avoid leaks / duplicates)
    DisposableEffect(gameId) {
        onDispose {
            gameViewModel.stopListeningToGame(gameId)
            drawingViewModel.stopListeningToStrokes(gameId)
            chatViewModel.stopListeningToChat(gameId)
        }
    }

    val isDrawer = currentUser?.uid == gameState?.drawerId

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(text = "Pictionary", style = MaterialTheme.typography.titleLarge)
                Text(
                    text = "Dessinateur : $drawerUsername",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Button(onClick = onBack) { Text("Quitter") }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Drawing area (replaces placeholder)
        Text("Zone de dessin", style = MaterialTheme.typography.bodyLarge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .border(1.dp, Color.Gray)
                .background(Color(0xFFF5F5F5))
                .padding(8.dp)
        ) {
            val drawer = gameState?.drawerId
            if (isDrawer) {
                // Drawer can draw
                DrawingCanvas(
                    strokesRemote = strokes,
                    currentUserId = currentUser?.uid,
                    isDrawingEnabled = true,
                    brushColor = Color.Black,
                    brushSize = 6f,
                    modifier = Modifier.fillMaxSize(),
                    onStrokeFinished = { stroke ->
                        // immediate local add for UX then send to Firebase
                        if (strokes.none { it.id == stroke.id }) strokes.add(stroke)
                        drawingViewModel.sendStroke(gameId, stroke)
                    }
                )
            } else {
                // Viewer: still render strokes but not able to draw
                DrawingCanvas(
                    strokesRemote = strokes,
                    currentUserId = currentUser?.uid,
                    isDrawingEnabled = false,
                    brushColor = Color.Black,
                    brushSize = 6f,
                    modifier = Modifier.fillMaxSize(),
                    onStrokeFinished = {}
                )
                // overlay text - n'affiche que s'il n'y a pas de traits
                if (strokes.isEmpty()) {
                    if (drawer == null) {
                        Text("En attente du dessinateur...", modifier = Modifier.align(Alignment.Center))
                    } else {
                        Text("$drawerUsername dessine...", modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info partie
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Tour: ${gameState?.round ?: "-"}", style = MaterialTheme.typography.bodyMedium)
                    Text("Votre score: ${gameState?.scores?.get(currentUser?.uid) ?: 0}",
                         style = MaterialTheme.typography.bodyMedium,
                         color = MaterialTheme.colorScheme.primary)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Rôle: ${if (isDrawer) "Dessinateur" else "Devineur"}",
                         style = MaterialTheme.typography.bodyMedium)
                    if (!isDrawer) {
                        Text("Indices: ${gameState?.currentWord?.length ?: 0} lettres",
                             style = MaterialTheme.typography.bodySmall,
                             color = Color.Gray)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ZONE DE DEVINETTE (séparée du chat)
        if (isDrawer) {
            // Le dessinateur voit le mot à dessiner
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Mot à dessiner :",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = gameState?.currentWord?.uppercase() ?: "???",
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Les devineurs ont un champ dédié
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Devinez le mot", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = guessText,
                        onValueChange = { guessText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Tapez votre réponse...") },
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val uid = currentUser?.uid ?: return@Button
                            if (guessText.isNotBlank()) {
                                val guess = guessText.trim()
                                val currentWord = gameState?.currentWord?.trim() ?: ""

                                // Vérifier si c'est correct
                                if (guess.equals(currentWord, ignoreCase = true)) {
                                    // Bonne réponse !
                                    val successMessage = ChatMessage(
                                        userId = uid,
                                        message = "✓ A trouvé le mot : $currentWord !",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    chatViewModel.sendMessage(gameId, successMessage)

                                    // +10 points
                                    val currentScore = gameState?.scores?.get(uid) ?: 0
                                    gameViewModel.updateScore(gameId, uid, currentScore + 10)

                                    guessText = ""

                                    // Effacer le canvas et passer au joueur suivant
                                    CoroutineScope(Dispatchers.Main).launch {
                                        delay(2000) // 2 secondes pour lire le message
                                        drawingViewModel.clearStrokes(gameId) // Le listener gérera strokes.clear()
                                        delay(500) // Petit délai pour que Firebase synchronise
                                        gameViewModel.nextTurn(gameId) { newWord ->
                                            // Le nouveau mot est défini dans Firebase
                                        }
                                    }
                                } else {
                                    // Mauvaise réponse
                                    val wrongMessage = ChatMessage(
                                        userId = uid,
                                        message = "❌ $guess",
                                        timestamp = System.currentTimeMillis()
                                    )
                                    chatViewModel.sendMessage(gameId, wrongMessage)
                                    guessText = ""
                                }
                            }
                        }
                    ) {
                        Text("Deviner")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // CHAT (séparé de la devinette)
        Text("Chat", style = MaterialTheme.typography.titleMedium)
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .border(1.dp, Color.LightGray)
                .padding(8.dp)
        ) {
            if (chatMessages.isEmpty()) {
                Text("Aucun message", color = Color.Gray)
            } else {
                LazyColumn {
                    items(chatMessages) { msg ->
                        var displayName by remember { mutableStateOf(msg.userId) }

                        LaunchedEffect(msg.userId) {
                            if (usernameCache.containsKey(msg.userId)) {
                                displayName = usernameCache[msg.userId]!!
                            } else {
                                val username = chatViewModel.getUsernameFromUid(msg.userId)
                                usernameCache[msg.userId] = username
                                displayName = username
                            }
                        }

                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                            Text(text = displayName, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                            Text(text = msg.message)
                            HorizontalDivider(modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Zone de saisie du chat (optionnel - pour discuter)
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = chatText,
                onValueChange = { chatText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Message...") },
                singleLine = true
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val uid = currentUser?.uid ?: return@Button
                    if (chatText.isNotBlank()) {
                        val chatMessage = ChatMessage(
                            userId = uid,
                            message = chatText.trim(),
                            timestamp = System.currentTimeMillis()
                        )
                        chatViewModel.sendMessage(gameId, chatMessage)
                        chatText = ""
                    }
                }
            ) {
                Text("💬")
            }
        }
    }
}