package io.eshort.user.presentation.screens.comments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.model.Comment
import io.eshort.user.domain.repository.VideoRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CommentsViewModel @Inject constructor(
    private val videoRepo: VideoRepo
) : ViewModel() {

    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadComments(videoId: String) {
        viewModelScope.launch {
            videoRepo.videoComments(videoId).collect {
                _comments.value = it
                _isLoading.value = false
            }
        }
    }

    fun postComment(videoId: String, text: String) {
        viewModelScope.launch { videoRepo.postComment(videoId, text) }
    }
}
