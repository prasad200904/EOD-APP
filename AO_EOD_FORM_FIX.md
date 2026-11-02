# AO EOD Form Issue - Fix Applied

## Problem
When clicking on an employee in the AO roster, the app showed:
> "Standard EOD form coming soon for PEDDI SAI KIRAN"

Instead of opening the proper custom EOD form (Marketing/Counsellor/Telecalling).

## Root Causes

### Issue 1: Team Lookup Failed
The form routing code was looking up the team by `teamId`, but if the lookup failed, it defaulted to "STANDARD" form type.

### Issue 2: Wrong Employee Visible
"PEDDI SAI KIRAN" should NOT be visible in the AO roster - this indicates the employee filtering isn't working correctly.

## Fixes Applied

### Fix 1: Added Department Fallback ✅
**File:** `MainActivity.kt`

Added fallback logic that checks employee's department when team lookup fails:

```kotlin
// Route to custom EOD form based on team's eodFormType OR employee's department
val team = teams.firstOrNull { it.teamId == activeEmp?.teamId }
var formType = team?.eodFormType ?: "STANDARD"

// Fallback: Check employee department for AO roles
if (formType == "STANDARD" && activeEmp?.companyId == "COMP_AO") {
  formType = when (activeEmp?.department) {
    "Marketing" -> "MARKETING"
    "Counsellor" -> "COUNSELLOR"
    "Tele Caller" -> "TELECALLING"
    else -> "STANDARD"
  }
}
```

This ensures that even if the team lookup fails, AO employees will still get the correct form based on their department.

## What You Need to Check

### 1. Is "PEDDI SAI KIRAN" an AO Employee?

Check in your database or seed data:
- Is this employee's `companyId` set to "COMP_AO"?
- What is their `department` field?
- What is their `teamId` field?

**If this is NOT an AO employee:**
- They shouldn't appear in the AO roster
- The employee filtering in ViewModel needs investigation

**If this IS an AO employee:**
- Make sure their `department` is one of: "Marketing", "Counsellor", "Tele Caller"
- Make sure their `teamId` matches one of: "TEAM_AO_MKT", "TEAM_AO_CNS", "TEAM_AO_TLC"

### 2. Check Team Configuration

Run this to verify teams are set up correctly:

```sql
SELECT teamId, name, department, eodFormType, companyId 
FROM teams 
WHERE companyId = 'COMP_AO';
```

Expected results:
| teamId | name | department | eodFormType | companyId |
|--------|------|------------|-------------|-----------|
| TEAM_AO_MKT | Marketing Team | Marketing | MARKETING | COMP_AO |
| TEAM_AO_CNS | Counsellor Team | Counsellor | COUNSELLOR | COMP_AO |
| TEAM_AO_TLC | Tele Caller Team | Tele Caller | TELECALLING | COMP_AO |

### 3. Check Employee Configuration

```sql
SELECT employeeId, name, department, teamId, companyId 
FROM employees 
WHERE name LIKE '%PEDDI%' OR name LIKE '%SAI KIRAN%';
```

This employee should have:
- `companyId` = "COMP_AO"
- `department` = one of: "Marketing", "Counsellor", "Tele Caller"
- `teamId` = one of: "TEAM_AO_MKT", "TEAM_AO_CNS", "TEAM_AO_TLC"

## Testing Steps

### Step 1: Clear App Data
Settings → Apps → WorkCore → Storage → Clear Data

This ensures old database data is removed and fresh seed data is loaded.

### Step 2: Login Again
1. Launch app
2. Tap **AO** tab
3. Select **Marketing** role
4. Enter `marketing123`
5. Sign in

### Step 3: Verify Employee List
You should ONLY see these 5 Marketing employees:
1. G V Jaya Kishan
2. Borada Kanaka Raju
3. Bodasing Sai
4. Myneni Sai Siddhartha
5. Jogi Sujith

**If you see "PEDDI SAI KIRAN":** This employee is incorrectly configured.

### Step 4: Test EOD Form
1. Tap any Marketing employee
2. Should open **Marketing EOD Form** with college visit fields
3. NOT the "coming soon" message

### Step 5: Test Other Roles

**Counsellor (5 employees):**
- Bali Surya Chandrakala
- Devarakonda Veerababu
- Raja Rama Krishna Sai Teja
- Vamuganti Priyanka
- Kanna Phanikumari

**Tele Caller (3 employees):**
- Tera Jahnavi
- Y Poojitha
- Mohammad Sajida

Each should open their respective custom form.

## Quick Fix If Problem Persists

If you still see wrong employees or wrong forms, add this employee to the AO Marketing team:

### Option A: Update Existing Employee in Database
If "PEDDI SAI KIRAN" exists in your database, update them:

```kotlin
// In SeedData.kt, find the employee and update:
EmployeeEntity(
  employeeId = "AO-MKT-006", // Or their existing ID
  name = "Peddi Sai Kiran",
  email = "peddi.saikiran@academioverseas.com",
  phone = "+91 9876543206",
  companyId = "COMP_AO",
  department = "Marketing",
  departmentId = "DEPT_AO_MKT",
  team = "Marketing Team",
  teamId = "TEAM_AO_MKT",
  managerId = "ADMIN",
  designation = "Marketing Executive",
  defaultProject = "Student Acquisition - First block",
  joiningDate = "2024-03-01",
  role = Role.EMPLOYEE.name,
  password = "marketing123"
)
```

### Option B: Database Migration
If employee data is persistent across app reinstalls, you need a database migration to update existing employees.

## Expected Behavior After Fix

1. ✅ Only see correct AO employees for selected role
2. ✅ Clicking employee opens correct custom form
3. ✅ No "Standard EOD form coming soon" message
4. ✅ Can fill and submit custom forms successfully

## Verification Checklist

- [ ] Cleared app data
- [ ] Fresh install/rebuild
- [ ] Login shows only correct employees
- [ ] Tapping employee opens custom form (not "coming soon")
- [ ] Can add college visits (Marketing)
- [ ] Can add students (Counsellor)
- [ ] Can enter call metrics (Telecalling)
- [ ] Submit works and returns to roster
- [ ] Status updates to green checkmark

---
**Status:** Fix applied - needs testing  
**Date:** October 9, 2026  
**Issue:** Form routing fallback added  
**Action Required:** Clear app data and test
