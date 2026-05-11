package com.eshort.admin.data.repository

import com.eshort.admin.data.model.AdminReport
import com.eshort.admin.data.model.AdminUser
import com.eshort.admin.data.model.AdminVideo
import com.eshort.admin.data.model.AnalyticsData
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AdminRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    private val usersCollection = firestore.collection("users")
    private val videosCollection = firestore.collection("videos")
    private val reportsCollection = firestore.collection("reports")

    fun getAllUsers(): Flow<List<AdminUser>> = callbackFlow {
        val listener = usersCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val users = snapshot?.toObjects(AdminUser::class.java) ?: emptyList()
                trySend(users)
            }
        awaitClose { listener.remove() }
    }

    fun getAllVideos(): Flow<List<AdminVideo>> = callbackFlow {
        val listener = videosCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val videos = snapshot?.toObjects(AdminVideo::class.java) ?: emptyList()
                trySend(videos)
            }
        awaitClose { listener.remove() }
    }

    fun getReports(): Flow<List<AdminReport>> = callbackFlow {
        val listener = reportsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val reports = snapshot?.toObjects(AdminReport::class.java) ?: emptyList()
                trySend(reports)
            }
        awaitClose { listener.remove() }
    }

    suspend fun banUser(userId: String, reason: String): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf("isBanned" to true, "banReason" to reason)
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unbanUser(userId: String): Result<Unit> {
        return try {
            usersCollection.document(userId).update(
                mapOf("isBanned" to false, "banReason" to "")
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteVideo(videoId: String): Result<Unit> {
        return try {
            videosCollection.document(videoId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun approveVideo(videoId: String): Result<Unit> {
        return try {
            videosCollection.document(videoId).update("isApproved", true).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun rejectVideo(videoId: String): Result<Unit> {
        return try {
            videosCollection.document(videoId).update("isApproved", false).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveReport(reportId: String, action: String): Result<Unit> {
        return try {
            reportsCollection.document(reportId).update("status", action).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getAnalytics(): AnalyticsData {
        return try {
            val users = usersCollection.get().await()
            val videos = videosCollection.get().await()
            val reports = reportsCollection.whereEqualTo("status", "pending").get().await()
            val bannedUsers = usersCollection.whereEqualTo("isBanned", true).get().await()

            val totalViews = videos.documents.sumOf {
                (it.getLong("viewCount") ?: 0L)
            }

            AnalyticsData(
                totalUsers = users.size(),
                totalVideos = videos.size(),
                totalViews = totalViews,
                pendingReports = reports.size(),
                bannedUsers = bannedUsers.size()
            )
        } catch (e: Exception) {
            AnalyticsData()
        }
    }

    suspend fun sendNotificationToAll(title: String, message: String): Result<Unit> {
        return try {
            val notification = mapOf(
                "title" to title,
                "message" to message,
                "type" to "admin_broadcast",
                "timestamp" to com.google.firebase.Timestamp.now()
            )
            firestore.collection("admin_notifications").add(notification).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
