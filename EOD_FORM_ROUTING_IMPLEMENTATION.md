# EOD Form Routing Implementation Guide

## Overview
This document explains how to integrate the 4 custom EOD forms (Counsellor, Telecalling, Marketing, Simple) into the existing EOD submission flow based on team form types.

---

## Current Architecture

### Existing Flow:
```
EmployeeDashboardScreen
  ↓
onSubmitEod callback (standard form)
  ↓
WorkCoreViewModel.submitDailyEod()
  ↓
WorkCoreRepository.submitDailyEod()
  ↓
Save to Database
```

### Parameters (Standard Form):
```kotlin
onSubmitEod: (
  date: String,
  project: String,
  taskDescription: String,
  hoursWorked: Double,
  progressPercentage: Int,
  status: String
) -> Unit
```

---

## New Architecture (Multi-Form Support)

### Updated Flow:
```
EmployeeDashboardScreen
  ↓
Check team.eodFormType
  ↓
Route to appropriate form:
  - STANDARD → Existing inline form
  - COUNSELLOR → CounsellorEodFormScreen
  - TELECALLING → TelecallingEodFormScreen
  - MARKETING → MarketingEodFormScreen
  - SIMPLE → SimpleEodFormScreen
  ↓
onSubmitCustomEod callback
  ↓
WorkCoreViewModel.submitCustomEod()
  ↓
WorkCoreRepository.submitCustomEod()
  ↓
Save to Database with customFormData
```

---

## Implementation Steps

### Step 1: Add Form Type to EmployeeDashboardScreen

**File:** `EmployeeDashboardScreen.kt`

**Add team parameter and form routing:**

```kotlin
@Composable
fun EmployeeDashboardScreen(
  employeeId: String,
  employee: EmployeeEntity?,
  team: TeamEntity?,  // NEW: Pass team entity
  eods: List<DailyEodEntity>,
  teams: List<TeamEntity>,
  onSubmitEod: (date: String, project: String, taskDescription: String, hoursWorked: Double, progressPercentage: Int, status: String) -> Unit,
  onSubmitCustomEod: (date: String, formType: String, customFormData: String) -> Unit,  // NEW: Custom form callback
  onOpenEodPopup: (() -> Unit)? = null,
  modifier: Modifier = Modifier
) {
  // Existing code...
  
  // Determine which form to show
  val formType = team?.eodFormType ?: EodFormType.STANDARD.name
  var showCustomForm by remember { mutableStateOf(false) }
  
  // ... rest of existing code
}
```

### Step 2: Add Form Routing Logic

**In the EOD submission section:**

```kotlin
// Replace existing EOD form with routing logic
when {
  formType == EodFormType.STANDARD.name -> {
    // Show existing standard form (current implementation)
    StandardEodFormSection(
      formDate = formDate,
      formProject = formProject,
      formTaskDescription = formTaskDescription,
      formHoursWorked = formHoursWorked,
      formProgressPercentage = formProgressPercentage,
      formStatus = formStatus,
      onSubmit = { date, project, task, hours, progress, status ->
        onSubmitEod(date, project, task, hours, progress, status)
      }
    )
  }
  
  formType == EodFormType.COUNSELLOR.name && showCustomForm -> {
    CounsellorEodFormScreen(
      employeeName = employee?.name ?: "",
      onSubmit = { customFormData ->
        onSubmitCustomEod(SeedData.TODAY, EodFormType.COUNSELLOR.name, customFormData)
        showCustomForm = false
      },
      onCancel = { showCustomForm = false }
    )
  }
  
  formType == EodFormType.TELECALLING.name && showCustomForm -> {
    TelecallingEodFormScreen(
      employeeName = employee?.name ?: "",
      onSubmit = { customFormData ->
        onSubmitCustomEod(SeedData.TODAY, EodFormType.TELECALLING.name, customFormData)
        showCustomForm = false
      },
      onCancel = { showCustomForm = false }
    )
  }
  
  formType == EodFormType.MARKETING.name && showCustomForm -> {
    MarketingEodFormScreen(
      employeeName = employee?.name ?: "",
      onSubmit = { customFormData ->
        onSubmitCustomEod(SeedData.TODAY, EodFormType.MARKETING.name, customFormData)
        showCustomForm = false
      },
      onCancel = { showCustomForm = false }
    )
  }
  
  formType == EodFormType.SIMPLE.name && showCustomForm -> {
    SimpleEodFormScreen(
      employeeName = employee?.name ?: "",
      onSubmit = { customFormData ->
        onSubmitCustomEod(SeedData.TODAY, EodFormType.SIMPLE.name, customFormData)
        showCustomForm = false
      },
      onCancel = { showCustomForm = false }
    )
  }
  
  else -> {
    // Button to open custom form
    Button(
      onClick = { showCustomForm = true },
      modifier = Modifier.fillMaxWidth()
    ) {
      Icon(Icons.Default.Edit, contentDescription = null)
      Spacer(modifier = Modifier.width(8.dp))
      Text("Submit ${formType.replace("_", " ")} EOD")
    }
  }
}
```

