package com.eshort.admin.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class AdminUser(
    @DocumentId val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val avatarUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val videoCount: Int = 0,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val banReason: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val followerCount: Int get() = followers.size
}

data class AdminVideo(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val sourceUrl: String = "",
    val videoUrl: String = "",
    val caption: String = "",
    val platform: String = "",
    val likes: List<String> = emptyList(),
    val commentCount: Int = 0,
    val viewCount: Int = 0,
    val isApproved: Boolean = true,
    val isReported: Boolean = false,
    val reportCount: Int = 0,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val likeCount: Int get() = likes.size
}

data class AdminReport(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val targetId: String = "",
    val targetType: String = "",
    val reason: String = "",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class AnalyticsData(
    val totalUsers: Int = 0,
    val totalVideos: Int = 0,
    val totalViews: Long = 0,
    val activeUsers: Int = 0,
    val pendingReports: Int = 0,
    val bannedUsers: Int = 0,
    val videosToday: Int = 0,
    val newUsersToday: Int = 0
)
