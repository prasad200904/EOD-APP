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
  MANAGER,
  EMPLOYEE
}

enum class EodFormType {
  STANDARD,      // Default form for Sandspace tech teams
  COUNSELLOR,    // Academic Overseas - Counsellors
  TELECALLING,   // Academic Overseas - Telecalling
  MARKETING,     // Academic Overseas - Marketing
  SIMPLE         // Academic Overseas - Other team
}

@Entity(
  tableName = "companies",
  indices = [
    Index(value = ["companyId"], unique = true),
    Index(value = ["name"])
  ]
)
data class CompanyEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val companyId: String,              // "COMP_SANDSPACE", "COMP_AO"
  val name: String,                   // "Sandspace Technologies Pvt Ltd", "Academic Overseas"
  val parentCompanyId: String? = null,   // null for parent, "COMP_SANDSPACE" for subsidiaries
  val type: String = "PARENT",        // "PARENT", "SUBSIDIARY"
  val description: String = "",
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

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
    Index(value = ["teamId"]),
    Index(value = ["companyId"]),
    Index(value = ["managerId"])
  ]
)
data class EmployeeEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String,
  val name: String,
  val email: String,
  val phone: String,
  val companyId: String = "COMP_SANDSPACE",  // NEW: Company assignment
  val department: String, // e.g. "ML", "GT", "Writing", "Cyber", "DB", "Counsellors"
  val departmentId: String = "DEPT_ML",
  val managedDepartments: String = "", // e.g. "ML,DB" for managers managing multiple departments
  val team: String = "Core Team",
  val teamId: String = "TEAM_ML",  // NEW: Direct team reference
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
    Index(value = ["teamId"]),
    Index(value = ["companyId"]),
    Index(value = ["departmentId"]),
    Index(value = ["eodFormType"]),
    Index(value = ["workStatus"]),
    Index(value = ["isOnTime"])
  ]
)
data class DailyEodEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String,
  val employeeName: String,
  val teamId: String = "TEAM_ML",             // NEW: Team reference
  val companyId: String = "COMP_SANDSPACE",   // NEW: Company reference
  val departmentId: String = "DEPT_ML",
  val departmentCode: String = "ML",
  val date: String, // YYYY-MM-DD
  
  // EOD Form Type (determines which form fields are used)
  val eodFormType: String = EodFormType.STANDARD.name,  // NEW: STANDARD, COUNSELLOR, TELECALLING, MARKETING, SIMPLE
  
  // STANDARD form fields (for tech teams)
  val project: String = "", // Project / Work Area
  val todayWork: String = "", // Summary of today's work
  val workCompleted: String = "", // Detailed accomplishments
  val hoursWorked: Double = 0.0, // Deprecated: hours tracking removed
  val progressPercentage: Int = 100, // 0 - 100
  val workStatus: String = "Completed", // Completed, In Progress, Blocked, No Work, On Leave
  val attendanceStatus: String = "Present", // Present, Leave, Holiday
  val blockers: String = "", // Blockers / Issues
  val remarks: String = "", // Remarks / Notes
  val tomorrowPlan: String = "", // Tomorrow's Plan
  
  // Custom form data (JSON string for COUNSELLOR, TELECALLING, MARKETING, SIMPLE forms)
  val customFormData: String = "",  // NEW: JSON object for custom fields
  
  // Common fields
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
    Index(value = ["name"]),
    Index(value = ["companyId"]),
    Index(value = ["eodFormType"])
  ]
)
data class TeamEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val teamId: String, // e.g. "TEAM_ML", "TEAM_WRITING", "TEAM_AO_COUNSELLORS"
  val name: String, // e.g. "ML Team", "Writing Team", "Counsellors"
  val companyId: String = "COMP_SANDSPACE",  // NEW: Company reference
  val department: String, // "Machine Learning", "Content", "Database", "Counsellors", "Telecalling"
  val eodFormType: String = EodFormType.STANDARD.name,  // NEW: STANDARD, COUNSELLOR, TELECALLING, MARKETING, SIMPLE
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

enum class TeamType(val displayName: String, val departmentCode: String) {
  COUNSELLING("Counselling Team", "COUNSEL"),
  TELECALLING("Telecalling Team", "TELECALL"),
  MARKETING("Marketing Team", "MKT"),
  OTHER("Other Team", "OTHER");

  companion object {
    fun fromCode(code: String): TeamType = entries.find { it.departmentCode.equals(code, ignoreCase = true) } ?: OTHER
    fun codeFor(code: String): String = fromCode(code).departmentCode
  }
}

