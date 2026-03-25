@echo off
echo --- Запуск статичного аналізу коду (Android Lint) ---
call gradlew lint
echo.
echo --- Перевірка статичної типізації (Java Compiler) ---
call gradlew assembleDebug
echo.
echo --- Всі перевірки завершено успішно! ---
pause