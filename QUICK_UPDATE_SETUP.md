# Quick Update System Setup

## ✅ What's Done
In-app update notification system is implemented and ready to use!

---

## Setup (5 Minutes)

### Step 1: Create Firebase Document

1. Open Firebase Console → Firestore Database
2. Create collection: `app_config`
3. Create document: `version_info`
4. Add these fields:

```
versionCode: 3 (number)
versionName: "1.2" (string)
downloadUrl: "YOUR_APK_LINK_HERE" (string)
releaseNotes: "• Simple EOD Form\n• CSV Download fixes" (string)
isMandatory: false (boolean)
minRequiredVersion: 1 (number)
```

### Step 2: Host Your APK

**Option A: Firebase Storage (Easiest)**
```
1. Firebase Console → Storage
2. Upload your APK file
3. Make file public
4. Copy download URL
5. Paste into downloadUrl field above
```

**Option B: GitHub Release**
```
1. Create GitHub release
2. Attach APK file
3. Copy download link
4. Paste into downloadUrl field
```

### Step 3: Test

1. Build and install app (version 1.1)
2. Login
3. Wait 2-3 seconds
4. Update dialog appears!
5. Tap "Download Update"
6. Browser opens with download

---

## How It Works

```
User Logs In
    ↓
App checks Firebase (2 sec delay)
    ↓
Firebase versionCode (3) > App versionCode (2)
    ↓
Shows "Update Available" dialog
    ↓
User taps "Download Update"
    ↓
Browser opens → Downloads APK
    ↓
User installs manually
```

---

## UI Preview

### Optional Update
```
┌───────────────────────────┐
│      📥 Blue Icon         │
│   Update Available        │
│   New: 1.2  Current: 1.1  │
│   [Release Notes]         │
│   [Download Update]       │
│   Maybe Later             │
└───────────────────────────┘
```

### Mandatory Update
```
┌───────────────────────────┐
│      🚨 Red Icon          │
│   Update Required         │
│   New: 2.0  Current: 1.1  │
│   [Release Notes]         │
│   ⚠️ Mandatory warning    │
│   [Download Update]       │
│   (No dismiss button)     │
└───────────────────────────┘
```

---

## Update Types

**Optional Update:**
- User can tap "Maybe Later"
- Won't show again for this version
- Blue icon, purple button

**Mandatory Update:**
- Cannot dismiss
- Must download to continue
- Red icon, red button
- Set `isMandatory: true`

---

## Firebase Document Structure

```json
{
  "versionCode": 3,           // Must be > current app version
  "versionName": "1.2",       // Display version
  "downloadUrl": "https://...",  // Direct APK link
  "releaseNotes": "• Fix 1\n• Fix 2",  // What's new
  "isMandatory": false,       // Force update?
  "minRequiredVersion": 1     // Oldest allowed version
}
```

---

## Version Comparison

| App Version | Firebase Version | Result |
|-------------|------------------|---------|
| 1 (v1.0) | 2 (v1.1) | ✅ Shows update |
| 2 (v1.1) | 2 (v1.1) | ❌ No update |
| 2 (v1.1) | 3 (v1.2) | ✅ Shows update |
| 3 (v1.2) | 2 (v1.1) | ❌ No update |

---

## Current State

**App Version:** 1.1 (versionCode: 2)  
**Location:** `app/build.gradle.kts`

To trigger update dialog:
- Set Firebase `versionCode` to 3 or higher
- Add valid `downloadUrl`
- Login to app

---

## Common Issues

**Dialog doesn't show?**
- Check Firebase versionCode > 2
- Check internet connection
- Check Firestore rules allow read

**Download doesn't work?**
- Test `downloadUrl` in browser
- Ensure HTTPS URL
- Verify APK file exists

**Can't install APK?**
- Enable "Install from unknown sources"
- Check versionCode increments
- Same package name required

---

## Files Created

✅ `AppUpdateChecker.kt` - Update logic  
✅ `UpdateDialog.kt` - UI components  
✅ `MainActivity.kt` - Integration  
✅ `build.gradle.kts` - Version 1.1  

**No compilation errors!**

---

## Next Steps

1. Create Firebase document (5 min)
2. Upload APK and get URL (5 min)
3. Test update flow (2 min)

**Total Time: ~12 minutes**

---

## Quick Test

**Test optional update:**
```json
{
  "versionCode": 3,
  "versionName": "1.2",
  "downloadUrl": "https://example.com/app.apk",
  "releaseNotes": "Test update",
  "isMandatory": false,
  "minRequiredVersion": 1
}
```

**Test mandatory update:**
```json
{
  "versionCode": 3,
  "versionName": "2.0",
  "downloadUrl": "https://example.com/app.apk",
  "releaseNotes": "Critical update required",
  "isMandatory": true,
  "minRequiredVersion": 1
}
```

---

**Status: READY TO USE** 🚀

Build the app and set up Firebase to test!
