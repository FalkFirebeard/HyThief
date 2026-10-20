@echo off
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-01-HyThief.ps1"
exit /b %errorlevel%
