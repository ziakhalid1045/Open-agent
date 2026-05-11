package io.eshort.user.presentation.screens.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.model.ShortVideo
import io.eshort.user.domain.repository.VideoRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FeedViewModel @Inject constructor(
    private val videoRepo: VideoRepo
) : ViewModel() {

    private val _feedVideos = MutableStateFlow<List<ShortVideo>>(emptyList())
    val feedVideos: StateFlow<List<ShortVideo>> = _feedVideos.asStateFlow()

    init {
        viewModelScope.launch {
            videoRepo.feedVideos().collect { _feedVideos.value = it }
        }
    }

    fun toggleLike(videoId: String) {
        viewModelScope.launch { videoRepo.toggleLike(videoId) }
    }

    fun recordView(videoId: String) {
        viewModelScope.launch { videoRepo.addView(videoId) }
    }

    fun shareVideo(videoId: String) {
        viewModelScope.launch { videoRepo.addShare(videoId) }
    }
}
