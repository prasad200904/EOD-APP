# 📦 WorkCore APK - Complete Build & Distribution Guide

## 🎯 Quick Start

### **Fastest Way to Get APK:**

1. **Open Android Studio**
2. Click: `Build` → `Build Bundle(s) / APK(s)` → `Build APK(s)`
3. Wait for build (~1-2 minutes)
4. Click `locate` in notification
5. ✅ APK is ready at: `app\build\outputs\apk\debug\app-debug.apk`

---

## 📍 **APK Locations**

### **Debug APK (For Testing):**
```
Full Path:
c:\Users\vundr\OneDrive\Desktop\app\workcore\app\build\outputs\apk\debug\app-debug.apk

Relative Path:
app\build\outputs\apk\debug\app-debug.apk

Size: ~10-15 MB
```

### **Release APK (For Distribution):**
```
Full Path:
c:\Users\vundr\OneDrive\Desktop\app\workcore\app\build\outputs\apk\release\app-release.apk

Size: ~8-12 MB (smaller, optimized)
```

---

## 🛠️ **Build Methods**

### **Method 1: Android Studio UI** ⭐ Easiest

#### **For Debug APK:**
```
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for "Build Successful"
3. Click "locate" notification
4. Copy app-debug.apk
```

#### **For Release APK:**
```
1. Build → Generate Signed Bundle / APK
2. Choose: APK
3. Create or select keystore
4. Choose build variant: release
5. Finish
6. Locate app-release.apk
```

### **Method 2: Gradle Command**

#### **In Android Studio Terminal:**
```bash
# Debug APK
gradlew assembleDebug

# Release APK (unsigned)
gradlew assembleRelease

# Clean and rebuild
gradlew clean assembleDebug
```

#### **In Windows CMD:**
```cmd
cd C:\Users\vundr\OneDrive\Desktop\app\workcore
gradlew.bat assembleDebug
```

### **Method 3: Automated Script** ⚡ Super Fast

#### **Just double-click:**
```
build-and-extract-apk.bat
```

**What it does:**
- ✅ Builds APK automatically
- ✅ Copies to Desktop as "WorkCore-v1.0.apk"
- ✅ Opens Desktop folder
- ✅ Shows file size

---

## 📱 **Installing APK on Devices**

### **Method A: USB Cable Transfer**

**Step 1: Copy APK to Phone**
```
1. Connect phone via USB cable
2. Phone appears in File Explorer
3. Copy APK to: Phone\Downloads\
4. Eject phone safely
```

**Step 2: Install on Phone**
```
1. Open Files app (or My Files)
2. Navigate to Downloads folder
3. Tap the APK file
4. If prompted: "Allow from this source" → Enable
5. Tap "Install"
6. Tap "Open" after installation
```

### **Method B: Google Drive / Cloud**

**Step 1: Upload**
```
1. Open Google Drive in browser
2. Click "+ New" → File upload
3. Select app-debug.apk
4. Wait for upload
5. Right-click → Get link → Copy link
```

**Step 2: Download on Phone**
```
1. Open link on phone
2. Tap "Download"
3. Open from notification or Downloads folder
4. Install
```

### **Method C: Email / WhatsApp**

**Email:**
```
1. Compose new email
2. Attach APK file (~10-15 MB)
3. Send to yourself
4. Open email on phone
5. Download attachment → Install
```

**WhatsApp:**
```
1. Open WhatsApp Web
2. Send file to yourself (or friend)
3. Download on phone → Install
```

### **Method D: Direct WiFi Transfer Apps**

**Using "Send Anywhere":**
```
1. Install "Send Anywhere" on PC and phone
2. PC: Select APK → Generate 6-digit code
3. Phone: Enter code → Receive file
4. Install
```

---

## 🔐 **Creating Release APK (Signed)**

For professional distribution or publishing:

### **Step 1: Generate Keystore (One Time)**

**In Terminal:**
```bash
keytool -genkey -v -keystore workcore-release.keystore -alias workcore -keyalg RSA -keysize 2048 -validity 10000
```

**Fill in details:**
```
Keystore password: workcore123 (choose your own)
Re-enter password: workcore123
First and last name: WorkCore Team
Organizational unit: Development
Organization: WorkCore
City: Your City
State: Your State
Country code: US (or your country)
```

**Confirm:** Is CN=..., OU=..., correct? → yes

✅ **Keystore created:** `workcore-release.keystore`

### **Step 2: Create Keystore Properties**

Create file: `app/keystore.properties`
```properties
storePassword=workcore123
keyPassword=workcore123
keyAlias=workcore
storeFile=../workcore-release.keystore
```

### **Step 3: Configure build.gradle.kts**

