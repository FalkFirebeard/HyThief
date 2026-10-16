$ErrorActionPreference='Stop'
$root='E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Container_Theft_Test_World'
$checkpoint=Join-Path $root 'Stationary_Witness_0_7_2'
$shortcut=Join-Path ([Environment]::GetFolderPath('Desktop')) 'Reset HyThief Container Test World.lnk'
$shell=New-Object -ComObject WScript.Shell
$link=$shell.CreateShortcut($shortcut)
if($link.TargetPath -cne (Join-Path $root 'Reset-Container-Theft.cmd') -or $link.WorkingDirectory -cne $root -or $link.Arguments){throw 'Unexpected desktop reset target; do not launch'}
$linkhash=(Get-FileHash -LiteralPath $shortcut).Hash
$env:FIREBEARD_RESET_NO_PAUSE='1'
$env:FIREBEARD_RESET_AUDIT_LOG=Join-Path $checkpoint 'actual-desktop-reset.log'
try{
 $process=Start-Process -FilePath $shortcut -WindowStyle Hidden -PassThru
 if($process -and !$process.WaitForExit(30000)){throw 'Desktop reset did not finish within 30 seconds'}
}finally{Remove-Item Env:FIREBEARD_RESET_NO_PAUSE -ErrorAction SilentlyContinue;Remove-Item Env:FIREBEARD_RESET_AUDIT_LOG -ErrorAction SilentlyContinue}
$log=Get-Content -LiteralPath (Join-Path $checkpoint 'actual-desktop-reset.log') -Raw
if($log -notmatch 'HyThief Container Theft reset: OK'){throw ('Desktop reset failed: '+$log)}
$active='C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Container Theft Test World'
$records=@(Get-Content -LiteralPath (Join-Path $root 'master-checksums.json') -Raw|ConvertFrom-Json)
foreach($r in $records){if((Get-FileHash -LiteralPath (Join-Path $active $r.Relative)).Hash -cne $r.Hash){throw ('Active mismatch '+$r.Relative)}}
if(@(Get-ChildItem -LiteralPath $active -File -Recurse -Force).Count -ne $records.Count){throw 'Unexpected active files'}
if((Get-FileHash -LiteralPath $shortcut).Hash -cne $linkhash){throw 'Shortcut changed'}
foreach($name in @('Reset-Container-Theft.cmd','Reset-Container-Theft.ps1')){if((Get-FileHash -LiteralPath (Join-Path $root $name)).Hash -cne (Get-FileHash -LiteralPath (Join-Path $checkpoint ('Rollback\'+$name))).Hash){throw 'Existing reset script changed'}}
[pscustomobject]@{ActualDesktopShortcut=$shortcut;Target=$link.TargetPath;WorkingDirectory=$link.WorkingDirectory;ShortcutUnchanged=$true;ScriptsUnchanged=$true;Active=$active;ActiveMatchesMaster=$true;VerifiedFiles=$records.Count;NativeTicking=(Get-Content -LiteralPath (Join-Path $active 'universe\worlds\default\config.json') -Raw|ConvertFrom-Json).IsTicking}|ConvertTo-Json|Set-Content -LiteralPath (Join-Path $checkpoint 'desktop-reset-verification.json')
Get-Content -LiteralPath (Join-Path $checkpoint 'desktop-reset-verification.json')
