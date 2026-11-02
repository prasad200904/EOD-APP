# Multi-Company Database Schema Design

## Overview
Support for multiple companies under Sandspace Technologies with custom EOD forms per team type.

---

## 1. Company/Organization Entity

```kotlin
@Entity(
  tableName = "companies",
  indices = [
    Index(value = ["companyId"], unique = true),
    Index(value = ["name"])
  ]
)
data class CompanyEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val companyId: String,        // "COMP_SANDSPACE", "COMP_AO"
  val name: String,              // "Sandspace Technologies Pvt Ltd", "Academic Overseas"
  val parentCompanyId: String = "", // "" for parent, "COMP_SANDSPACE" for AO
  val type: String = "PARENT",   // "PARENT", "SUBSIDIARY"
  val description: String = "",
  val isActive: Boolean = true,
  val createdAt: Long = System.currentTimeMillis()
)
```

### Seed Data:
```kotlin
- COMP_SANDSPACE: "Sandspace Technologies Pvt Ltd" (PARENT)
- COMP_AO: "Academic Overseas" (SUBSIDIARY, parent = COMP_SANDSPACE)
```

---

## 2. Updated Team Entity

```kotlin
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
  val teamId: String,            // "TEAM_ML", "TEAM_COUNSELLORS", etc.
  val name: String,              // "ML Team", "Counsellors", etc.
  val companyId: String,         // "COMP_SANDSPACE" or "COMP_AO"
  val department: String,        // "Machine Learning", "Sales", etc.
  val eodFormType: String = "STANDARD", // "STANDARD", "COUNSELLOR", "TELECALLING", "MARKETING", "SIMPLE"
  val managerId: String,
  val managerName: String,
  val projects: String,
  val createdDate: String = "2024-01-01",
  val status: String = "Active",
  val description: String = "",
  val teamPassword: String = "password123"
)
```

### EOD Form Types:
- **STANDARD**: Default form (Sandspace tech teams) - existing fields
- **COUNSELLOR**: Academic Overseas counsellors - multiple students
- **TELECALLING**: Academic Overseas telecalling - call metrics
- **MARKETING**: Academic Overseas marketing - multiple visits
- **SIMPLE**: Academic Overseas "Other" team - description only

---

## 3. Updated Daily EOD Entity

```kotlin
@Entity(
  tableName = "daily_eods",
  indices = [
    Index(value = ["employeeId", "date"], unique = true),
    Index(value = ["employeeId"]),
    Index(value = ["date"]),
    Index(value = ["teamId"]),
    Index(value = ["companyId"]),
    Index(value = ["eodFormType"]),
    Index(value = ["workStatus"]),
    Index(value = ["isOnTime"])
  ]
)
data class DailyEodEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val employeeId: String,
  val employeeName: String,
  val teamId: String = "TEAM_ML",
  val companyId: String = "COMP_SANDSPACE",
  val departmentId: String = "DEPT_ML",
  val departmentCode: String = "ML",
  val date: String, // YYYY-MM-DD
  
  // STANDARD form fields (existing)
  val project: String = "",
  val todayWork: String = "",
  val workCompleted: String = "",
  val hoursWorked: Double = 0.0,
  val progressPercentage: Int = 100,
  val workStatus: String = "Completed",
  val attendanceStatus: String = "Present",
  val blockers: String = "",
  val remarks: String = "",
  val tomorrowPlan: String = "",
  
  // EOD Form Type
  val eodFormType: String = "STANDARD", // "STANDARD", "COUNSELLOR", "TELECALLING", "MARKETING", "SIMPLE"
  
  // Custom form data (stored as JSON strings)
  val customFormData: String = "", // JSON object for custom fields
  
  // Common fields
  val submissionTime: String = "17:45",
  val isOnTime: Boolean = true,
  val syncStatus: String = SyncStatus.PENDING.name,
  val submittedAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)
```

---

## 4. Custom Form Data JSON Structures

### A. Counsellor Form (`eodFormType = "COUNSELLOR"`)
```json
{
  "students": [
    {
      "studentName": "Rahul Sharma",
      "universityName": "Harvard University",
      "country": "USA",
      "status": "Applied",
      "description": "Completed application review and sent documents"
    },
    {
      "studentName": "Priya Patel",
      "universityName": "Oxford University",
      "country": "UK",
      "status": "Interview Scheduled",
      "description": "Mock interview preparation completed"
    }
  ]
}
```

**Data Classes:**
```kotlin
data class CounsellorFormData(
  val students: List<StudentEntry>
)

data class StudentEntry(
  val studentName: String,
  val universityName: String,
  val country: String,
  val status: String, // "Applied", "Interview Scheduled", "Accepted", "Rejected", "In Progress"
  val description: String
)
```

---

### B. Telecalling Form (`eodFormType = "TELECALLING"`)
```json
{
  "callsMade": 45,
  "followUps": 12,
  "conversions": 3,
  "status": "Target Met",
  "description": "Focused on hot leads from last week. 3 confirmed enrollments for UK universities."
}
```

**Data Classes:**
```kotlin
data class TelecallingFormData(
  val callsMade: Int,
  val followUps: Int,
  val conversions: Int,
  val status: String, // "Target Met", "In Progress", "Below Target"
  val description: String
)
```

**Team Reports (Analytics Tab):**
- Show daily/weekly/monthly totals:
  - Total Calls Made
  - Total Follow-ups
  - Total Conversions
  - Conversion Rate %

---

