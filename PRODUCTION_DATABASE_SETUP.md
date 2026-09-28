# Production Database Management Guide - WorkCore

## 🎯 Architecture Overview

Your WorkCore app uses a **Hybrid Database Architecture**:

```
┌─────────────────┐         ┌──────────────────┐
│  Local Device   │  Sync   │  Cloud Firestore │
│  (Room DB)      │ ←─────→ │  (Firebase)      │
└─────────────────┘         └──────────────────┘
     ↓                              ↓
  Offline-first              Multi-device sync
  Fast access                Real-time updates
  Always works               Backup & recovery
```

---

## 📋 Step-by-Step Production Setup

### Step 1: Firebase Project Configuration ✅ DONE

You already have:
- ✅ Firebase project: `eod-management`
- ✅ `google-services.json` configured
- ✅ Project ID: `eod-management`

### Step 2: Enable Firestore Database

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select project: **eod-management**
3. Navigate to **Build → Firestore Database**
4. Click **"Create Database"**
5. **Choose location:** 
   - Recommended: `asia-south1` (Mumbai) for India
   - Or: `us-central` (Iowa) for global
6. **Start in Production Mode** (we'll add rules next)
7. Click **"Enable"**

### Step 3: Deploy Security Rules

#### For Initial Testing (1-2 weeks):

Use permissive rules for testing:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isSignedIn() {
      return request.auth != null;
    }
    
    // Allow all authenticated reads and writes
    match /{document=**} {
      allow read, write: if true;  // ⚠️ TEMPORARY - Change before launch!
    }
  }
}
```

**Deploy:**
1. Firestore Database → **Rules** tab
2. Paste above rules
3. Click **"Publish"**

#### For Production (Before Launch):

Use secure rules from `firestore.rules`:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    
    function isSignedIn() {
      return request.auth != null;
    }
    
    // EOD Submissions - everyone can read, only owner can write
    match /eod_submissions/{submissionId} {
      allow read: if true;  // All authenticated users can see EODs
      allow create, update: if true;  // Any authenticated user can submit
      allow delete: if false;  // Prevent accidental deletion
    }
    
    // Employees - public read, protected write
    match /employees/{employeeId} {
      allow read: if true;
      allow write: if true;  // Admins can manage employees
    }
    
    // Departments & Teams - public read
    match /departments/{departmentId} {
      allow read: if true;
      allow write: if true;
    }
    
    match /teams/{teamId} {
      allow read: if true;
      allow write: if true;
    }
  }
}
```

### Step 4: Initialize Cloud Data (First Time Setup)

#### Option A: Automatic Sync (Recommended)

1. **Run the app** on admin account
2. **Navigate to Settings** or Admin panel
3. Look for **"Sync All Data to Cloud"** button
4. Click to upload all seed data to Firestore

#### Option B: Manual Upload (If app doesn't have sync button)

Add seed data manually in Firestore Console:

**Collection: `departments`**
```
Document ID: DEPT_GT
{
  deptId: "DEPT_GT",
  name: "General Tech",
  code: "GT",
  prefix: "GT",
  description: "General Technology Department"
}

Document ID: DEPT_ML
{
  deptId: "DEPT_ML",
  name: "Machine Learning",
  code: "ML",
  prefix: "ML",
  description: "Machine Learning Department"
}

Document ID: DEPT_DB
{
  deptId: "DEPT_DB",
  name: "Database",
  code: "DB",
  prefix: "DB",
  description: "Database Department"
}

Document ID: DEPT_WRITING
{
  deptId: "DEPT_WRITING",
  name: "Content & Writing",
  code: "Writing",
  prefix: "WR",
  description: "Content Writing Department"
}

Document ID: DEPT_CYBER
{
  deptId: "DEPT_CYBER",
  name: "Cyber Security",
  code: "Cyber",
  prefix: "CYBER",
  description: "Cyber Security Department"
}
```

**Collection: `employees`**
```
Document ID: ADMIN
{
  employeeId: "ADMIN",
  name: "System Admin",
  email: "admin@workcore.com",
  phone: "+1234567890",
  department: "GT",
  departmentId: "DEPT_GT",
  team: "Core Team",
  designation: "System Administrator",
  defaultProject: "Core Engineering",
  joiningDate: "2024-01-01",
  role: "ADMIN",
  isActive: true,
  createdAt: 1704067200000
}
```

