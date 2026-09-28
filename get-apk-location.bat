@echo off
echo ====================================
echo   WorkCore APK Location Finder
echo ====================================
echo.

set APK_PATH=app\build\outputs\apk\debug\app-debug.apk

if exist "%APK_PATH%" (
    echo ✓ APK Found!
    echo.
    echo Location:
    echo %CD%\%APK_PATH%
    echo.
    echo Full Path:
    for %%i in ("%APK_PATH%") do echo %%~fi
    echo.
    echo File Size:
    for %%A in ("%APK_PATH%") do echo %%~zA bytes
    echo.
    echo ====================================
    echo Next Steps:
    echo ====================================
    echo 1. Upload this APK to Firebase Storage
    echo 2. Get the download URL
    echo 3. Add URL to Firestore app_config/version_info
    echo.
    echo Opening folder...
    explorer app\build\outputs\apk\debug
) else (
    echo ✗ APK not found!
    echo.
    echo Please build the APK first:
    echo 1. Open project in Android Studio
    echo 2. Build -^> Build Bundle^(s^) / APK^(s^) -^> Build APK^(s^)
    echo 3. Wait for build to complete
    echo 4. Run this script again
)

echo.
pause
