package io.eshort.user.presentation.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.model.ShortVideo
import io.eshort.user.domain.model.UserProfile
import io.eshort.user.domain.repository.ChatRepo
import io.eshort.user.domain.repository.SocialRepo
import io.eshort.user.domain.repository.VideoRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val socialRepo: SocialRepo,
    private val videoRepo: VideoRepo,
    private val chatRepo: ChatRepo,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _userVideos = MutableStateFlow<List<ShortVideo>>(emptyList())
    val userVideos: StateFlow<List<ShortVideo>> = _userVideos.asStateFlow()

    private val _isFollowing = MutableStateFlow(false)
    val isFollowing: StateFlow<Boolean> = _isFollowing.asStateFlow()

    fun loadProfile(uid: String) {
        viewModelScope.launch {
            socialRepo.profileStream(uid).collect { profile ->
                _profile.value = profile
                val myUid = auth.currentUser?.uid
                _isFollowing.value = profile?.followers?.contains(myUid) == true
            }
        }
        viewModelScope.launch {
            videoRepo.userVideos(uid).collect { _userVideos.value = it }
        }
    }

    fun toggleFollow(uid: String) {
        viewModelScope.launch {
            val nowFollowing = socialRepo.toggleFollow(uid)
            _isFollowing.value = nowFollowing
        }
    }

    fun startChat(otherUid: String, onCreated: (String) -> Unit) {
        viewModelScope.launch {
            val convoId = chatRepo.getOrCreateConversation(otherUid)
            onCreated(convoId)
        }
    }
}
