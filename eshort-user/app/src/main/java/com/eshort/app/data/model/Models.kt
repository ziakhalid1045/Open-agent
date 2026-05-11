package com.eshort.app.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class User(
    @DocumentId val uid: String = "",
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val bio: String = "",
    val avatarUrl: String = "",
    val followers: List<String> = emptyList(),
    val following: List<String> = emptyList(),
    val videoCount: Int = 0,
    val isVerified: Boolean = false,
    val isOnline: Boolean = false,
    val fcmToken: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    val followerCount: Int get() = followers.size
    val followingCount: Int get() = following.size
}

data class Video(
    @DocumentId val id: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val sourceUrl: String = "",
    val videoUrl: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
    val hashtags: List<String> = emptyList(),
    val platform: String = "",
    val likes: List<String> = emptyList(),
    val commentCount: Int = 0,
    val shareCount: Int = 0,
    val viewCount: Int = 0,
    val isApproved: Boolean = true,
    val isReported: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val likeCount: Int get() = likes.size
}

data class Comment(
    @DocumentId val id: String = "",
    val videoId: String = "",
    val userId: String = "",
    val username: String = "",
    val userAvatar: String = "",
    val text: String = "",
    val likes: List<String> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null
) {
    val likeCount: Int get() = likes.size
}

data class ChatRoom(
    @DocumentId val id: String = "",
    val participants: List<String> = emptyList(),
    val participantNames: Map<String, String> = emptyMap(),
    val participantAvatars: Map<String, String> = emptyMap(),
    val lastMessage: String = "",
    val lastMessageSenderId: String = "",
    val lastMessageTimestamp: Timestamp? = null,
    val unreadCount: Map<String, Int> = emptyMap()
)

data class ChatMessage(
    @DocumentId val id: String = "",
    val chatRoomId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val isRead: Boolean = false,
    val isTyping: Boolean = false,
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class Notification(
    @DocumentId val id: String = "",
    val userId: String = "",
    val fromUserId: String = "",
    val fromUsername: String = "",
    val fromUserAvatar: String = "",
    val type: String = "",
    val videoId: String = "",
    val message: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val timestamp: Timestamp? = null
)

data class Report(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val targetId: String = "",
    val targetType: String = "",
    val reason: String = "",
    val status: String = "pending",
    @ServerTimestamp val createdAt: Timestamp? = null
)

enum class VideoPlatform(val displayName: String) {
    TIKTOK("TikTok"),
    YOUTUBE_SHORTS("YouTube Shorts"),
    INSTAGRAM_REELS("Instagram Reels"),
    FACEBOOK_REELS("Facebook Reels"),
    OTHER("Other");

    companion object {
        fun detect(url: String): VideoPlatform {
            return when {
                url.contains("tiktok.com") -> TIKTOK
                url.contains("youtube.com/shorts") || url.contains("youtu.be") -> YOUTUBE_SHORTS
                url.contains("instagram.com/reel") -> INSTAGRAM_REELS
                url.contains("facebook.com/reel") || url.contains("fb.watch") -> FACEBOOK_REELS
                else -> OTHER
            }
        }
    }
}
