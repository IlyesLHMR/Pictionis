package com.pictionis.ap.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pictionis.ap.model.Game
import kotlinx.coroutines.tasks.await

class GameViewModel : ViewModel() {
    private val db: DatabaseReference = FirebaseDatabase
        .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("games")

    // keep references to value listeners so we can detach them
    private val gameValueListeners = mutableMapOf<String, ValueEventListener>()

    // Création d'une partie avec le pseudo du host
    fun createGame(hostId: String, hostUsername: String, onSuccess: (String) -> Unit = {}) {
        val gameId = db.push().key ?: return

        val game = Game(
            id = gameId,
            hostId = hostId,
            hostUsername = hostUsername,
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
                Log.e("GameViewModel", "Erreur création partie: ${error.message}")
            }
    }

    // Recherche une partie par pseudo du host (uniquement les parties non démarrées)
    suspend fun findGameByHostUsername(hostUsername: String): String? {
        return try {
            val snapshot = db.orderByChild("hostUsername")
                .equalTo(hostUsername)
                .get()
                .await()

            // Filtrer pour trouver uniquement les parties non démarrées
            var foundGameId: String? = null
            snapshot.children.forEach { child ->
                val game = child.getValue(Game::class.java)
                if (game != null && !game.started && foundGameId == null) {
                    foundGameId = child.key
                }
            }

            foundGameId
        } catch (e: Exception) {
            Log.e("GameViewModel", "Erreur recherche partie: ${e.message}")
            null
        }
    }

    // Rejoindre une partie : ajoute le joueur, met à jour les scores
    fun joinGame(gameId: String, userId: String, onResult: (Boolean) -> Unit) {
        val gameRef = db.child(gameId)
        gameRef.get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            if (game != null) {
                val players = game.players.toMutableList()
                if (!players.contains(userId)) {
                    players.add(userId)
                }

                val scores = game.scores.toMutableMap()
                if (!scores.containsKey(userId)) {
                    scores[userId] = 0
                }

                // Mettre à jour la partie entière pour garantir que les listeners se déclenchent
                val updatedGame = game.copy(
                    players = players,
                    scores = scores
                )

                gameRef.setValue(updatedGame)
                    .addOnSuccessListener { onResult(true) }
                    .addOnFailureListener { error ->
                        Log.e("GameViewModel", "Erreur ajout joueur: ${error.message}")
                        onResult(false)
                    }
            } else {
                onResult(false)
            }
        }.addOnFailureListener { error ->
            Log.e("GameViewModel", "Erreur récupération partie: ${error.message}")
            onResult(false)
        }
    }

    // Listen to game value changes (stores listener for later detach)
    fun listenToGame(gameId: String, onUpdate: (Game?) -> Unit) {
        val ref = db.child(gameId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val game = snapshot.getValue(Game::class.java)
                onUpdate(game)
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("GameViewModel", "Erreur listener: ${error.message}")
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

    // Fonctions pour le gameplay
    fun setCurrentWordAndDrawer(gameId: String, word: String, drawerId: String) {
        db.child(gameId).child("currentWord").setValue(word)
        db.child(gameId).child("drawerId").setValue(drawerId)
    }

    fun updateScore(gameId: String, userId: String, newScore: Int) {
        db.child(gameId).child("scores").child(userId).setValue(newScore)
    }

    fun setRound(gameId: String, round: Int) {
        db.child(gameId).child("round").setValue(round)
    }

    // Passer au joueur suivant
    fun nextTurn(gameId: String, onNewWord: (String) -> Unit) {
        val gameRef = db.child(gameId)
        gameRef.get().addOnSuccessListener { snapshot ->
            val game = snapshot.getValue(Game::class.java)
            if (game != null) {
                val players = game.players
                val currentDrawerIndex = players.indexOf(game.drawerId)
                val nextDrawerIndex = (currentDrawerIndex + 1) % players.size
                val nextDrawer = players[nextDrawerIndex]

                // Nouveau mot aléatoire
                val newWord = com.pictionis.ap.utils.WordsLibrary.getRandomWord()

                // Incrémenter le tour si on revient au premier joueur
                val newRound = if (nextDrawerIndex == 0) game.round + 1 else game.round

                // Mettre à jour Firebase
                gameRef.child("drawerId").setValue(nextDrawer)
                gameRef.child("currentWord").setValue(newWord)
                gameRef.child("round").setValue(newRound)

                onNewWord(newWord)
            }
        }.addOnFailureListener { error ->
            Log.e("GameViewModel", "Erreur nextTurn: ${error.message}")
        }
    }

    // Récupérer le pseudo d'un joueur
    suspend fun getUsernameFromUid(uid: String): String {
        return try {
            val usersDb = FirebaseDatabase
                .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
                .getReference("users")
            val snap = usersDb.child(uid).child("username").get().await()
            snap.getValue(String::class.java) ?: uid
        } catch (e: Exception) {
            uid
        }
    }
}