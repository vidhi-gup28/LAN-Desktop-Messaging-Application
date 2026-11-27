@echo off
setlocal
cd /d "%~dp0"
title LAN Messaging Server - Control Center

echo =========================================================
echo               LAN MESSAGING SERVER
echo =========================================================
echo Checking Java environment...

set "JAVA_CMD=java"

if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe" (
    set "JAVA_CMD=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe"
    set "PATH=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin;%PATH%"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe" (
    set "JAVA_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin;%PATH%"
)

set JAR_PATH=lan-server\target\lan-server-1.0.0-executable.jar

if not exist "%JAR_PATH%" (
    echo [INFO] Server executable JAR not found. Building with Maven...
    call build.bat
    if not exist "%JAR_PATH%" (
        echo [ERROR] Build failed or JAR not created.
        pause
        exit /b 1
    )
)

echo Starting LAN Server Dashboard...
"%JAVA_CMD%" -jar "%JAR_PATH%" %*

endlocal
