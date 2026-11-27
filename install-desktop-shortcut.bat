@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"
title LAN Messenger - Desktop Shortcut Installer

echo =========================================================
echo       LAN MESSENGER - DESKTOP SHORTCUT INSTALLER
echo =========================================================
echo.
echo Creating professional desktop icons...

set "ICON_PATH=%~dp0lan-messenger.ico"
set "DEMO_BAT=%~dp0start-demo.bat"
set "SERVER_BAT=%~dp0start-server.bat"
set "CLIENT_BAT=%~dp0start-client.bat"
set "WORK_DIR=%~dp0"

powershell -Command ^
  "$ws = New-Object -ComObject WScript.Shell; " ^
  "$desktop = [Environment]::GetFolderPath('Desktop'); " ^
  "$s1 = $ws.CreateShortcut(\"$desktop\LAN Messenger (Demo).lnk\"); " ^
  "$s1.TargetPath = '%DEMO_BAT%'; " ^
  "$s1.WorkingDirectory = '%WORK_DIR%'; " ^
  "$s1.IconLocation = '%ICON_PATH%,0'; " ^
  "$s1.Description = 'LAN Messaging Application - One Click Demo'; " ^
  "$s1.Save(); " ^
  "$s2 = $ws.CreateShortcut(\"$desktop\LAN Messenger Client.lnk\"); " ^
  "$s2.TargetPath = '%CLIENT_BAT%'; " ^
  "$s2.WorkingDirectory = '%WORK_DIR%'; " ^
  "$s2.IconLocation = '%ICON_PATH%,0'; " ^
  "$s2.Description = 'LAN Messenger Client Chat Window'; " ^
  "$s2.Save(); " ^
  "$s3 = $ws.CreateShortcut(\"$desktop\LAN Server Console.lnk\"); " ^
  "$s3.TargetPath = '%SERVER_BAT%'; " ^
  "$s3.WorkingDirectory = '%WORK_DIR%'; " ^
  "$s3.IconLocation = '%ICON_PATH%,0'; " ^
  "$s3.Description = 'LAN Messenger Central Server Dashboard'; " ^
  "$s3.Save(); " ^
  "Write-Host '[SUCCESS] 3 Desktop Shortcuts created with custom App Icon!'"

echo.
echo =========================================================
echo Shortcuts installed successfully on your Desktop!
echo Check your Windows Desktop:
echo   - 'LAN Messenger (Demo)'
echo   - 'LAN Messenger Client'
echo   - 'LAN Server Console'
echo =========================================================
echo.
pause
endlocal
