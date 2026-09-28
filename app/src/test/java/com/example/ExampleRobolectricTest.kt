package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.AppDatabase
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
      SeedData.dailyEods,
      SeedData.TODAY
    )

    assertEquals(SeedData.employees.size, summary.totalEmployees)
    assertEquals(SeedData.employees.size, summary.activeEmployees)
    assertTrue("Should have today EOD submissions", summary.todayEodSubmitted > 0)
    assertTrue("Should have some employees needing attention", summary.employeesNeedingAttentionCount > 0)
  }

  @Test
  fun testWorkMonitorAndAttentionRequired() = runBlocking {
    val monitorItems = repository.computeWorkMonitor(
      SeedData.employees,
      SeedData.dailyEods,
      SeedData.TODAY
    )

    assertEquals(SeedData.employees.size, monitorItems.size)
    val attentionEmployee = monitorItems.firstOrNull { it.employee.employeeId == "ML-001" }
      ?: monitorItems.first { it.employee.employeeId == "EMP003" }
    // Employee has missed consecutive EODs
    assertNotNull(attentionEmployee.attentionReason)
  }

  @Test
  fun testBehaviorMetricsCalculation() = runBlocking {
    val metrics = repository.computeBehaviorMetrics(
      SeedData.employees,
      SeedData.dailyEods,
      SeedData.TODAY,
      expectedDays = 7
    )

    assertEquals(SeedData.employees.size, metrics.size)
    val ravi = metrics.first { it.employee.employeeId == "EMP001" }
    assertEquals("Strong", ravi.eodConsistencyStatus)
    assertTrue("Ravi has high submission rate", ravi.submissionRate >= 90)

    val attentionEmp = metrics.firstOrNull { it.employee.employeeId == "ML-001" }
      ?: metrics.first { it.employee.employeeId == "EMP003" }
    assertEquals("Needs Attention", attentionEmp.eodConsistencyStatus)
    assertNotNull("Employee should have attention alert", attentionEmp.attentionAlert)
  }

  @Test
  fun testCsvExport() = runBlocking {
    val metrics = repository.computeBehaviorMetrics(
      SeedData.employees,
      SeedData.dailyEods,
      SeedData.TODAY
    )
    val csv = repository.generateCsvReport(
      reportType = "EOD",
      employees = SeedData.employees,
      eods = SeedData.dailyEods,
      metrics = metrics
    )

    assertTrue(csv.startsWith("Employee ID,Employee Name,Date"))
    assertTrue(csv.contains("Ravi Kumar"))
  }
}
