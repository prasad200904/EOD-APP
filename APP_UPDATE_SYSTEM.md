# In-App Update System - Complete Guide

## Summary
Implemented a complete in-app update notification system that checks for new versions from Firebase Firestore and prompts users to download and install updates.

---

## Features

✅ **Automatic Update Checking**  
✅ **Firebase-Based Version Management**  
✅ **Mandatory vs Optional Updates**  
✅ **Release Notes Display**  
✅ **User-Friendly UI Dialog**  
✅ **Smart Dismissal (Don't Show Again for Same Version)**  
✅ **APK Download via Browser**  
✅ **Version Comparison Logic**  

---

## How It Works

### 1. Version Configuration in Firebase

Create a document in Firestore with version information:

**Collection:** `app_config`  
**Document:** `version_info`

**Fields:**
```json
{
  "versionCode": 3,
  "versionName": "1.2",
  "downloadUrl": "https://your-server.com/workcore-v1.2.apk",
  "releaseNotes": "• Simple EOD Form\n• CSV Download with Today option\n• Bug fixes and performance improvements",
  "isMandatory": false,
  "minRequiredVersion": 1
}
```

**Field Descriptions:**
- `versionCode` (Number): Integer version number (must be > current app version)
- `versionName` (String): Display version like "1.2" or "2.0"
- `downloadUrl` (String): Direct link to APK file
- `releaseNotes` (String): What's new in this version (supports multi-line)
- `isMandatory` (Boolean): If true, user cannot dismiss the update dialog
- `minRequiredVersion` (Number): Versions below this are forced to update

---

### 2. Update Check Flow

```
App Launch
    ↓
User Logs In
    ↓
Wait 2 seconds
    ↓
Show "Checking for updates..." dialog
    ↓
Query Firebase Firestore
    ↓
Compare versionCode (Firestore vs Local)
    ↓
If Firestore > Local → Update Available
    ↓
Check if user dismissed this version
    ↓
If not dismissed OR mandatory → Show Update Dialog
    ↓
User taps "Download Update"
    ↓
Open downloadUrl in browser
    ↓
User downloads APK
    ↓
User taps APK file
    ↓
Android prompts for installation approval
    ↓
User approves
    ↓
App installs and replaces old version
```

---

## UI Components

### 1. Checking Update Dialog

```
┌─────────────────────────┐
│                         │
│      ⏳ (spinner)       │
│                         │
│  Checking for updates...│
│                         │
└─────────────────────────┘
```

- Shows briefly while checking Firebase
- Cannot be dismissed
- Automatically closes when check completes

---

### 2. Update Available Dialog (Optional)

```
┌─────────────────────────────────┐
│                                 │
│        📥 (blue icon)          │
│                                 │
│      Update Available          │
│                                 │
│   New Version: 1.2             │
│   Current Version: 1.1         │
│                                 │
│  ┌───────────────────────────┐ │
│  │ What's New                 │ │
│  │ • Simple EOD Form          │ │
│  │ • CSV Download fixes       │ │
│  │ • Bug fixes               │ │
│  └───────────────────────────┘ │
│                                 │
│  [Download Update] (purple)    │
│                                 │
│      Maybe Later (gray)        │
│                                 │
└─────────────────────────────────┘
```

- User can dismiss with "Maybe Later"
- Won't show again for this version (until next launch)
- Download button opens browser

---

### 3. Update Required Dialog (Mandatory)

```
┌─────────────────────────────────┐
│                                 │
│        🚨 (red icon)           │
│                                 │
│      Update Required           │
│                                 │
│   New Version: 2.0             │
│   Current Version: 1.1         │
│                                 │
│  ┌───────────────────────────┐ │
│  │ What's New                 │ │
│  │ • Critical security fix    │ │
│  │ • Database migration       │ │
│  └───────────────────────────┘ │
│                                 │
│  ⚠️ This update is mandatory   │
│  for security and compatibility│
│                                 │
│  [Download Update] (red)       │
│                                 │
└─────────────────────────────────┘
```

- NO "Maybe Later" button
- Cannot be dismissed
- Must download to continue using app
- Red color for urgency

---

## Implementation Details

### Files Created

**1. `AppUpdateChecker.kt`** - Core update checking logic
- `checkForUpdate()` - Checks Firebase for new version
- `getCurrentVersionCode()` - Gets app's current version
- `getCurrentVersionName()` - Gets version display name
- `openDownloadUrl()` - Opens browser to download APK
- `markUpdateDismissed()` - Remembers user dismissed this version
- `isUpdateDismissed()` - Checks if user already dismissed
- `clearDismissedVersion()` - Reset dismissal (for testing)

**2. `UpdateDialog.kt`** - UI components
- `UpdateAvailableDialog` - Main update notification dialog
- `CheckingUpdateDialog` - Loading spinner during check

**3. `MainActivity.kt`** (Modified) - Integration
- Added update check on login
- Shows dialogs based on update status
- Handles download button click

**4. `build.gradle.kts`** (Modified) - Version bump
- Changed `versionCode` from 1 → 2
- Changed `versionName` from "1.0" → "1.1"

---

## Setup Instructions

### Step 1: Configure Firebase Firestore

1. Open Firebase Console
2. Go to Firestore Database
3. Create new collection: `app_config`
4. Create new document: `version_info`
5. Add fields:
   ```
   versionCode: 3 (Number)
   versionName: "1.2" (String)
   downloadUrl: "https://your-download-link.com/app.apk" (String)
   releaseNotes: "• Feature 1\n• Feature 2" (String)
   isMandatory: false (Boolean)
   minRequiredVersion: 1 (Number)
   ```

### Step 2: Update Firestore Rules

Add read permission for `app_config` collection:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Allow all reads/writes (for testing)
    match /{document=**} {
      allow read, write: if true;
    }
    
    // Or more restrictive (production):
    match /app_config/{document} {
      allow read: if true;  // Anyone can read version info
      allow write: if false; // Only admin can write (via console)
    }
  }
}
```

### Step 3: Host APK File

You need a public URL for your APK file. Options:

**Option A: Firebase Storage**
1. Upload APK to Firebase Storage
2. Make file public
3. Copy download URL
4. Use in `downloadUrl` field

**Option B: GitHub Releases**
1. Create GitHub release
2. Attach APK file
3. Copy direct download link
4. Use in `downloadUrl` field

**Option C: Your Own Server**
1. Upload APK to your server
2. Ensure public HTTP/HTTPS access
3. Use direct URL

---

## Testing the Update System

### Test 1: Optional Update

**Current State:**
- App installed: v1.1 (versionCode: 2)

**Firebase Config:**
```json
{
  "versionCode": 3,
  "versionName": "1.2",
  "downloadUrl": "https://example.com/app-v1.2.apk",
  "releaseNotes": "• New features\n• Bug fixes",
  "isMandatory": false,
  "minRequiredVersion": 1
}
```

**Expected Behavior:**
1. Login to app
2. Wait 2 seconds
3. See "Checking for updates..." (brief)
4. See "Update Available" dialog (blue icon)
5. Shows version 1.2 is available
6. "Maybe Later" button visible
7. Tap "Maybe Later" → Dialog closes
8. Won't show again until next app launch

---

### Test 2: Mandatory Update

**Firebase Config:**
```json
{
  "versionCode": 3,
  "versionName": "2.0",
  "downloadUrl": "https://example.com/app-v2.0.apk",
  "releaseNotes": "• Critical security fix\n• Important database changes",
  "isMandatory": true,
  "minRequiredVersion": 1
}
```

**Expected Behavior:**
1. Login to app
2. See "Update Required" dialog (red icon)
3. Warning message about mandatory update
4. NO "Maybe Later" button
5. Cannot dismiss by tapping outside
6. Must download to continue
7. Dialog stays visible until download starts

---

### Test 3: Force Update for Old Versions

**Firebase Config:**
```json
{
  "versionCode": 4,
  "versionName": "2.1",
  "downloadUrl": "https://example.com/app-v2.1.apk",
  "releaseNotes": "Latest version",
  "isMandatory": false,
  "minRequiredVersion": 3
}
```

**Expected Behavior:**
- If app version < 3 (minRequiredVersion):
  - Dialog becomes mandatory (red)
  - Even though `isMandatory: false`
  - Old versions forced to update

---

### Test 4: No Update Available

**Firebase Config:**
```json
{
  "versionCode": 2,
  "versionName": "1.1"
}
```

**Expected Behavior:**
- App version 2 (same as Firebase)
- No dialog shows
- Check completes silently
- User continues normally

---

## Version Number Logic

### Version Code (Integer)
```kotlin
versionCode = 1  // First release
versionCode = 2  // Second release
versionCode = 3  // Third release
```

**Rules:**
- Must increment by at least 1 for each release
- Firebase `versionCode` > App `versionCode` = Update Available
- Used for comparison logic (not shown to users)

### Version Name (String)
```kotlin
versionName = "1.0"  // First release
versionName = "1.1"  // Minor update
versionName = "1.2"  // Another minor update
versionName = "2.0"  // Major release
```

**Rules:**
- Shown to users in UI
- Can be any string format
- Common formats: "1.0", "2.0.1", "1.5-beta"

---

## Download and Installation Process

### 1. User Taps "Download Update"
```kotlin
AppUpdateChecker.openDownloadUrl(context, updateInfo.downloadUrl)
```

### 2. Browser Opens
- Opens default browser app
- Navigates to `downloadUrl`
- APK file starts downloading

### 3. Download Completes
- Android shows notification
- User taps notification or goes to Downloads folder

### 4. Install Prompt
- Android shows "Do you want to install this application?"
- Shows package name and permissions
- User must tap "Install"

### 5. Installation
- Android replaces old version
- User data preserved (database, preferences)
- App reopens with new version

---

## Update Dismissal Logic

### How It Works
```kotlin
// User taps "Maybe Later"
AppUpdateChecker.markUpdateDismissed(context, versionCode = 3)

