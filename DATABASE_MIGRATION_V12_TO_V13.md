# Database Migration: Version 12 → 13

## Overview
This migration adds multi-company support with Academic Overseas and custom EOD form types.

## Migration Strategy
**Using `fallbackToDestructiveMigration()`** - Database will be recreated with new schema.

**⚠️ WARNING:** All existing data will be cleared. Seed data will repopulate on first launch.

---

## Changes Summary

### 1. New Table: `companies`
```sql
CREATE TABLE companies (
  id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
  companyId TEXT NOT NULL,
  name TEXT NOT NULL,
  parentCompanyId TEXT NOT NULL DEFAULT '',
  type TEXT NOT NULL DEFAULT 'PARENT',
  description TEXT NOT NULL DEFAULT '',
  isActive INTEGER NOT NULL DEFAULT 1,
  createdAt INTEGER NOT NULL
);

CREATE UNIQUE INDEX index_companies_companyId ON companies(companyId);
CREATE INDEX index_companies_name ON companies(name);
```

**Initial Data:**
- `COMP_SANDSPACE`: "Sandspace Technologies Pvt Ltd" (PARENT)
- `COMP_AO`: "Academic Overseas" (SUBSIDIARY, parentCompanyId = COMP_SANDSPACE)

---

### 2. Modified Table: `teams`

**New Columns:**
```sql
ALTER TABLE teams ADD COLUMN companyId TEXT NOT NULL DEFAULT 'COMP_SANDSPACE';
ALTER TABLE teams ADD COLUMN eodFormType TEXT NOT NULL DEFAULT 'STANDARD';
```

**New Indexes:**
```sql
CREATE INDEX index_teams_companyId ON teams(companyId);
CREATE INDEX index_teams_eodFormType ON teams(eodFormType);
```

**Updated Teams:**
- Existing teams (GT, ML, DB, Cyber, Writing) → `COMP_SANDSPACE`, `STANDARD`
- New AO teams:
  - Counsellors → `COMP_AO`, `COUNSELLOR`
  - Telecalling → `COMP_AO`, `TELECALLING`
  - Marketing → `COMP_AO`, `MARKETING`
  - Other → `COMP_AO`, `SIMPLE`

---

### 3. Modified Table: `employees`

**New Columns:**
```sql
ALTER TABLE employees ADD COLUMN companyId TEXT NOT NULL DEFAULT 'COMP_SANDSPACE';
ALTER TABLE employees ADD COLUMN teamId TEXT NOT NULL DEFAULT 'TEAM_ML';
```

**New Indexes:**
```sql
CREATE INDEX index_employees_companyId ON employees(companyId);
CREATE INDEX index_employees_teamId ON employees(teamId);
```

**Migration Logic:**
- All existing employees → `companyId = 'COMP_SANDSPACE'`
- `teamId` derived from `team` field name matching

---

### 4. Modified Table: `daily_eods`

**New Columns:**
```sql
ALTER TABLE daily_eods ADD COLUMN teamId TEXT NOT NULL DEFAULT 'TEAM_ML';
ALTER TABLE daily_eods ADD COLUMN companyId TEXT NOT NULL DEFAULT 'COMP_SANDSPACE';
ALTER TABLE daily_eods ADD COLUMN eodFormType TEXT NOT NULL DEFAULT 'STANDARD';
ALTER TABLE daily_eods ADD COLUMN customFormData TEXT NOT NULL DEFAULT '';
```

**Modified Columns (now with defaults):**
```sql
-- project, todayWork, workCompleted, workStatus now have DEFAULT ''
```

**New Indexes:**
```sql
CREATE INDEX index_daily_eods_teamId ON daily_eods(teamId);
CREATE INDEX index_daily_eods_companyId ON daily_eods(companyId);
CREATE INDEX index_daily_eods_eodFormType ON daily_eods(eodFormType);
```

---

## EOD Form Types

### STANDARD (Default)
Used by: GT Team, ML Team, DB Team, Cyber Security, Writing Team
- Uses existing fields: project, todayWork, workCompleted, workStatus, etc.
- `customFormData` = empty

### COUNSELLOR
Used by: Academic Overseas - Counsellors
- `customFormData` contains JSON:
```json
{
  "students": [
    {
      "studentName": "...",
      "universityName": "...",
      "country": "...",
      "status": "...",
      "description": "..."
    }
  ]
}
```

### TELECALLING
Used by: Academic Overseas - Telecalling
- `customFormData` contains JSON:
```json
{
  "callsMade": 45,
  "followUps": 12,
  "conversions": 3,
  "status": "Target Met",
  "description": "..."
}
```

### MARKETING
Used by: Academic Overseas - Marketing
- `customFormData` contains JSON:
```json
{
  "visits": [
    {
      "collegeName": "...",
      "personVisited": "...",
      "designation": "...",
      "description": "..."
    }
  ]
}
```

### SIMPLE
Used by: Academic Overseas - Other
- `customFormData` contains JSON:
```json
{
  "description": "..."
}
```

---

## Testing After Migration

### 1. Verify Companies
```kotlin
val companies = companyDao.getAllCompanies().first()
// Should return 2 companies: Sandspace + AO
```

### 2. Verify Teams
```kotlin
val aoTeams = teamDao.getTeamsByCompany("COMP_AO").first()
// Should return 4 teams: Counsellors, Telecalling, Marketing, Other

val sandspaceTeams = teamDao.getTeamsByCompany("COMP_SANDSPACE").first()
// Should return 5 teams: GT, ML, DB, Cyber, Writing
```

### 3. Verify Employees
```kotlin
val allEmployees = employeeDao.getAllEmployees().first()
// All should have valid companyId and teamId
```

### 4. Test Custom Forms
- Login as Counsellor → Should show Counsellor form (multiple students)
- Login as Telecalling → Should show Telecalling form (call metrics)
- Login as Marketing → Should show Marketing form (college visits)
- Login as Other → Should show Simple form (description only)
- Login as ML Team → Should show Standard form (existing)

---

## Rollback Strategy

If migration fails or data corruption occurs:

1. **Uninstall app** from device/emulator
2. **Reinstall app** - Will create fresh database with version 13
3. **Seed data** will automatically populate on first launch
4. **Re-import employees** from Firebase if needed

---

## Files Modified

1. `Entities.kt` - Added CompanyEntity, EodFormType enum, updated existing entities
2. `Daos.kt` - Added CompanyDao, updated queries for multi-company
3. `AppDatabase.kt` - Version 12 → 13, added CompanyEntity
4. `SeedData.kt` - Will add company and AO team seed data (next step)

---

## Next Steps

After this migration:
1. ✅ Update SeedData.kt with companies and AO teams
2. ✅ Create custom EOD form UI screens
3. ✅ Update WorkCoreRepository with company-aware logic
4. ✅ Update Firebase sync for new fields
5. ✅ Add Team Reports to Analytics

---

**Date:** 2026-09-29  
**Migration Type:** Destructive (data reset)  
**Reason:** Adding new tables and modifying multiple existing tables with new columns
