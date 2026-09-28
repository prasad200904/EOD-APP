@echo off
echo =========================================
echo WorkCore App - Clear Data Instructions
echo =========================================
echo.
echo DATABASE NOT SHOWING FIX - 3 SIMPLE STEPS:
echo.
echo Step 1: ON YOUR PHONE
echo ----------------------
echo 1. Open Settings on your phone
echo 2. Go to Apps (or Application Manager)
echo 3. Find and tap "WorkCore"
echo 4. Tap "Storage" or "Storage and cache"
echo 5. Tap "Clear data" or "Clear storage"
echo 6. Confirm the action
echo.
echo Step 2: REBUILD THE APP
echo ----------------------
echo Run this in Android Studio Terminal:
echo    File ^> Open ^> Build ^> Rebuild Project
echo.
echo OR if you have gradle in PATH:
echo    gradle clean assembleDebug installDebug
echo.
echo Step 3: OPEN THE APP
echo ----------------------
echo After clearing data and rebuilding:
echo 1. Open WorkCore on your phone
echo 2. Login as: admin / admin123
echo 3. Select GT Team from dropdown
echo 4. You should see 5 employees!
echo.
echo =========================================
echo WHY THIS WORKS:
echo =========================================
echo - Old database file is removed
echo - New version (database v8) installs fresh
echo - Seed data auto-initializes
echo - All GT Team employees load automatically
echo.
echo =========================================
echo EXPECTED RESULT:
echo =========================================
echo GT Team Employees (5):
echo - GT-001: Deepak Kumar (Growth Analyst)
echo - GT-002: Nisha Reddy (SEO Specialist)
echo - GT-003: Arjun Joseph (Growth Marketer)
echo - GT-004: Pooja Thomas (Content Strategist)
echo - GT-005: Vikram Singh (Performance Marketer)
echo.
echo =========================================
echo.
pause