// Next time app checks
val isDismissed = AppUpdateChecker.isUpdateDismissed(context, versionCode = 3)
if (isDismissed && !isMandatory) {
  // Don't show dialog
}
```

### Storage
- Saved in SharedPreferences
- Key: `"dismissed_version"`
- Value: Latest dismissed version code
- Persists across app launches
- Clears on app uninstall

### Reset for Testing
```kotlin
AppUpdateChecker.clearDismissedVersion(context)
```

---

## Best Practices

### 1. Version Numbering
✅ **DO:** Increment versionCode sequentially (1, 2, 3, 4...)  
✅ **DO:** Use semantic versioning for versionName (1.0, 1.1, 2.0)  
❌ **DON'T:** Skip version codes  
❌ **DON'T:** Reuse same version code  

### 2. Release Notes
✅ **DO:** Keep brief and user-friendly  
✅ **DO:** Highlight most important changes first  
✅ **DO:** Use bullet points for clarity  
❌ **DON'T:** Use technical jargon  
❌ **DON'T:** Make release notes too long  

### 3. Mandatory Updates
✅ **DO:** Use for critical security fixes  
✅ **DO:** Use for breaking database changes  
✅ **DO:** Explain why update is required  
❌ **DON'T:** Overuse mandatory updates  
❌ **DON'T:** Force updates for minor features  

### 4. Download URLs
✅ **DO:** Use HTTPS (secure)  
✅ **DO:** Test download link before publishing  
✅ **DO:** Use CDN for faster downloads  
❌ **DON'T:** Use HTTP (insecure)  
❌ **DON'T:** Use temporary/expiring links  

---

## Troubleshooting

### Issue: Update dialog doesn't show
**Check:**
- Firebase `versionCode` > App `versionCode`?
- Firestore rules allow read access?
- Internet connection working?
- User already dismissed this version?

**Solution:**
```kotlin
// Reset dismissal
AppUpdateChecker.clearDismissedVersion(context)

