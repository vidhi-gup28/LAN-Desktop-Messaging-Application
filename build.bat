@echo off
setlocal
cd /d "%~dp0"
title Build LAN Messaging Application

echo =========================================================
echo       BUILDING LAN MESSAGING APPLICATION (MAVEN)
echo =========================================================

set MVN_CMD=mvn
where mvn >nul 2>&1
if %ERRORLEVEL% neq 0 (
    if exist "C:\Users\abc\.maven\maven-3.9.12\bin\mvn.cmd" (
        set MVN_CMD="C:\Users\abc\.maven\maven-3.9.12\bin\mvn.cmd"
    ) else (
        echo [ERROR] Maven not found in PATH or standard location.
        echo Please ensure Apache Maven is installed.
        pause
        exit /b 1
    )
)

echo Using Maven: %MVN_CMD%
call %MVN_CMD% clean package

if %ERRORLEVEL% equ 0 (
    echo.
    echo =========================================================
    echo [SUCCESS] Build completed!
    echo Server JAR: lan-server\target\lan-server-1.0.0-executable.jar
    echo Client JAR: lan-client\target\lan-client-1.0.0-executable.jar
    echo =========================================================
) else (
    echo.
    echo [ERROR] Build failed. Please check errors above.
    pause
    exit /b 1
)

endlocal
