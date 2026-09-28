package com.example.data.firebase

import android.content.Context
import android.util.Log
import com.example.data.DailyEodEntity
import com.example.data.DepartmentEntity
import com.example.data.EmployeeEntity
import com.example.data.Role
import com.example.data.SyncStatus
import com.example.data.TeamEntity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseDataSource(private val context: Context) {

  private val TAG = "FirebaseDataSource"

  // Graceful initialization: checks if FirebaseApp is configured via google-services.json
  val isFirebaseInitialized: Boolean
    get() = runCatching {
      FirebaseApp.getApps(context).isNotEmpty()
    }.getOrDefault(false)

  private val auth: FirebaseAuth?
    get() = if (isFirebaseInitialized) {
      runCatching { FirebaseAuth.getInstance() }.getOrNull()
    } else null

  private val firestore: FirebaseFirestore?
    get() = if (isFirebaseInitialized) {
      runCatching { FirebaseFirestore.getInstance() }.getOrNull()
    } else null

  val currentUser: FirebaseUser?
    get() = auth?.currentUser

  suspend fun signInWithEmail(email: String, pass: String): Result<FirebaseUser> {
    val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase is not initialized. Please connect your google-services.json."))
    return try {
      val res = authInstance.signInWithEmailAndPassword(email, pass).await()
      val user = res.user ?: throw IllegalStateException("Firebase returned null user.")
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "signInWithEmail failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun createUserWithEmail(email: String, pass: String): Result<FirebaseUser> {
    val authInstance = auth ?: return Result.failure(IllegalStateException("Firebase is not initialized. Please connect your google-services.json."))
    return try {
      val res = authInstance.createUserWithEmailAndPassword(email, pass).await()
      val user = res.user ?: throw IllegalStateException("Firebase user creation failed.")
      Result.success(user)
    } catch (e: Exception) {
      Log.e(TAG, "createUserWithEmail failed: ${e.message}", e)
      Result.failure(e)
    }
  }

  fun signOut() {
    auth?.signOut()
  }

  // -------------------------------------------------------------
  // FIRESTORE COLLECTIONS & DOCUMENTS
  // -------------------------------------------------------------

  suspend fun saveUserProfile(
    uid: String,
    name: String,
    email: String,
    employeeId: String,
    role: String,
    teamId: String,
    departmentId: String
  ): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "uid" to uid,
        "name" to name,
        "email" to email,
        "employeeId" to employeeId,
        "role" to if (role.equals(Role.ADMIN.name, ignoreCase = true)) Role.ADMIN.name else Role.EMPLOYEE.name,
        "teamId" to teamId,
        "departmentId" to departmentId,
        "isActive" to true,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("users").document(uid).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "saveUserProfile error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun uploadEod(eod: DailyEodEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      // Deterministic document ID to prevent duplicates across retries
      val docId = "${eod.employeeId}_${eod.date}"
      val data = hashMapOf(
        "submissionId" to docId,
        "employeeId" to eod.employeeId,
        "employeeName" to eod.employeeName,
        "departmentId" to eod.departmentId,
        "departmentCode" to eod.departmentCode,
        "date" to eod.date,
        "project" to eod.project,
        "todayWork" to eod.todayWork,
        "workCompleted" to eod.workCompleted,
        "workStatus" to eod.workStatus,
        "attendanceStatus" to eod.attendanceStatus,
        "blockers" to eod.blockers,
        "remarks" to eod.remarks,
        "tomorrowPlan" to eod.tomorrowPlan,
        "submissionTime" to eod.submissionTime,
        "isOnTime" to eod.isOnTime,
        "submittedAt" to eod.submittedAt,
        "updatedAt" to eod.updatedAt,
        "syncStatus" to SyncStatus.SYNCED.name
      )
      db.collection("eod_submissions").document(docId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "uploadEod error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchAllEods(): Result<List<DailyEodEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("eod_submissions").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          date = date,
          project = doc.getString("project") ?: "",
          todayWork = doc.getString("todayWork") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchAllEods error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchEodsForEmployee(employeeId: String): Result<List<DailyEodEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("eod_submissions")
        .whereEqualTo("employeeId", employeeId)
        .get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          date = date,
          project = doc.getString("project") ?: "",
          todayWork = doc.getString("todayWork") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchEodsForEmployee error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun uploadEmployee(emp: EmployeeEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "employeeId" to emp.employeeId,
        "name" to emp.name,
        "email" to emp.email,
        "phone" to emp.phone,
        "department" to emp.department,
        "departmentId" to emp.departmentId,
        "team" to emp.team,
        "designation" to emp.designation,
        "defaultProject" to emp.defaultProject,
        "joiningDate" to emp.joiningDate,
        "role" to if (emp.role.equals(Role.ADMIN.name, ignoreCase = true)) Role.ADMIN.name else Role.EMPLOYEE.name,
        "isActive" to emp.isActive,
        "createdAt" to emp.createdAt,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("employees").document(emp.employeeId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "uploadEmployee error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun fetchEmployees(): Result<List<EmployeeEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("employees").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        EmployeeEntity(
          id = 0,
          employeeId = empId,
          name = doc.getString("name") ?: "",
          email = doc.getString("email") ?: "",
          phone = doc.getString("phone") ?: "",
          department = doc.getString("department") ?: "ML",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          team = doc.getString("team") ?: "Core Team",
          designation = doc.getString("designation") ?: "Engineer",
          defaultProject = doc.getString("defaultProject") ?: "Core Engineering",
          joiningDate = doc.getString("joiningDate") ?: "2024-01-01",
          role = doc.getString("role") ?: Role.EMPLOYEE.name,
          isActive = doc.getBoolean("isActive") ?: true,
          createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Log.e(TAG, "fetchEmployees error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun uploadDepartment(dept: DepartmentEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "deptId" to dept.deptId,
        "name" to dept.name,
        "code" to dept.code,
        "prefix" to dept.prefix,
        "description" to dept.description,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("departments").document(dept.deptId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fetchDepartments(): Result<List<DepartmentEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("departments").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val deptId = doc.getString("deptId") ?: return@mapNotNull null
        DepartmentEntity(
          id = 0,
          deptId = deptId,
          name = doc.getString("name") ?: "",
          code = doc.getString("code") ?: "",
          prefix = doc.getString("prefix") ?: "",
          description = doc.getString("description") ?: ""
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun uploadTeam(team: TeamEntity): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val data = hashMapOf(
        "teamId" to team.teamId,
        "name" to team.name,
        "department" to team.department,
        "projects" to team.projects,
        "status" to team.status,
        "description" to team.description,
        "updatedAt" to System.currentTimeMillis()
      )
      db.collection("teams").document(team.teamId).set(data, SetOptions.merge()).await()
      Result.success(Unit)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun fetchTeams(): Result<List<TeamEntity>> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("teams").get().await()
      val list = snapshot.documents.mapNotNull { doc ->
        val teamId = doc.getString("teamId") ?: return@mapNotNull null
        TeamEntity(
          id = 0,
          teamId = teamId,
          name = doc.getString("name") ?: "",
          department = doc.getString("department") ?: "",
          managerId = "",
          managerName = "",
          projects = doc.getString("projects") ?: "",
          status = doc.getString("status") ?: "Active",
          description = doc.getString("description") ?: ""
        )
      }
      Result.success(list)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun deleteEmployee(empId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      db.collection("employees").document(empId).delete().await()
      Log.d(TAG, "Successfully deleted employee $empId from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteEmployee Firestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun deleteEod(empId: String, date: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val docId = "${empId}_${date}"
      db.collection("eod_submissions").document(docId).delete().await()
      Log.d(TAG, "Successfully deleted EOD $docId from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteEod Firestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  suspend fun deleteEodsForEmployee(empId: String): Result<Unit> {
    val db = firestore ?: return Result.failure(IllegalStateException("Firebase is not initialized"))
    return try {
      val snapshot = db.collection("eod_submissions")
        .whereEqualTo("employeeId", empId)
        .get().await()
      for (doc in snapshot.documents) {
        doc.reference.delete().await()
      }
      Log.d(TAG, "Successfully deleted all EODs for employee $empId from Firestore")
      Result.success(Unit)
    } catch (e: Exception) {
      Log.e(TAG, "deleteEodsForEmployee Firestore error: ${e.message}", e)
      Result.failure(e)
    }
  }

  fun listenToEodSubmissions(onUpdate: (List<DailyEodEntity>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("eod_submissions").addSnapshotListener { snapshot, e ->
      if (e != null || snapshot == null) return@addSnapshotListener
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        val date = doc.getString("date") ?: return@mapNotNull null
        DailyEodEntity(
          id = 0,
          employeeId = empId,
          employeeName = doc.getString("employeeName") ?: "",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          departmentCode = doc.getString("departmentCode") ?: "ML",
          date = date,
          project = doc.getString("project") ?: "",
          todayWork = doc.getString("todayWork") ?: "",
          workCompleted = doc.getString("workCompleted") ?: "",
          workStatus = doc.getString("workStatus") ?: "Completed",
          attendanceStatus = doc.getString("attendanceStatus") ?: "Present",
          blockers = doc.getString("blockers") ?: "",
          remarks = doc.getString("remarks") ?: "",
          tomorrowPlan = doc.getString("tomorrowPlan") ?: "",
          submissionTime = doc.getString("submissionTime") ?: "17:45",
          isOnTime = doc.getBoolean("isOnTime") ?: true,
          syncStatus = SyncStatus.SYNCED.name,
          submittedAt = doc.getLong("submittedAt") ?: System.currentTimeMillis(),
          updatedAt = doc.getLong("updatedAt") ?: System.currentTimeMillis()
        )
      }
      onUpdate(list)
    }
  }

  fun listenToEmployees(onUpdate: (List<EmployeeEntity>) -> Unit): com.google.firebase.firestore.ListenerRegistration? {
    val db = firestore ?: return null
    return db.collection("employees").addSnapshotListener { snapshot, e ->
      if (e != null || snapshot == null) return@addSnapshotListener
      val list = snapshot.documents.mapNotNull { doc ->
        val empId = doc.getString("employeeId") ?: return@mapNotNull null
        EmployeeEntity(
          id = 0,
          employeeId = empId,
          name = doc.getString("name") ?: "",
          email = doc.getString("email") ?: "",
          phone = doc.getString("phone") ?: "",
          department = doc.getString("department") ?: "ML",
          departmentId = doc.getString("departmentId") ?: "DEPT_ML",
          team = doc.getString("team") ?: "Core Team",
          designation = doc.getString("designation") ?: "Engineer",
          defaultProject = doc.getString("defaultProject") ?: "Core Engineering",
          joiningDate = doc.getString("joiningDate") ?: "2024-01-01",
          role = doc.getString("role") ?: Role.EMPLOYEE.name,
          isActive = doc.getBoolean("isActive") ?: true,
          createdAt = doc.getLong("createdAt") ?: System.currentTimeMillis()
        )
      }
      onUpdate(list)
    }
  }
}

