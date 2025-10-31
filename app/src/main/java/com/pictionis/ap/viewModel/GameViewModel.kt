package com.pictionis.ap.viewModel

import androidx.lifecycle.ViewModel
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pictionis.ap.model.Game

class GameViewModel : ViewModel() {
    private val db: DatabaseReference = FirebaseDatabase
        .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("games")

    fun createGame(hostId: String, onSuccess: (String) -> Unit = {}) {
        val gameId = db.push().key ?: return
        val game = Game(id = gameId, hostId = hostId, players = listOf(hostId), started = false)
        db.child(gameId).setValue(game)
            .addOnSuccessListener { onSuccess(gameId) }
            .addOnFailureListener { error ->
                android.util.Log.e("GameViewModel", "Erreur lors de la création de la partie : ${error.message}")
            }
    }

    fun joinGame(gameId: String, userId: String, onResult: (Boolean) -> Unit) {
        val gameRef = db.child(gameId)
        gameRef.child("players").get().addOnSuccessListener { snapshot ->
            val players = snapshot.children.mapNotNull { it.getValue(String::class.java) }.toMutableList()
            if (!players.contains(userId)) players.add(userId)
            gameRef.child("players").setValue(players)
                .addOnSuccessListener { onResult(true) }
                .addOnFailureListener { onResult(false) }
        }.addOnFailureListener { onResult(false) }
    }
}