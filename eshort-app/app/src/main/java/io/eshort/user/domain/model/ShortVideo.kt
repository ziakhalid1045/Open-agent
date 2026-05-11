package io.eshort.user.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class ShortVideo(
    @DocumentId val id: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhoto: String = "",
    val sourceUrl: String = "",
    val streamUrl: String = "",
    val thumbnailUrl: String = "",
    val caption: String = "",
    val tags: List<String> = emptyList(),
    val platform: String = "",
    val likedBy: List<String> = emptyList(),
    val comments: Int = 0,
    val views: Long = 0L,
    val shares: Int = 0,
    val status: String = "approved",
    val reportCount: Int = 0,
    @ServerTimestamp val postedAt: Timestamp? = null
) {
    val likeCount: Int get() = likedBy.size
}

enum class VideoPlatform(val label: String) {
    TIKTOK("TikTok"),
    YOUTUBE_SHORTS("YouTube Shorts"),
    INSTAGRAM_REELS("Instagram Reels"),
    FACEBOOK_REELS("Facebook Reels"),
    UNKNOWN("Unknown");

    companion object {
        fun detect(url: String): VideoPlatform {
            val lower = url.lowercase()
            return when {
                "tiktok.com" in lower -> TIKTOK
                "youtube.com/shorts" in lower || "youtu.be" in lower -> YOUTUBE_SHORTS
                "instagram.com/reel" in lower || "instagram.com/p/" in lower -> INSTAGRAM_REELS
                "facebook.com/reel" in lower || "fb.watch" in lower || "facebook.com/watch" in lower -> FACEBOOK_REELS
                else -> UNKNOWN
            }
        }
    }
}
