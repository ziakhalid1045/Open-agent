package com.eshort.app.data.repository

import com.eshort.app.data.model.Comment
import com.eshort.app.data.model.Video
import com.eshort.app.data.model.VideoPlatform
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val videosCollection = firestore.collection("videos")
    private val commentsCollection = firestore.collection("comments")
    private val usersCollection = firestore.collection("users")

    fun getFeedVideos(): Flow<List<Video>> = callbackFlow {
        val listener = videosCollection
            .whereEqualTo("isApproved", true)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val videos = snapshot?.toObjects(Video::class.java) ?: emptyList()
                trySend(videos)
            }
        awaitClose { listener.remove() }
    }

    fun getUserVideos(userId: String): Flow<List<Video>> = callbackFlow {
        val listener = videosCollection
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val videos = snapshot?.toObjects(Video::class.java) ?: emptyList()
                trySend(videos)
            }
        awaitClose { listener.remove() }
    }

    suspend fun publishVideo(
        sourceUrl: String,
        caption: String,
        hashtags: List<String>
    ): Result<Video> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val userDoc = usersCollection.document(uid).get().await()
            val platform = VideoPlatform.detect(sourceUrl)

            val video = Video(
                userId = uid,
                username = userDoc.getString("username") ?: "",
                userAvatar = userDoc.getString("avatarUrl") ?: "",
                sourceUrl = sourceUrl,
                videoUrl = sourceUrl,
                thumbnailUrl = "",
                caption = caption,
                hashtags = hashtags,
                platform = platform.name
            )

            val docRef = videosCollection.add(video).await()
            usersCollection.document(uid)
                .update("videoCount", FieldValue.increment(1)).await()

            Result.success(video.copy(id = docRef.id))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(videoId: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val videoRef = videosCollection.document(videoId)
            val doc = videoRef.get().await()
            val likes = doc.get("likes") as? List<*> ?: emptyList<String>()

            if (likes.contains(uid)) {
                videoRef.update("likes", FieldValue.arrayRemove(uid)).await()
                Result.success(false)
            } else {
                videoRef.update("likes", FieldValue.arrayUnion(uid)).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun incrementView(videoId: String) {
        try {
            videosCollection.document(videoId)
                .update("viewCount", FieldValue.increment(1)).await()
        } catch (_: Exception) {}
    }

    suspend fun incrementShare(videoId: String) {
        try {
            videosCollection.document(videoId)
                .update("shareCount", FieldValue.increment(1)).await()
        } catch (_: Exception) {}
    }

    fun getComments(videoId: String): Flow<List<Comment>> = callbackFlow {
        val listener = commentsCollection
            .whereEqualTo("videoId", videoId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val comments = snapshot?.toObjects(Comment::class.java) ?: emptyList()
                trySend(comments)
            }
        awaitClose { listener.remove() }
    }

    suspend fun addComment(videoId: String, text: String): Result<Comment> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val userDoc = usersCollection.document(uid).get().await()

            val comment = Comment(
                videoId = videoId,
                userId = uid,
                username = userDoc.getString("username") ?: "",
                userAvatar = userDoc.getString("avatarUrl") ?: "",
                text = text
            )

            commentsCollection.add(comment).await()
            videosCollection.document(videoId)
                .update("commentCount", FieldValue.increment(1)).await()

            Result.success(comment)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchVideos(query: String): List<Video> {
        return try {
            val hashtagResults = videosCollection
                .whereArrayContains("hashtags", query.lowercase())
                .limit(20)
                .get().await()
                .toObjects(Video::class.java)

            val captionResults = videosCollection
                .orderBy("caption")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get().await()
                .toObjects(Video::class.java)

            (hashtagResults + captionResults).distinctBy { it.id }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTrendingVideos(): List<Video> {
        return try {
            videosCollection
                .whereEqualTo("isApproved", true)
                .orderBy("viewCount", Query.Direction.DESCENDING)
                .limit(30)
                .get().await()
                .toObjects(Video::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
