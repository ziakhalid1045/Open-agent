package com.eshort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshort.app.data.model.User
import com.eshort.app.data.model.Video
import com.eshort.app.data.repository.UserRepository
import com.eshort.app.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProfileState(
    val user: User? = null,
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = true,
    val isCurrentUser: Boolean = false,
    val isFollowing: Boolean = false,
    val isUpdating: Boolean = false,
    val updateSuccess: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults.asStateFlow()

    fun loadProfile(userId: String, currentUserId: String?) {
        viewModelScope.launch {
            _profileState.value = ProfileState(isLoading = true)
            userRepository.getUserProfile(userId).collect { user ->
                _profileState.value = _profileState.value.copy(
                    user = user,
                    isLoading = false,
                    isCurrentUser = userId == currentUserId,
                    isFollowing = user?.followers?.contains(currentUserId) == true
                )
            }
        }
        viewModelScope.launch {
            videoRepository.getUserVideos(userId).collect { videos ->
                _profileState.value = _profileState.value.copy(videos = videos)
            }
        }
    }

    fun toggleFollow(targetUserId: String) {
        viewModelScope.launch {
            val result = userRepository.toggleFollow(targetUserId)
            result.onSuccess { isFollowing ->
                _profileState.value = _profileState.value.copy(isFollowing = isFollowing)
            }
        }
    }

    fun updateProfile(displayName: String, username: String, bio: String) {
        viewModelScope.launch {
            _profileState.value = _profileState.value.copy(isUpdating = true)
            val result = userRepository.updateProfile(
                displayName = displayName,
                username = username,
                bio = bio
            )
            result.fold(
                onSuccess = {
                    _profileState.value = _profileState.value.copy(
                        isUpdating = false,
                        updateSuccess = true
                    )
                },
                onFailure = {
                    _profileState.value = _profileState.value.copy(
                        isUpdating = false,
                        error = it.message
                    )
                }
            )
        }
    }

    fun searchUsers(query: String) {
        viewModelScope.launch {
            _searchResults.value = userRepository.searchUsers(query)
        }
    }

    fun clearUpdateSuccess() {
        _profileState.value = _profileState.value.copy(updateSuccess = false)
    }
}