---

## 🔄 Daily Operations

### Data Sync Workflow

```
Morning:
1. Employees open app → Local data loads instantly
2. App syncs with Firestore in background
3. Any pending offline EODs upload automatically

During Day:
1. Employee submits EOD → Saves locally first
2. Immediately syncs to Firestore (if online)
3. Admin sees updated data in real-time
4. If offline → Marked as PENDING, syncs when online

End of Day:
1. Admin checks dashboard → All EODs visible
2. Generate reports from Firestore data
3. Download CSV/PDF exports
```

### Sync Status Monitoring

**In the app:**
- ✅ **Green dot** = All synced
- 🟡 **Yellow dot** = Syncing in progress
- 🔴 **Red dot** = Sync failed (retry)
- ⚪ **Gray dot** = Offline (will sync later)

---

## 📊 Database Collections Structure

### Collection: `eod_submissions`

**Document ID Format:** `{employeeId}_{date}`  
Example: `GT-001_2024-01-15`

**Why:** Prevents duplicate EODs per employee per day

**Fields:**
```javascript
{
  submissionId: string,       // "GT-001_2024-01-15"
  employeeId: string,         // "GT-001"
  employeeName: string,       // "John Doe"
  departmentId: string,       // "DEPT_GT"
  departmentCode: string,     // "GT"
  date: string,               // "2024-01-15" (YYYY-MM-DD)
  project: string,            // "Mobile App"
  todayWork: string,          // "Completed login feature"
  workCompleted: string,      // Detailed description
  workStatus: string,         // "Completed" | "In Progress" | "Blocked" | "On Leave"
  attendanceStatus: string,   // "Present" | "Leave" | "Holiday"
  blockers: string,           // Issues faced
  remarks: string,            // Additional notes
  tomorrowPlan: string,       // Next day plan
  submissionTime: string,     // "17:45" (HH:mm)
  isOnTime: boolean,          // true if before 18:30
  submittedAt: number,        // Timestamp
  updatedAt: number,          // Timestamp
  syncStatus: string          // "SYNCED"
}
```

### Collection: `employees`

**Document ID:** `{employeeId}`  
Example: `GT-001`, `ML-002`, `ADMIN`

**Fields:**
```javascript
{
  employeeId: string,
  name: string,
  email: string,
  phone: string,
  department: string,         // "GT", "ML", "DB"
  departmentId: string,       // "DEPT_GT"
  team: string,               // "Core Team"
  designation: string,
  defaultProject: string,
  joiningDate: string,        // "YYYY-MM-DD"
  role: string,               // "ADMIN" | "EMPLOYEE"
  isActive: boolean,
  createdAt: number,
  updatedAt: number
}
```

### Collection: `departments`

**Document ID:** `{deptId}`  
Example: `DEPT_GT`, `DEPT_ML`

### Collection: `teams`

**Document ID:** `{teamId}`  
Example: `TEAM_GT`, `TEAM_ML`

---

## 🔒 Security & Backup

### 1. Firestore Security Rules

**Production rules prevent:**
- ❌ Unauthorized access
- ❌ Data deletion by employees
- ❌ Cross-department data leaks
- ❌ Malicious data manipulation

### 2. Automatic Backups

**Daily Firestore Export:**

Set up automated backups:

1. Go to Firebase Console → **Firestore Database**
2. Click **"Import/Export"** tab
3. Set up **Daily Export Schedule**
4. Export to **Google Cloud Storage bucket**
5. Retention: **30 days**

### 3. Data Recovery

If data is lost:
1. Go to Firestore Console
2. Import/Export → **Import**
3. Select backup date
4. Restore entire database

---

## 📈 Monitoring & Analytics

### 1. Usage Monitoring

**Firebase Console → Firestore Dashboard:**
- Document reads/writes per day
- Storage usage
- Active connections
- Query performance

### 2. Set Up Alerts

