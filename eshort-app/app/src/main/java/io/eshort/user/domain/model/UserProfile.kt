package io.eshort.user.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class UserProfile(
    @DocumentId val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val coverUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val postsCount: Int = 0,
    val totalLikes: Long = 0L,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val isAdmin: Boolean = false,
    val fcmToken: String = "",
    @ServerTimestamp val joinedAt: Timestamp? = null
) {
    val followerCount: Int get() = followers.size
    val followingCount: Int get() = following.size
}
