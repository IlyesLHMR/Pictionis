package com.pictionis.ap.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pictionis.ap.model.Game

class GameViewModel : ViewModel() {
    private val db: DatabaseReference = FirebaseDatabase
        .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("games")

    // keep references to value listeners so we can detach them
    private val gameValueListeners = mutableMapOf<String, ValueEventListener>()

    // Création d'une partie : initialise tous les champs du modèle Game
    fun createGame(hostId: String, onSuccess: (String) -> Unit = {}) {
        val gameId = db.push().key ?: return
        val game = Game(
            id = gameId,
            hostId = hostId,
            players = listOf(hostId),
            started = false,
            currentWord = "",
            drawerId = hostId,
            scores = mapOf(hostId to 0),
            round = 1,
            chat = emptyList()
        )
        db.child(gameId).setValue(game)
            .addOnSuccessListener { onSuccess(gameId) }
            .addOnFailureListener { error ->
                Log.e("GameViewModel", "Erreur lors de la création de la partie : ${error.message}")
            }
    }

    // Rejoindre une partie : ajoute le joueur, met à jour les scores
    fun joinGame(gameId: String, userId: String, onResult: (Boolean) -> Unit) {
        val gameRef = db.child(gameId)
        gameRef.get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            if (game != null) {
                val players = game.players.toMutableList()
                if (!players.contains(userId)) players.add(userId)
                val scores = game.scores.toMutableMap()
                if (!scores.containsKey(userId)) scores[userId] = 0
                gameRef.child("players").setValue(players)
                gameRef.child("scores").setValue(scores)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { onResult(false) }
            } else {
                onResult(false)
            }
        }.addOnFailureListener { onResult(false) }
    }

    // Listen to game value changes (stores listener for later detach)
    fun listenToGame(gameId: String, onUpdate: (Game?) -> Unit) {
        if (gameValueListeners.containsKey(gameId)) return

        val ref = db.child(gameId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(Game::class.java)
                onUpdate(game)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("GameViewModel", "listenToGame cancelled: ${error.message}")
                onUpdate(null)
            }
        }
        ref.addValueEventListener(listener)
        gameValueListeners[gameId] = listener
    }

    fun stopListeningToGame(gameId: String) {
        val listener = gameValueListeners.remove(gameId) ?: return
        db.child(gameId).removeEventListener(listener)
    }

    // Démarre la partie (host uniquement)
    fun startGame(gameId: String) {
        db.child(gameId).child("started").setValue(true)
    }

    // Met à jour le mot à deviner et le dessinateur
    fun setCurrentWordAndDrawer(gameId: String, word: String, drawerId: String) {
        db.child(gameId).child("currentWord").setValue(word)
        db.child(gameId).child("drawerId").setValue(drawerId)
    }

    // Met à jour le score d'un joueur
    fun updateScore(gameId: String, userId: String, newScore: Int) {
        db.child(gameId).child("scores").child(userId).setValue(newScore)
    }

    // Change le round
    fun setRound(gameId: String, round: Int) {
        db.child(gameId).child("round").setValue(round)
    }
}