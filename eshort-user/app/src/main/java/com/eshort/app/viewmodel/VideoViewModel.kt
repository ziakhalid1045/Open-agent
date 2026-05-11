package com.eshort.app.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshort.app.data.model.Comment
import com.eshort.app.data.model.Video
import com.eshort.app.data.repository.VideoRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class VideoFeedState(
    val videos: List<Video> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

data class UploadState(
    val isUploading: Boolean = false,
    val isSuccess: Boolean = false,
    val error: String? = null,
    val previewUrl: String = ""
)

data class CommentsState(
    val comments: List<Comment> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class VideoViewModel @Inject constructor(
    private val videoRepository: VideoRepository
) : ViewModel() {

    private val _feedState = MutableStateFlow(VideoFeedState())
    val feedState: StateFlow<VideoFeedState> = _feedState.asStateFlow()

    private val _uploadState = MutableStateFlow(UploadState())
    val uploadState: StateFlow<UploadState> = _uploadState.asStateFlow()

    private val _commentsState = MutableStateFlow(CommentsState())
    val commentsState: StateFlow<CommentsState> = _commentsState.asStateFlow()

    private val _trendingVideos = MutableStateFlow<List<Video>>(emptyList())
    val trendingVideos: StateFlow<List<Video>> = _trendingVideos.asStateFlow()

    init {
        loadFeed()
    }

    fun loadFeed() {
        viewModelScope.launch {
            _feedState.value = VideoFeedState(isLoading = true)
            videoRepository.getFeedVideos().collect { videos ->
                _feedState.value = VideoFeedState(videos = videos, isLoading = false)
            }
        }
    }

    fun publishVideo(sourceUrl: String, caption: String, hashtags: List<String>) {
        viewModelScope.launch {
            _uploadState.value = UploadState(isUploading = true)
            val result = videoRepository.publishVideo(sourceUrl, caption, hashtags)
            result.fold(
                onSuccess = {
                    _uploadState.value = UploadState(isSuccess = true)
                },
                onFailure = {
                    _uploadState.value = UploadState(error = it.message)
                }
            )
        }
    }

    fun toggleLike(videoId: String) {
        viewModelScope.launch {
            videoRepository.toggleLike(videoId)
        }
    }

    fun recordView(videoId: String) {
        viewModelScope.launch {
            videoRepository.incrementView(videoId)
        }
    }

    fun shareVideo(videoId: String) {
        viewModelScope.launch {
            videoRepository.incrementShare(videoId)
        }
    }

    fun loadComments(videoId: String) {
        viewModelScope.launch {
            _commentsState.value = CommentsState(isLoading = true)
            videoRepository.getComments(videoId).collect { comments ->
                _commentsState.value = CommentsState(comments = comments)
            }
        }
    }

    fun addComment(videoId: String, text: String) {
        viewModelScope.launch {
            videoRepository.addComment(videoId, text)
        }
    }

    fun loadTrending() {
        viewModelScope.launch {
            _trendingVideos.value = videoRepository.getTrendingVideos()
        }
    }

    fun searchVideos(query: String, onResult: (List<Video>) -> Unit) {
        viewModelScope.launch {
            val results = videoRepository.searchVideos(query)
            onResult(results)
        }
    }

    fun resetUploadState() {
        _uploadState.value = UploadState()
    }
}
