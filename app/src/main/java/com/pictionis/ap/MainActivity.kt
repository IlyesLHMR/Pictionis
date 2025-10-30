package com.pictionis.ap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.pictionis.ap.auth.AuthViewModel
import com.pictionis.ap.ui.theme.PictionisTheme
import com.pictionis.ap.ui.navigation.AuthNavHost
import com.pictionis.ap.ui.navigation.GameNavHost
import com.pictionis.ap.ui.screen.HomeScreen


class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PictionisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    AppContent(authViewModel = authViewModel, modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppContent(authViewModel: AuthViewModel, modifier: Modifier = Modifier) {
    val currentUser by authViewModel.currentUser.collectAsState()

    if (currentUser != null) {
        GameNavHost(authViewModel = authViewModel, modifier = modifier)
    } else {
        AuthNavHost(authViewModel = authViewModel, modifier = modifier)
    }
}

