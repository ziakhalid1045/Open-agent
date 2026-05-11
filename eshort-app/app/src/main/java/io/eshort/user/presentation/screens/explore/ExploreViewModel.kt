package io.eshort.user.presentation.screens.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.user.domain.model.ShortVideo
import io.eshort.user.domain.model.UserProfile
import io.eshort.user.domain.repository.SocialRepo
import io.eshort.user.domain.repository.VideoRepo
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SearchState(
    val users: List<UserProfile> = emptyList(),
    val videos: List<ShortVideo> = emptyList()
)

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val videoRepo: VideoRepo,
    private val socialRepo: SocialRepo
) : ViewModel() {

    private val _searchResults = MutableStateFlow(SearchState())
    val searchResults: StateFlow<SearchState> = _searchResults.asStateFlow()

    private val _trendingVideos = MutableStateFlow<List<ShortVideo>>(emptyList())
    val trendingVideos: StateFlow<List<ShortVideo>> = _trendingVideos.asStateFlow()

    private var searchJob: Job? = null

    fun search(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = SearchState()
            return
        }
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            val users = socialRepo.searchUsers(query)
            val videos = videoRepo.searchVideos(query)
            _searchResults.value = SearchState(users = users, videos = videos)
        }
    }

    fun loadTrending() {
        viewModelScope.launch {
            _trendingVideos.value = videoRepo.trendingVideos()
        }
    }
}
