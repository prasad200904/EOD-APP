package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.SeedData
import com.example.data.WorkCoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for login flows:
 * - SS team login (General Team, ML Team, Cyber Security, etc.)
 * - AO team login (Marketing, Counsellor, Tele Caller)
 * - Admin login
 * - Wrong password rejected
 * - Unknown team rejected
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class LoginFlowTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkCoreRepository

    @Before
    fun setup() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WorkCoreRepository(db)
        repository.initializeSeedDataIfNeeded()
    }

    @After
    fun teardown() { db.close() }

    // ── Admin Login ───────────────────────────────────────────────────

    @Test
    fun `admin login with correct password succeeds`() = runBlocking {
        val result = repository.authenticate(
            role = com.example.data.Role.ADMIN,
            teamName = null,
            identifier = "admin",
            passwordInput = "admin123"
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
            role = com.example.data.Role.ADMIN,
            teamName = null,
            identifier = "admin",
            passwordInput = "wrongpassword"
        )
        assertTrue("Wrong password should fail", result.isFailure)
    }

    // ── SS Team Login ─────────────────────────────────────────────────

    @Test
    fun `general team login with correct password succeeds`() = runBlocking {
        val team = SeedData.teams.first { it.name == "General Team" }
        val result = repository.authenticateByTeam(
            teamName = "General Team",
            password = team.teamPassword
        )
        assertTrue("General Team login should succeed", result.isSuccess)
    }

    @Test
    fun `cyber security team login with correct password succeeds`() = runBlocking {
        val team = SeedData.teams.first { it.name == "Cyber Security" }
        val result = repository.authenticateByTeam(
            teamName = "Cyber Security",
            password = team.teamPassword
        )
        assertTrue("Cyber Security login should succeed", result.isSuccess)
    }

    @Test
    fun `ml team login with correct password succeeds`() = runBlocking {
        val team = SeedData.teams.first { it.name == "ML Team" }
        val result = repository.authenticateByTeam(
            teamName = "ML Team",
            password = team.teamPassword
        )
        assertTrue("ML Team login should succeed", result.isSuccess)
    }

    @Test
    fun `team login with wrong password fails`() = runBlocking {
        val result = repository.authenticateByTeam(
            teamName = "General Team",
            password = "wrongpassword123"
        )
        assertTrue("Wrong team password should fail", result.isFailure)
    }

    @Test
    fun `unknown team login fails`() = runBlocking {
        val result = repository.authenticateByTeam(
            teamName = "NonExistentTeam",
            password = "anypassword"
        )
        assertTrue("Unknown team login should fail", result.isFailure)
    }

    // ── AO Team Login ─────────────────────────────────────────────────

    @Test
    fun `AO marketing team login with correct password succeeds`() = runBlocking {
        val team = SeedData.teams.first { it.name == "Marketing" }
        val result = repository.authenticateByTeam(
            teamName = "Marketing",
            password = team.teamPassword
        )
        assertTrue("Marketing team login should succeed", result.isSuccess)
    }

    @Test
    fun `AO counsellor team login with correct password succeeds`() = runBlocking {
        val team = SeedData.teams.first { it.name == "Counsellor" }
        val result = repository.authenticateByTeam(
            teamName = "Counsellor",
            password = team.teamPassword
        )
        assertTrue("Counsellor team login should succeed", result.isSuccess)
    }

    @Test
    fun `AO tele caller team login with correct password succeeds`() = runBlocking {
        val team = SeedData.teams.first { it.name == "Tele Caller" }
        val result = repository.authenticateByTeam(
            teamName = "Tele Caller",
            password = team.teamPassword
        )
        assertTrue("Tele Caller team login should succeed", result.isSuccess)
    }

    // ── Employee Count After Login ────────────────────────────────────

    @Test
    fun `general team has correct employees after seed`() = runBlocking {
        val members = repository.getEmployeesByTeam("General Team")
        assertTrue("General Team should have employees", members.isNotEmpty())
        members.forEach { emp ->
            assertTrue(
                "All members should be from General Team",
                emp.team.equals("General Team", ignoreCase = true)
            )
        }
    }

    @Test
    fun `AO marketing team has correct employees after seed`() = runBlocking {
        val members = repository.getEmployeesByTeam("Marketing")
        assertTrue("Marketing team should have employees", members.isNotEmpty())
        members.forEach { emp ->
            assertTrue(
                "All members should be from Marketing",
                emp.team.equals("Marketing", ignoreCase = true)
            )
        }
    }
}
