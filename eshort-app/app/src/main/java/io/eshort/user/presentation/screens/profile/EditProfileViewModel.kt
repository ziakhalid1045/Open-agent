package io.eshort.user.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.model.UserProfile
import io.eshort.user.domain.repository.SocialRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val socialRepo: SocialRepo
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            socialRepo.profileStream(uid).collect { _profile.value = it }
        }
    }

    fun saveProfile(displayName: String, username: String, bio: String) {
        viewModelScope.launch {
            _isSaving.value = true
            socialRepo.updateProfile(displayName, username, bio).fold(
                onSuccess = { _isSaved.value = true },
                onFailure = { _isSaving.value = false }
            )
        }
    }
}
