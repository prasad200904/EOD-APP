# Firebase Sync Migration - Multi-Company Schema

## Overview
Updated Firebase sync logic to support the new multi-company schema fields (v13). All sync methods now properly handle `teamId`, `companyId`, `eodFormType`, and `customFormData` fields.

## Changes Made

### 1. FirebaseDataSource.kt Updates

#### EOD Sync Methods

**`uploadEod()`** - Added 4 new fields to Firebase payload:
```kotlin
"teamId" to (eod.teamId ?: ""),
"companyId" to (eod.companyId ?: ""),
"eodFormType" to (eod.eodFormType ?: "STANDARD"),
"customFormData" to (eod.customFormData ?: "")
```

**`fetchAllEods()`** - Parse new fields from Firebase:
```kotlin
teamId = doc.getString("teamId"),
companyId = doc.getString("companyId"),
eodFormType = doc.getString("eodFormType") ?: "STANDARD",
customFormData = doc.getString("customFormData")
```

**`fetchEodsForEmployee()`** - Same field parsing as fetchAllEods

#### Employee Sync Methods

**`uploadEmployee()`** - Added 2 new fields:
```kotlin
"teamId" to (emp.teamId ?: ""),
"companyId" to (emp.companyId ?: "")
```

**`fetchEmployees()`** - Parse new fields:
```kotlin
teamId = doc.getString("teamId"),
companyId = doc.getString("companyId")
```

#### Team Sync Methods

**`uploadTeam()`** - Added 2 new fields:
```kotlin
"companyId" to (team.companyId ?: ""),
"eodFormType" to (team.eodFormType ?: "STANDARD")
```

**`fetchTeams()`** - Parse new fields:
```kotlin
companyId = doc.getString("companyId"),
eodFormType = doc.getString("eodFormType") ?: "STANDARD"
```

#### New Company Sync Methods

**`uploadCompany(company: CompanyEntity): Result<Unit>`**
- Uploads company to Firebase `companies` collection
- Fields: companyId, name, type, parentCompanyId, isActive, createdAt, updatedAt
- Uses SetOptions.merge() for safe updates

**`fetchCompanies(): Result<List<CompanyEntity>>`**
- Downloads all companies from Firebase
- Handles Timestamp vs Long for createdAt field
- Returns empty list on error (graceful degradation)

**`deleteCompany(companyId: String): Result<Unit>`**
- Deletes company document from Firebase
- Used for admin company management

### 2. SyncManager.kt Updates

#### Added CompanyDao
```kotlin
private val companyDao = database.companyDao()
```

#### Updated syncAllPending() Method

Added Steps 9 and 10 for company sync:

**Step 9: Upload Local Companies**
```kotlin
Log.d(TAG, "📤 Uploading local companies to Firebase...")
val localCompanies = companyDao.getAllCompanies().first()
for (company in localCompanies) {
  firebaseDataSource.uploadCompany(company)
}
```

**Step 10: Download Cloud Companies**
```kotlin
Log.d(TAG, "📥 Pulling latest companies from Firebase...")
val cloudCompaniesRes = firebaseDataSource.fetchCompanies()
if (cloudCompaniesRes.isSuccess) {
  val cloudCompanies = cloudCompaniesRes.getOrNull().orEmpty()
  for (cloudCompany in cloudCompanies) {
    val localCompany = companyDao.getCompanyById(cloudCompany.companyId)
    if (localCompany == null) {
      companyDao.insert(cloudCompany)
      // New company from another device
    } else if (cloudCompany.updatedAt > localCompany.updatedAt) {
      companyDao.update(cloudCompany.copy(id = localCompany.id))
      // Cloud version is newer
    }
  }
}
```

## Firestore Schema

### Collections Structure

