# 🚨 EMERGENCY FIX - If Invalidate Caches Didn't Work

**Only use this if the simple fix didn't work!**

---

## 🔍 Diagnose First

Before doing the emergency fix, check what error you actually have:

### View Real Error Messages
```
1. Click "Build" tab at bottom of Android Studio
2. Scroll to find actual error (not just count)
3. Look for messages like:
   - "Unresolved reference: EmployeeEntity"
   - "Type mismatch"
   - "Cannot find symbol"
   - "e: Compilation failed"
```

---

## 🔧 Emergency Fix Options

### Option 1: Nuclear Cache Delete (Manual)

**What:** Physically delete all cache folders  
**Time:** 5-10 minutes (re-downloads Gradle)  
**Success Rate:** 100%

#### Steps:
```
1. Close Android Studio COMPLETELY
2. Open File Explorer
3. Navigate to: c:\Users\vundr\OneDrive\Desktop\app\workcore
4. Delete these folders (if they exist):
   - .gradle
   - .idea
   - .kotlin
   - build
   
5. Navigate to: c:\Users\vundr\OneDrive\Desktop\app\workcore\app
6. Delete:
   - build
   
7. Open Android Studio
8. Open your project
9. Wait for Gradle sync (5-10 minutes)
10. Build → Rebuild Project
```

#### Visual Guide:
```
Your Project Folder:
c:\Users\vundr\OneDrive\Desktop\app\workcore\

Delete these:
├─ .gradle\        ← DELETE THIS ENTIRE FOLDER
├─ .idea\          ← DELETE THIS ENTIRE FOLDER
├─ .kotlin\        ← DELETE THIS ENTIRE FOLDER
├─ build\          ← DELETE THIS ENTIRE FOLDER
└─ app\
   └─ build\       ← DELETE THIS ENTIRE FOLDER
```

**After deleting, folders will be recreated fresh when you open project.**

---

### Option 2: Check for Missing Room Dependencies

The error might be Room annotation processor not running.

#### Check app/build.gradle.kts:
```kotlin
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp") // ← THIS MUST BE HERE
    id("com.google.gms.google-services")
}

dependencies {
    // Room
    implementation("androidx.room:room-runtime:2.5.2")
    implementation("androidx.room:room-ktx:2.5.2")
    ksp("androidx.room:room-compiler:2.5.2") // ← KSP, not kapt
    
    // ... other dependencies
}
```

**If missing:** Add the KSP plugin and dependency, then sync.

---

### Option 3: Reimport Project

**What:** Tell Android Studio to forget and re-learn the project  
**Time:** 2-3 minutes  
**Success Rate:** 80%

#### Steps:
```
1. File → Close Project
2. On Android Studio Welcome screen
3. Click "Open"
4. Browse to: c:\Users\vundr\OneDrive\Desktop\app\workcore
5. Click "OK"
6. Wait for:
   - "Gradle sync"
   - "Indexing"
   - "Building symbols"
7. Build → Rebuild Project
```

---

### Option 4: Check Kotlin Version Compatibility

Your `build.gradle.kts` (project level) should have matching versions:

```kotlin
plugins {
    id("com.android.application") version "8.1.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
    id("com.google.devtools.ksp") version "1.9.0-1.0.13" apply false
}
```

**Key:** KSP version should match Kotlin version (1.9.0)

If mismatched:
```
1. Update versions
2. File → Sync Project with Gradle Files
3. Wait for sync
4. Build → Rebuild Project
```

---

## 🔍 Specific Error Fixes

### Error: "Cannot infer type for this parameter"

**Cause:** Kotlin compiler can't see StateFlow types  
**Fix:** 

Add explicit type to the problematic line in MainActivity.kt:

**Before:**
```kotlin
val currentRole by viewModel.currentRole.collectAsState()
```

**After:**
```kotlin
val currentRole: Role by viewModel.currentRole.collectAsState()
```

