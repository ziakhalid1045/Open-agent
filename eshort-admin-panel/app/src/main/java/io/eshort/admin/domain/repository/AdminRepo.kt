package io.eshort.admin.domain.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.eshort.admin.domain.model.DashboardStats
import io.eshort.admin.domain.model.ReportInfo
import io.eshort.admin.domain.model.UserInfo
import io.eshort.admin.domain.model.VideoInfo
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepo @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val users = db.collection("users")
    private val videos = db.collection("videos")
    private val reports = db.collection("reports")

    fun allUsers(): Flow<List<UserInfo>> = callbackFlow {
        val reg = users.orderBy("joinedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(UserInfo::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun allVideos(): Flow<List<VideoInfo>> = callbackFlow {
        val reg = videos.orderBy("postedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(VideoInfo::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun allReports(): Flow<List<ReportInfo>> = callbackFlow {
        val reg = reports.orderBy("filedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(ReportInfo::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun toggleBanUser(uid: String, ban: Boolean) {
        users.document(uid).update("isBanned", ban).await()
    }

    suspend fun verifyUser(uid: String, verify: Boolean) {
        users.document(uid).update("isVerified", verify).await()
    }

    suspend fun updateVideoStatus(videoId: String, status: String) {
        videos.document(videoId).update("status", status).await()
    }

    suspend fun deleteVideo(videoId: String) {
        videos.document(videoId).delete().await()
    }

    suspend fun resolveReport(reportId: String) {
        reports.document(reportId).update("status", "resolved").await()
    }

    suspend fun dismissReport(reportId: String) {
        reports.document(reportId).update("status", "dismissed").await()
    }

    suspend fun getDashboardStats(): DashboardStats {
        return try {
            val usersSnap = users.get().await()
            val videosSnap = videos.get().await()
            val reportsSnap = reports.whereEqualTo("status", "pending").get().await()

            val allUsers = usersSnap.toObjects(UserInfo::class.java)
            val allVideos = videosSnap.toObjects(VideoInfo::class.java)

            DashboardStats(
                totalUsers = allUsers.size,
                totalVideos = allVideos.size,
                pendingReports = reportsSnap.size(),
                bannedUsers = allUsers.count { it.isBanned },
                totalViews = allVideos.sumOf { it.views }
            )
        } catch (_: Exception) {
            DashboardStats()
        }
    }

    suspend fun broadcastNotification(title: String, body: String) {
        val notification = mapOf(
            "title" to title,
            "body" to body,
            "type" to "broadcast",
            "sentAt" to com.google.firebase.Timestamp.now()
        )
        db.collection("broadcasts").add(notification).await()
    }
}
