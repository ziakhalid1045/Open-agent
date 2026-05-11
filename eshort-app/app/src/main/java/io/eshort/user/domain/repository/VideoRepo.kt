package io.eshort.user.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.eshort.user.domain.model.Comment
import io.eshort.user.domain.model.ShortVideo
import io.eshort.user.domain.model.VideoPlatform
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepo @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val videos = db.collection("videos")
    private val commentsCol = db.collection("comments")

    fun feedVideos(): Flow<List<ShortVideo>> = callbackFlow {
        val reg = videos
            .whereEqualTo("status", "approved")
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .limit(100)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(ShortVideo::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun userVideos(userId: String): Flow<List<ShortVideo>> = callbackFlow {
        val reg = videos
            .whereEqualTo("authorId", userId)
            .orderBy("postedAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(ShortVideo::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun publishVideo(
        sourceUrl: String,
        caption: String,
        tags: List<String>
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val user = db.collection("users").document(uid).get().await()
            val platform = VideoPlatform.detect(sourceUrl)
            val video = ShortVideo(
                authorId = uid,
                authorName = user.getString("username").orEmpty(),
                authorPhoto = user.getString("photoUrl").orEmpty(),
                sourceUrl = sourceUrl,
                streamUrl = sourceUrl,
                caption = caption,
                tags = tags,
                platform = platform.label
            )
            videos.add(video).await()
            db.collection("users").document(uid)
                .update("postsCount", FieldValue.increment(1))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleLike(videoId: String): Boolean {
        val uid = auth.currentUser?.uid ?: return false
        val ref = videos.document(videoId)
        val snap = ref.get().await()
        val liked = snap.toObject(ShortVideo::class.java)?.likedBy?.contains(uid) == true
        if (liked) {
            ref.update("likedBy", FieldValue.arrayRemove(uid)).await()
        } else {
            ref.update("likedBy", FieldValue.arrayUnion(uid)).await()
        }
        return !liked
    }

    suspend fun addView(videoId: String) {
        videos.document(videoId).update("views", FieldValue.increment(1))
    }

    suspend fun addShare(videoId: String) {
        videos.document(videoId).update("shares", FieldValue.increment(1))
    }

    fun videoComments(videoId: String): Flow<List<Comment>> = callbackFlow {
        val reg = commentsCol
            .whereEqualTo("videoId", videoId)
            .orderBy("writtenAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(Comment::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun postComment(videoId: String, text: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val user = db.collection("users").document(uid).get().await()
            val comment = Comment(
                videoId = videoId,
                authorId = uid,
                authorName = user.getString("username").orEmpty(),
                authorPhoto = user.getString("photoUrl").orEmpty(),
                text = text
            )
            commentsCol.add(comment).await()
            videos.document(videoId).update("comments", FieldValue.increment(1))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchVideos(query: String): List<ShortVideo> {
        return try {
            val snap = videos
                .whereEqualTo("status", "approved")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(50)
                .get().await()
            snap.toObjects(ShortVideo::class.java)
                .filter { it.caption.contains(query, ignoreCase = true) ||
                          it.tags.any { t -> t.contains(query, ignoreCase = true) } ||
                          it.authorName.contains(query, ignoreCase = true) }
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun trendingVideos(): List<ShortVideo> {
        return try {
            videos.whereEqualTo("status", "approved")
                .orderBy("views", Query.Direction.DESCENDING)
                .limit(30)
                .get().await()
                .toObjects(ShortVideo::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }
}
