package com.eshort.app.viewmodel

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshort.app.data.model.User
import com.eshort.app.data.repository.AuthRepository
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val user: User? = null,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _state = MutableStateFlow(AuthState())
    val state: StateFlow<AuthState> = _state.asStateFlow()

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        viewModelScope.launch {
            authRepository.authState.collect { firebaseUser ->
                if (firebaseUser != null) {
                    val profile = authRepository.getCurrentUserProfile()
                    _state.value = AuthState(isLoggedIn = true, user = profile)
                } else {
                    _state.value = AuthState(isLoggedIn = false)
                }
            }
        }
    }

    fun handleSignInResult(data: Intent?) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                val account = task.getResult(ApiException::class.java)
                val result = authRepository.signInWithGoogle(account)
                result.fold(
                    onSuccess = {
                        val profile = authRepository.getCurrentUserProfile()
                        _state.value = AuthState(isLoggedIn = true, user = profile)
                    },
                    onFailure = {
                        _state.value = _state.value.copy(
                            isLoading = false,
                            error = it.message ?: "Sign in failed"
                        )
                    }
                )
            } catch (e: ApiException) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = "Google sign in failed: ${e.message}"
                )
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
        googleSignInClient.signOut()
        _state.value = AuthState(isLoggedIn = false)
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