### Step 3: Update WorkCoreViewModel

**File:** `WorkCoreViewModel.kt`

**Add new method for custom EOD submission:**

```kotlin
fun submitCustomEod(
  employeeId: String,
  employeeName: String,
  teamId: String,
  companyId: String,
  date: String,
  eodFormType: String,
  customFormData: String
) {
  viewModelScope.launch {
    try {
      val eod = DailyEodEntity(
        employeeId = employeeId,
        employeeName = employeeName,
        teamId = teamId,
        companyId = companyId,
        departmentId = "", // Can derive from employee
        departmentCode = "",
        date = date,
        eodFormType = eodFormType,
        customFormData = customFormData,
        // Standard fields remain empty for custom forms
        project = "",
        todayWork = "",
        workCompleted = "",
        workStatus = "Completed",
        submissionTime = getCurrentTime(),
        isOnTime = isOnTime(getCurrentTime()),
        syncStatus = SyncStatus.PENDING.name
      )
      
      repository.insertOrUpdateEod(eod)
      
      // Refresh EOD list
      loadEods()
      
    } catch (e: Exception) {
      Log.e("WorkCoreViewModel", "Error submitting custom EOD: ${e.message}")
    }
  }
}

private fun getCurrentTime(): String {
  val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
  return sdf.format(Date())
}

private fun isOnTime(time: String): Boolean {
  return time <= "18:30" // Use company config cutoff time
}
```

### Step 4: Update WorkCoreRepository

**File:** `WorkCoreRepository.kt`

**Add method to handle custom EOD:**

```kotlin
suspend fun insertOrUpdateEod(eod: DailyEodEntity): Result<Long> = withContext(Dispatchers.IO) {
  try {
    val existing = dailyEodDao.getEod(eod.employeeId, eod.date)
    val id = if (existing != null) {
      dailyEodDao.update(eod.copy(id = existing.id))
      existing.id
    } else {
      dailyEodDao.insert(eod)
    }
    
    // Audit log
    auditLogDao.insert(
      AuditLogEntity(
        userId = eod.employeeId,
        action = if (existing != null) "UPDATE_EOD" else "SUBMIT_EOD",
        entityType = "DAILY_EOD",
        entityId = "${eod.employeeId}-${eod.date}",
        description = "${eod.employeeName} submitted ${eod.eodFormType} EOD for ${eod.date}"
      )
    )
    
    Result.success(id)
  } catch (e: Exception) {
    Log.e("WorkCoreRepository", "Error inserting/updating EOD: ${e.message}", e)
    Result.failure(e)
  }
}
```

### Step 5: Update MainActivity Navigation

**File:** `MainActivity.kt`

**Pass team entity to EmployeeDashboardScreen:**

```kotlin
// In the navigation composable
val employee = employees.firstOrNull { it.employeeId == employeeId }
val team = teams.firstOrNull { it.teamId == employee?.teamId }

EmployeeDashboardScreen(
  employeeId = employeeId,
  employee = employee,
  team = team,  // NEW: Pass team
  eods = eods,
  teams = teams,
  onSubmitEod = { date, project, task, hours, progress, status ->
    viewModel.submitDailyEod(
      employeeId, employee?.name ?: "", date,
      project, task, hours, progress, status
    )
  },
  onSubmitCustomEod = { date, formType, customFormData ->  // NEW: Custom callback
    viewModel.submitCustomEod(
      employeeId, employee?.name ?: "",
      team?.teamId ?: "", team?.companyId ?: "",
      date, formType, customFormData
    )
  }
)
```

---

## Display Custom Form Data (History View)

### Parsing Custom Forms in EOD History:

