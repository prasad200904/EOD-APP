package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
import com.example.data.DailyEodEntity
import com.example.data.SeedData
import com.example.data.WorkCoreRepository
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: WorkCoreRepository

  private val testEod = DailyEodEntity(
    employeeId = "SS001",
    employeeName = "AADEPU SUSHMA",
    departmentId = "DEPT_CYBER",
    departmentCode = "CYBER",
    attendanceStatus = "Present",
    date = SeedData.TODAY,
    project = "Cyber Security Audit",
    todayWork = "Audited systems",
    workCompleted = "Completed initial review",
    hoursWorked = 8.0,
    progressPercentage = 100,
    workStatus = "Completed",
    submissionTime = "18:00",
    isOnTime = true
  )

  @Before
  fun setup() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = WorkCoreRepository(db)
  }

  @After
  fun teardown() {
    db.close()
  }

  @Test
  fun testAppNameResource() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("WorkCore", appName)
  }

  @Test
  fun testSeedDataAndSummary() = runBlocking {
    repository.initializeSeedDataIfNeeded()
    val summary = repository.computeDashboardSummary(
      SeedData.employees,
      listOf(testEod),
      SeedData.TODAY
    )

    assertEquals(SeedData.employees.size, summary.totalEmployees)
    assertEquals(SeedData.employees.size, summary.activeEmployees)
    assertTrue("Should have today EOD submissions", summary.todayEodSubmitted > 0)
    assertTrue("Should have pending submissions", summary.todayEodPending > 0)
  }

  @Test
  fun testWorkMonitorAndAttentionRequired() = runBlocking {
    val monitorItems = repository.computeWorkMonitor(
      SeedData.employees,
      listOf(testEod),
      SeedData.TODAY
    )

    assertEquals(SeedData.employees.size, monitorItems.size)
    val submittedEmp = monitorItems.first { it.employee.employeeId == "SS001" }
    assertTrue(submittedEmp.isSubmittedToday)
    assertEquals("Completed", submittedEmp.workStatus)

    val pendingEmp = monitorItems.first { it.employee.employeeId != "SS001" }
    assertEquals(false, pendingEmp.isSubmittedToday)
    assertEquals("Pending", pendingEmp.workStatus)
  }

  @Test
  fun testBehaviorMetricsCalculation() = runBlocking {
    val weekEods = (1..7).map { day ->
      testEod.copy(date = "2026-10-0$day")
    }
    val metrics = repository.computeBehaviorMetrics(
      SeedData.employees,
      weekEods,
      SeedData.TODAY,
      expectedDays = 7
    )

    assertEquals(SeedData.employees.size, metrics.size)
    val sushma = metrics.first { it.employee.employeeId == "SS001" }
    assertEquals("Strong", sushma.eodConsistencyStatus)
    assertTrue("Sushma has high submission rate", sushma.submissionRate >= 90)

    val attentionEmp = metrics.first { it.employee.employeeId != "SS001" }
    assertEquals("Needs Attention", attentionEmp.eodConsistencyStatus)
    assertNotNull("Employee should have attention alert", attentionEmp.attentionAlert)
  }

  @Test
  fun testCsvExport() = runBlocking {
    val metrics = repository.computeBehaviorMetrics(
      SeedData.employees,
      listOf(testEod),
      SeedData.TODAY
    )
    val csv = repository.generateCsvReport(
      reportType = "EOD",
      employees = SeedData.employees,
      eods = listOf(testEod),
      metrics = metrics
    )

    assertTrue(csv.startsWith("Employee ID,Employee Name,Date"))
    assertTrue(csv.contains("AADEPU SUSHMA"))
    assertTrue(csv.contains("Cyber Security Audit"))
  }
}
