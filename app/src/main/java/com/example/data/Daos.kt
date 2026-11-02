package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
  @Query("SELECT * FROM companies ORDER BY name ASC")
  fun getAllCompanies(): Flow<List<CompanyEntity>>

  @Query("SELECT * FROM companies WHERE companyId = :companyId LIMIT 1")
  suspend fun getCompanyById(companyId: String): CompanyEntity?

  @Query("SELECT * FROM companies WHERE isActive = 1 ORDER BY name ASC")
  fun getActiveCompanies(): Flow<List<CompanyEntity>>

  @Query("SELECT * FROM companies WHERE type = 'PARENT' ORDER BY name ASC")
  fun getParentCompanies(): Flow<List<CompanyEntity>>

  @Query("SELECT * FROM companies WHERE parentCompanyId = :parentId ORDER BY name ASC")
  fun getSubsidiaries(parentId: String): Flow<List<CompanyEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(company: CompanyEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(companies: List<CompanyEntity>)

  @Update
  suspend fun update(company: CompanyEntity)

  @Query("UPDATE companies SET isActive = :isActive WHERE companyId = :companyId")
  suspend fun setActiveStatus(companyId: String, isActive: Boolean)

  @Query("DELETE FROM companies WHERE companyId = :companyId")
  suspend fun deleteCompany(companyId: String)
}

@Dao
interface EmployeeDao {
  @Query("SELECT * FROM employees ORDER BY name ASC")
  fun getAllEmployees(): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE companyId = :companyId ORDER BY name ASC")
  fun getEmployeesByCompany(companyId: String): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE teamId = :teamId ORDER BY name ASC")
  fun getEmployeesByTeamId(teamId: String): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE department = :deptCode OR departmentId = :deptCode ORDER BY name ASC")
  fun getEmployeesForDepartment(deptCode: String): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE department = :deptCode OR departmentId = :deptCode")
  suspend fun getEmployeesByDepartment(deptCode: String): List<EmployeeEntity>

  @Query("SELECT * FROM employees WHERE managerId = :managerId OR employeeId = :managerId ORDER BY name ASC")
  fun getEmployeesForManager(managerId: String): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE team = :team ORDER BY name ASC")
  fun getEmployeesForTeam(team: String): Flow<List<EmployeeEntity>>

  @Query("SELECT * FROM employees WHERE employeeId = :empId LIMIT 1")
  suspend fun getEmployeeById(empId: String): EmployeeEntity?