### C. Marketing Form (`eodFormType = "MARKETING"`)
```json
{
  "visits": [
    {
      "collegeName": "Delhi University",
      "personVisited": "Dr. Rajesh Kumar",
      "designation": "Dean, Student Affairs",
      "description": "Discussed partnership opportunities and student referral program"
    },
    {
      "collegeName": "IIT Bombay",
      "personVisited": "Prof. Meera Singh",
      "designation": "Head, International Relations",
      "description": "Presented scholarship options for overseas programs"
    }
  ]
}
```

**Data Classes:**
```kotlin
data class MarketingFormData(
  val visits: List<CollegeVisit>
)

data class CollegeVisit(
  val collegeName: String,
  val personVisited: String,
  val designation: String,
  val description: String
)
```

---

### D. Simple Form (`eodFormType = "SIMPLE"`)
```json
{
  "description": "Worked on updating student database and organizing application documents."
}
```

**Data Classes:**
```kotlin
data class SimpleFormData(
  val description: String
)
```

---

## 5. Updated Employee Entity

```kotlin
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
  val companyId: String = "COMP_SANDSPACE", // NEW: Company assignment
  val department: String, // e.g. "ML", "Counsellors", "Telecalling"
  val departmentId: String = "DEPT_ML",
  val managedDepartments: String = "",
  val team: String = "Core Team",
  val teamId: String = "TEAM_ML", // NEW: Direct team reference
  val managerId: String = "EMP008",
  val designation: String,
  val defaultProject: String = "Core Engineering",
  val joiningDate: String,
  val role: String = Role.EMPLOYEE.name,
  val isActive: Boolean = true,
  val password: String = "password123",
  val createdAt: Long = System.currentTimeMillis()
)
```

---

## 6. Team Hierarchy

### Sandspace Technologies Pvt Ltd (COMP_SANDSPACE)
```
├── General Team (TEAM_GT) - STANDARD form
├── Cyber Security (TEAM_CYBER) - STANDARD form
├── Machine Learning (TEAM_ML) - STANDARD form
├── Database (TEAM_DB) - STANDARD form
├── Writing Team (TEAM_WRITING) - STANDARD form
└── Academic Overseas (COMP_AO) - Subsidiary
    ├── Counsellors (TEAM_AO_COUNSELLORS) - COUNSELLOR form
    ├── Telecalling (TEAM_AO_TELECALLING) - TELECALLING form
    ├── Marketing (TEAM_AO_MARKETING) - MARKETING form
    └── Other (TEAM_AO_OTHER) - SIMPLE form
```

---

## 7. Database Migration Strategy

### Current Version: 11
### New Version: 12

**Changes:**
1. Add `companies` table
2. Add `companyId`, `eodFormType` to `teams` table
3. Add `companyId`, `teamId`, `eodFormType`, `customFormData` to `daily_eods` table
4. Add `companyId`, `teamId` to `employees` table
5. Create seed data for companies and new AO teams

---

## 8. Analytics Screen Updates

### Team Reports Tab (NEW)

**For Telecalling Team:**
```
📊 Telecalling Team Performance

Today (2024-01-15):
- Total Calls: 180
- Total Follow-ups: 45
- Total Conversions: 12
- Conversion Rate: 6.67%

This Week:
- Total Calls: 850
- Total Follow-ups: 210
- Total Conversions: 58
- Conversion Rate: 6.82%

This Month:
- Total Calls: 3,200
- Total Follow-ups: 890
- Total Conversions: 215
- Conversion Rate: 6.72%
```

**Individual Performance:**
- Show top performers
- Calls/Follow-ups/Conversions per person
- Trend charts

---

## 9. UI Form Rendering Logic

```kotlin
when (team.eodFormType) {
  "STANDARD" -> StandardEodForm(...)
  "COUNSELLOR" -> CounsellorEodForm(...) // Multiple student entries
  "TELECALLING" -> TelecallingEodForm(...) // Call metrics
  "MARKETING" -> MarketingEodForm(...) // Multiple college visits
  "SIMPLE" -> SimpleEodForm(...) // Description only
  else -> StandardEodForm(...)
}
```

---

## 10. Admin Dashboard Changes

**Company Selector:**
```
[ All Companies ▼ ]
  - All Companies
  - Sandspace Technologies Pvt Ltd
  - Academic Overseas
```

**Features:**
- Filter teams by company
- View cross-company analytics
- Manage employees across all companies
- Company-specific reports

---

## Summary

### New Tables:
- `companies` (2 rows: Sandspace + Academic Overseas)

### Modified Tables:
- `teams` (+2 columns: `companyId`, `eodFormType`)
- `employees` (+2 columns: `companyId`, `teamId`)
- `daily_eods` (+4 columns: `companyId`, `teamId`, `eodFormType`, `customFormData`)

### New EOD Forms:
1. **Counsellor Form**: Array of students with university/country/status
2. **Telecalling Form**: Call metrics with team totals
3. **Marketing Form**: Array of college visits
4. **Simple Form**: Description only

### JSON Storage:
All custom form data stored in `customFormData` column as JSON strings, parsed at runtime.

---

## Next Steps

1. ✅ Review and approve this schema
2. Create Kotlin entities and data classes
3. Write database migration (v11 → v12)
4. Create seed data for AO teams
5. Build custom UI forms for each EOD type
6. Update Analytics screen with Team Reports tab
7. Test end-to-end workflow

---

**Date:** 2026-09-29
**Status:** Schema Design - Awaiting Approval
