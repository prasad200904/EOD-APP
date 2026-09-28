@echo off
echo =========================================
echo WorkCore APK Builder
echo =========================================
echo.

echo Building APK...
echo.

REM Build the APK using Gradle
call gradlew.bat assembleDebug

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo ✗ Build failed!
    echo Check Android Studio for errors.
    pause
    exit /b 1
)

echo.
echo =========================================
echo ✓ Build successful!
echo =========================================
echo.

REM Define paths
set SOURCE_APK=app\build\outputs\apk\debug\app-debug.apk
set DESKTOP=%USERPROFILE%\Desktop
set OUTPUT_NAME=WorkCore-v1.0.apk

REM Check if APK exists
if not exist "%SOURCE_APK%" (
    echo ✗ APK file not found at: %SOURCE_APK%
    pause
    exit /b 1
)

REM Copy to Desktop
echo Copying APK to Desktop...
copy "%SOURCE_APK%" "%DESKTOP%\%OUTPUT_NAME%"

if %ERRORLEVEL% NEQ 0 (
    echo ✗ Failed to copy APK to Desktop
    pause
    exit /b 1
)

echo.
echo =========================================
echo ✓ SUCCESS!
echo =========================================
echo.
echo APK saved to: %DESKTOP%\%OUTPUT_NAME%
echo File size: 
dir "%DESKTOP%\%OUTPUT_NAME%" | find "%OUTPUT_NAME%"
echo.
echo You can now:
echo 1. Transfer to phone via USB
echo 2. Upload to Google Drive
echo 3. Share via email/WhatsApp
echo 4. Install on multiple devices
echo.
echo Original APK also at:
echo %SOURCE_APK%
echo.

REM Open Desktop folder
start "" "%DESKTOP%"

pause
