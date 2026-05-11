package io.eshort.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import io.eshort.admin.domain.model.DashboardStats
import io.eshort.admin.domain.model.ReportInfo
import io.eshort.admin.domain.model.UserInfo
import io.eshort.admin.domain.model.VideoInfo
import io.eshort.admin.domain.repository.AdminRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val adminRepo: AdminRepo
) : ViewModel() {

    private val _users = MutableStateFlow<List<UserInfo>>(emptyList())
    val users: StateFlow<List<UserInfo>> = _users.asStateFlow()

    private val _videos = MutableStateFlow<List<VideoInfo>>(emptyList())
    val videos: StateFlow<List<VideoInfo>> = _videos.asStateFlow()

    private val _reports = MutableStateFlow<List<ReportInfo>>(emptyList())
    val reports: StateFlow<List<ReportInfo>> = _reports.asStateFlow()

    private val _stats = MutableStateFlow(DashboardStats())
    val stats: StateFlow<DashboardStats> = _stats.asStateFlow()

    private val _broadcastResult = MutableStateFlow<String?>(null)
    val broadcastResult: StateFlow<String?> = _broadcastResult.asStateFlow()

    init {
        viewModelScope.launch { adminRepo.allUsers().collect { _users.value = it } }
        viewModelScope.launch { adminRepo.allVideos().collect { _videos.value = it } }
        viewModelScope.launch { adminRepo.allReports().collect { _reports.value = it } }
        loadStats()
    }

    fun loadStats() {
        viewModelScope.launch { _stats.value = adminRepo.getDashboardStats() }
    }

    fun toggleBan(uid: String, ban: Boolean) {
        viewModelScope.launch { adminRepo.toggleBanUser(uid, ban) }
    }

    fun verifyUser(uid: String, verify: Boolean) {
        viewModelScope.launch { adminRepo.verifyUser(uid, verify) }
    }

    fun updateVideoStatus(videoId: String, status: String) {
        viewModelScope.launch { adminRepo.updateVideoStatus(videoId, status) }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch { adminRepo.deleteVideo(videoId) }
    }

    fun resolveReport(reportId: String) {
        viewModelScope.launch { adminRepo.resolveReport(reportId) }
    }

    fun dismissReport(reportId: String) {
        viewModelScope.launch { adminRepo.dismissReport(reportId) }
    }

    fun sendBroadcast(title: String, body: String) {
        viewModelScope.launch {
            try {
                adminRepo.broadcastNotification(title, body)
                _broadcastResult.value = "Broadcast sent"
            } catch (e: Exception) {
                _broadcastResult.value = "Failed: ${e.message}"
            }
        }
    }

    fun clearBroadcastResult() { _broadcastResult.value = null }
}
