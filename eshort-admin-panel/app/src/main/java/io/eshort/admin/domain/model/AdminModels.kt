package io.eshort.admin.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserInfo(
    @DocumentId val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val postsCount: Int = 0,
    val followers: List<String> = emptyList(),
    val isBanned: Boolean = false,
    val isVerified: Boolean = false,
    @ServerTimestamp val joinedAt: Timestamp? = null
) {
    val followerCount: Int get() = followers.size
}

data class VideoInfo(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val sourceUrl: String = "",
    val caption: String = "",
    val platform: String = "",
    val views: Long = 0L,
    val likedBy: List<String> = emptyList(),
    val comments: Int = 0,
    val status: String = "approved",
    val reportCount: Int = 0,
    @ServerTimestamp val postedAt: Timestamp? = null
) {
    val likes: Int get() = likedBy.size
}

data class ReportInfo(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val targetId: String = "",
    val targetType: String = "",
    val reason: String = "",
    val status: String = "pending",
    @ServerTimestamp val filedAt: Timestamp? = null
)

data class DashboardStats(
    val totalUsers: Int = 0,
    val totalVideos: Int = 0,
    val pendingReports: Int = 0,
    val bannedUsers: Int = 0,
    val totalViews: Long = 0L
)
