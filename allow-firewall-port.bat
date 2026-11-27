@echo off
setlocal
cd /d "%~dp0"
title Open Port 5000 for LAN Messenger in Windows Firewall

echo =========================================================
echo      WINDOWS FIREWALL RULE CONFIGURATION FOR PORT 5000
echo =========================================================
echo.
echo Adding inbound firewall rule to allow other computers on your
echo Wi-Fi/LAN to connect to this server on TCP Port 5000...
echo.

netsh advfirewall firewall add rule name="LAN Messenger Port 5000" dir=in action=allow protocol=TCP localport=5000

if %ERRORLEVEL% equ 0 (
    echo.
    echo [SUCCESS] Inbound rule successfully added to Windows Firewall!
    echo Other computers on your LAN can now connect to your local IP on port 5000.
) else (
    echo.
    echo [ERROR] Failed to add firewall rule.
    echo Please make sure you right-click this script and select 'Run as administrator'.
)

echo.
pause
endlocal
