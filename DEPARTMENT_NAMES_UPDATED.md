# Department Names Updated to Match CSV

## ✅ Changes Made

Updated all department names in `SeedData.kt` to **exactly match** `employees-by-team.csv`.

---

## Before → After

| Old Department | New Department | Team |
|----------------|----------------|------|
| ❌ Cyber | ✅ Cyber Security | Cyber Security (7 employees) |
| ❌ ML | ✅ ML Team | ML Team (8 employees) |
| ❌ DB | ✅ DB Team | DB Team (2 employees) |
| ❌ GT | ✅ General Team | General Team (4 employees) |
| ❌ Writing | ✅ Writing Team | Writing Team (14 employees) |
| ✅ TEGA | ✅ TEGA | TEGA (7 employees) |
| ✅ Marketing | ✅ Marketing | Marketing (11 employees) |
| ✅ Counsellor | ✅ Counsellor | Counsellor (13 employees) |
| ✅ Tele Caller | ✅ Tele Caller | Tele Caller (3 employees) |
| ✅ Manager | ✅ Manager | Manager (2 employees) |

---

## All Department Names Now:

### Sandspace (SS):
1. **Cyber Security** (was "Cyber")
2. **ML Team** (was "ML")
3. **DB Team** (was "DB")
4. **General Team** (was "GT")
5. **Writing Team** (was "Writing")
6. **TEGA** (no change)

### Academic Overseas (AO):
1. **Marketing** (no change)
2. **Counsellor** (no change)
3. **Tele Caller** (no change)
4. **Manager** (no change)

---

## Files Updated

1. ✅ `SeedData.kt`
   - Updated all employee `department` fields
   - Updated all team `department` fields
   - Updated all department entities

2. ✅ `AppDatabase.kt`
   - Version 16 → **17**

---

## Firebase Impact

When you click **"Reset Firebase"** button, it will now upload employees with correct department names:
- ✅ `department: "Cyber Security"` (not "Cyber")
- ✅ `department: "General Team"` (not "GT")
- ✅ `department: "ML Team"` (not "ML")
- etc.

This matches your CSV exactly!

---

## Next Steps

1. **Uninstall app** (clear old department names)
2. **Build and install**
3. **Login as Admin**
4. **Click "Reset Firebase"** button
5. **Verify Firestore** - Check that departments match CSV

Database Version: **17**