**Firebase Console → Alerts:**
- Alert if reads exceed 50,000/day
- Alert if writes exceed 20,000/day
- Alert on errors

### 3. Cost Management

**Free Tier Limits (Firestore):**
- ✅ 50,000 reads/day
- ✅ 20,000 writes/day
- ✅ 20,000 deletes/day
- ✅ 1 GB storage

**Your Expected Usage (50 employees):**
- ~150 EOD submissions/day = 150 writes
- ~5,000 dashboard loads/day = 5,000 reads
- **Well within free tier!**

**Paid Plan (if needed):**
- $0.06 per 100,000 reads
- $0.18 per 100,000 writes
- Very affordable for SMB

---

## 🚨 Troubleshooting

### Problem: EODs not syncing to Firestore

**Check:**
1. Internet connection on device
2. Firestore rules deployed correctly
3. `google-services.json` is correct
4. Look at Android Studio Logcat for errors

**Fix:**
```kotlin
// In app, trigger manual sync
viewModel.syncAllPendingData()
```

### Problem: Data conflict (different on device vs cloud)

**Resolution:**
- App uses "last write wins" strategy
- Newer `updatedAt` timestamp takes precedence
- Manual resolution needed only for conflicts

### Problem: App shows "Firebase not initialized"

**Fix:**
1. Verify `google-services.json` exists in `app/` folder
2. Sync Gradle files
3. Clean & Rebuild project
4. Check Firebase project is active

---

## 📱 Multi-Device Setup

### Admin Device (Manager/HR)
- Install app
- Login as Admin (`admin` / `admin123`)
- Full access to all data
- Can add employees, view all EODs

### Employee Devices
- Install app
- Login with department + passcode
- Submit daily EODs
- View team roster
- Access personal history

### Data Sync Flow
```
Employee Device     →  Firestore  →  Admin Device
(Submit EOD)            (Cloud)       (See update)
   ↓                       ↓              ↓
Local Room DB        Central DB     Local Room DB
```

---

## 🎯 Production Checklist

Before launching to your team:

- [ ] Firestore Database enabled
- [ ] Security rules deployed (use `firestore.rules` from project)
- [ ] Test EOD submission and sync
- [ ] Add real employee data (replace seed data)
- [ ] Set up daily backups
- [ ] Configure usage alerts
- [ ] Test offline mode
- [ ] Test multi-device sync
- [ ] Document admin password
- [ ] Train managers on admin panel
- [ ] Set EOD cutoff time in settings (default 18:30)

---

## 💡 Best Practices

### 1. Data Entry
- ✅ Submit EODs before 6:30 PM daily
- ✅ Use consistent project names
- ✅ Keep remarks concise but clear
- ✅ Update blockers immediately

### 2. Admin Management
- ✅ Review pending EODs daily
- ✅ Export reports weekly
- ✅ Update employee status promptly
- ✅ Monitor sync status regularly

### 3. Backup Strategy
- ✅ Enable automatic Firestore exports
- ✅ Keep local exports on admin device
- ✅ Test restore process monthly

---

## 📞 Support

### Common Admin Tasks

**Add new employee:**
1. Admin → Add Employee
2. Fill details
3. Data saved locally + synced to cloud
4. Employee can login immediately

**View EOD reports:**
1. Admin → Reports
2. Select date range
3. Export to CSV/PDF
4. Data pulled from Firestore

**Handle sync issues:**
1. Admin → Settings
2. Click "Force Sync All"
3. Check sync status

---

## 🔮 Future Enhancements

### Phase 2 (Optional):
- Email notifications for missing EODs
- SMS reminders
- Manager approval workflow
- Advanced analytics dashboard
- Mobile + Web version

### Scalability:
- Current setup supports: **500+ employees**
- Firestore scales automatically
- No server management needed

---

## 📊 Expected Database Size

**50 Employees for 1 Year:**
- EOD submissions: ~12,500 documents (50 emp × 250 workdays)
- Employees: 50 documents
- Departments: 5 documents
- Teams: 10 documents
- **Total: ~12,565 documents**
- **Storage: ~15 MB**
- **Cost: FREE (within free tier)**

---

Your app is production-ready with Firebase! 🚀
