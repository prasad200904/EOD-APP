package com.example.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class SyncStatus {
  PENDING,
  SYNCING,
  SYNCED,
  FAILED
}

enum class Role {
  ADMIN,
  EMPLOYEE
}

@Entity(
  tableName = "departments",
  indices = [
    Index(value = ["deptId"], unique = true),
    Index(value = ["code"], unique = true)
  ]
)
data class DepartmentEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val deptId: String, // "DEPT_ML", "DEPT_GT", "DEPT_WRITING", "DEPT_CYBER", "DEPT_DB"
  val name: String, // "Machine Learning", "General Tech", "Content & Writing", "Cyber Security", "Database"
  val code: String, // "ML", "GT", "Writing", "Cyber", "DB"
  val prefix: String, // "ML", "GT", "WR", "CYBER", "DB"
  val managerIds: String = "", // Comma-separated manager IDs e.g. "EMP009"
  val description: String = ""
)

@Entity(
  tableName = "employees",
  indices = [
    Index(value = ["employeeId"], unique = true),
    Index(value = ["department"]),
    Index(value = ["departmentId"]),
    Index(value = ["team"]),
    Index(value = ["managerId"])
  ]
)
data class EmployeeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String,
  val name: String,
  val email: String,
  val phone: String,
  val department: String, // e.g. "ML", "GT", "Writing", "Cyber", "DB"
  val departmentId: String = "DEPT_ML",
  val managedDepartments: String = "", // e.g. "ML,DB" for managers managing multiple departments
  val team: String = "Core Team",
  val managerId: String = "EMP008",
  val designation: String,
  val defaultProject: String = "Core Engineering",
  val joiningDate: String,
  val role: String = Role.EMPLOYEE.name,
  val isActive: Boolean = true,
  val password: String = "password123",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(
  tableName = "daily_eods",
  indices = [
    Index(value = ["employeeId", "date"], unique = true),
    Index(value = ["employeeId"]),
    Index(value = ["date"]),
    Index(value = ["departmentId"]),
    Index(value = ["workStatus"]),
    Index(value = ["isOnTime"])
  ]
)
data class DailyEodEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String,
  val employeeName: String,
  val departmentId: String = "DEPT_ML",
  val departmentCode: String = "ML",
  val date: String, // YYYY-MM-DD
  val project: String, // Project / Work Area
  val todayWork: String, // Summary of today's work
  val workCompleted: String = "", // Detailed accomplishments
  val hoursWorked: Double = 0.0, // Deprecated: hours tracking removed
  val progressPercentage: Int = 100, // 0 - 100
  val workStatus: String, // Completed, In Progress, Blocked, No Work, On Leave
  val attendanceStatus: String = "Present", // Present, Leave, Holiday
  val blockers: String = "", // Blockers / Issues
  val remarks: String = "", // Remarks / Notes
  val tomorrowPlan: String = "", // Tomorrow's Plan
  val submissionTime: String = "17:45", // HH:mm:ss
  val isOnTime: Boolean = true, // Calculated based on configured cutoff time (e.g. 18:30)
  val syncStatus: String = SyncStatus.PENDING.name, // PENDING, SYNCING, SYNCED, FAILED - Changed to PENDING for auto-sync
  val submittedAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "company_config")
data class CompanyConfigEntity(
  @PrimaryKey val configKey: String = "default_config",
  val eodCutoffTime: String = "18:30", // 6:30 PM cutoff
  val expectedDailyHours: Double = 8.0,
  val workDaysPerWeek: Int = 5
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val userId: String,
  val action: String,
  val entityType: String,
  val entityId: String,
  val timestamp: Long = System.currentTimeMillis(),
  val description: String
)

@Entity(
  tableName = "teams",
  indices = [
    Index(value = ["teamId"], unique = true),
    Index(value = ["name"])
  ]
)
data class TeamEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val teamId: String, // e.g. "TEAM_ML", "TEAM_WRITING", "TEAM_DB", "TEAM_GENERAL", "TEAM_WEB", "TEAM_TEST"
  val name: String, // e.g. "ML Team", "Writing Team", "DB Team", "General Team", "Web Development Team", "Testing Team"
  val department: String, // "Machine Learning", "Content", "Database", "Operations", "Engineering", "QA"
  val managerId: String, // e.g. "EMP009"
  val managerName: String, // e.g. "Vikram Joshi"
  val projects: String, // Comma-separated or serialized projects e.g. "Employee Behavior Prediction, NLP Summarizer"
  val createdDate: String = "2024-01-01",
  val status: String = "Active", // "Active", "Inactive"
  val description: String = "",
  val teamPassword: String = "password123" // Team-level login password (unique per team)
)

@Entity(
  tableName = "notifications",
  indices = [
    Index(value = ["targetRole"]),
    Index(value = ["targetTeam"])
  ]
)
data class NotificationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val title: String, // "🔴 EOD Pending", "🟠 Late EOD", "🟢 EOD Complete", "🔴 Multiple Pending"
  val message: String,
  val type: String, // "PENDING", "LATE", "COMPLETE", "MULTIPLE_PENDING"
  val targetRole: String = "ALL", // "ALL", "ADMIN", "MANAGER", "EMPLOYEE"
  val targetTeam: String = "",
  val targetEmployeeId: String = "",
  val timestamp: Long = System.currentTimeMillis(),
  val timeAgo: String = "Today",
  val isRead: Boolean = false
)

data class TeamMemberBehaviorItem(
  val employee: EmployeeEntity,
  val initials: String,
  val submissionRate: Int,
  val missedCount: Int,
  val missedDates: List<String> = emptyList(),
  val leavesCount: Int = 0,
  val lateCount: Int = 0,
  val currentStreak: Int = 0,
  val todayStatus: String = "Pending", // "Pending", "Leave", "Done"
  val todayEod: DailyEodEntity? = null
)

