$ErrorActionPreference='Stop'
$python='C:\Users\askin\.cache\codex-runtimes\codex-primary-runtime\dependencies\python\python.exe'
$root='E:\Hytale adjustments\Mod Projects\HyThief\DevTesting\Persistent_Container_Ownership_Proof'
$run=Join-Path $root ('Runs\'+[DateTime]::UtcNow.ToString('yyyyMMdd-HHmmss-fffffff'))
# New output only; never clear or reset a previous proof or any playable world.
& $python (Join-Path $PSScriptRoot 'prepare.py') $run
if($LASTEXITCODE -ne 0){throw 'Proof preparation failed'}
& $python (Join-Path $PSScriptRoot 'run.py') $run
if($LASTEXITCODE -ne 0){throw 'Native ownership proof failed; logs/save retained'}
Write-Output ('Ownership proof passed: '+$run)