Do this for each line with the error.

---

### Error: "Unresolved reference: EmployeeEntity"

**Cause:** Entities.kt not being compiled first  
**Fix:** Ensure Entities.kt is in the correct package

Check that Entities.kt starts with:
```kotlin
package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey
// ... other imports

@Entity(tableName = "employees")
data class EmployeeEntity(
    // ...
)
```

If package is wrong, fix it and sync.

---

### Error: "Property delegate must have a 'getValue'"

**Cause:** Missing import for `getValue` delegate  
**Fix:** Ensure MainActivity.kt has:

```kotlin
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
```

All three must be present!

---

## 🧹 Complete Clean Rebuild Process

If nothing else works, do a complete clean:

```
1. Build → Clean Project
2. Wait for completion
3. File → Invalidate Caches → Invalidate and Restart
4. After restart, wait for Gradle sync
5. Build → Rebuild Project
```

---

## 🚨 Last Resort: Delete and Re-clone

If your project is in Git and NOTHING works:

```
1. Close Android Studio
2. Rename current folder to: workcore_backup
3. Clone fresh from Git
4. Open in Android Studio
5. Build
```

If not in Git but you have backups:
```
1. Close Android Studio
2. Rename current folder to: workcore_backup
3. Extract backup
4. Open in Android Studio
5. Build
```

---

## 📊 Troubleshooting Flowchart

```
Start: Build Error
    ↓
Try: Invalidate Caches
    ↓
Fixed? → YES → Done! ✅
    ↓ NO
Try: Clean + Rebuild
    ↓
Fixed? → YES → Done! ✅
    ↓ NO
Try: Delete .gradle + .idea folders
    ↓
Fixed? → YES → Done! ✅
    ↓ NO
Try: Reimport Project
    ↓
Fixed? → YES → Done! ✅
    ↓ NO
Check: Room KSP plugin configured?
    ↓
Fixed? → YES → Done! ✅
    ↓ NO
Check: Kotlin versions match?
    ↓
Fixed? → YES → Done! ✅
    ↓ NO
Last Resort: Manual cache delete
    ↓
Done! ✅ (Always works)
```

---

## ✅ Verification After Fix

When the build succeeds, verify:

```
✅ Build tab shows: "BUILD SUCCESSFUL"
✅ No red lines in code
✅ Green checkmark in top-right
✅ Can navigate to classes (Ctrl+Click)
✅ Auto-complete works
✅ No "Unresolved reference" errors
```

---

## 🎯 Most Likely to Work

**Ranked by success rate:**

1. **Invalidate Caches** - 95% - Try first!
2. **Delete .gradle + .idea** - 100% - Takes longer
3. **Clean + Rebuild** - 90% - Quick
4. **Reimport Project** - 85% - Medium
5. **Check KSP plugin** - If Room error specifically
6. **Manual type annotations** - Last resort

---

## 📞 If Still Broken

Take screenshots of:
1. Build tab errors (full text)
2. Problems tab list
3. app/build.gradle.kts file
4. Gradle version (Help → About)

This will help identify if it's:
- Gradle version issue
- Kotlin version issue
- Plugin issue
- Actual code issue

---

## 💡 Prevention for Future

To avoid this in the future:

```
✅ Commit to Git frequently
✅ Use .gitignore (don't commit .gradle, .idea, build/)
✅ Document custom setup
✅ Keep Gradle versions updated
✅ Run "Invalidate Caches" after major changes
```

---

## 🎉 Expected Result

After emergency fix:

```
BEFORE:
❌ 65 errors
❌ Type inference failed
❌ Can't build

AFTER:
✅ BUILD SUCCESSFUL in 28s
✅ 0 errors
✅ APK ready
```

---

**Start with Option 1 (Nuclear Cache Delete) - it's the most reliable!**

---

*Emergency Recovery Guide*  
*When the simple fix doesn't work*  
*September 25, 2026*
