# Database Version Update - Force Reload AO Data

## Problem
The app wasn't showing AO employees because the database version wasn't incremented after adding the new data to SeedData.kt.

## Solution Applied ✅

**Changed database version: 13 → 14**

File: `AppDatabase.kt`
```kotlin
version = 14,  // Added AO employees, departments, and teams
```

This forces Room to drop and recreate the database with the new seed data.

## What This Will Load

### Companies (2)
1. Sandspace Technologies (Parent)
2. Academic Overseas (Subsidiary)

### AO Departments (3)
1. DEPT_AO_MKT - Marketing (AO)
2. DEPT_AO_CNS - Counsellor (AO)
3. DEPT_AO_TLC - Tele Caller (AO)

### AO Teams (3)
1. TEAM_AO_MKT - Marketing Team → MARKETING form
2. TEAM_AO_CNS - Counsellor Team → COUNSELLOR form
3. TEAM_AO_TLC - Tele Caller Team → TELECALLING form

### AO Employees (13)

**Marketing (5):**
- AO-MKT-001: G V Jaya Kishan
- AO-MKT-002: Borada Kanaka Raju
- AO-MKT-003: Bodasing Sai
- AO-MKT-004: Myneni Sai Siddhartha
- AO-MKT-005: Jogi Sujith

**Counsellor (5):**
- AO-CNS-001: Bali Surya Chandrakala
- AO-CNS-002: Devarakonda Veerababu
- AO-CNS-003: Raja Rama Krishna Sai Teja
- AO-CNS-004: Vamuganti Priyanka
- AO-CNS-005: Kanna Phanikumari

**Tele Caller (3):**
- AO-TLC-001: Tera Jahnavi
- AO-TLC-002: Y Poojitha
- AO-TLC-003: Mohammad Sajida

## Testing Steps

### Step 1: Uninstall Old App
**IMPORTANT:** Uninstall the app completely to ensure clean slate:

```bash
# On device/emulator
Settings → Apps → WorkCore → Uninstall

# OR via ADB
adb uninstall com.example (replace with your package name)
```

### Step 2: Rebuild and Install
1. In Android Studio: **Build → Clean Project**
2. Then: **Build → Rebuild Project**
3. Run the app on device/emulator

### Step 3: Verify Database Creation
Check Logcat for this message:
```
✅ Database instance created (version 14 - Academic Overseas employees and custom EOD forms)
```

### Step 4: Test AO Login
1. Launch app
2. Tap **AO** tab
3. Select **Marketing**
4. Enter passcode: `marketing123`
5. Tap "Sign in to Marketing"

### Step 5: Verify Employee List
You should see **EXACTLY 5 employees**:
1. G V Jaya Kishan
2. Borada Kanaka Raju
3. Bodasing Sai
4. Myneni Sai Siddhartha
5. Jogi Sujith

**❌ Should NOT see:**
- PEDDI SAI KIRAN
- Any Sandspace employees
- Employees from other AO teams

### Step 6: Test EOD Form
1. Tap **G V Jaya Kishan** (or any Marketing employee)
2. Should open: **Marketing EOD - G V Jaya Kishan**
3. Should see:
   - "College Visit Records" title
   - "Add details for each college/institution you visited today"
   - Fields: College Name, Location, Meeting Person, Follow-up Action
   - "Add Another Visit" button
   - Submit & Cancel buttons

**❌ Should NOT see:**
- "Standard EOD form coming soon"
- Close button only

### Step 7: Fill and Submit Form
1. Enter college visit details:
   - College Name: "ABC University"
   - Location: "First block"
   - Meeting Person: "Dr. Smith"
   - Follow-up Action: "Schedule demo"
2. (Optional) Click "Add Another Visit" for multiple entries
3. Tap **Submit**
4. Should return to roster
5. Employee should now show green ✅ checkmark

### Step 8: Test Other Roles

**Counsellor:**
1. Logout → Login as Counsellor (`counsellor123`)
2. Should see 5 Counsellor employees
3. Tap any → Counsellor EOD Form
4. Fields: Student Name, Course Interest, Country Preference, Visa Status, Next Action

**Tele Caller:**
1. Logout → Login as Tele Caller (`telecaller123`)
2. Should see 3 Tele Caller employees
3. Tap any → Telecalling EOD Form
4. Fields: Calls Made, Follow-ups, Conversions, Status, Description

## Troubleshooting

### Still seeing old data?
1. Uninstall app completely (Settings → Apps → Uninstall)
2. Clear Android Studio cache: **File → Invalidate Caches → Invalidate and Restart**
3. Rebuild project
4. Install fresh

### Database not recreating?
Check if version is actually 14:
```kotlin
// In AppDatabase.kt
version = 14  // Make sure this is 14, not 13
```

### Logcat shows errors?
Look for:
```
🔧 Creating database instance...
✅ Database instance created (version 14 - Academic Overseas employees and custom EOD forms)
```

If you see errors about migrations, the fallbackToDestructiveMigration should handle it.

### Still getting wrong employees?
Check ViewModel employee filtering:
```kotlin
when (dept) {
  "Marketing" -> emps.filter { 
    it.department == "Marketing" && 
    it.companyId == "COMP_AO" 
  }
  // ... other cases
}
```

## Verification Checklist

- [ ] Database version = 14
- [ ] Uninstalled old app
- [ ] Rebuilt project
- [ ] Fresh install
- [ ] Logcat shows version 14 message
- [ ] AO login works
- [ ] Marketing shows 5 employees
- [ ] Counsellor shows 5 employees
- [ ] Tele Caller shows 3 employees
- [ ] Tapping employee opens custom form
- [ ] Can add multiple entries (Marketing/Counsellor)
- [ ] Can enter metrics (Telecalling)
- [ ] Submit works
- [ ] Status updates to green checkmark
- [ ] No "Standard EOD form coming soon" message
- [ ] No PEDDI SAI KIRAN or other wrong employees

## Expected Result

✅ Clean database with version 14  
✅ All 13 AO employees loaded  
✅ Correct employee filtering per role  
✅ Custom EOD forms open properly  
✅ No placeholder "coming soon" messages  
✅ Form submission works  
✅ Status tracking works  

---
**Status:** Database version incremented to 14  
**Action Required:** Uninstall old app, rebuild, and test  
**Date:** October 9, 2026
