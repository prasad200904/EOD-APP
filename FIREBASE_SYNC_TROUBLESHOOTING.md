# Firebase Sync Troubleshooting Guide

## 🔍 Current Issue: App Not Syncing with Firestore

Your Firebase is configured correctly, but data is not appearing in Firestore. Let's fix it!

---

## ✅ Step 1: Deploy Wide-Open Rules (For Testing Only)

**IMPORTANT: Do this FIRST!**

**Go to Firebase Console:**
1. https://console.firebase.google.com/
2. Select project: **eod-management**
3. Navigate to: **Firestore Database → Rules**
4. **Replace ALL content** with:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

5. Click **"Publish"**
6. Wait 1-2 minutes for rules to propagate

**Why:** This removes all permission restrictions to test if sync works at all.

---

## ✅ Step 2: Check Firestore Database is Created

1. Firebase Console → **Firestore Database**
2. Make sure you see "Cloud Firestore" tab (not "Realtime Database")
3. You should see an empty database or collections list
4. **If you see "Create Database" button, click it:**
   - **Location:** Choose **asia-south1** (Mumbai) or **us-central1** (Iowa)
   - **Mode:** Start in **Production mode** 
   - Click "Enable"

---

## ✅ Step 3: Verify Internet Connection

The app needs internet to sync.

**On Emulator:**
- Emulator should have internet by default
- Test by opening Chrome in emulator → visit google.com

**On Physical Device:**
- Enable WiFi or Mobile Data
- Verify internet works by opening browser

---

## ✅ Step 4: Check App Logs for Errors

### Run the app with Android Studio:

1. Open **Android Studio**
2. Bottom toolbar → Click **"Logcat"** tab
3. In filter box, type: `Firebase`
4. Run the app
5. Login and submit an EOD
6. Watch the logs

**Look for these messages:**

### ✅ Good Signs (Working):
```
D/SyncManager: Network became online - triggering auto-sync
D/SyncManager: Successfully synced EOD for GT-001 on 2024-01-15
D/FirebaseDataSource: uploadEod success
```

### ❌ Bad Signs (Problems):

**Problem 1: Firebase Not Initialized**
```
E/FirebaseDataSource: Firebase is not initialized
```
**Fix:** 
- Check `google-services.json` exists in `app/` folder
- Clean project: Build → Clean Project
- Rebuild: Build → Rebuild Project

**Problem 2: Permission Denied**
```
E/FirebaseDataSource: PERMISSION_DENIED: Missing or insufficient permissions
```
**Fix:** Deploy the open rules from Step 1

**Problem 3: Network Error**
```
E/SyncManager: No internet connection available
```
**Fix:** Check internet connection (Step 3)

**Problem 4: API Not Enabled**
```
E/FirebaseDataSource: Cloud Firestore API has not been used in project
```
**Fix:** 
- Go to Firebase Console → Firestore Database
- Click "Create Database" if you haven't already

---

## ✅ Step 5: Force Manual Sync Test

Your app has a sync function! Let's test it:

### Method 1: Through Code (Quick Test)

Add this temporary button to test sync. Let me check your admin screen:

<function_calls>
<invoke name="grep_search">
<parameter name="query">syncAllPending|syncPendingToCloud|pullFromCloud


### Test Sync Manually:

Your ViewModel has a `triggerManualSync()` function. Here's how to test it:

**Option A: Add Temporary Debug Button**

1. Open any admin screen (like `AdminDashboardScreen.kt`)
2. Add this button temporarily at the top:

```kotlin
// Add this import at top
import androidx.compose.material3.OutlinedButton

// Add this button in your screen
OutlinedButton(
    onClick = { viewModel.triggerManualSync() },
    modifier = Modifier.fillMaxWidth()
) {
    Text("🔄 Force Sync to Firebase (DEBUG)")
}
```

3. Run app → Login as admin → Click the button
4. Check Logcat for sync messages
5. Check Firebase Console for data

**Option B: Trigger Sync by Submitting EOD**

The app automatically syncs when you submit an EOD:

1. Login (admin or employee)
2. Go to "Today" screen or EOD submission
3. Fill out and submit an EOD
4. Watch Logcat for sync messages:
   ```
   D/SyncManager: triggerEodSync called
   D/FirebaseDataSource: uploadEod for GT-001
   ```
5. Go to Firebase Console → Firestore Database → eod_submissions
6. You should see the document appear!

---

## ✅ Step 6: Verify Sync is Working

### Check Firebase Console:

1. Go to https://console.firebase.google.com/
2. Select: **eod-management**
3. Click: **Firestore Database**
4. Look for collections:
   - `eod_submissions` - Should have documents after submitting EODs
   - `employees` - Should have employee documents
   - `departments` - Should have department documents

### Document Format Example:

When you submit an EOD, you should see:

**Collection:** `eod_submissions`  
**Document ID:** `GT-001_2024-01-15` (employeeId_date)  
**Fields:**
```
employeeId: "GT-001"
employeeName: "John Doe"
date: "2024-01-15"
todayWork: "Completed login feature"
workStatus: "Completed"
submittedAt: 1705329600000
syncStatus: "SYNCED"
```

---

## ✅ Step 7: Common Issues & Fixes

### Issue 1: "Firebase is not initialized"

**Symptoms:** App logs show "Firebase is not initialized"

