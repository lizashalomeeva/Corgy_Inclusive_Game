@echo off
echo ===================================================
echo [PROD] Starting Production Release Build...
echo ===================================================

cd ..\..
echo 1. Cleaning project cache...
call gradlew.bat clean

echo 2. Running Linters and Tests...
call gradlew.bat lint test

echo 3. Building Release AAB for Google Play...
call gradlew.bat bundleRelease

echo ===================================================
echo DONE! The AAB file is ready at: app\build\outputs\bundle\release\
echo ===================================================
pause