# Database Not Showing - Debug Guide

## Problem
The app is installed but shows no employees or data when you login.

## Root Cause
The database initialization (`initializeSeedDataIfNeeded()`) runs only ONCE when the database is first created. If:
1. The old database still exists from a previous build
2. The database was corrupted
3. The initialization failed silently

Then the app will have an **empty database** with no seed data.

## Quick Fix Solutions

### Solution 1: Clear App Data (Fastest)
1. On your Android device, go to **Settings → Apps → WorkCore**
2. Tap **Storage & cache**
3. Tap **Clear storage** or **Clear data**
4. Reopen the app - seed data will auto-initialize

### Solution 2: Uninstall & Reinstall
```bash
# From your computer (in platform-tools directory)
adb -s 10BF8201LQ002ZB uninstall com.aistudio.workcore.kpmz
adb -s 10BF8201LQ002ZB install path\to\app-debug.apk
```

### Solution 3: Force Database Rebuild (Automated)
I've updated the database version from `6` to `7` in `AppDatabase.kt`. 

When you rebuild and install the app:
- Room will detect version change
- `.fallbackToDestructiveMigration()` will DROP and RECREATE all tables
- Seed data will reinitialize automatically

**Steps:**
1. Build the app: `gradlew assembleDebug`
2. Install: `gradlew installDebug`
3. Open app - fresh database with all GT Team employees

## Verify Seed Data

After clearing data, you should see:

### GT Team Employees (5 people):
- **GT-001**: Deepak Kumar (Growth Analyst)
- **GT-002**: Nisha Reddy (SEO Specialist)
- **GT-003**: Arjun Joseph (Growth Marketer)
- **GT-004**: Pooja Thomas (Content Strategist)
- **GT-005**: Vikram Singh (Performance Marketer)

### Login Credentials:
- **Admin**: `admin` / `admin123`
- **GT Team Employees**: Select "GT Team" → Any employee ID (e.g., `GT-001`) → Password: `password123`

## Technical Details

### Database Initialization Flow:
```kotlin
// WorkCoreViewModel.init block:
viewModelScope.launch {
  repository.initializeSeedDataIfNeeded()  // Called ONCE on first launch
}

// WorkCoreRepository.kt:
suspend fun initializeSeedDataIfNeeded() {
  val existingDepts = departmentDao.getAllDepartments().first()
  if (existingDepts.isEmpty()) {  // Only runs if DB is empty
    departmentDao.insertAll(SeedData.departments)
    employeeDao.insertAll(SeedData.employees)  // 👈 Inserts GT Team here
    dailyEodDao.insertAll(SeedData.dailyEods)
    // ... more seed data
  }
}
```

### Why Database Shows Empty:
1. **Old database persisted**: Previous installation left database file intact
2. **isEmpty() check returns false**: Database has tables but no data (corrupted state)
3. **Seed insertion skipped**: Logic assumes data already exists

### Database Location on Device:
```
/data/data/com.aistudio.workcore.kpmz/databases/workcore_database
```

## Quick Test Commands

### Check if database exists:
```bash
adb -s 10BF8201LQ002ZB shell "ls -la /data/data/com.aistudio.workcore.kpmz/databases/"
```

### Manually delete database (forces recreation):
```bash
adb -s 10BF8201LQ002ZB shell "rm -f /data/data/com.aistudio.workcore.kpmz/databases/workcore_database*"
```

### View app logs:
```bash
adb -s 10BF8201LQ002ZB logcat | grep -i "workcore\|database\|seed"
```

## Prevention

To prevent this in future:
1. Always **clear app data** when testing major database changes
2. Increment database `version` number in `AppDatabase.kt` when schema changes
3. Use `.fallbackToDestructiveMigration()` during development (already configured)
4. Add database reset button in debug builds (optional)

## Expected Result

After fix, when you:
1. Login as Admin (`admin` / `admin123`)
2. Navigate to Dashboard
3. Select "GT Team" from department dropdown

You should see:
- **5 team members** in the list
- Submission statistics (X submitted, Y pending)
- Team roster with employee cards

When you login as Employee:
1. Select **GT Team**
2. Enter employee ID: `GT-001` or any GT employee
3. Enter password: `password123`
4. You'll see the GT Team roster dashboard

## Need More Help?

If data still doesn't appear:
1. Check Android Studio Logcat for errors
2. Look for "initializeSeedDataIfNeeded" in logs
3. Verify database version changed from 6 → 7
4. Try uninstall + reinstall (clean slate)
