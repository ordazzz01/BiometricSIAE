package com.siae.biometricsiae.data.remote.firebase

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseAuthManager @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) {
    val currentUser: FirebaseUser?
        get() = firebaseAuth.currentUser

    val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    suspend fun signInWithEmailAndPassword(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            result.user?.let {
                Result.success(it)
            } ?: Result.failure(Exception("Error al iniciar sesión"))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        firebaseAuth.signOut()
    }

    suspend fun getIdToken(): String? {
        return try {
            val user = firebaseAuth.currentUser ?: return null
            val tokenResult = user.getIdToken(true).await()
            tokenResult.token
        } catch (e: Exception) {
            null
        }
    }

    fun addAuthStateListener(listener: (FirebaseUser?) -> Unit) {
        firebaseAuth.addAuthStateListener { auth ->
            listener(auth.currentUser)
        }
    }
}
