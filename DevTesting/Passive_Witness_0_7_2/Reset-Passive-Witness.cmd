@echo off
setlocal
echo HyThief passive personal-witness proof
echo 1 Goblin Scrapper
echo 2 Trork Warrior
echo 3 Outlander Brute
echo 4 Skeleton Fighter
echo 5 Zombie
set /p "PICK=Choose family (1-5): "
set "ROLE="
if "%PICK%"=="1" set "ROLE=Goblin_Scrapper"
if "%PICK%"=="2" set "ROLE=Trork_Warrior"
if "%PICK%"=="3" set "ROLE=Outlander_Brute"
if "%PICK%"=="4" set "ROLE=Skeleton_Fighter"
if "%PICK%"=="5" set "ROLE=Zombie"
if not defined ROLE goto invalid
"C:\Users\askin\.cache\codex-runtimes\codex-primary-runtime\dependencies\native\powershell\pwsh.exe" -NoProfile -ExecutionPolicy Bypass -File "%~dp0Reset-Passive-Witness.ps1" -Role "%ROLE%"
set "RESULT=%ERRORLEVEL%"
pause
exit /b %RESULT%
:invalid
echo No valid selection. Nothing reset.
pause
exit /b 1
