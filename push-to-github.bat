@echo off
setlocal
cd /d "%~dp0"
title Push LAN Messenger to GitHub

echo =========================================================
echo       PUSH LAN MESSAGING APPLICATION TO GITHUB
echo =========================================================
echo.

git status
echo.
set /p COMMIT_MSG="Enter commit message (or press Enter for default): "
if "%COMMIT_MSG%"=="" set COMMIT_MSG=Update LAN Messaging Application

echo.
echo Staging changes...
git add -A

echo Committing...
git commit -m "%COMMIT_MSG%"

echo.
echo Pushing to GitHub (main)...
git push origin main

echo.
if %ERRORLEVEL% equ 0 (
    echo =========================================================
    echo [SUCCESS] Code successfully pushed to GitHub!
    echo URL: https://github.com/vidhi-gup28/LAN-Desktop-Messaging-Application
    echo =========================================================
) else (
    echo [ERROR] Push failed. Please check internet connection or Git credentials.
)

pause
endlocal
