package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
  entities = [
    CompanyEntity::class,
    EmployeeEntity::class,
    DailyEodEntity::class,
    DepartmentEntity::class,
    CompanyConfigEntity::class,
    AuditLogEntity::class,
    TeamEntity::class,
    NotificationEntity::class,
    TeamMemberDataEntity::class
  ],
  version = 19,  // V19: Password-free login migration - cleared stored passwords
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun companyDao(): CompanyDao
  abstract fun employeeDao(): EmployeeDao
  abstract fun dailyEodDao(): DailyEodDao
  abstract fun departmentDao(): DepartmentDao
  abstract fun companyConfigDao(): CompanyConfigDao
  abstract fun auditLogDao(): AuditLogDao
  abstract fun teamDao(): TeamDao
  abstract fun notificationDao(): NotificationDao
  abstract fun teamMemberDataDao(): TeamMemberDataDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    val MIGRATION_18_19 = object : androidx.room.migration.Migration(18, 19) {
      override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
        // Clear stored passwords from employees and teams tables
        db.execSQL("UPDATE employees SET password = ''")
        db.execSQL("UPDATE teams SET teamPassword = ''")
      }
    }

    fun getDatabase(context: Context): AppDatabase {
      android.util.Log.d("AppDatabase", "🔧 Creating database instance...")
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "workcore_database"
        )
        .addMigrations(MIGRATION_18_19)
        // TODO: Remove fallbackToDestructiveMigration() once production installs have completed migration to v19
        .fallbackToDestructiveMigration()
        .build()
        
        android.util.Log.i("AppDatabase", "✅ Database instance created (version 19 - password-free migration)")
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
