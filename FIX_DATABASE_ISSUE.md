# 🔧 FIX: Database Not Showing Data

## Problem
Your mobile app opens but shows **no employees, no teams, no data**.

## Root Cause
The old database file still exists on your device from a previous installation. The seed data initialization only runs when the database is **completely empty**, so it's being skipped.

---

## ✅ SOLUTION 1: Clear App Data (FASTEST - 30 seconds)

### On Your Android Phone:
1. Go to **Settings** → **Apps** (or **Application Manager**)
2. Find **WorkCore** in the app list
3. Tap on **WorkCore**
4. Tap **Storage** (or **Storage & cache**)
5. Tap **Clear data** or **Clear storage**
6. Confirm when prompted
7. Go back and open **WorkCore** app again

### What This Does:
- Deletes the old database file completely
- Next time app opens, database will be created fresh
- Seed data will auto-initialize with all GT Team employees

---

## ✅ SOLUTION 2: Uninstall & Reinstall

### Using Android Studio:
1. **Uninstall the app:**
   - Open Terminal in Android Studio (bottom panel)
   - Run:
   ```bash
   gradlew uninstallDebug
   ```

2. **Rebuild and install:**
   ```bash
   gradlew clean assembleDebug installDebug
   ```

### OR Using ADB Commands:
Open Command Prompt in `C:\Users\vundr\OneDrive\Desktop\app\workcore\` and run:

```bash
# Navigate to platform-tools
cd C:\Users\vundr\AppData\Local\Android\Sdk\platform-tools

# Uninstall old app
adb -s 10BF8201LQ002ZB uninstall com.aistudio.workcore.kpmz

# Go back to project
cd C:\Users\vundr\OneDrive\Desktop\app\workcore

# Build fresh APK
gradlew.bat assembleDebug

# Install new version
gradlew.bat installDebug
```

---

## ✅ SOLUTION 3: Use the Reinstall Script

I've created an automated script for you. Just double-click:

📁 **`reinstall-app.bat`** (in your project folder)

This script will:
1. ✓ Uninstall old version
2. ✓ Build fresh APK
3. ✓ Install new version
4. ✓ Database will auto-initialize with GT Team data

---

## 🔍 How to Verify It's Fixed

After using any solution above, open the app and:

### Test 1: Admin Login
```
Username: admin
Password: admin123
```

**Expected Result:**
- Dashboard opens
- Department selector shows "ML Team" (default)
- Can switch to "GT Team"
- Shows 5 team members

### Test 2: GT Team Employee Login
```
1. Select Department: GT Team
2. Employee ID: GT-001
3. Password: password123
```

**Expected Result:**
- Shows GT Team roster with 5 employees:
  - GT-001: Deepak Kumar
  - GT-002: Nisha Reddy
  - GT-003: Arjun Joseph
  - GT-004: Pooja Thomas
  - GT-005: Vikram Singh

---

## 🐛 Still Not Working? Check Logs

### View Real-Time Logs:
In Android Studio → **Logcat** tab → Filter by "WorkCore"

Look for these messages:
```
✅ DATABASE INITIALIZED SUCCESSFULLY!
✅ GT Team employees: 5 employees
✓ Database instance created (version 8)
```

### Manual Log Check:
```bash
adb -s 10BF8201LQ002ZB logcat | findstr "WorkCoreRepository Database"
```

---

## 📋 What I Changed

### 1. **Database Version Bump** (v7 → v8)
Forces Room to drop and recreate the database on next install.

**File:** `AppDatabase.kt`
```kotlin
@Database(
  // ...
  version = 8  // ← Changed from 6 to 8
)
```

### 2. **Enhanced Logging**
Added detailed logs to track database initialization.

**File:** `WorkCoreRepository.kt`
- Now logs every step of seed data insertion
- Shows employee counts
- Confirms GT Team employees are loaded

### 3. **Reinstall Script**
Created `reinstall-app.bat` for easy one-click fix.

---

## 🎯 Recommended Approach

**For quickest fix right now:**
1. Pick up your phone
2. Settings → Apps → WorkCore → Storage → Clear data
3. Open app → Login as `admin` / `admin123`
4. Check dashboard → Select GT Team
5. You should see 5 employees!

**For development (cleaner):**
1. Run `reinstall-app.bat`
2. Wait for installation to complete
3. Open app and test

---

## 📱 Expected Data After Fix

### Departments (5):
- ML Team (Machine Learning)
- DB Team (Database)
- GT Team (General Tech) ⭐ Your focus
- Writing Team (Content & Writing)
- Cyber Security

### GT Team Employees (5):
| ID      | Name          | Designation          | Project                    |
|---------|---------------|----------------------|----------------------------|
| GT-001  | Deepak Kumar  | Growth Analyst       | Performance Marketing      |
| GT-002  | Nisha Reddy   | SEO Specialist       | Organic Search Optimization|
| GT-003  | Arjun Joseph  | Growth Marketer      | User Acquisition Funnel    |
| GT-004  | Pooja Thomas  | Content Strategist   | Brand Campaign v2          |
| GT-005  | Vikram Singh  | Performance Marketer | Paid Ads Scaling           |

### Total Database:
- **20+ employees** (across all departments)
- **100+ EOD records** (historical data)
- **5 departments**
- **4 teams**
- **5 notifications**

---

## 💡 Why This Happened

The app uses Room database with this initialization check:

```kotlin
val existing = employeeDao.getAllEmployees().first()
if (existing.isEmpty()) {
  // Insert seed data ← Only runs if NO employees exist
  employeeDao.insertAll(SeedData.employees)
}
```

**Problem:** Old database file existed with corrupted/empty state, so check failed.

**Solution:** Clear data completely → Forces fresh database → Seed data loads.

---

## 🚀 Next Steps After Fix

Once data appears:

1. **Test Admin View:**
   - Login as admin
   - Navigate departments
   - View team analytics

2. **Test Employee View:**
   - Login to GT Team (GT-001)
   - Submit an EOD
   - Check Firebase sync

3. **Test CSV Export:**
   - Go to Download/Reports tab
   - Generate GT Team report
   - Verify CSV format

---

Need help? Check the logs and let me know what you see!
