@echo off
setlocal
set "PWSH=pwsh.exe"
where pwsh.exe >nul 2>nul
if errorlevel 1 set "PWSH=C:\Users\askin\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\powershell\pwsh.exe"
if not exist "%PWSH%" if not "%PWSH%"=="pwsh.exe" (
 echo PowerShell 7 is missing. Ask Dex to run Reset-Test-World.ps1.
 pause
 exit /b 1
)
"%PWSH%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-Test-World.ps1"
if errorlevel 1 (
 echo Reset stopped. Read the error above; do not launch the world until resolved.
) else (
 echo HyThief Test World is ready. Launch Hytale and select it.
)
pause
