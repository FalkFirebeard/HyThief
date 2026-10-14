[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$root=Split-Path -Parent $PSScriptRoot
$project=[IO.Path]::GetFullPath((Join-Path $root '..\..'))
$run=Join-Path $root ('.verification\Current-'+(Get-Date -Format yyyyMMdd-HHmmss)+'-'+[guid]::NewGuid().ToString('N'))
$server=Join-Path $project '.build\dependencies\HytaleServer-0.7.1.jar'
$jdk='E:\Hytale adjustments\Toolchains\JDK25\bin'
$public=Join-Path $project '.build\release-candidates\1.3.2-0.7.1\HyThief-1.3.2.jar'
foreach($path in @($server,$public,(Join-Path $root 'WorldVerifier.java'))){if(!(Test-Path -LiteralPath $path)){throw "Missing validation dependency: $path"}}
Copy-Item -LiteralPath (Join-Path $root 'Master') -Destination $run -Recurse
New-Item -ItemType Directory -Path (Join-Path $run 'classes'),(Join-Path $run 'mods') -Force|Out-Null
& "$jdk\javac.exe" -cp $server -d (Join-Path $run 'classes') (Join-Path $root 'WorldVerifier.java')
if($LASTEXITCODE){throw 'World verifier compile failed'}
@{Group='FirebeardDev';Name='WorldVerifier';Version='1.0.0';Main='com.firebeard.hythief.dev.WorldVerifier';ServerVersion='=0.7.1';Dependencies=@{'Hytale:BuilderTools'='*';'Local:HyThief'='*'}}|ConvertTo-Json -Depth 8|Set-Content -LiteralPath (Join-Path $run 'classes\manifest.json')
& "$jdk\jar.exe" --create --file (Join-Path $run 'mods\WorldVerifier.jar') -C (Join-Path $run 'classes') .
if($LASTEXITCODE){throw 'World verifier package failed'}
Copy-Item -LiteralPath $public -Destination (Join-Path $run 'mods')
$cfg=Get-Content -LiteralPath (Join-Path $run 'config.json') -Raw|ConvertFrom-Json
$cfg.Mods|Add-Member -NotePropertyName 'FirebeardDev:WorldVerifier' -NotePropertyValue @{Enabled=$true} -Force
$cfg|ConvertTo-Json -Depth 40|Set-Content -LiteralPath (Join-Path $run 'config.json')
Push-Location -LiteralPath $run
try{& "$jdk\java.exe" -jar $server --assets 'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip' --auth-mode offline --bind 127.0.0.1:15529 --disable-sentry --disable-file-watcher --disable-cpb-build --boot-command hythiefworld-verify *> (Join-Path $run 'native.log')}finally{Pop-Location}
$log=Join-Path $run 'native.log'
if(!(Select-String -LiteralPath $log -Pattern 'WORLD_VERIFIED count=16' -Quiet) -or !(Select-String -LiteralPath $log -Pattern 'CELL_GEOMETRY_PASS' -Quiet)){throw "Native world check failed: $log"}
if(Select-String -LiteralPath $log -Pattern 'nonexistent role|Failed to validate|WORLD_FAILED' -Quiet){throw "Obsolete asset/role errors: $log"}
if(Compare-Object (Get-Content -LiteralPath (Join-Path $root 'expected-npcs.txt')) (Get-Content -LiteralPath (Join-Path $run 'verified-npcs.txt'))){throw 'NPC role/UUID mismatch'}
[pscustomobject]@{NativePassed=$true;NPCs=16;Bays=16;MasterUntouched=$true;Run=$run;RealClientPending=$true}|ConvertTo-Json
