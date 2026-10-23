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
 * Tests for key screen data flows:
 * - Admin console: live status data
 * - Roster screen: correct employees per team
 * - History screen: correct EOD filter
 * - Reports screen: correct employee scope
 * - AO roster: only AO employees shown
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [33])
class KeyScreensTest {

    private lateinit var db: AppDatabase
    private lateinit var repository: WorkCoreRepository

    private val TODAY = "2026-10-22"

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

    // ── Admin Console (Team Status) ───────────────────────────────────

    @Test
    fun `admin console shows all employees when no filter`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()
            .filter { it.role == "EMPLOYEE" }
        val eods = db.dailyEodDao().getAllEods().first()

        val members = repository.computeManagerTeamMetrics(
            departmentCode = "ALL",
            employees = allEmployees,
            dailyEods = eods,
            todayDate = TODAY
        )

        assertEquals("ALL filter should show all employees",
            allEmployees.size, members.size)
    }

    @Test
    fun `admin console GT filter shows only General Team`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()
        val eods = db.dailyEodDao().getAllEods().first()

        val members = repository.computeManagerTeamMetrics(
            departmentCode = "GT",
            employees = allEmployees,
            dailyEods = eods,
            todayDate = TODAY
        )

        assertTrue("GT filter should return employees", members.isNotEmpty())
        members.forEach { item ->
            assertTrue("All members should be from General Team",
                item.employee.team.contains("General", ignoreCase = true) ||
                item.employee.department.contains("General", ignoreCase = true))
        }
    }

    @Test
    fun `admin console TeleCaller filter shows only Tele Caller team`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()
        val eods = db.dailyEodDao().getAllEods().first()

        val members = repository.computeManagerTeamMetrics(
            departmentCode = "TeleCaller",
            employees = allEmployees,
            dailyEods = eods,
            todayDate = TODAY
        )

        assertTrue("TeleCaller filter should return employees", members.isNotEmpty())
        members.forEach { item ->
            assertTrue("All members should be from Tele Caller",
                item.employee.team.contains("Tele", ignoreCase = true) ||
                item.employee.department.contains("Tele", ignoreCase = true))
        }
    }

    @Test
    fun `admin console shows pending status before EOD submission`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()
        val eods = db.dailyEodDao().getAllEods().first()

        val members = repository.computeManagerTeamMetrics(
            departmentCode = "ALL",
            employees = allEmployees,
            dailyEods = eods,
            todayDate = TODAY // Fresh day - no EODs submitted yet
        )

        val pendingCount = members.count { it.todayStatus == "Pending" }
        assertTrue("All employees should be Pending on fresh day",
            pendingCount == members.size)
    }

    @Test
    fun `admin console shows Done status after EOD submission`() = runBlocking {
        // Submit EOD for SS001
        repository.saveOrUpdateEmployeeDashboardEod(
            employeeId = "SS001",
            employeeName = "Test",
            date = TODAY,
            project = "Test Project",
            taskDescription = "Test work",
            hoursWorked = 8.0,
            progressPercentage = 100,
            workStatus = "Completed"
        )

        val allEmployees = db.employeeDao().getAllEmployees().first()
        val eods = db.dailyEodDao().getAllEods().first()

        val members = repository.computeManagerTeamMetrics(
            departmentCode = "ALL",
            employees = allEmployees,
            dailyEods = eods,
            todayDate = TODAY
        )

        val ss001Status = members.firstOrNull { it.employee.employeeId == "SS001" }?.todayStatus
        assertEquals("SS001 should show Done after submission", "Done", ss001Status)
    }

    // ── Roster Screen (Department/Team) ───────────────────────────────

    @Test
    fun `General Team roster shows only General Team employees`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()

        val gtEmployees = allEmployees.filter { emp ->
            emp.role == "EMPLOYEE" &&
            (emp.team.equals("General Team", ignoreCase = true) ||
             emp.department.equals("General Team", ignoreCase = true))
        }

        assertTrue("General Team should have employees", gtEmployees.isNotEmpty())
        gtEmployees.forEach { emp ->
            assertFalse("No SS employees in GT should have AO prefix",
                emp.employeeId.startsWith("AO") && !emp.team.contains("General"))
        }
    }

    @Test
    fun `AO roster shows only AO employees`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()

        val aoEmployees = allEmployees.filter { emp ->
            emp.companyId == "COMP_AO" && emp.role == "EMPLOYEE"
        }

        assertTrue("Should have AO employees", aoEmployees.isNotEmpty())
        aoEmployees.forEach { emp ->
            assertTrue("All AO employees should start with AO",
                emp.employeeId.startsWith("AO"))
        }
    }

    @Test
    fun `AO marketing roster shows only Marketing team`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()

        val marketingEmployees = allEmployees.filter { emp ->
            val isAO = emp.companyId == "COMP_AO" || emp.employeeId.startsWith("AO")
            val isMarketing = emp.team.equals("Marketing", ignoreCase = true)
            isAO && isMarketing && emp.role == "EMPLOYEE"
        }

        assertTrue("Marketing team should have employees", marketingEmployees.isNotEmpty())
        marketingEmployees.forEach { emp ->
            assertEquals("All should be in Marketing team", "Marketing", emp.team)
        }
    }

    @Test
    fun `AO counsellor roster shows only Counsellor team`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()

        val counsellorEmployees = allEmployees.filter { emp ->
            val isAO = emp.companyId == "COMP_AO" || emp.employeeId.startsWith("AO")
            val isCounsellor = emp.team.equals("Counsellor", ignoreCase = true)
            isAO && isCounsellor && emp.role == "EMPLOYEE"
        }

        assertTrue("Counsellor team should have employees", counsellorEmployees.isNotEmpty())
        counsellorEmployees.forEach { emp ->
            assertEquals("All should be in Counsellor team", "Counsellor", emp.team)
        }
    }

    @Test
    fun `SS employees not visible in AO roster`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()

        val aoRosterEmployees = allEmployees.filter { emp ->
            emp.companyId == "COMP_AO" && emp.role == "EMPLOYEE"
        }

        val ssInAoRoster = aoRosterEmployees.filter { it.employeeId.startsWith("SS") }
        assertEquals("No SS employees should appear in AO roster", 0, ssInAoRoster.size)
    }

    // ── History Screen Filter ─────────────────────────────────────────

    @Test
    fun `history filter by team shows only that team EODs`() = runBlocking {
        // Submit EODs for both SS and AO employees
        repository.saveOrUpdateEmployeeDashboardEod(
            employeeId = "SS001",
            employeeName = "SS Employee",
            date = TODAY,
            project = "Project",
            taskDescription = "SS work",
            hoursWorked = 8.0,
            progressPercentage = 100,
            workStatus = "Completed"
        )
        repository.submitCustomEod(
            employeeId = "AO001",
            employeeName = "AO Employee",
            teamId = SeedData.teams.first { it.name == "Marketing" }.teamId,
            companyId = "COMP_AO",
            date = TODAY,
            customFormData = """{"visits":[]}"""
        )

        val allEods = db.dailyEodDao().getAllEods().first()

        // Filter for General Team only
        val allEmployees = db.employeeDao().getAllEmployees().first()
        val gtTeamIds = allEmployees
            .filter { it.team.contains("General", ignoreCase = true) }
            .map { it.employeeId }
            .toSet()

        val gtEods = allEods.filter { it.employeeId in gtTeamIds }
        val hasAoInGtFilter = gtEods.any { it.employeeId.startsWith("AO") }
        assertFalse("GT team filter should not show AO EODs", hasAoInGtFilter)
    }

    // ── Reports Screen ────────────────────────────────────────────────

    @Test
    fun `CSV report contains correct employee data`() = runBlocking {
        // Submit an EOD first
        repository.saveOrUpdateEmployeeDashboardEod(
            employeeId = "SS001",
            employeeName = "AADEPU SUSHMA",
            date = TODAY,
            project = "Cyber Security Audit",
            taskDescription = "Network vulnerability assessment",
            hoursWorked = 8.0,
            progressPercentage = 100,
            workStatus = "Completed"
        )

        val employees = db.employeeDao().getAllEmployees().first()
        val eods = db.dailyEodDao().getAllEods().first()
        val metrics = repository.computeBehaviorMetrics(employees, eods, TODAY)

        val csv = repository.generateCsvReport(
            reportType = "EOD",
            employees = employees,
            eods = eods,
            metrics = metrics
        )

        assertTrue("CSV should have header", csv.contains("Employee ID"))
        assertTrue("CSV should have employee name", csv.contains("AADEPU SUSHMA"))
        assertTrue("CSV should have project", csv.contains("Cyber Security Audit"))
    }

    @Test
    fun `dashboard summary shows correct totals`() = runBlocking {
        val allEmployees = db.employeeDao().getAllEmployees().first()
        val eods = db.dailyEodDao().getAllEods().first()

        val summary = repository.computeDashboardSummary(allEmployees, eods, TODAY)

        assertEquals("Total employees should match seed data",
            allEmployees.size, summary.totalEmployees)
        assertTrue("Today submitted should be 0 on fresh day",
            summary.todayEodSubmitted == 0)
    }
}
