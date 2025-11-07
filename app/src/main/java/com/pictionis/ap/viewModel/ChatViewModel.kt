package com.pictionis.ap.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pictionis.ap.model.ChatMessage
import kotlinx.coroutines.tasks.await

class ChatViewModel : ViewModel() {
    private val db: DatabaseReference = FirebaseDatabase
        .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("games")

    private val usersDb: DatabaseReference = FirebaseDatabase
        .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("users")

    private val chatListeners = mutableMapOf<String, ValueEventListener>()

    // Cache des pseudos pour éviter de refaire des requêtes
    private val usernameCache = mutableMapOf<String, String>()

    fun sendMessage(gameId: String, message: ChatMessage) {
        val gameRef = db.child(gameId).child("chat")
        gameRef.get().addOnSuccessListener { snapshot ->
            val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }.toMutableList()
            messages.add(message)
            gameRef.setValue(messages)
        }.addOnFailureListener { e -> Log.e("ChatViewModel", "sendMessage failed: ${e.message}") }
    }

    fun listenToChat(gameId: String, onMessages: (List<ChatMessage>) -> Unit) {
        if (chatListeners.containsKey(gameId)) return
        val ref = db.child(gameId).child("chat")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = snapshot.children.mapNotNull { it.getValue(ChatMessage::class.java) }
                onMessages(messages)
            }
            override fun onCancelled(error: DatabaseError) { Log.e("ChatViewModel", "listenToChat cancelled: ${error.message}") }
        }
        ref.addValueEventListener(listener)
        chatListeners[gameId] = listener
    }

    fun stopListeningToChat(gameId: String) {
        val listener = chatListeners.remove(gameId) ?: return
        db.child(gameId).child("chat").removeEventListener(listener)
    }

    // Récupère le pseudo d'un UID (avec cache)
    suspend fun getUsernameFromUid(uid: String): String {
        // Vérifier le cache
        usernameCache[uid]?.let { return it }

        // Sinon récupérer depuis DB
        return try {
            val snap = usersDb.child(uid).child("username").get().await()
            val username = snap.getValue(String::class.java) ?: uid
            usernameCache[uid] = username
            username
        } catch (e: Exception) {
            uid // Si erreur, retourner l'UID
        }
    }
}