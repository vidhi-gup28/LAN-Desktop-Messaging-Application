@echo off
setlocal
cd /d "%~dp0"

set "JAVA_CMD=java"
set "JAVAW_CMD=javaw"

if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe" (
    set "JAVA_CMD=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe"
    set "JAVAW_CMD=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\javaw.exe"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe"
    set "JAVAW_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\javaw.exe"
)

netstat -ano | findstr :5000 | findstr LISTENING >nul 2>&1
if %ERRORLEVEL% neq 0 (
    start "" "%JAVAW_CMD%" -jar "lan-server\target\lan-server-1.0.0-executable.jar"
    ping 127.0.0.1 -n 3 >nul
)

start "" "%JAVAW_CMD%" -jar "lan-client\target\lan-client-1.0.0-executable.jar"
start "" "%JAVAW_CMD%" -jar "lan-client\target\lan-client-1.0.0-executable.jar"
endlocal
