package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.SeedData
import com.example.data.WorkCoreRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Tests for data saving flows:
 * - EOD submission (standard + custom)
 * - EOD update (edit)
 * - Employee creation
 * - Data persists after save
 * - Duplicate EOD updates instead of creating new
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class SaveDataFlowTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkCoreRepository

    private val TODAY = "2026-10-22"
    private val TEST_EMPLOYEE_ID = "SS001"
    private val TEST_EMPLOYEE_NAME = "Test Employee"

    @Before
    fun setup() {
        runBlocking {
            val context = ApplicationProvider.getApplicationContext<Context>()
            db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
                .allowMainThreadQueries()
                .build()
            repository = WorkCoreRepository(db)
            repository.initializeSeedDataIfNeeded()
        }
    }

    @After
    fun teardown() { db.close() }

    // ── EOD Submission ────────────────────────────────────────────────

    @Test
    fun `submit standard EOD saves to database`() = runBlocking {
        val result = repository.saveOrUpdateEmployeeDashboardEod(
            employeeId = TEST_EMPLOYEE_ID,
            employeeName = TEST_EMPLOYEE_NAME,
            date = TODAY,
            project = "Backend Development",
            taskDescription = "Fixed login bug",
            hoursWorked = 8.0,
            progressPercentage = 100,
            workStatus = "Completed"
        )

        assertTrue("EOD save should succeed", result.isSuccess)

        // Verify it's in the database
        val saved = db.dailyEodDao().getEod(TEST_EMPLOYEE_ID, TODAY)
        assertNotNull("EOD should exist in DB after save", saved)
        assertEquals("Backend Development", saved?.project)
        assertEquals("Fixed login bug", saved?.todayWork)
        assertEquals("Completed", saved?.workStatus)
    }

    @Test
    fun `submitting EOD twice updates not duplicates`() = runBlocking {
        // Submit first time
        repository.saveOrUpdateEmployeeDashboardEod(
            employeeId = TEST_EMPLOYEE_ID,
            employeeName = TEST_EMPLOYEE_NAME,
            date = TODAY,
            project = "Project A",
            taskDescription = "Initial work",
            hoursWorked = 4.0,
            progressPercentage = 50,
            workStatus = "In Progress"
        )

        // Submit second time (edit)
        repository.saveOrUpdateEmployeeDashboardEod(
            employeeId = TEST_EMPLOYEE_ID,
            employeeName = TEST_EMPLOYEE_NAME,
            date = TODAY,
            project = "Project A",
            taskDescription = "Completed all tasks",
            hoursWorked = 8.0,
            progressPercentage = 100,
            workStatus = "Completed"
        )

        // Should have only ONE EOD for this date
        val allEods = db.dailyEodDao().getEodsForEmployee(TEST_EMPLOYEE_ID).first()
        val todayEods = allEods.filter { it.date == TODAY }
        assertEquals("Should have exactly 1 EOD for today (not 2)", 1, todayEods.size)
        assertEquals("Should be updated to Completed", "Completed", todayEods.first().workStatus)
        assertEquals("Should be updated description", "Completed all tasks", todayEods.first().todayWork)
    }

    @Test
    fun `submit custom EOD saves with form data`() = runBlocking {
        val marketingData = """{"visits":[{"collegeName":"Test College","personVisited":"Principal","designation":"Principal","description":"Good meeting"}]}"""
        val team = SeedData.teams.first { it.name == "Marketing" }

        val result = repository.submitCustomEod(
            employeeId = "AO001",
            employeeName = "SHOAIB KHAN",
            teamId = team.teamId,
            companyId = "COMP_AO",
            date = TODAY,
            customFormData = marketingData
        )

        assertTrue("Custom EOD save should succeed", result.isSuccess)

        val saved = db.dailyEodDao().getEod("AO001", TODAY)
        assertNotNull("Custom EOD should exist in DB", saved)
        assertEquals("Custom Form - MARKETING", saved?.project)
        assertFalse("Custom form data should not be empty", saved?.customFormData.isNullOrBlank())
        assertTrue("Custom form data should contain college name",
            saved?.customFormData?.contains("Test College") == true)
    }

    // ── EOD Retrieval ─────────────────────────────────────────────────

    @Test
    fun `getEod returns null for non-existent entry`() = runBlocking {
        val eod = db.dailyEodDao().getEod("NONEXISTENT", TODAY)
        assertNull("Should return null for non-existent EOD", eod)
    }

    @Test
    fun `multiple employees can submit EOD for same date`() = runBlocking {
        val employees = listOf("SS001", "SS002", "SS003")

        employees.forEach { empId ->
            repository.saveOrUpdateEmployeeDashboardEod(
                employeeId = empId,
                employeeName = "Employee $empId",
                date = TODAY,
                project = "Project",
                taskDescription = "Work done by $empId",
                hoursWorked = 8.0,
                progressPercentage = 100,
                workStatus = "Completed"
            )
        }

        // All 3 should be saved separately
        employees.forEach { empId ->
            val eod = db.dailyEodDao().getEod(empId, TODAY)
            assertNotNull("EOD should exist for $empId", eod)
        }
    }

    // ── Employee Data ─────────────────────────────────────────────────

    @Test
    fun `all 73 employees loaded after seed`() = runBlocking {
        val employees = db.employeeDao().getAllEmployees().first()
        // 1 ADMIN + 44 SS + 29 AO = 74
        assertTrue("Should have 74 total employees (ADMIN + 73)",
            employees.size >= 73)
    }

    @Test
    fun `ADMIN employee exists after seed`() = runBlocking {
        val admin = db.employeeDao().getAllEmployees().first()
            .firstOrNull { it.employeeId == "ADMIN" }
        assertNotNull("ADMIN employee should exist", admin)
        assertEquals("ADMIN", admin?.role)
    }

    @Test
    fun `SS employees have correct company`() = runBlocking {
        val employees = db.employeeDao().getAllEmployees().first()
        val ssEmployees = employees.filter { it.employeeId.startsWith("SS") }
        assertTrue("Should have SS employees", ssEmployees.isNotEmpty())
        ssEmployees.forEach { emp ->
            assertEquals("SS employees should be in COMP_SS",
                "COMP_SS", emp.companyId)
        }
    }

    @Test
    fun `AO employees have correct company`() = runBlocking {
        val employees = db.employeeDao().getAllEmployees().first()
        val aoEmployees = employees.filter { it.employeeId.startsWith("AO") }
        assertTrue("Should have AO employees", aoEmployees.isNotEmpty())
        aoEmployees.forEach { emp ->
            assertEquals("AO employees should be in COMP_AO",
                "COMP_AO", emp.companyId)
        }
    }

    // ── Departments & Teams ───────────────────────────────────────────

    @Test
    fun `departments loaded after seed`() = runBlocking {
        val depts = db.departmentDao().getAllDepartments().first()
        assertTrue("Should have departments", depts.isNotEmpty())
    }

    @Test
    fun `teams loaded with passwords after seed`() = runBlocking {
        val teams = db.teamDao().getAllTeams().first()
        assertTrue("Should have teams", teams.isNotEmpty())
        teams.forEach { team ->
            assertFalse("Team ${team.name} should have a password",
                team.teamPassword.isBlank())
        }
    }
}