enum class Country(val displayName: String) {
  USA("USA"), UK("UK"), CANADA("Canada"), AUSTRALIA("Australia"), OTHER("Other");

  companion object {
    fun fromString(s: String): Country = entries.find { it.name.equals(s, ignoreCase = true) } ?: OTHER
  }
}

enum class CounsellingApplicationStatus(val displayName: String) {
  APPLICATION_SUBMITTED("Application Submitted"),
  PENDING_DOCUMENTS("Pending Documents"),
  OFFER_RECEIVED("Offer Letter Received"),
  VISA_LODGED("Visa Lodged");

  companion object {
    fun fromString(s: String): CounsellingApplicationStatus = entries.find { it.name.equals(s, ignoreCase = true) } ?: APPLICATION_SUBMITTED
  }
}

enum class TelecallingStatus(val displayName: String) {
  IN_PROGRESS("In Progress"),
  TARGET_ACHIEVED("Target Achieved"),
  LEADS_PENDING("Leads Pending");

  companion object {
    fun fromString(s: String): TelecallingStatus = entries.find { it.name.equals(s, ignoreCase = true) } ?: IN_PROGRESS
  }
}

@Entity(
  tableName = "team_member_data",
  indices = [
    Index(value = ["employeeId", "teamType"], unique = true),
    Index(value = ["employeeId"])
  ]
)
data class TeamMemberDataEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String = "",
  val teamType: String, // "COUNSEL", "TELECALL", "MKT", "OTHER"
  // Counselling fields
  val studentName: String = "",
  val universityName: String = "",
  val country: String = "", // enum name: USA/UK/CANADA/AUSTRALIA/OTHER
  val applicationStatus: String = "", // enum name
  // Telecalling fields
  val telecallingStatus: String = "", // enum name
  val calls: Int = 0,
  val followUps: Int = 0,
  val conversions: Int = 0,
  // Marketing fields
  val collegeName: String = "",
  val personNameVisited: String = "",
  val personDesignation: String = "",
  // Common
  val description: String = ""
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



// ============================================================================
// CUSTOM EOD FORM DATA CLASSES (JSON serialization)
// ============================================================================

/**
 * Counsellor EOD Form - Multiple students per submission
 */
data class CounsellorFormData(
  val students: List<StudentEntry> = emptyList()
)

data class StudentEntry(
  val studentName: String,
  val universityName: String,
  val country: String,        // "USA", "UK", "Canada", "Australia", "Other"
  val status: String,         // "Applied", "Interview Scheduled", "Accepted", "Rejected", "In Progress"
  val description: String
)

/**
 * Telecalling EOD Form - Call metrics with team totals
 */
data class TelecallingFormData(
  val callsMade: Int,
  val followUps: Int,
  val conversions: Int,
  val status: String,         // "Target Met", "In Progress", "Below Target"
  val description: String
)

/**
 * Marketing EOD Form - Multiple college visits per submission
 */
data class MarketingFormData(
  val visits: List<CollegeVisit> = emptyList()
)

data class CollegeVisit(
  val collegeName: String,
  val personVisited: String,
  val designation: String,
  val description: String
)

/**
 * Simple EOD Form - Description only
 */
data class SimpleFormData(
  val description: String
)

// JSON serialization helpers
object CustomFormDataSerializer {
  private val gson = com.google.gson.Gson()
  
  fun serializeCounsellor(data: CounsellorFormData): String = gson.toJson(data)
  fun deserializeCounsellor(json: String): CounsellorFormData? = 
    try { gson.fromJson(json, CounsellorFormData::class.java) } catch (e: Exception) { null }
  
  fun serializeTelecalling(data: TelecallingFormData): String = gson.toJson(data)
  fun deserializeTelecalling(json: String): TelecallingFormData? = 
    try { gson.fromJson(json, TelecallingFormData::class.java) } catch (e: Exception) { null }
  
  fun serializeMarketing(data: MarketingFormData): String = gson.toJson(data)
  fun deserializeMarketing(json: String): MarketingFormData? = 
    try { gson.fromJson(json, MarketingFormData::class.java) } catch (e: Exception) { null }
  
  fun serializeSimple(data: SimpleFormData): String = gson.toJson(data)
  fun deserializeSimple(json: String): SimpleFormData? = 
    try { gson.fromJson(json, SimpleFormData::class.java) } catch (e: Exception) { null }
}
