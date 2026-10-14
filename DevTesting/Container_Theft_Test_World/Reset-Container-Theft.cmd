@echo off
setlocal
set "PWSH=C:\Users\askin\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\powershell\pwsh.exe"
if not exist "%PWSH%" set "PWSH=pwsh.exe"
if defined FIREBEARD_RESET_AUDIT_LOG goto audit
"%PWSH%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-Container-Theft.ps1"
goto done
:audit
"%PWSH%" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-Container-Theft.ps1" > "%FIREBEARD_RESET_AUDIT_LOG%" 2>&1
:done
set "RESULT=%ERRORLEVEL%"
if defined FIREBEARD_RESET_NO_PAUSE goto finish
if /i not "%~1"=="--no-pause" pause
:finish
exit /b %RESULT%
