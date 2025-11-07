package com.pictionis.ap.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val db by lazy {
        FirebaseDatabase.getInstance("https://pictionis-8733b-default-rtdb.europe-west1.firebasedatabase.app").reference
    }

    private val _currentUser = MutableStateFlow<FirebaseUser?>(auth.currentUser)
    val currentUser: StateFlow<FirebaseUser?> = _currentUser

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError

    fun login(email: String, password: String) {
        _authError.value = null
        viewModelScope.launch {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        _currentUser.value = auth.currentUser
                    } else {
                        _authError.value = task.exception?.message
                    }
                }
        }
    }

    // Inscription avec pseudo
    fun signup(email: String, password: String, username: String) {
        _authError.value = null
        viewModelScope.launch {
            try {
                // Créer le compte Firebase Auth d'abord
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = result.user?.uid

                if (uid != null) {
                    // Maintenant que l'utilisateur est authentifié, enregistrer le pseudo
                    db.child("users").child(uid).child("username").setValue(username).await()
                    _currentUser.value = auth.currentUser
                } else {
                    _authError.value = "Erreur lors de la création du compte"
                }
            } catch (e: Exception) {
                _authError.value = e.message ?: "Erreur lors de l'inscription"
            }
        }
    }

    // Récupère le pseudo de l'utilisateur connecté
    fun getCurrentUsername(onResult: (String?) -> Unit) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            onResult(null)
            return
        }
        viewModelScope.launch {
            try {
                val snap = db.child("users").child(uid).child("username").get().await()
                onResult(snap.getValue(String::class.java))
            } catch (_: Exception) {
                onResult(null)
            }
        }
    }

    // Récupère le pseudo d'un utilisateur par son UID
    fun getUsernameById(uid: String, onResult: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val snap = db.child("users").child(uid).child("username").get().await()
                onResult(snap.getValue(String::class.java))
            } catch (_: Exception) {
                onResult(null)
            }
        }
    }

    fun logout() {
        auth.signOut()
        _currentUser.value = null
    }
}
