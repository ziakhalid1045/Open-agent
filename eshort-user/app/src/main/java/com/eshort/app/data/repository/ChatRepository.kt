package com.eshort.app.data.repository

import com.eshort.app.data.model.ChatMessage
import com.eshort.app.data.model.ChatRoom
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val realtimeDb: FirebaseDatabase,
    private val auth: FirebaseAuth
) {
    private val chatRoomsCollection = firestore.collection("chatRooms")
    private val messagesCollection = firestore.collection("messages")

    fun getChatRooms(): Flow<List<ChatRoom>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: run {
            trySend(emptyList())
            close()
            return@callbackFlow
        }
        val listener = chatRoomsCollection
            .whereArrayContains("participants", uid)
            .orderBy("lastMessageTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val rooms = snapshot?.toObjects(ChatRoom::class.java) ?: emptyList()
                trySend(rooms)
            }
        awaitClose { listener.remove() }
    }

    fun getMessages(chatRoomId: String): Flow<List<ChatMessage>> = callbackFlow {
        val listener = messagesCollection
            .whereEqualTo("chatRoomId", chatRoomId)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val messages = snapshot?.toObjects(ChatMessage::class.java) ?: emptyList()
                trySend(messages)
            }
        awaitClose { listener.remove() }
    }

    suspend fun getOrCreateChatRoom(otherUserId: String): Result<String> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val userDoc = firestore.collection("users").document(uid).get().await()
            val otherDoc = firestore.collection("users").document(otherUserId).get().await()

            val existing = chatRoomsCollection
                .whereArrayContains("participants", uid)
                .get().await()
                .toObjects(ChatRoom::class.java)
                .find { it.participants.contains(otherUserId) }

            if (existing != null) {
                return Result.success(existing.id)
            }

            val room = ChatRoom(
                participants = listOf(uid, otherUserId),
                participantNames = mapOf(
                    uid to (userDoc.getString("username") ?: ""),
                    otherUserId to (otherDoc.getString("username") ?: "")
                ),
                participantAvatars = mapOf(
                    uid to (userDoc.getString("avatarUrl") ?: ""),
                    otherUserId to (otherDoc.getString("avatarUrl") ?: "")
                )
            )

            val docRef = chatRoomsCollection.add(room).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendMessage(chatRoomId: String, text: String): Result<Unit> {
        return try {
            val uid = auth.currentUser?.uid ?: throw Exception("Not authenticated")
            val userDoc = firestore.collection("users").document(uid).get().await()

            val message = ChatMessage(
                chatRoomId = chatRoomId,
                senderId = uid,
                senderName = userDoc.getString("username") ?: "",
                text = text
            )

            messagesCollection.add(message).await()

            chatRoomsCollection.document(chatRoomId).update(
                mapOf(
                    "lastMessage" to text,
                    "lastMessageSenderId" to uid,
                    "lastMessageTimestamp" to com.google.firebase.Timestamp.now()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun setTypingStatus(chatRoomId: String, isTyping: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        realtimeDb.reference
            .child("typing")
            .child(chatRoomId)
            .child(uid)
            .setValue(isTyping)
    }

    fun observeTyping(chatRoomId: String): Flow<Map<String, Boolean>> = callbackFlow {
        val ref = realtimeDb.reference.child("typing").child(chatRoomId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val typing = mutableMapOf<String, Boolean>()
                snapshot.children.forEach { child ->
                    child.key?.let { typing[it] = child.getValue(Boolean::class.java) ?: false }
                }
                trySend(typing)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setOnlineStatus(isOnline: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        realtimeDb.reference.child("status").child(uid).apply {
            setValue(isOnline)
            onDisconnect().setValue(false)
        }
    }

    fun observeOnlineStatus(userId: String): Flow<Boolean> = callbackFlow {
        val ref = realtimeDb.reference.child("status").child(userId)
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot.getValue(Boolean::class.java) ?: false)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
