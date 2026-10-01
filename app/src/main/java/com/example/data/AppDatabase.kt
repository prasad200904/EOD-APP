package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    EmployeeEntity::class,
    DailyEodEntity::class,
    DepartmentEntity::class,
    CompanyConfigEntity::class,
    AuditLogEntity::class,
    TeamEntity::class,
    NotificationEntity::class
  ],
  version = 11,  // Incremented for teamPassword migration
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun employeeDao(): EmployeeDao
  abstract fun dailyEodDao(): DailyEodDao
  abstract fun departmentDao(): DepartmentDao
  abstract fun companyConfigDao(): CompanyConfigDao
  abstract fun auditLogDao(): AuditLogDao
  abstract fun teamDao(): TeamDao
  abstract fun notificationDao(): NotificationDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      android.util.Log.d("AppDatabase", "🔧 Creating database instance...")
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "workcore_database"
        )
        .fallbackToDestructiveMigration()
        .build()
        
        android.util.Log.i("AppDatabase", "✅ Database instance created (version 11 - Added team passwords)")
        INSTANCE = instance
        instance
      }
    }
    
    fun clearInstance() {
      INSTANCE?.close()
      INSTANCE = null
    }
  }
}
