@echo off
setlocal
cd /d "%~dp0"
title Launch Full LAN Messenger Demo

set "JAVA_CMD=java"
set "JAVAW_CMD=javaw"

if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe" (
    set "JAVA_CMD=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe"
    set "JAVAW_CMD=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\javaw.exe"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe"
    set "JAVAW_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\javaw.exe"
)

echo =========================================================
echo       LAN MESSAGING APPLICATION - ONE CLICK DEMO
echo =========================================================
echo.
echo 1. Launching LAN Server Console...
start "LAN Server Console" cmd /k "call start-server.bat"

echo.
echo Waiting 2 seconds for server to start listening on port 5000...
ping 127.0.0.1 -n 3 >nul

echo.
echo 2. Launching Client 1 (e.g. Alice)...
start "" "%JAVAW_CMD%" -jar "lan-client\target\lan-client-1.0.0-executable.jar"

echo.
echo 3. Launching Client 2 (e.g. Bob)...
start "" "%JAVAW_CMD%" -jar "lan-client\target\lan-client-1.0.0-executable.jar"

echo.
echo =========================================================
echo Both Server and 2 Clients are running!
echo In Client 1: Type Alice and click Connect.
echo In Client 2: Type Bob and click Connect.
echo =========================================================
endlocal
