package io.eshort.user.domain.repository

import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import io.eshort.user.domain.model.UserProfile
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepo @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) {
    val currentUser: FirebaseUser? get() = auth.currentUser

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signInWithGoogle(account: GoogleSignInAccount): Result<FirebaseUser> {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val result = auth.signInWithCredential(credential).await()
            val user = result.user ?: throw Exception("Sign-in failed")

            val isNew = result.additionalUserInfo?.isNewUser == true
            if (isNew) {
                val token = messaging.token.await()
                val profile = UserProfile(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    username = user.email?.substringBefore("@").orEmpty(),
                    displayName = user.displayName.orEmpty(),
                    photoUrl = user.photoUrl?.toString().orEmpty(),
                    fcmToken = token
                )
                db.collection("users").document(user.uid).set(profile).await()
            } else {
                val token = messaging.token.await()
                db.collection("users").document(user.uid)
                    .update("fcmToken", token)
            }

            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getProfile(uid: String): UserProfile? {
        return try {
            db.collection("users").document(uid)
                .get().await()
                .toObject(UserProfile::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun signOut() {
        auth.signOut()
    }
}