Update `app/build.gradle.kts`:
```kotlin
android {
    signingConfigs {
        create("release") {
            val keystorePropertiesFile = rootProject.file("app/keystore.properties")
            if (keystorePropertiesFile.exists()) {
                val keystoreProperties = java.util.Properties()
                keystoreProperties.load(keystorePropertiesFile.inputStream())
                
                storeFile = file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }
    
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

### **Step 4: Build Release APK**

**Method A - Android Studio:**
```
Build → Generate Signed Bundle / APK → APK
→ Select keystore
→ Enter passwords
→ Release
→ Finish
```

**Method B - Terminal:**
```bash
gradlew assembleRelease
```

**Output:** `app\build\outputs\apk\release\app-release.apk`

---

## 📊 **APK Information**

### **Debug APK:**
- **Purpose:** Testing, development
- **Size:** ~10-15 MB
- **Optimizations:** None (faster builds)
- **Signing:** Debug keystore (auto-generated)
- **Can publish:** ❌ No (Google Play rejects debug APKs)

### **Release APK:**
- **Purpose:** Production, distribution
- **Size:** ~8-12 MB
- **Optimizations:** ProGuard, code shrinking
- **Signing:** Your custom keystore
- **Can publish:** ✅ Yes (Google Play ready)

---

## 🔍 **Verify APK Details**

### **Using AAPT (Android Asset Packaging Tool):**

```bash
# Get APK info
aapt dump badging app-debug.apk

# Check version
aapt dump badging app-debug.apk | findstr "versionCode versionName"

# Check package name
aapt dump badging app-debug.apk | findstr "package"
```

**Expected Output:**
```
package: name='com.aistudio.workcore.kpmz' versionCode='1' versionName='1.0'
```

---

## 📦 **Version Management**

### **Update Version for New Release:**

Edit `app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        versionCode = 2        // Increment for each release
        versionName = "1.1"    // User-facing version
    }
}
```

### **Version Naming Convention:**
```
v1.0 - Initial release
v1.1 - Minor updates, bug fixes
v1.2 - Small features
v2.0 - Major update, new features
```

---

## 🚀 **Distribution Checklist**

Before sharing APK:

- [ ] Test on multiple devices
- [ ] Verify Firebase sync works
- [ ] Check all features (login, EOD, reports)
- [ ] Confirm database initializes correctly
- [ ] Test offline functionality
- [ ] Verify CSV export works
- [ ] Build release APK (not debug)
- [ ] Rename APK meaningfully (WorkCore-v1.0.apk)
- [ ] Test installation on fresh device
- [ ] Document any known issues

---

## 📝 **APK Naming Convention**

### **For Internal Testing:**
```
WorkCore-debug-v1.0-20260920.apk
  └─ App name
         └─ Build type
              └─ Version
                   └─ Date
```

### **For Release:**
```
WorkCore-v1.0.apk
  └─ App name
       └─ Version
```

---

## 🐛 **Troubleshooting**

### **Issue: Build fails**
```
Solution:
1. Build → Clean Project
2. Build → Rebuild Project
3. Check for errors in Build tab
```

### **Issue: APK not found**
```
Solution:
1. Build → Build Bundle(s) / APK(s) → Build APK(s)
2. Wait for "Build Successful"
3. Navigate manually to:
   app\build\outputs\apk\debug\
```

### **Issue: Can't install on phone**
```
Solution:
1. Settings → Security → Enable "Unknown Sources"
2. Or Settings → Apps → Special Access → Install Unknown Apps
3. Allow installation from Files/Chrome
```

### **Issue: APK too large**
```
Solution:
1. Build release APK (smaller than debug)
2. Enable minifyEnabled and shrinkResources
3. Remove unused resources
4. Compress images
```

---

## 📱 **Multiple Device Installation**

### **Scenario: Install on 10 employees' phones**

**Best Method: USB Installation**
```
1. Build APK once
2. Copy to USB flash drive
3. Give USB to each employee
4. Copy APK to phone → Install
```

**Alternative: QR Code Share**
```
1. Upload APK to Google Drive
2. Generate shareable link
3. Create QR code (qr-code-generator.com)
4. Print QR code
5. Employees scan → Download → Install
```

---

## 🎯 **Quick Commands Reference**

```bash
# Build debug APK
gradlew assembleDebug

# Build release APK
gradlew assembleRelease

# Clean project
gradlew clean

# Clean and rebuild
gradlew clean assembleDebug

# Install to connected device
gradlew installDebug

# Uninstall from device
gradlew uninstallDebug

# List all tasks
gradlew tasks
```

---

## ✨ **Summary**

**To get APK right now:**
1. ✅ Open Android Studio
2. ✅ Build → Build APK(s)
3. ✅ Click "locate"
4. ✅ Copy APK from `app\build\outputs\apk\debug\`
5. ✅ Transfer to phone and install

**APK Location:**
```
c:\Users\vundr\OneDrive\Desktop\app\workcore\app\build\outputs\apk\debug\app-debug.apk
```

**Ready to share!** 🚀
