package io.eshort.user.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import io.eshort.user.core.auth.AuthViewModel
import io.eshort.user.presentation.navigation.EShortNavHost
import io.eshort.user.presentation.theme.EShortTheme

@AndroidEntryPoint
class EShortActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EShortTheme {
                val authViewModel: AuthViewModel = hiltViewModel()
                val authState by authViewModel.authState.collectAsState()
                EShortNavHost(
                    authState = authState,
                    authViewModel = authViewModel
                )
            }
        }
    }
}
