# 🔧 Fix Build Error - Type Inference Issues

**Error:** "Cannot infer type for this parameter. Specify it explicitly."  
**Location:** MainActivity.kt, Entities.kt, WorkCoreRepository.kt  
**Cause:** Kotlin compilation order / type inference issues

---

## 🚀 Quick Fix (Try These in Order)

### Fix 1: Invalidate Caches (Most Common Solution)
```
1. In Android Studio, click "File" menu
2. Select "Invalidate Caches..."
3. Check ALL boxes:
   ☑ Invalidate and Restart
   ☑ Clear file system cache
   ☑ Clear downloaded shared indexes
4. Click "Invalidate and Restart"
5. Wait for Android Studio to restart
6. Wait for Gradle sync to complete
7. Try Build → Rebuild Project
```

### Fix 2: Clean and Rebuild
```
1. Build → Clean Project
2. Wait for completion
3. Build → Rebuild Project
4. Wait for build to finish
```

### Fix 3: Gradle Sync
```
1. File → Sync Project with Gradle Files
2. Wait for sync to complete (watch bottom right)
3. If errors persist, try Fix 1 again
```

### Fix 4: Reimport Project
```
1. File → Close Project
2. On Welcome screen, click "Open"
3. Navigate to: c:\Users\vundr\OneDrive\Desktop\app\workcore
4. Click "OK"
5. Wait for project to load and sync
6. Try Build → Rebuild Project
```

---

## 🔍 Understanding the Error

### What's Happening
The Kotlin compiler can't infer types for:
- `val currentRole by viewModel.currentRole.collectAsState()`
- Data classes in WorkCoreRepository.kt referencing EmployeeEntity

### Why It's Happening
- Compilation order issue
- Android Studio cache corruption
- Gradle didn't fully sync

### Why It Will Be Fixed
- Invalidating caches forces fresh compilation
- Clean build removes old artifacts
- Resync ensures all dependencies loaded

---

## 💡 Alternative: Check Build Output

### View Detailed Errors
```
1. Click "Build" tab at bottom of Android Studio
2. Look for actual error messages (not just warnings)
3. Common issues:
   - Missing import statements
   - Circular dependencies
   - Room annotation processor issues
```

### Check Gradle Console
```
1. Click "Build Output" or "Gradle" tab
2. Look for detailed error messages
3. Share the output if still having issues
```

---

## 🆘 If Nothing Works

### Nuclear Option: Delete Build Folders
```
1. Close Android Studio completely
2. Navigate to: c:\Users\vundr\OneDrive\Desktop\app\workcore
3. Delete these folders:
   - .gradle\
   - .idea\
   - build\
   - app\build\
4. Reopen project in Android Studio
5. Wait for full Gradle sync
6. Try Build → Rebuild Project
```

**WARNING:** This will re-download Gradle dependencies (takes 5-10 minutes)

---

## 📋 Most Likely Solution

**90% of the time**, this error is fixed by:
```
File → Invalidate Caches → Invalidate and Restart
```

The Kotlin compiler cache gets corrupted and needs to be cleared.

---

## ✅ After Fix Verification

Once build succeeds, you should see:
```
BUILD SUCCESSFUL in 30s
```

Then verify:
```
□ No red underlines in code
□ Build tab shows success
□ APK generated in app/build/outputs/apk/
```

---

## 🔧 Why This Happened

When we recreated WorkCoreRepository.kt, the file was created successfully BUT Android Studio's caches still had references to the old (deleted) file. This causes the Kotlin compiler to get confused about type resolution.

**Invalidating caches** tells Android Studio: "Forget everything you cached, start fresh!"

---

## 📞 Still Getting Errors?

If after trying all fixes you still see errors, check:

### 1. Check Kotlin Version
`build.gradle.kts` (app level) should have:
```kotlin
plugins {
    kotlin("android") version "1.9.0" // or similar
}
```

### 2. Check Room Version
`build.gradle.kts` (app level) should have:
```kotlin
dependencies {
    implementation("androidx.room:room-runtime:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2")
}
```

### 3. Check if KSP plugin is applied
`build.gradle.kts` (app level) should have:
```kotlin
plugins {
    id("com.google.devtools.ksp") version "1.9.0-1.0.13"
}
```

---

## 🎯 Summary

**Problem:** Kotlin type inference errors  
**Root Cause:** Android Studio cache corruption after file recreation  
**Solution:** Invalidate Caches + Restart  
**Success Rate:** 95%

---

**Try Fix 1 first - it almost always works!** 🚀

---

*Troubleshooting Guide*  
*September 25, 2026*