```kotlin
@Composable
fun EodHistoryCard(eod: DailyEodEntity) {
  Card(modifier = Modifier.fillMaxWidth()) {
    Column(modifier = Modifier.padding(16.dp)) {
      Text("Date: ${eod.date}", fontWeight = FontWeight.Bold)
      
      when (eod.eodFormType) {
        EodFormType.STANDARD.name -> {
          Text("Project: ${eod.project}")
          Text("Work: ${eod.todayWork}")
          Text("Status: ${eod.workStatus}")
        }
        
        EodFormType.COUNSELLOR.name -> {
          val formData = CustomFormDataSerializer.deserializeCounsellor(eod.customFormData)
          formData?.students?.forEach { student ->
            Text("Student: ${student.studentName}")
            Text("University: ${student.universityName} (${student.country})")
            Text("Status: ${student.status}")
            Text("Notes: ${student.description}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          }
        }
        
        EodFormType.TELECALLING.name -> {
          val formData = CustomFormDataSerializer.deserializeTelecalling(eod.customFormData)
          formData?.let {
            Text("Calls Made: ${it.callsMade}")
            Text("Follow-ups: ${it.followUps}")
            Text("Conversions: ${it.conversions}")
            Text("Status: ${it.status}")
            Text("Notes: ${it.description}")
          }
        }
        
        EodFormType.MARKETING.name -> {
          val formData = CustomFormDataSerializer.deserializeMarketing(eod.customFormData)
          formData?.visits?.forEach { visit ->
            Text("College: ${visit.collegeName}")
            Text("Person: ${visit.personVisited} (${visit.designation})")
            Text("Notes: ${visit.description}")
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
          }
        }
        
        EodFormType.SIMPLE.name -> {
          val formData = CustomFormDataSerializer.deserializeSimple(eod.customFormData)
          formData?.let {
            Text("Work Summary: ${it.description}")
          }
        }
      }
    }
  }
}
```

---

## Testing Checklist

### For Each Custom Form:

1. **Login Flow:**
   - [ ] Login with team credentials (e.g., `counsellor123`)
   - [ ] Verify correct form type is routed

2. **Form Submission:**
   - [ ] Fill form with valid data
   - [ ] Submit EOD
   - [ ] Verify success message
   - [ ] Verify data saved to database

3. **Form Validation:**
   - [ ] Try submitting with empty fields
   - [ ] Verify error messages
   - [ ] Verify submit button disabled/enabled correctly

4. **History View:**
   - [ ] Navigate to EOD history
   - [ ] Verify submitted EOD displays correctly
   - [ ] Verify custom form data is parsed and displayed

5. **Edit Flow:**
   - [ ] Edit existing custom EOD
   - [ ] Verify form pre-populates with existing data
   - [ ] Submit changes
   - [ ] Verify updates saved

---

## Database Verification

### Check Custom Form Storage:

```sql
-- View custom EODs
SELECT employeeId, date, eodFormType, customFormData
FROM daily_eods
WHERE eodFormType != 'STANDARD'
ORDER BY date DESC;

-- Count by form type
SELECT eodFormType, COUNT(*) as count
FROM daily_eods
GROUP BY eodFormType;
```

---

## Import Statements Needed

Add to files that use custom forms:

```kotlin
import com.example.data.EodFormType
import com.example.data.CustomFormDataSerializer
import com.example.data.CounsellorFormData
import com.example.data.TelecallingFormData
import com.example.data.MarketingFormData
import com.example.data.SimpleFormData
import com.example.ui.screens.CounsellorEodFormScreen
import com.example.ui.screens.TelecallingEodFormScreen
import com.example.ui.screens.MarketingEodFormScreen
import com.example.ui.screens.SimpleEodFormScreen
```

---

## Common Issues & Solutions

### Issue 1: Form Not Showing
**Cause:** team entity is null or eodFormType not set  
**Solution:** Verify team is passed correctly and has eodFormType field

### Issue 2: JSON Parse Error
**Cause:** Malformed JSON in customFormData  
**Solution:** Use try-catch when deserializing, show error gracefully

### Issue 3: Standard Fields Required
**Cause:** Database constraints expect non-null standard fields  
**Solution:** Use default empty strings for standard fields when using custom forms

### Issue 4: Form Doesn't Pre-populate on Edit
**Cause:** Not checking existing EOD when form opens  
**Solution:** Add LaunchedEffect to load existing data if editing

---

## Next Steps After Implementation

1. **Test all 4 custom forms** with real data
2. **Verify Firebase sync** handles custom form data
3. **Update Analytics** to support custom form metrics (Task 8)
4. **Add Admin views** for viewing custom form submissions
5. **Export functionality** - CSV export should handle custom forms

---

**Date:** 2026-09-29  
**Status:** Implementation Guide - Ready for Integration  
**Estimated Effort:** 4-6 hours for full integration and testing
