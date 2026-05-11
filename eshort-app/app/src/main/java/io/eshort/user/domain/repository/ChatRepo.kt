package io.eshort.user.domain.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import io.eshort.user.domain.model.ChatMessage
import io.eshort.user.domain.model.Conversation
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatRepo @Inject constructor(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val rtdb: FirebaseDatabase
) {
    private val conversations = db.collection("conversations")

    fun myConversations(): Flow<List<Conversation>> = callbackFlow {
        val uid = auth.currentUser?.uid ?: return@callbackFlow
        val reg = conversations
            .whereArrayContains("members", uid)
            .orderBy("lastTimestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(Conversation::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun messages(conversationId: String): Flow<List<ChatMessage>> = callbackFlow {
        val reg = conversations.document(conversationId)
            .collection("messages")
            .orderBy("sentAt", Query.Direction.ASCENDING)
            .addSnapshotListener { snap, err ->
                if (err != null) { close(err); return@addSnapshotListener }
                trySend(snap?.toObjects(ChatMessage::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun sendMessage(conversationId: String, text: String): Result<Unit> {
        val uid = auth.currentUser?.uid ?: return Result.failure(Exception("Not signed in"))
        return try {
            val userDoc = db.collection("users").document(uid).get().await()
            val msg = ChatMessage(
                senderId = uid,
                senderName = userDoc.getString("username").orEmpty(),
                senderPhoto = userDoc.getString("photoUrl").orEmpty(),
                text = text
            )
            conversations.document(conversationId)
                .collection("messages").add(msg).await()

            conversations.document(conversationId).update(
                mapOf(
                    "lastText" to text,
                    "lastSenderId" to uid,
                    "lastTimestamp" to FieldValue.serverTimestamp()
                )
            ).await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getOrCreateConversation(otherUid: String): String {
        val uid = auth.currentUser?.uid ?: throw Exception("Not signed in")

        val existing = conversations
            .whereArrayContains("members", uid)
            .get().await()
            .toObjects(Conversation::class.java)
            .firstOrNull { otherUid in it.members }

        if (existing != null) return existing.id

        val newConvo = Conversation(members = listOf(uid, otherUid))
        val ref = conversations.add(newConvo).await()
        return ref.id
    }

    fun setTyping(conversationId: String, isTyping: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        rtdb.reference.child("typing/$conversationId/$uid").setValue(isTyping)
    }

    fun observeTyping(conversationId: String, otherUid: String): Flow<Boolean> = callbackFlow {
        val ref = rtdb.reference.child("typing/$conversationId/$otherUid")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                trySend(snap.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(err: DatabaseError) { close(err.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun setOnlineStatus(online: Boolean) {
        val uid = auth.currentUser?.uid ?: return
        val ref = rtdb.reference.child("presence/$uid")
        ref.setValue(mapOf("online" to online, "lastSeen" to System.currentTimeMillis()))
        ref.onDisconnect().setValue(
            mapOf("online" to false, "lastSeen" to System.currentTimeMillis())
        )
    }

    fun observeOnline(uid: String): Flow<Boolean> = callbackFlow {
        val ref = rtdb.reference.child("presence/$uid/online")
        val listener = object : ValueEventListener {
            override fun onDataChange(snap: DataSnapshot) {
                trySend(snap.getValue(Boolean::class.java) == true)
            }
            override fun onCancelled(err: DatabaseError) { close(err.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }
}