  @Query("SELECT * FROM employees WHERE isActive = 1")
  fun getActiveEmployees(): Flow<List<EmployeeEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(employee: EmployeeEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(employees: List<EmployeeEntity>)

  @Update
  suspend fun update(employee: EmployeeEntity)

  @Query("UPDATE employees SET isActive = :isActive WHERE employeeId = :empId")
  suspend fun setActiveStatus(empId: String, isActive: Boolean)

  @Query("DELETE FROM employees WHERE employeeId = :empId")
  suspend fun deleteEmployeeById(empId: String)

  @Query("DELETE FROM employees WHERE employeeId != 'ADMIN'")
  suspend fun deleteAllNonAdminEmployees()
}

@Dao
interface DailyEodDao {
  @Query("SELECT * FROM daily_eods ORDER BY date DESC, submissionTime DESC")
  fun getAllEods(): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE companyId = :companyId ORDER BY date DESC, submissionTime DESC")
  fun getEodsByCompany(companyId: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE teamId = :teamId ORDER BY date DESC, submissionTime DESC")
  fun getEodsByTeam(teamId: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE eodFormType = :formType ORDER BY date DESC, submissionTime DESC")
  fun getEodsByFormType(formType: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE date = :date ORDER BY submissionTime DESC")
  fun getEodsForDate(date: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE teamId = :teamId AND date = :date ORDER BY submissionTime DESC")
  fun getEodsForTeamAndDate(teamId: String, date: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE departmentCode = :deptCode OR departmentId = :deptCode ORDER BY date DESC")
  fun getEodsForDepartment(deptCode: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE employeeId = :empId ORDER BY date DESC")
  fun getEodsForEmployee(empId: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE employeeId = :empId AND date = :date LIMIT 1")
  suspend fun getEod(empId: String, date: String): DailyEodEntity?

  @Query("SELECT * FROM daily_eods WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
  fun getEodsInRange(startDate: String, endDate: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE teamId = :teamId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
  fun getEodsForTeamInRange(teamId: String, startDate: String, endDate: String): Flow<List<DailyEodEntity>>

  @Query("SELECT * FROM daily_eods WHERE syncStatus = :status")
  suspend fun getEodsBySyncStatus(status: String): List<DailyEodEntity>

  @Query("UPDATE daily_eods SET syncStatus = :status WHERE employeeId = :empId AND date = :date")
  suspend fun updateSyncStatus(empId: String, date: String, status: String)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(eod: DailyEodEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(eods: List<DailyEodEntity>)

  @Update
  suspend fun update(eod: DailyEodEntity)

  @Query("DELETE FROM daily_eods WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM daily_eods WHERE employeeId = :empId")
  suspend fun deleteEodsForEmployee(empId: String)

  @Query("DELETE FROM daily_eods")
  suspend fun deleteAllEods()
}

@Dao
interface DepartmentDao {
  @Query("SELECT * FROM departments ORDER BY id ASC")
  fun getAllDepartments(): Flow<List<DepartmentEntity>>

  @Query("SELECT * FROM departments WHERE code = :code LIMIT 1")
  suspend fun getDepartmentByCode(code: String): DepartmentEntity?

  @Query("SELECT * FROM departments WHERE deptId = :deptId LIMIT 1")
  suspend fun getDepartmentById(deptId: String): DepartmentEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(departments: List<DepartmentEntity>)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(department: DepartmentEntity): Long
}

@Dao
interface CompanyConfigDao {
  @Query("SELECT * FROM company_config WHERE configKey = 'default_config' LIMIT 1")
  suspend fun getConfig(): CompanyConfigEntity?

  @Query("SELECT * FROM company_config WHERE configKey = 'default_config' LIMIT 1")
  fun getConfigFlow(): Flow<CompanyConfigEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun saveConfig(config: CompanyConfigEntity)
}

@Dao
interface AuditLogDao {
  @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT 100")
  fun getRecentLogs(): Flow<List<AuditLogEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(log: AuditLogEntity): Long
}

@Dao
interface TeamDao {
  @Query("SELECT * FROM teams ORDER BY name ASC")
  fun getAllTeams(): Flow<List<TeamEntity>>

  @Query("SELECT * FROM teams WHERE companyId = :companyId ORDER BY name ASC")
  fun getTeamsByCompany(companyId: String): Flow<List<TeamEntity>>

  @Query("SELECT * FROM teams WHERE eodFormType = :formType ORDER BY name ASC")
  fun getTeamsByFormType(formType: String): Flow<List<TeamEntity>>

  @Query("SELECT * FROM teams WHERE teamId = :teamId LIMIT 1")
  suspend fun getTeamById(teamId: String): TeamEntity?

  @Query("SELECT * FROM teams WHERE name = :name LIMIT 1")
  suspend fun getTeamByName(name: String): TeamEntity?

  @Query("SELECT * FROM teams WHERE status = 'Active' ORDER BY name ASC")
  fun getActiveTeams(): Flow<List<TeamEntity>>

  @Query("SELECT * FROM teams WHERE companyId = :companyId AND status = 'Active' ORDER BY name ASC")
  fun getActiveTeamsByCompany(companyId: String): Flow<List<TeamEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(team: TeamEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(teams: List<TeamEntity>)

  @Update
  suspend fun update(team: TeamEntity)

  @Query("UPDATE teams SET status = :status WHERE teamId = :teamId")
  suspend fun setTeamStatus(teamId: String, status: String)

  @Query("DELETE FROM teams WHERE teamId = :teamId")
  suspend fun deleteTeam(teamId: String)

  @Query("DELETE FROM teams")
  suspend fun deleteAllTeams()
}

@Dao
interface NotificationDao {
  @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
  fun getAllNotifications(): Flow<List<NotificationEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(notification: NotificationEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAll(notifications: List<NotificationEntity>)

  @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
  suspend fun markAsRead(id: Long)

  @Query("UPDATE notifications SET isRead = 1")
  suspend fun markAllAsRead()

  @Query("DELETE FROM notifications")
  suspend fun deleteAllNotifications()
}

@Dao
interface TeamMemberDataDao {
  @Query("SELECT * FROM team_member_data WHERE employeeId = :employeeId AND teamType = :teamType LIMIT 1")
  suspend fun getTeamDataByEmployeeId(employeeId: String, teamType: String): TeamMemberDataEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTeamMemberData(data: TeamMemberDataEntity): Long

  @Update
  suspend fun updateTeamMemberData(data: TeamMemberDataEntity)

  @Query("DELETE FROM team_member_data WHERE employeeId = :employeeId AND teamType = :teamType")
  suspend fun deleteTeamDataByEmployeeId(employeeId: String, teamType: String)

  @Query("DELETE FROM team_member_data WHERE employeeId = :employeeId")
  suspend fun deleteTeamDataForEmployee(employeeId: String)

  @Query("DELETE FROM team_member_data")
  suspend fun deleteAllTeamData()
}

