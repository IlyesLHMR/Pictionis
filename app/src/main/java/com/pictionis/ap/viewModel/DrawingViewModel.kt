package com.pictionis.ap.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pictionis.ap.model.Stroke

class DrawingViewModel : ViewModel() {
    private val db: DatabaseReference = FirebaseDatabase
        .getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app")
        .getReference("games")

    private val strokeListeners = mutableMapOf<String, ChildEventListener>()

    fun sendStroke(gameId: String, stroke: Stroke) {
        val strokesRef = db.child(gameId).child("strokes")
        strokesRef.push().setValue(stroke)
            .addOnFailureListener { e -> Log.e("DrawingViewModel", "sendStroke failed: ${e.message}") }
    }

    fun listenToStrokes(gameId: String, onStroke: (Stroke) -> Unit, onClear: () -> Unit = {}) {
        if (strokeListeners.containsKey(gameId)) return
        val strokesRef = db.child(gameId).child("strokes")
        val listener = object : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val stroke = snapshot.getValue(Stroke::class.java)
                if (stroke != null) onStroke(stroke)
            }
            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {
                // Quand tous les strokes sont supprimés, appeler onClear
                strokesRef.get().addOnSuccessListener {
                    if (!it.exists()) {
                        onClear()
                    }
                }
            }
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) { Log.e("DrawingViewModel", "listenToStrokes cancelled: ${error.message}") }
        }
        strokesRef.addChildEventListener(listener)
        strokeListeners[gameId] = listener
    }

    fun stopListeningToStrokes(gameId: String) {
        val listener = strokeListeners.remove(gameId) ?: return
        db.child(gameId).child("strokes").removeEventListener(listener)
    }

    fun clearStrokes(gameId: String) {
        db.child(gameId).child("strokes").removeValue()
    }
}