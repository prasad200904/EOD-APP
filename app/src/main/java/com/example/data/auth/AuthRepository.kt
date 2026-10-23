package com.example.data.auth

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.tasks.await

data class VerifiedSession(
    val uid: String,
    val email: String,
    val role: String,
    val employeeId: String? = null,
    val teamId: String? = null,
    val companyId: String? = null
) {
    val isAdmin: Boolean get() = role.equals("ADMIN", ignoreCase = true)
    val isManager: Boolean get() = role.equals("MANAGER", ignoreCase = true)
    val isEmployee: Boolean get() = role.equals("EMPLOYEE", ignoreCase = true)
}

open class AuthRepository(
    private val authProvider: () -> FirebaseAuth? = {
        runCatching { FirebaseAuth.getInstance() }.getOrNull()
    }
) {
    constructor(firebaseAuth: FirebaseAuth) : this({ firebaseAuth })

    private val auth: FirebaseAuth?
        get() = authProvider()

    val currentUser: FirebaseUser?
        get() = auth?.currentUser

    open suspend fun signIn(email: String, password: String): Result<VerifiedSession> {
        val authInstance = auth ?: return Result.failure(
            IllegalStateException("Firebase Auth is not available.")
        )
        return try {
            val authResult = authInstance.signInWithEmailAndPassword(email.trim(), password).await()
            val user = authResult.user ?: throw IllegalStateException("Firebase returned null user.")

            val tokenResult = user.getIdToken(true).await()
            val claims = tokenResult.claims

            val roleClaim = (claims["role"] as? String)?.uppercase()
                ?: if (claims["admin"] == true) "ADMIN" else null

            if (roleClaim.isNullOrBlank()) {
                authInstance.signOut()
                return Result.failure(SecurityException("No authorized role assigned to this account."))
            }

            val session = VerifiedSession(
                uid = user.uid,
                email = user.email.orEmpty(),
                role = roleClaim,
                employeeId = claims["employeeId"] as? String,
                teamId = (claims["teamId"] ?: claims["team"]) as? String,
                companyId = claims["companyId"] as? String
            )
            Result.success(session)
        } catch (e: SecurityException) {
            Result.failure(e)
        } catch (e: Exception) {
            // Map auth errors to generic message; never log or expose passwords or specific auth exceptions
            Result.failure(IllegalArgumentException("Invalid ID or password."))
        }
    }

    open suspend fun readSession(forceRefresh: Boolean = false): VerifiedSession? {
        val authInstance = auth ?: return null
        val user = authInstance.currentUser ?: return null
        return try {
            val tokenResult = user.getIdToken(forceRefresh).await()
            val claims = tokenResult.claims

            val roleClaim = (claims["role"] as? String)?.uppercase()
                ?: if (claims["admin"] == true) "ADMIN" else null

            if (roleClaim.isNullOrBlank()) {
                authInstance.signOut()
                null
            } else {
                VerifiedSession(
                    uid = user.uid,
                    email = user.email.orEmpty(),
                    role = roleClaim,
                    employeeId = claims["employeeId"] as? String,
                    teamId = (claims["teamId"] ?: claims["team"]) as? String,
                    companyId = claims["companyId"] as? String
                )
            }
        } catch (e: Exception) {
            null
        }
    }

    open suspend fun sendPasswordReset(email: String): Result<Unit> {
        val cleanEmail = email.trim()
        if (cleanEmail.isEmpty()) {
            return Result.failure(IllegalArgumentException("Email is required."))
        }
        val authInstance = auth ?: return Result.success(Unit)
        return try {
            authInstance.sendPasswordResetEmail(cleanEmail).await()
            Result.success(Unit)
        } catch (e: Exception) {
            // Return generic success to prevent account enumeration
            Result.success(Unit)
        }
    }

    open fun signOut() {
        auth?.signOut()
    }
}
