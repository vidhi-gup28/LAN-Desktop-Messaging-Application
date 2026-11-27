@echo off
setlocal
cd /d "%~dp0"
title LAN Messenger - Clear History & Reset Users

echo =========================================================
echo       CLEAR CHAT HISTORY & RESET USERS DATABASE
echo =========================================================
echo.
echo This script will permanently delete all chat history,
echo registered user accounts, and groups from SQLite.
echo.
echo Press 'Y' to confirm, or any other key to cancel.
set /p CONFIRM="Are you sure you want to proceed? (Y/N): "

if /i "%CONFIRM%" neq "Y" (
    echo [INFO] Operation cancelled by user.
    pause
    exit /b 0
)

echo.
echo Stopping any running server or client instances if active...
taskkill /f /im java.exe /fi "WINDOWTITLE eq LAN Messaging Server*" >nul 2>&1
taskkill /f /im javaw.exe >nul 2>&1

timeout /t 1 /nobreak >nul

echo Deleting SQLite database files...
if exist "lan_messenger.db" (
    del /f /q "lan_messenger.db"
    echo [OK] Removed lan_messenger.db
)
if exist "lan_messenger.db-wal" (
    del /f /q "lan_messenger.db-wal"
    echo [OK] Removed lan_messenger.db-wal
)
if exist "lan_messenger.db-shm" (
    del /f /q "lan_messenger.db-shm"
    echo [OK] Removed lan_messenger.db-shm
)

echo.
echo =========================================================
echo [SUCCESS] Chat history and all old users have been CLEARED!
echo When you start the server next time, a fresh, clean
echo database will automatically be created.
echo =========================================================
echo.
pause
endlocal
