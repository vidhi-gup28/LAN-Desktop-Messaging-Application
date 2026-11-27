@echo off
setlocal
cd /d "%~dp0"
title LAN Messenger - Client

echo =========================================================
echo               LAN MESSENGER CLIENT
echo =========================================================
echo Checking Java environment...

set "JAVAW_CMD=javaw"

if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\java.exe" (
    set "JAVAW_CMD=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin\javaw.exe"
    set "PATH=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-17\bin;%PATH%"
) else if exist "C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\java.exe" (
    set "JAVAW_CMD=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin\javaw.exe"
    set "PATH=C:\Program Files\Eclipse Adoptium\jdk-17.0.21.10002-hotspot\bin;%PATH%"
)

set JAR_PATH=lan-client\target\lan-client-1.0.0-executable.jar

if not exist "%JAR_PATH%" (
    echo [INFO] Client executable JAR not found. Building with Maven...
    call build.bat
    if not exist "%JAR_PATH%" (
        echo [ERROR] Build failed or JAR not created.
        pause
        exit /b 1
    )
)

echo Starting LAN Messenger Desktop Application...
start "" "%JAVAW_CMD%" -jar "%JAR_PATH%" %*

endlocal