```
Firestore Root
├── companies/
│   └── {companyId}
│       ├── companyId: String
│       ├── name: String
│       ├── type: String ("PARENT" | "SUBSIDIARY")
│       ├── parentCompanyId: String (nullable)
│       ├── isActive: Boolean
│       ├── createdAt: Timestamp/Long
│       └── updatedAt: Timestamp/Long
│
├── teams/
│   └── {teamId}
│       ├── teamId: String
│       ├── name: String
│       ├── department: String
│       ├── companyId: String ⬅️ NEW
│       ├── eodFormType: String ⬅️ NEW
│       ├── projects: String
│       ├── status: String
│       ├── description: String
│       └── updatedAt: Timestamp/Long
│
├── employees/
│   └── {employeeId}
│       ├── employeeId: String
│       ├── name: String
│       ├── email: String
│       ├── phone: String
│       ├── department: String
│       ├── departmentId: String
│       ├── team: String
│       ├── teamId: String ⬅️ NEW
│       ├── companyId: String ⬅️ NEW
│       ├── designation: String
│       ├── defaultProject: String
│       ├── joiningDate: String
│       ├── role: String
│       ├── isActive: Boolean
│       ├── createdAt: Timestamp/Long
│       └── updatedAt: Timestamp/Long
│
└── eod_submissions/
    └── {employeeId}_{date}
        ├── submissionId: String
        ├── employeeId: String
        ├── employeeName: String
        ├── departmentId: String
        ├── departmentCode: String
        ├── teamId: String ⬅️ NEW
        ├── companyId: String ⬅️ NEW
        ├── eodFormType: String ⬅️ NEW
        ├── customFormData: String ⬅️ NEW (JSON)
        ├── date: String
        ├── project: String
        ├── todayWork: String
        ├── workCompleted: String
        ├── workStatus: String
        ├── attendanceStatus: String
        ├── blockers: String
        ├── remarks: String
        ├── tomorrowPlan: String
        ├── submissionTime: String
        ├── isOnTime: Boolean
        ├── submittedAt: Long
        ├── updatedAt: Long
        └── syncStatus: String
```

## Backward Compatibility

### Graceful Field Handling

All new fields use safe defaults when reading from Firebase:

```kotlin
// EODs
teamId = doc.getString("teamId")  // null if doesn't exist
companyId = doc.getString("companyId")  // null if doesn't exist
eodFormType = doc.getString("eodFormType") ?: "STANDARD"  // default to STANDARD
customFormData = doc.getString("customFormData")  // null if doesn't exist

// Employees
teamId = doc.getString("teamId")  // null if doesn't exist
companyId = doc.getString("companyId")  // null if doesn't exist

// Teams
companyId = doc.getString("companyId")  // null if doesn't exist
eodFormType = doc.getString("eodFormType") ?: "STANDARD"  // default to STANDARD
```

### Empty String Handling

When uploading nullable fields, convert null to empty string for Firestore:

```kotlin
"teamId" to (eod.teamId ?: "")
"companyId" to (eod.companyId ?: "")
```

This ensures Firestore queries work correctly (Firestore treats null and missing fields differently).

## Sync Flow

### Full Sync Sequence (syncAllPending)

1. **Verify Authentication** - Check Firebase Auth state
2. **Upload Pending EODs** - Push local changes to cloud
3. **Download Cloud EODs** - Pull latest submissions
4. **Upload Local Employees** - Push new employees
5. **Download Cloud Employees** - Pull employee updates
6. **Upload Local Departments** - Push department changes
7. **Download Cloud Departments** - Pull department updates
8. **Upload Local Teams** - Push team changes
9. **Download Cloud Teams** - Pull team updates
10. **Upload Local Companies** - Push company changes ⬅️ NEW
11. **Download Cloud Companies** - Pull company updates ⬅️ NEW

### Real-Time Sync Triggers

- **Network becomes available** → Auto-sync all pending
- **EOD submitted** → Immediate upload (if online)
- **Periodic sync** → Every 30 seconds
- **Manual sync** → User clicks sync button

### Conflict Resolution

**Last-Write-Wins Strategy:**
- Compare `updatedAt` timestamps
- Cloud version > Local version → Update local
- Local version ≥ Cloud version → Keep local

**Special Cases:**
- Admin employee (employeeId = "ADMIN") → Never deleted from local
- Pending/Failed EODs → Always uploaded, not overwritten by cloud
- New employees → Always merged (never deleted)

## Security Rules (Recommended)

Update Firestore Security Rules to allow company operations:

```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    // Company collection
    match /companies/{companyId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/employees/$(request.auth.uid)).data.role == 'ADMIN';
    }
    
    // Teams collection - updated to include companyId
    match /teams/{teamId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/employees/$(request.auth.uid)).data.role == 'ADMIN';
    }
    
    // Employees collection - updated to include teamId, companyId
    match /employees/{employeeId} {
      allow read: if request.auth != null;
      allow write: if request.auth != null && 
        get(/databases/$(database)/documents/employees/$(request.auth.uid)).data.role == 'ADMIN';
    }
    
    // EOD submissions - updated to include teamId, companyId, eodFormType, customFormData
    match /eod_submissions/{submissionId} {
      allow read: if request.auth != null;
      allow create: if request.auth != null && 
        request.resource.data.employeeId == request.auth.uid;
      allow update: if request.auth != null && 
        (resource.data.employeeId == request.auth.uid || 
         get(/databases/$(database)/documents/employees/$(request.auth.uid)).data.role == 'ADMIN');
    }
  }
}
```

