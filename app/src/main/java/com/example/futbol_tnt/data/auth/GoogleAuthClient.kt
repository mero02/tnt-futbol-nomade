package com.example.futbol_tnt.data.auth

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

sealed class AuthResult {
    data class Pending(val signInIntent: Intent) : AuthResult()
    data class Success(val userId: String, val displayName: String?, val email: String?) : AuthResult()
    data class Error(val message: String) : AuthResult()
}

class GoogleAuthClient(
    private val context: Context
) {
private val googleSignInClient: GoogleSignInClient by lazy {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("1066898158938-nrml7g85fmqqg9orgmb13qbg72q01r59.apps.googleusercontent.com")
            .requestEmail()
            .requestProfile()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    suspend fun signIn(): AuthResult {
        return try {
            val intent = googleSignInClient.signInIntent
            AuthResult.Pending(intent)
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error")
        }
    }

    suspend fun handleSignInResult(intent: Intent?): AuthResult {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = FirebaseAuth.getInstance().signInWithCredential(credential).await()
            val user = authResult.user
            if (user != null) {
                AuthResult.Success(
                    userId = user.uid,
                    displayName = user.displayName,
                    email = user.email
                )
            } else {
                AuthResult.Error("User is null")
            }
        } catch (e: ApiException) {
            AuthResult.Error(e.message ?: "Google sign in failed")
        } catch (e: Exception) {
            AuthResult.Error(e.message ?: "Unknown error")
        }
    }

    fun signOut() {
        FirebaseAuth.getInstance().signOut()
        googleSignInClient.signOut()
    }

    fun getCurrentUserId(): String? = FirebaseAuth.getInstance().currentUser?.uid

    fun isLoggedIn(): Boolean = FirebaseAuth.getInstance().currentUser != null
}