package io.eshort.user.core.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.repository.AuthRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AuthState(
    val user: FirebaseUser? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepo: AuthRepo,
    val googleSignInClient: GoogleSignInClient
) : ViewModel() {

    private val _authState = MutableStateFlow(AuthState())
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    init {
        viewModelScope.launch {
            authRepo.authStateFlow.collect { user ->
                _authState.value = _authState.value.copy(
                    user = user,
                    isLoading = false
                )
            }
        }
    }

    fun handleSignInResult(account: GoogleSignInAccount) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null)
            authRepo.signInWithGoogle(account).fold(
                onSuccess = { user ->
                    _authState.value = _authState.value.copy(
                        user = user, isLoading = false
                    )
                },
                onFailure = { e ->
                    _authState.value = _authState.value.copy(
                        error = e.message, isLoading = false
                    )
                }
            )
        }
    }

    fun signOut() {
        googleSignInClient.signOut()
        authRepo.signOut()
        _authState.value = AuthState(isLoading = false)
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }
}
