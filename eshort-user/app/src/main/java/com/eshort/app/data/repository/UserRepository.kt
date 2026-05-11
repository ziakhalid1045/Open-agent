package com.eshort.app.data.repository

import com.eshort.app.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val storage: FirebaseStorage
) {
    private val usersCollection = firestore.collection("users")

    fun getUserProfile(userId: String): Flow<User?> = callbackFlow {
        val listener = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(User::class.java))
            }
        awaitClose { listener.remove() }
    }

    suspend fun updateProfile(
        displayName: String? = null,
        username: String? = null,
        bio: String? = null,
        avatarUrl: String? = null
    ): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val updates = mutableMapOf<String, Any>()
            displayName?.let { updates["displayName"] = it }
            username?.let { updates["username"] = it }
            bio?.let { updates["bio"] = it }
            avatarUrl?.let { updates["avatarUrl"] = it }
            if (updates.isNotEmpty()) {
                usersCollection.document(uid).update(updates).await()
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun toggleFollow(targetUserId: String): Result<Boolean> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val userDoc = usersCollection.document(uid).get().await()
            val following = userDoc.get("following") as? List<*> ?: emptyList<String>()

            if (following.contains(targetUserId)) {
                usersCollection.document(uid)
                    .update("following", FieldValue.arrayRemove(targetUserId)).await()
                usersCollection.document(targetUserId)
                    .update("followers", FieldValue.arrayRemove(uid)).await()
                Result.success(false)
            } else {
                usersCollection.document(uid)
                    .update("following", FieldValue.arrayUnion(targetUserId)).await()
                usersCollection.document(targetUserId)
                    .update("followers", FieldValue.arrayUnion(uid)).await()
                Result.success(true)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun searchUsers(query: String): List<User> {
        return try {
            val usernameResults = usersCollection
                .orderBy("username")
                .startAt(query.lowercase())
                .endAt(query.lowercase() + "\uf8ff")
                .limit(20)
                .get().await()
                .toObjects(User::class.java)

            val nameResults = usersCollection
                .orderBy("displayName")
                .startAt(query)
                .endAt(query + "\uf8ff")
                .limit(20)
                .get().await()
                .toObjects(User::class.java)

            (usernameResults + nameResults).distinctBy { it.uid }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getSuggestedUsers(): List<User> {
        return try {
            usersCollection
                .orderBy("followerCount")
                .limit(20)
                .get().await()
                .toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun uploadAvatar(imageBytes: ByteArray): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val ref = storage.reference.child("avatars/$uid.jpg")
            ref.putBytes(imageBytes).await()
            val url = ref.downloadUrl.await().toString()
            usersCollection.document(uid).update("avatarUrl", url).await()
            Result.success(url)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
