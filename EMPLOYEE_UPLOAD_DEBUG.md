# Employee Upload Debug Guide

## Issue: Only ADMIN in Firebase, Missing 73 Employees

### Fixed:
✅ **LoginScreen.kt** - Team labels updated:
- "GT Team" → "General Team"
- Added "TEGA" team button
- Fixed passwords (general123, writing123, tega123)

### Now Debug:

## Step 1: Check What Happened

**When you clicked "Reset Firebase", what message appeared?**

A) ✅ **Success:** "✅ SUCCESS: Uploaded 10 departments, 10 teams, and 74 employees to Firestore"

B) ❌ **Error:** "Upload failed: [some error message]"

C) ⏳ **Nothing:** Button clicked but no message

---

## Step 2: Check Android Studio Logcat

**Filter by:** `WorkCoreRepository`

**Look for these log messages:**

### Success Pattern:
```
📊 Found 74 employees, 10 teams, 10 departments
✅ Uploaded 10 departments
✅ Uploaded 10 teams
✅ Uploaded 74 employees to Firestore
✅ SUCCESS: Uploaded 10 departments, 10 teams, and 74 employees
```

### Error Pattern:
```
❌ Upload failed: [error message]
❌ uploadAllEmployeesToFirestore error: [details]
```

---

## Step 3: Check Local Database

**Is the local database empty?**

1. In Android Studio: **Device File Explorer**
2. Navigate to: `/data/data/com.example/databases/`
3. Find `workcore_database`
4. Check if file size > 0

**OR** in your app:
- Login to any team
- Do you see employee names?
- If YES → Local data exists, Firebase upload failed
- If NO → Local data missing, need to rebuild

---

## Step 4: Possible Causes

### Cause A: Firebase Rules Not Deployed
**Symptom:** Error: "PERMISSION_DENIED"
**Solution:**
1. Go to Firebase Console → Firestore → Rules
2. Make sure it shows: `allow read, write: if true;`
3. Click "Publish" if needed
4. Wait 1 minute, try again

### Cause B: Local Database Empty
**Symptom:** "Found 1 employees" (only ADMIN)
**Solution:**
1. Uninstall app completely
2. Rebuild (database version 17 should trigger SeedData)
3. Install
4. Login as Admin
5. Click "Reset Firebase" again

### Cause C: Upload Failed Silently
**Symptom:** No error, but employees not in Firebase
**Solution:**
1. Check Logcat for actual error
2. May need to upload in smaller batches
3. Check internet connection

---

## Step 5: Manual Verification

**Check these in Firebase Console:**

### departments collection:
Should have: 10 documents
- DEPT_CYBER, DEPT_ML, DEPT_DB, DEPT_GT, DEPT_WRITING, DEPT_TEGA
- DEPT_AO_MKT, DEPT_AO_CNS, DEPT_AO_TLC, DEPT_AO_MGR

### teams collection:
Should have: 10 documents
- TEAM_CYBER, TEAM_ML, TEAM_DB, TEAM_GT, TEAM_WRITING, TEAM_TEGA
- TEAM_AO_MKT, TEAM_AO_CNS, TEAM_AO_TLC, TEAM_AO_MGR

### employees collection:
Should have: 74 documents
- ADMIN, SS001-SS044, AO001-AO029

---

## What to Tell Me:

1. **What message appeared** when you clicked "Reset Firebase"?
2. **What does Logcat show?** (filter: WorkCoreRepository)
3. **Can you see employees in the app** when you login to a team?

Based on your answers, I'll fix the exact issue!