// Check current version
Log.d("Update", "Current: ${AppUpdateChecker.getCurrentVersionCode(context)}")
```

---

### Issue: Download doesn't work
**Check:**
- Is `downloadUrl` valid and accessible?
- Is APK file actually there?
- Is URL using HTTPS?

**Solution:**
- Test URL in browser manually
- Check Firebase Storage permissions
- Verify APK file uploaded correctly

---

### Issue: APK won't install
**Check:**
- Did user enable "Install from unknown sources"?
- Is package name same as existing app?
- Is new versionCode > old versionCode?

**Solution:**
- Settings → Security → Enable unknown sources
- Build with same `applicationId` in build.gradle.kts
- Increment versionCode properly

---

## Firebase Console Setup (Step-by-Step)

1. **Open Firestore Database**
   ```
   Firebase Console → Project → Firestore Database
   ```

2. **Create Collection**
   ```
   Click "Start collection"
   Collection ID: app_config
   Click "Next"
   ```

3. **Create Document**
   ```
   Document ID: version_info
   Click "Save"
   ```

4. **Add Fields**
   ```
   Click "Add field"
   
   Field 1:
   - Name: versionCode
   - Type: number
   - Value: 3
   
   Field 2:
   - Name: versionName
   - Type: string
   - Value: 1.2
   
   Field 3:
   - Name: downloadUrl
   - Type: string
   - Value: https://your-download-link.com/app.apk
   
   Field 4:
   - Name: releaseNotes
   - Type: string
   - Value: • Simple EOD Form\n• CSV Download fixes\n• Bug fixes
   
   Field 5:
   - Name: isMandatory
   - Type: boolean
   - Value: false
   
   Field 6:
   - Name: minRequiredVersion
   - Type: number
   - Value: 1
   ```

5. **Click "Save"**

---

## Example Update Scenarios

### Scenario 1: Bug Fix Release
```json
{
  "versionCode": 4,
  "versionName": "1.1.1",
  "downloadUrl": "https://cdn.example.com/workcore-1.1.1.apk",
  "releaseNotes": "• Fixed CSV download issue\n• Improved date formatting\n• Performance optimizations",
  "isMandatory": false,
  "minRequiredVersion": 1
}
```

### Scenario 2: New Feature Release
```json
{
  "versionCode": 5,
  "versionName": "1.2.0",
  "downloadUrl": "https://cdn.example.com/workcore-1.2.0.apk",
  "releaseNotes": "• New analytics dashboard\n• Team performance reports\n• Export to Excel feature\n• UI improvements",
  "isMandatory": false,
  "minRequiredVersion": 1
}
```

### Scenario 3: Critical Security Update
```json
{
  "versionCode": 6,
  "versionName": "1.2.1",
  "downloadUrl": "https://cdn.example.com/workcore-1.2.1.apk",
  "releaseNotes": "• CRITICAL: Security vulnerability fixed\n• Database encryption improved\n• Authentication enhancements",
  "isMandatory": true,
  "minRequiredVersion": 1
}
```

### Scenario 4: Major Version (Breaking Changes)
```json
{
  "versionCode": 10,
  "versionName": "2.0.0",
  "downloadUrl": "https://cdn.example.com/workcore-2.0.0.apk",
  "releaseNotes": "• Complete UI redesign\n• New database structure\n• Enhanced Firebase sync\n• Material Design 3",
  "isMandatory": false,
  "minRequiredVersion": 5
}
```

---

## Status: READY TO USE ✅

**Files Created:**
1. ✅ `AppUpdateChecker.kt` - Update checking logic
2. ✅ `UpdateDialog.kt` - UI components
3. ✅ `MainActivity.kt` - Integration (updated)
4. ✅ `build.gradle.kts` - Version bump (1.1)

**No compilation errors.**  
**Ready to build and test!**

---

## Next Steps

1. **Setup Firebase:**
   - Create `app_config/version_info` document
   - Add version fields
   - Set versionCode to 3 (higher than current 2)

2. **Host APK:**
   - Build release APK
   - Upload to Firebase Storage or server
   - Get public download URL

3. **Update Firebase:**
   - Set `downloadUrl` to your APK link
   - Set `releaseNotes` with changes
   - Set `isMandatory` as needed

4. **Test:**
   - Build and install current version (1.1)
   - Login to app
   - Wait 2 seconds
   - See update dialog appear
   - Tap "Download Update"
   - Verify browser opens

---

**Last Updated:** September 25, 2026  
**Version:** 1.0 - In-App Update System Complete
