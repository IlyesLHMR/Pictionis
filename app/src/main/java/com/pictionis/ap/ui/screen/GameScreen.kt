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
    val scope = rememberCoroutineScope()

    var gameState by remember { mutableStateOf<Game?>(null) }
    val strokes = remember { mutableStateListOf<Stroke>() }
    var chatMessages by remember { mutableStateOf<List<ChatMessage>>(emptyList()) }
    var messageText by remember { mutableStateOf("") }

    // Cache des pseudos pour le chat
    val usernameCache = remember { mutableMapOf<String, String>() }

    // Attach listeners once when composable is launched for this gameId
    LaunchedEffect(gameId) {
        gameViewModel.listenToGame(gameId) { game -> gameState = game }
        drawingViewModel.listenToStrokes(gameId) { stroke ->
            if (strokes.none { it.id == stroke.id }) {
                strokes.add(stroke)
            }
        }
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
            Text(text = "Partie: $gameId", style = MaterialTheme.typography.titleMedium)
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
                    brushColor = androidx.compose.ui.graphics.Color.Black,
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
                    brushColor = androidx.compose.ui.graphics.Color.Black,
                    brushSize = 6f,
                    modifier = Modifier.fillMaxSize(),
                    onStrokeFinished = {}
                )
                // overlay text
                if (drawer == null) {
                    Text("En attente du dessinateur...", modifier = Modifier.align(Alignment.Center))
                } else {
                    Text("En attente du dessin du joueur : $drawer", modifier = Modifier.align(Alignment.Center))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Info partie simple
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Tour: ${gameState?.round ?: "-"}")
            Text("Drawer: ${gameState?.drawerId ?: "-"}")
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Chat area
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
                            // Récupérer le pseudo depuis le cache ou DB
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
                            Divider(modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            OutlinedTextField(
                value = messageText,
                onValueChange = { messageText = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Écrire un message...") }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    val uid = currentUser?.uid ?: return@Button
                    if (messageText.isNotBlank()) {
                        val chatMessage = ChatMessage(
                            userId = uid,
                            message = messageText.trim(),
                            timestamp = System.currentTimeMillis()
                        )
                        // use ChatViewModel to send message
                        chatViewModel.sendMessage(gameId, chatMessage)
                        messageText = ""
                    }
                }
            ) {
                Text("Envoyer")
            }
        }
    }
}