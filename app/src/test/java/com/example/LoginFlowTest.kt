package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.Role
import com.example.data.SeedData
import com.example.data.WorkCoreRepository
import com.example.data.auth.AuthRepository
import com.example.data.auth.VerifiedSession
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for password-free login flows:
 * - Admin login via token claims
 * - Employee login via token claims
 * - Invalid credentials rejected
 * - Team roster access by team name
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LoginFlowTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkCoreRepository
    private lateinit var fakeAuth: FakeTestAuthRepository

    class FakeTestAuthRepository : AuthRepository() {
        private val accounts = mutableMapOf<String, VerifiedSession>()

        fun registerUser(email: String, session: VerifiedSession) {
            accounts[email.lowercase()] = session
        }

        override suspend fun signIn(email: String, password: String): Result<VerifiedSession> {
            val session = accounts[email.lowercase()]
            return if (session != null && password == "validPassword123") {
                Result.success(session)
            } else {
                Result.failure(IllegalArgumentException("Invalid ID or password."))
            }
        }

        override suspend fun readSession(forceRefresh: Boolean): VerifiedSession? {
            return accounts.values.firstOrNull()
        }

        override suspend fun sendPasswordReset(email: String): Result<Unit> {
            return Result.success(Unit)
        }
    }

    @Before
    fun setup() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            fakeAuth = FakeTestAuthRepository()

            fakeAuth.registerUser(
                "admin@workcore.com",
                VerifiedSession(
                    uid = "ADMIN_UID",
                    email = "admin@workcore.com",
                    role = "ADMIN",
                    employeeId = "ADMIN"
                )
            )

            fakeAuth.registerUser(
                "ss001@sandspace.com",
                VerifiedSession(
                    uid = "SS001_UID",
                    email = "ss001@sandspace.com",
                    role = "EMPLOYEE",
                    employeeId = "SS001",
                    teamId = "General Team"
                )
            )

            repository = WorkCoreRepository(db, authRepository = fakeAuth)
            repository.initializeSeedDataIfNeeded()
        }
    }

    @After
    fun teardown() { db.close() }

    // ── Admin Login ───────────────────────────────────────────────────

    @Test
    fun `admin login with valid token claims succeeds`() = runBlocking {
        val result = repository.authenticate(
            role = Role.ADMIN,
            teamName = null,
            identifier = "admin@workcore.com",
            passwordInput = "validPassword123"
        )
        assertTrue("Admin login should succeed", result.isSuccess)
        val employee = result.getOrNull()
        assertNotNull(employee)
        assertEquals("ADMIN", employee?.employeeId)
        assertEquals("ADMIN", employee?.role)
    }

    @Test
    fun `admin login with wrong password fails`() = runBlocking {
        val result = repository.authenticate(
            role = Role.ADMIN,
            teamName = null,
            identifier = "admin@workcore.com",
            passwordInput = "wrongpassword"
        )
        assertTrue("Wrong password should fail", result.isFailure)
    }

    @Test
    fun `employee login with valid token claims succeeds`() = runBlocking {
        val result = repository.authenticate(
            role = Role.EMPLOYEE,
            teamName = "General Team",
            identifier = "ss001@sandspace.com",
            passwordInput = "validPassword123"
        )
        assertTrue("Employee login should succeed", result.isSuccess)
        val employee = result.getOrNull()
        assertNotNull(employee)
        assertEquals("EMPLOYEE", employee?.role)
    }

    @Test
    fun `unknown account login fails`() = runBlocking {
        val result = repository.authenticate(
            role = Role.EMPLOYEE,
            teamName = "General Team",
            identifier = "unknown@company.com",
            passwordInput = "validPassword123"
        )
        assertTrue("Unknown user should fail", result.isFailure)
    }

    // ── Team Roster Lookups (Password-Free Roster Flow) ────────────────

    @Test
    fun `general team roster retrieval succeeds`() = runBlocking {
        val members = repository.getEmployeesByTeam("General Team")
        assertTrue("General Team should have employees", members.isNotEmpty())
    }

    @Test
    fun `cyber security team roster retrieval succeeds`() = runBlocking {
        val members = repository.getEmployeesByTeam("Cyber Security")
        assertTrue("Cyber Security should have employees", members.isNotEmpty())
    }

    @Test
    fun `ml team roster retrieval succeeds`() = runBlocking {
        val members = repository.getEmployeesByTeam("ML Team")
        assertTrue("ML Team should have employees", members.isNotEmpty())
    }

    @Test
    fun `AO marketing team roster retrieval succeeds`() = runBlocking {
        val members = repository.getEmployeesByTeam("Marketing")
        assertTrue("Marketing team should have employees", members.isNotEmpty())
    }

    @Test
    fun `AO counsellor team roster retrieval succeeds`() = runBlocking {
        val members = repository.getEmployeesByTeam("Counsellor")
        assertTrue("Counsellor team should have employees", members.isNotEmpty())
    }

    @Test
    fun `AO tele caller team roster retrieval succeeds`() = runBlocking {
        val members = repository.getEmployeesByTeam("Tele Caller")
        assertTrue("Tele Caller team should have employees", members.isNotEmpty())
    }
}
