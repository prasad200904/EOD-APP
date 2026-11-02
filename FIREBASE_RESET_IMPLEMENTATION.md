# Firebase Reset Implementation - Complete Guide

## Overview
Implemented a **ONE-TIME ADMIN FUNCTION** to:
1. Delete all old employee data from Firebase Firestore (74 outdated employees)
2. Upload 73 new real employees from local SeedData (CSV import)
3. Auto-sync will then pull the correct data after login

## What Was Added

### 1. FirebaseDataSource.kt - New Methods

#### `clearAllEmployeesFromFirestore()`
- Deletes all employee documents from Firestore collection
- Uses batched deletes (500 per batch) for efficiency
- Returns count of deleted employees

#### `uploadAllEmployeesToFirestore(employees: List<EmployeeEntity>)`
- Uploads all local employees to Firestore
- Uses batched writes (500 per batch)
- Returns count of uploaded employees

### 2. WorkCoreRepository.kt - Extension Function

#### `resetFirestoreWithLocalData(): Result<String>`
- Orchestrates the complete reset process:
  1. Gets all 73 local employees from Room database (from SeedData)
  2. Calls clearAllEmployeesFromFirestore() to delete old data
  3. Calls uploadAllEmployeesToFirestore() to push new data
  4. Returns success/failure message

### 3. WorkCoreViewModel.kt - UI Function

#### `resetFirestoreWithLocalData(onResult: (Boolean, String) -> Unit)`
- Exposes the reset operation to UI
- Shows snackbar messages for success/failure
- Runs in viewModelScope coroutine

### 4. AdminDashboardScreen.kt - UI Button

Added a new admin banner with **"Reset Firebase"** button:
- Orange-themed warning banner (admin-only operation)
- CloudUpload icon
- Clear description: "Delete all old Firestore employees and upload 73 new CSV employees"

### 5. MainActivity.kt - Wired Up

Connected the button click to ViewModel:
```kotlin
onResetFirestoreClick = {
  viewModel.resetFirestoreWithLocalData { success, message ->
    if (success) {
      viewModel.snackbarMessage.value = "✅ Firebase reset complete! Re-enable auto-sync now."
    } else {
      viewModel.snackbarMessage.value = message
    }
  }
}
```

### 6. Re-enabled Auto-Sync

Uncommented all three auto-sync blocks in WorkCoreViewModel.kt:
- Line ~452: Sandspace login auto-sync
- Line ~502: Academic Overseas login auto-sync
- Line ~785: Admin login auto-sync

## How to Use (Steps)

### Step 1: Login as Admin
- Open the app
- Login with admin credentials
- Navigate to Admin Dashboard

### Step 2: Click "Reset Firebase" Button
- Look for the orange banner: "🔥 Reset Firebase Data (Admin Only)"
- Click the **"Reset Firebase"** button
- Wait for completion (may take 10-30 seconds)

### Step 3: Verify Success
- Check logcat for messages:
  ```
  ✅ Deleted X old employees from Firestore
  📤 Preparing to upload 73 local employees...
  ✅ Successfully uploaded 73 employees to Firestore
  ```
- Snackbar will show: "✅ SUCCESS: Deleted old data, uploaded 73 new employees to Firestore"

### Step 4: Test Auto-Sync
- **Uninstall the app** (to clear local database)
- **Reinstall the app**
- Login (Sandspace, AO, or Admin)
- Auto-sync will pull the 73 new employees from Firebase
- Verify correct employees appear:
  - **Sandspace**: SS001-SS056 (44 employees)
  - **Academic Overseas**: AO001-AO029 (29 employees)

## Employee Data Structure

### Sandspace (44 employees)
- Cyber: 7 employees
- ML: 8 employees
- DB: 3 employees
- General: 4 employees
- Writing: 13 employees
- TEGA: 7 employees
- Manager: 2 employees

### Academic Overseas (29 employees)
- Marketing: 11 employees
- Counsellor: 13 employees
- Tele Caller: 3 employees
- Manager: 2 employees

## Important Notes

1. **ONE-TIME OPERATION**: This should only be run once to migrate from old data to new CSV data

2. **Internet Required**: Firebase operations require active internet connection

3. **Batched Operations**: Both delete and upload use batching (500 per batch) to avoid Firestore limits

4. **Auto-Sync Enabled**: After reset, auto-sync will work correctly because Firebase now has the right data

5. **Local Data Preserved**: The reset only affects Firebase. Local SeedData remains unchanged.

## Technical Details

### Firestore Structure
Each employee document contains:
```json
{
  "employeeId": "SS001",
  "name": "Employee Name",
  "department": "Cyber",
  "designation": "Team Leader",
  "role": "EMPLOYEE",
  "password": "cyber123",
  "phone": "1234567890",
  "companyId": "COMP_SS",
  "isActive": true,
  "createdAt": 1234567890000,
  "updatedAt": 1234567890000
}
```

### Error Handling
- Firebase not initialized → Returns error message
- Delete fails → Stops process, returns error
- Upload fails → Returns error with details
- All errors logged to Android logcat

## Files Modified

1. `app/src/main/java/com/example/data/firebase/FirebaseDataSource.kt`
   - Added `clearAllEmployeesFromFirestore()`
   - Added `uploadAllEmployeesToFirestore()`

2. `app/src/main/java/com/example/data/WorkCoreRepository.kt`
   - Added `resetFirestoreWithLocalData()` extension function

3. `app/src/main/java/com/example/ui/viewmodel/WorkCoreViewModel.kt`
   - Added `resetFirestoreWithLocalData()` UI function
   - Re-enabled all three auto-sync blocks

4. `app/src/main/java/com/example/ui/screens/AdminDashboardScreen.kt`
   - Added CloudUpload icon import
   - Added `onResetFirestoreClick` parameter
   - Added Firebase Reset banner UI

5. `app/src/main/java/com/example/MainActivity.kt`
   - Wired up `onResetFirestoreClick` callback

## Status

✅ **IMPLEMENTATION COMPLETE**

Ready to:
1. Build the app
2. Install on device
3. Login as admin
4. Click "Reset Firebase" button
5. Verify 73 new employees in Firebase console
6. Test auto-sync works correctly

## Next Steps

After successful Firebase reset:
- Monitor auto-sync logs to ensure correct data is pulled
- Verify all 73 employees appear in respective company dashboards
- Old Firebase data (CYB-001, DB-006, EMP011, etc.) will be permanently deleted
- New CSV data (AO001-AO029, SS001-SS056) will be the source of truth
