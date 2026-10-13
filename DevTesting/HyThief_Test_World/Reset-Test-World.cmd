@echo off
setlocal
set "PWSH=C:\Users\askin\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\powershell\pwsh.exe"
if not exist "%PWSH%" set "PWSH=pwsh.exe"
"%PWSH%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-Test-World.ps1"
set "RESULT=%ERRORLEVEL%"
if /i not "%~1"=="--no-pause" pause
exit /b %RESULT%
