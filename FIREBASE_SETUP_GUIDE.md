# Firebase Setup Guide for WorkCore

## ✅ Setup Checklist

### 1. Firebase Console Configuration
- [ ] Created Firebase project: `eod-management`
- [ ] Downloaded `google-services.json` (already done ✅)
- [ ] Enabled Firestore Database
- [ ] Enabled Email/Password Authentication
- [ ] Deployed Firestore Security Rules

### 2. First Admin User Setup
- [ ] Created admin user in Authentication
- [ ] Added admin profile to Firestore `users` collection
- [ ] Verified admin has `role: "ADMIN"`

### 3. Initial Data Setup (Optional)
- [ ] Created departments collection
- [ ] Created teams collection
- [ ] Added seed employees

---

## 📋 Firebase Collections Structure

### Collection: `users`
Stores user profiles linked to Firebase Auth UIDs
```
Document ID: <Firebase Auth UID>
{
  uid: string,
  name: string,
  email: string,
  employeeId: string,       // e.g., "EMP001"
  role: string,             // "ADMIN" or "EMPLOYEE"
  teamId: string,           // e.g., "TEAM_ML"
  departmentId: string,     // e.g., "DEPT_ML"
  isActive: boolean,
  updatedAt: number
}
```

### Collection: `employees`
Stores employee master data
```
Document ID: <employeeId>  // e.g., "EMP001"
{
  employeeId: string,
  name: string,
  email: string,
  phone: string,
  department: string,       // e.g., "ML"
  departmentId: string,     // e.g., "DEPT_ML"
  team: string,
  designation: string,
  defaultProject: string,
  joiningDate: string,      // YYYY-MM-DD
  role: string,             // "ADMIN" or "EMPLOYEE"
  isActive: boolean,
  createdAt: number,
  updatedAt: number
}
```

### Collection: `eod_submissions`
Stores daily EOD reports
```
Document ID: <employeeId>_<date>  // e.g., "EMP001_2024-01-15"
{
  submissionId: string,
  employeeId: string,
  employeeName: string,
  departmentId: string,
  departmentCode: string,
  date: string,             // YYYY-MM-DD
  project: string,
  todayWork: string,
  workCompleted: string,
  workStatus: string,       // "Completed", "In Progress", "Blocked", etc.
  attendanceStatus: string, // "Present", "Leave", "Holiday"
  blockers: string,
  remarks: string,
  tomorrowPlan: string,
  submissionTime: string,   // HH:mm
  isOnTime: boolean,
  submittedAt: number,
  updatedAt: number,
  syncStatus: string        // "SYNCED"
}
```

### Collection: `departments`
Stores department information
```
Document ID: <deptId>  // e.g., "DEPT_ML"
{
  deptId: string,
  name: string,
  code: string,         // e.g., "ML"
  prefix: string,       // e.g., "ML"
  description: string,
  updatedAt: number
}
```

### Collection: `teams`
Stores team information
```
Document ID: <teamId>  // e.g., "TEAM_ML"
{
  teamId: string,
  name: string,
  department: string,
  projects: string,     // Comma-separated
  status: string,       // "Active" or "Inactive"
  description: string,
  updatedAt: number
}
```

---

## 🔑 Sample Admin User

Use these details to create your first admin:

**Firebase Authentication:**
- Email: `admin@workcore.com`
- Password: `Admin@123456`

**Firestore `users` Collection:**
- Document ID: `<Firebase Auth UID>`
- Fields:
  ```
  uid: <Firebase Auth UID>
  name: "System Admin"
  email: "admin@workcore.com"
  employeeId: "EMP001"
  role: "ADMIN"
  teamId: "TEAM_GENERAL"
  departmentId: "DEPT_GT"
  isActive: true
  updatedAt: 1737550000000
  ```

**Firestore `employees` Collection:**
- Document ID: `EMP001`
- Fields:
  ```
  employeeId: "EMP001"
  name: "System Admin"
  email: "admin@workcore.com"
  phone: "+1234567890"
  department: "GT"
  departmentId: "DEPT_GT"
  team: "Core Team"
  designation: "System Administrator"
  defaultProject: "Core Engineering"
  joiningDate: "2024-01-01"
  role: "ADMIN"
  isActive: true
  createdAt: 1737550000000
  updatedAt: 1737550000000
  ```

---

## 🧪 Testing the Setup

### Test 1: Login with Admin Account
1. Run the app on Android Studio
2. Open Login Screen
3. Enter admin credentials
4. Should successfully login and see Admin Dashboard

### Test 2: Create EOD Submission
1. Login as admin
2. Navigate to "Today" screen
3. Fill out EOD form
4. Submit
5. Check Firestore Console → `eod_submissions` collection
6. Verify the document was created

### Test 3: Check Security Rules
1. Try to access Firestore without authentication (should fail)
2. Login as employee and try to modify another employee's EOD (should fail)
3. Login as admin and modify any EOD (should succeed)

---

## 🔒 Security Rules Deployment

### For Development/Testing
Use `firestore.rules.dev`:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    function isSignedIn() {
      return request.auth != null;
    }
    match /{document=**} {
      allow read, write: if isSignedIn();
    }
  }
}
```

### For Production
Use `firestore.rules`:
- Role-based access control
- Employees can only modify their own data
- Admins have full access
- Inactive users are blocked

---

## 📱 App Configuration

Make sure these files exist:
- ✅ `app/google-services.json` (Firebase config)
- ✅ `.env` (Gemini API key - optional)
- ✅ `firestore.rules` (Security rules)

---

## 🆘 Troubleshooting

### "Firebase is not initialized"
- Check if `google-services.json` exists in `app/` folder
- Sync Gradle files in Android Studio
- Clean and rebuild project

### "User not authorized"
- Verify user exists in Authentication
- Check if user profile exists in `users` collection
- Verify `role` field is set correctly
- Check if `isActive` is `true`

### "Permission denied"
- Check Firestore Rules are deployed
- Verify user is authenticated
- For testing, use `firestore.rules.dev`

### "Cannot sign in"
- Verify Email/Password auth is enabled
- Check email and password are correct
- Look at Android Studio Logcat for error messages

---

## 📞 Next Steps

After setup is complete:
1. ✅ Run the app and login as admin
2. ✅ Create departments using the app
3. ✅ Add employees using the app
4. ✅ Test EOD submission
5. ✅ Review analytics and reports
6. ✅ Switch to production security rules

---

## 🌟 Tips

- **Backup regularly**: Export Firestore data periodically
- **Monitor usage**: Check Firebase Console for usage metrics
- **Update rules**: Switch from dev to production rules before launch
- **Test thoroughly**: Test all user roles and permissions
- **Secure API keys**: Never commit `.env` file to version control
