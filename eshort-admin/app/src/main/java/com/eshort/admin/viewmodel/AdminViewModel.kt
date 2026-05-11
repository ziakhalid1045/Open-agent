package com.eshort.admin.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eshort.admin.data.model.AdminReport
import com.eshort.admin.data.model.AdminUser
import com.eshort.admin.data.model.AdminVideo
import com.eshort.admin.data.model.AnalyticsData
import com.eshort.admin.data.repository.AdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminState(
    val users: List<AdminUser> = emptyList(),
    val videos: List<AdminVideo> = emptyList(),
    val reports: List<AdminReport> = emptyList(),
    val analytics: AnalyticsData = AnalyticsData(),
    val isLoading: Boolean = true,
    val actionMessage: String? = null
)

@HiltViewModel
class AdminViewModel @Inject constructor(
    private val repository: AdminRepository
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        loadUsers()
        loadVideos()
        loadReports()
        loadAnalytics()
    }

    private fun loadUsers() {
        viewModelScope.launch {
            repository.getAllUsers().collect { users ->
                _state.value = _state.value.copy(users = users, isLoading = false)
            }
        }
    }

    private fun loadVideos() {
        viewModelScope.launch {
            repository.getAllVideos().collect { videos ->
                _state.value = _state.value.copy(videos = videos)
            }
        }
    }

    private fun loadReports() {
        viewModelScope.launch {
            repository.getReports().collect { reports ->
                _state.value = _state.value.copy(reports = reports)
            }
        }
    }

    private fun loadAnalytics() {
        viewModelScope.launch {
            val analytics = repository.getAnalytics()
            _state.value = _state.value.copy(analytics = analytics)
        }
    }

    fun banUser(userId: String, reason: String) {
        viewModelScope.launch {
            repository.banUser(userId, reason).fold(
                onSuccess = { showMessage("User banned successfully") },
                onFailure = { showMessage("Failed to ban user: ${it.message}") }
            )
        }
    }

    fun unbanUser(userId: String) {
        viewModelScope.launch {
            repository.unbanUser(userId).fold(
                onSuccess = { showMessage("User unbanned") },
                onFailure = { showMessage("Failed to unban user: ${it.message}") }
            )
        }
    }

    fun deleteVideo(videoId: String) {
        viewModelScope.launch {
            repository.deleteVideo(videoId).fold(
                onSuccess = { showMessage("Video deleted") },
                onFailure = { showMessage("Failed to delete video: ${it.message}") }
            )
        }
    }

    fun approveVideo(videoId: String) {
        viewModelScope.launch {
            repository.approveVideo(videoId).fold(
                onSuccess = { showMessage("Video approved") },
                onFailure = { showMessage("Failed: ${it.message}") }
            )
        }
    }

    fun rejectVideo(videoId: String) {
        viewModelScope.launch {
            repository.rejectVideo(videoId).fold(
                onSuccess = { showMessage("Video rejected") },
                onFailure = { showMessage("Failed: ${it.message}") }
            )
        }
    }

    fun resolveReport(reportId: String, action: String) {
        viewModelScope.launch {
            repository.resolveReport(reportId, action).fold(
                onSuccess = { showMessage("Report $action") },
                onFailure = { showMessage("Failed: ${it.message}") }
            )
        }
    }

    fun sendBroadcast(title: String, message: String) {
        viewModelScope.launch {
            repository.sendNotificationToAll(title, message).fold(
                onSuccess = { showMessage("Notification sent") },
                onFailure = { showMessage("Failed: ${it.message}") }
            )
        }
    }

    private fun showMessage(msg: String) {
        _state.value = _state.value.copy(actionMessage = msg)
    }

    fun clearMessage() {
        _state.value = _state.value.copy(actionMessage = null)
    }
}
