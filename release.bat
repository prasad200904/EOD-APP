@echo off
echo ================================================
echo   WorkCore - Release Script
echo ================================================
echo.

REM ── Step 1: Read current version ────────────────
for /f "tokens=2 delims==" %%a in ('findstr "VERSION_MAJOR" version.properties') do set MAJOR=%%a
for /f "tokens=2 delims==" %%a in ('findstr "VERSION_MINOR" version.properties') do set MINOR=%%a
for /f "tokens=2 delims==" %%a in ('findstr "VERSION_PATCH" version.properties') do set PATCH=%%a
for /f "tokens=2 delims==" %%a in ('findstr "VERSION_CODE" version.properties') do set CODE=%%a

echo Current version: %MAJOR%.%MINOR%.%PATCH% (code %CODE%)
echo.

REM ── Step 2: Ask what changed ────────────────────
set /p NOTES=What changed in this release? 

REM ── Step 3: Bump version ────────────────────────
set /p BUMP=Bump version? [1=patch  2=minor  3=major  ENTER=keep]: 

if "%BUMP%"=="1" (
  set /a PATCH=%PATCH%+1
)
if "%BUMP%"=="2" (
  set /a MINOR=%MINOR%+1
  set PATCH=0
)
if "%BUMP%"=="3" (
  set /a MAJOR=%MAJOR%+1
  set MINOR=0
  set PATCH=0
)
set /a CODE=%CODE%+1

set VERSION=%MAJOR%.%MINOR%.%PATCH%
set TAG=v%VERSION%

echo.
echo New version: %VERSION% (code %CODE%)
echo Tag: %TAG%
echo.

REM ── Step 4: Update version.properties ──────────
echo # WorkCore Version Management > version.properties
echo # This file is automatically updated on each build >> version.properties
echo. >> version.properties
echo # Major.Minor.Patch version ^(e.g., 1.2.5^) >> version.properties
echo VERSION_MAJOR=%MAJOR% >> version.properties
echo VERSION_MINOR=%MINOR% >> version.properties
echo VERSION_PATCH=%PATCH% >> version.properties
echo. >> version.properties
echo # Build number ^(auto-incremented^) >> version.properties
echo VERSION_CODE=%CODE% >> version.properties
echo. >> version.properties
echo # Release notes for next version >> version.properties
echo RELEASE_NOTES=%NOTES% >> version.properties

echo [OK] version.properties updated
echo.

REM ── Step 5: Check APK exists ────────────────────
set APK_PATH=app\release\app-release.apk
if not exist "%APK_PATH%" (
  echo [ERROR] APK not found at: %APK_PATH%
  echo.
  echo Please build the APK first in Android Studio:
  echo   Build ^> Generate Signed Bundle / APK ^> APK ^> release ^> Finish
  echo.
  pause
  exit /b 1
)
echo [OK] APK found: %APK_PATH%
echo.

REM ── Step 6: Copy APK with version name ──────────
copy "%APK_PATH%" "app\release\WorkCore-%VERSION%.apk" >nul
echo [OK] APK copied as WorkCore-%VERSION%.apk
echo.

REM ── Step 7: Git commit and push ─────────────────
echo Committing to Git...
git add version.properties CHANGELOG.md
git commit -m "release: WorkCore %VERSION% (code %CODE%) - %NOTES%"
git push origin main
echo [OK] Pushed to GitHub
echo.

REM ── Step 8: Create GitHub release with gh CLI ───
where gh >nul 2>&1
if %errorlevel%==0 (
  echo Creating GitHub release...
  gh release create "%TAG%" ^
    "app\release\WorkCore-%VERSION%.apk#WorkCore-%VERSION%.apk" ^
    --title "WorkCore %VERSION%" ^
    --notes "## WorkCore %VERSION%

### Changes
%NOTES%

### Install
1. Download WorkCore-%VERSION%.apk below
2. Settings ^> Security ^> Unknown sources ^> Enable
3. Open APK ^> Install

### Version Info
- Version: %VERSION%
- Build Code: %CODE%"
  echo [OK] GitHub release created!
  echo.
  echo Download link:
  echo https://github.com/prasad200904/EOD-APP/releases/download/%TAG%/WorkCore-%VERSION%.apk
  echo.
  echo Release page:
  echo https://github.com/prasad200904/EOD-APP/releases/latest
) else (
  echo [!] GitHub CLI ^(gh^) not installed.
  echo     Uploading APK manually is needed.
  echo     See instructions below.
  echo.
  echo ── MANUAL STEPS ──────────────────────────────
  echo 1. Go to: https://github.com/prasad200904/EOD-APP/releases/new
  echo 2. Tag: %TAG%
  echo 3. Title: WorkCore %VERSION%
  echo 4. Upload: app\release\WorkCore-%VERSION%.apk
  echo 5. Click Publish release
  echo ──────────────────────────────────────────────
)

echo.
echo ── FIRESTORE UPDATE ──────────────────────────
echo Update these values in Firebase Console:
echo   Collection: app_config
echo   Document:   version_info
echo   versionCode:  %CODE%
echo   versionName:  "%VERSION%"
echo   downloadUrl:  "https://github.com/prasad200904/EOD-APP/releases/download/%TAG%/WorkCore-%VERSION%.apk"
echo ──────────────────────────────────────────────
echo.
echo Done! WorkCore %VERSION% is ready to share.
pause
