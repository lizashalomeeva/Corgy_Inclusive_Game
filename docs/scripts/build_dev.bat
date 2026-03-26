@echo off
echo ===================================================
echo [DEV] Starting Development Build for Corgy Game...
echo ===================================================

cd ..\..
echo 1. Cleaning project cache...
call gradlew.bat clean

echo 2. Building Debug APK...
call gradlew.bat assembleDebug

echo ===================================================
echo DONE! You can find the APK in: app\build\outputs\apk\debug\
echo ===================================================
pause