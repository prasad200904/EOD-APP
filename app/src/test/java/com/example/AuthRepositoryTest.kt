package com.example

import com.example.data.auth.AuthRepository
import com.example.data.auth.VerifiedSession
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class AuthRepositoryTest {

    @Test
    fun `verifiedSession correctly identifies roles`() {
        val adminSession = VerifiedSession(
            uid = "uid1",
            email = "admin@workcore.com",
            role = "ADMIN"
        )
        assertTrue(adminSession.isAdmin)
        assertFalse(adminSession.isManager)
        assertFalse(adminSession.isEmployee)

        val managerSession = VerifiedSession(
            uid = "uid2",
            email = "manager@workcore.com",
            role = "MANAGER",
            teamId = "ML Team"
        )
        assertFalse(managerSession.isAdmin)
        assertTrue(managerSession.isManager)
        assertFalse(managerSession.isEmployee)

        val employeeSession = VerifiedSession(
            uid = "uid3",
            email = "emp@workcore.com",
            role = "EMPLOYEE",
            teamId = "ML Team"
        )
        assertFalse(employeeSession.isAdmin)
        assertFalse(employeeSession.isManager)
        assertTrue(employeeSession.isEmployee)
    }

    @Test
    fun `readSession returns null when not logged in`() = runBlocking {
        val authRepo = AuthRepository { null }
        val session = authRepo.readSession()
        assertNull("Session should be null when auth instance has no user", session)
    }

    @Test
    fun `signIn fails gracefully when auth provider returns null`() = runBlocking {
        val authRepo = AuthRepository { null }
        val result = authRepo.signIn("test@example.com", "password")
        assertTrue("Sign in should fail when auth is unavailable", result.isFailure)
    }

    @Test
    fun `sendPasswordReset handles empty email with failure`() = runBlocking {
        val authRepo = AuthRepository { null }
        val result = authRepo.sendPasswordReset("   ")
        assertTrue("Password reset should fail for empty email", result.isFailure)
    }

    @Test
    fun `sendPasswordReset fails when Firebase Auth is unavailable`() = runBlocking {
        val authRepo = AuthRepository { null }
        val result = authRepo.sendPasswordReset("user@example.com")
        assertTrue("Password reset should fail when Firebase Auth is unavailable", result.isFailure)
    }
}
