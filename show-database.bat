@echo off
setlocal
cd /d "%~dp0"
title LAN Messenger - SQLite Database Viewer (CMD)

echo =========================================================
echo       LAN MESSENGER - DATABASE VIEWER & SQL CONSOLE
echo =========================================================
echo.

set JAR_PATH=lan-server\target\lan-server-1.0.0-executable.jar

if not exist "%JAR_PATH%" (
    echo [ERROR] Server JAR not found. Please build the project first.
    pause
    exit /b 1
)

java -cp "%JAR_PATH%" com.lanmessenger.server.db.DatabaseViewerMain lan_messenger.db

echo.
pause
endlocal
