package io.eshort.user.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Conversation(
    @DocumentId val id: String = "",
    val members: List<String> = emptyList(),
    val lastText: String = "",
    val lastSenderId: String = "",
    val lastTimestamp: Timestamp? = null,
    val unreadCount: Map<String, Int> = emptyMap()
)

data class ChatMessage(
    @DocumentId val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val text: String = "",
    val imageUrl: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val sentAt: Timestamp? = null
)

data class Comment(
    @DocumentId val id: String = "",
    val videoId: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhoto: String = "",
    val text: String = "",
    val likedBy: List<String> = emptyList(),
    @ServerTimestamp val writtenAt: Timestamp? = null
) {
    val likes: Int get() = likedBy.size
}

data class Report(
    @DocumentId val id: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val targetId: String = "",
    val targetType: String = "",
    val reason: String = "",
    val status: String = "pending",
    @ServerTimestamp val filedAt: Timestamp? = null
)

data class Notification(
    @DocumentId val id: String = "",
    val recipientId: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val type: String = "",
    val message: String = "",
    val referenceId: String = "",
    val isRead: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null
)