## Testing Sync

### Test Scenario 1: New EOD with Custom Form
1. Employee submits Telecalling EOD with custom data
2. Verify `eodFormType` = "TELECALLING"
3. Verify `customFormData` contains JSON
4. Check Firestore document includes all 4 new fields
5. Sync to second device
6. Verify custom form displays correctly

### Test Scenario 2: New Company Sync
1. Admin creates new company on Device A
2. Trigger sync
3. Check Firestore `companies` collection
4. Sync on Device B
5. Verify company appears in Device B database
6. Verify company filter shows new company

### Test Scenario 3: Employee Company Assignment
1. Admin assigns employee to Academic Overseas
2. Employee submits EOD
3. Verify `companyId` in EOD document
4. Sync to cloud
5. Pull from cloud on Device B
6. Verify employee shows under correct company

### Test Scenario 4: Team Form Type Update
1. Admin updates team eodFormType to "COUNSELLOR"
2. Sync to cloud
3. Pull on Device B
4. Employee clicks team member
5. Verify Counsellor form appears (not standard form)

## Migration Steps

### For Existing Deployments

1. **Update App Code**
   - Deploy updated FirebaseDataSource.kt
   - Deploy updated SyncManager.kt

2. **Firestore Data Migration** (Optional)
   - Existing documents without new fields will work fine
   - New fields default to null/empty
   - No manual migration needed

3. **Force Full Sync**
   - Users should trigger manual sync after update
   - This uploads local schema v13 data to cloud
   - Ensures all devices have latest data

4. **Update Security Rules**
   - Deploy new Firestore security rules
   - Add `companies` collection rules

## Monitoring & Logs

### Sync Logs to Watch

```
🔐 Firebase Auth verified - UID: {uid}, Email: {email}
📤 Uploading X pending EODs to Firebase...
📥 Pulling latest EODs from Firebase...
📤 Uploading local employees to Firebase...
✅ Uploaded X employees to Firebase
📤 Uploading local companies to Firebase...
✅ Uploaded X companies to Firebase
📥 Pulling latest companies from Firebase...
➕ New company from cloud: {name} ({id})
🔄 Updated company from cloud: {name} ({id})
✅ Company sync: X new
✅ Full sync completed successfully!
```

### Common Issues

**Issue: "User not authenticated with Firebase Auth"**
- Cause: Firebase Auth not initialized before sync
- Fix: Ensure `authenticate()` calls `ensureFirebaseSession()`

**Issue: Custom form data not syncing**
- Cause: `customFormData` is null but expected
- Fix: Check JSON serialization before upload

**Issue: Company filter shows old data**
- Cause: Local database not updated from cloud
- Fix: Trigger manual sync, verify companyDao.insert() called

**Issue: Team form type not updating**
- Cause: `eodFormType` not parsed from Firebase
- Fix: Check fetchTeams() parses `eodFormType` field

## Performance Considerations

### Payload Sizes
- **Custom Form Data**: JSON strings can be 1-10KB
- **Recommendation**: Keep custom form data concise
- **Limit**: Firestore document max 1MB (plenty of room)

### Batch Operations
- Current: Sequential uploads/downloads
- Future optimization: Use Firestore batch writes for bulk operations
- Benefit: Reduce network round-trips

### Indexes
Recommended Firestore indexes:
- `eod_submissions`: index on `companyId`
- `eod_submissions`: index on `teamId`
- `eod_submissions`: index on `eodFormType`
- `employees`: index on `companyId`
- `teams`: index on `companyId`

## Code Stats

- **Lines Added:** ~250 lines
- **Files Modified:** 2 (FirebaseDataSource, SyncManager)
- **New Methods:** 3 (uploadCompany, fetchCompanies, deleteCompany)
- **Updated Methods:** 6 (uploadEod, fetchAllEods, fetchEodsForEmployee, uploadEmployee, fetchEmployees, uploadTeam, fetchTeams)
- **New Firestore Collection:** 1 (companies)

## Next Steps

1. ✅ Deploy updated code
2. ✅ Test sync on dev environment
3. ✅ Update Firestore security rules
4. ✅ Trigger full sync on all devices
5. ⏳ Monitor sync logs for errors
6. ⏳ Verify custom form data integrity
7. ⏳ Test multi-device scenarios
8. ⏳ Document any edge cases found

## Rollback Plan

If issues arise:
1. Revert to previous FirebaseDataSource.kt version
2. Database schema v13 is backward compatible (new fields nullable)
3. Existing data in Firestore remains valid
4. Re-deploy when issues resolved
