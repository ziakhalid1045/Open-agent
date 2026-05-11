package io.eshort.user.domain.repository

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import io.eshort.user.domain.model.Notification
import io.eshort.user.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SocialRepo @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val users = db.collection("users")

    fun profileStream(uid: String): Flow<UserProfile?> = callbackFlow {
        val reg = users.document(uid).addSnapshotListener { snap, err ->
            if (err != null) { close(err); return@addSnapshotListener }
            trySend(snap?.toObject(UserProfile::class.java))
        }
        awaitClose { reg.remove() }
    }

    suspend fun toggleFollow(targetUid: String): Boolean {
        val myUid = auth.currentUser?.uid ?: return false
        val myRef = users.document(myUid)
        val targetRef = users.document(targetUid)

        val myDoc = myRef.get().await()
        val isFollowing = myDoc.toObject(UserProfile::class.java)
            ?.following?.contains(targetUid) == true

        if (isFollowing) {
            myRef.update("following", FieldValue.arrayRemove(targetUid)).await()
            targetRef.update("followers", FieldValue.arrayRemove(myUid)).await()
        } else {
            myRef.update("following", FieldValue.arrayUnion(targetUid)).await()
            targetRef.update("followers", FieldValue.arrayUnion(myUid)).await()
        }
        return !isFollowing
    }

    suspend fun updateProfile(
        displayName: String,
        username: String,
        bio: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            users.document(uid).update(
                mapOf(
                    "displayName" to displayName,
                    "username" to username,
                    "bio" to bio
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun uploadAvatar(imageUri: Uri): Result<String> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val ref = storage.reference.child("avatars/$uid.jpg")
            ref.putFile(imageUri).await()
            val url = ref.downloadUrl.await().toString()
            users.document(uid).update("photoUrl", url).await()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): List<UserProfile> {
        return try {
            val snap = users.orderBy("username")
                .startAt(query.lowercase())
                .endAt(query.lowercase() + "\uf8ff")
                .limit(20)
                .get().await()
            snap.toObjects(UserProfile::class.java)
        } catch (_: Exception) {
            emptyList()
        }
    }

    fun notifications(): Flow<List<Notification>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val reg = db.collection("notifications")
            .whereEqualTo("recipientId", uid)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(Notification::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun reportContent(
        targetId: String,
        targetType: String,
        reason: String
    ): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val user = users.document(uid).get().await()
            val report = io.eshort.user.domain.model.Report(
                reporterId = uid,
                reporterName = user.getString("username").orEmpty(),
                targetId = targetId,
                targetType = targetType,
                reason = reason
            )
            db.collection("reports").add(report).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