**Causes:**
- `google-services.json` missing or in wrong location
- Gradle sync failed
- Firebase dependencies not loaded

**Fixes:**
1. Check file exists: `app/google-services.json`
2. Android Studio → **File → Sync Project with Gradle Files**
3. Clean & Rebuild:
   - **Build → Clean Project**
   - **Build → Rebuild Project**
4. Restart Android Studio
5. Uninstall app from device/emulator and reinstall

### Issue 2: "Permission Denied"

**Symptoms:** Logs show "PERMISSION_DENIED" or "insufficient permissions"

**Cause:** Firestore security rules blocking access

**Fix:**
1. Deploy the open rules from Step 1
2. Wait 1-2 minutes for rules to update
3. Try sync again

### Issue 3: Data Not Appearing in Firestore

**Symptoms:** No errors in logs, but collections empty

**Possible Causes:**
- Looking at wrong Firebase project
- Firestore database not created yet
- Rules preventing writes

**Fixes:**
1. Verify you're in the correct project: **eod-management**
2. Check project_id in `google-services.json` matches console
3. Make sure you clicked "Create Database" in Firestore
4. Deploy open rules (Step 1)

### Issue 4: Network Error

**Symptoms:** "No internet connection available" or "Network error"

**Fixes:**
1. **On Emulator:**
   - Restart emulator
   - Check host machine has internet
   - Try: Tools → AVD Manager → Wipe Data → Cold Boot

2. **On Physical Device:**
   - Check WiFi/Mobile data enabled
   - Try opening browser to test internet
   - Check firewall/proxy settings

### Issue 5: App Crashes on Sync

**Symptoms:** App crashes when submitting EOD

**Check Logcat for:**
```
E/AndroidRuntime: FATAL EXCEPTION
```

**Common Causes:**
- Missing dependency
- Corrupted gradle cache
- ProGuard issue

**Fixes:**
1. Clean project: **Build → Clean Project**
2. Invalidate caches: **File → Invalidate Caches → Invalidate and Restart**
3. Check `build.gradle.kts` has all Firebase dependencies

---

## 🔍 Step 8: Enable Debug Logging

Add more detailed Firebase logs:

1. In your app's `MainActivity.kt`, add at the top of `onCreate()`:

```kotlin
// Enable Firebase debug logging
FirebaseFirestore.setLoggingEnabled(true)
FirebaseDatabase.getInstance().setLogLevel(Logger.Level.DEBUG)
```

2. Run app and check Logcat with filter: `Firestore`

You'll see detailed Firebase operations:
```
D/Firestore: Writing document: eod_submissions/GT-001_2024-01-15
D/Firestore: Upload successful
```

---

## ✅ Step 9: Test Checklist

Run through this checklist:

- [ ] Firestore Database created in Firebase Console
- [ ] Wide-open rules deployed (allow read, write: if true)
- [ ] `google-services.json` exists in `app/` folder
- [ ] Internet connection working on device/emulator
- [ ] Gradle synced successfully
- [ ] App runs without crashes
- [ ] Logcat open and filtered for "Firebase"
- [ ] Submitted an EOD in the app
- [ ] Checked Firebase Console → Firestore Database
- [ ] Document appears in `eod_submissions` collection

---

## 📊 Expected Behavior (Working Correctly)

### When EOD is Submitted:

**Logcat Should Show:**
```
D/SyncManager: triggerEodSync for employeeId: GT-001, date: 2024-01-15
D/FirebaseDataSource: uploadEod starting...
D/Firestore: Writing to path: eod_submissions/GT-001_2024-01-15
D/FirebaseDataSource: uploadEod success
D/SyncManager: Successfully synced EOD for GT-001 on 2024-01-15
```

**Firebase Console Should Show:**
- New document in `eod_submissions` collection
- Document ID: `GT-001_2024-01-15`
- All fields populated correctly

---

## 🆘 Still Not Working?

If after following all steps sync still doesn't work, gather this info:

### Debug Information Needed:

1. **Logcat Output:**
   - Filter: `Firebase`
   - Copy all messages after submitting EOD

2. **Firebase Console:**
   - Screenshot of Firestore Database page
   - Screenshot of Firestore Rules

3. **App Configuration:**
   - Confirm `google-services.json` project_id matches console

4. **Network Status:**
   - Confirm internet works on device
   - Try opening google.com in browser

### Share these logs and I can help debug further!

---

## 🎯 Quick Fix Summary

**Most Common Solution (90% of cases):**

1. ✅ Deploy these rules in Firebase Console:
   ```javascript
   rules_version = '2';
   service cloud.firestore {
     match /databases/{database}/documents {
       match /{document=**} {
         allow read, write: if true;
       }
     }
   }
   ```

2. ✅ Clean & Rebuild in Android Studio:
   - Build → Clean Project
   - Build → Rebuild Project

3. ✅ Uninstall and reinstall app

4. ✅ Submit an EOD and check Firebase Console

**That fixes it 90% of the time!** 🎉

---

## 📱 Next Steps After Sync Works

Once you see data in Firebase:

1. ✅ Test from multiple devices
2. ✅ Verify data syncs between devices
3. ✅ Replace test rules with secure production rules
4. ✅ Set up automatic backups
5. ✅ Configure usage monitoring

Good luck! 🚀
