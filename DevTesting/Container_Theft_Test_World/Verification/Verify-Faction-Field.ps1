[CmdletBinding()]
param([switch]$Author,[string]$InputWorld,[ValidateSet('','Kweebec','Feran')][string]$Response='')
$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) -Parent
$root=Join-Path $project 'DevTesting\Container_Theft_Test_World'
$jdk=Join-Path (Split-Path (Split-Path $project -Parent) -Parent) 'Toolchains\JDK25\bin'
$server=Join-Path $project '.build\dependencies\HytaleServer-0.6.8.jar'
$jar=Join-Path $project 'build\libs\HyThief-1.3.1-container.10.jar'
if(!$InputWorld){$InputWorld=Join-Path $root 'Faction-Field-Candidate'}
$run=Join-Path $root ('FactionVerificationRuns\'+(Get-Date -Format yyyyMMdd-HHmmss)+'-'+[guid]::NewGuid().ToString('N'))
if(![IO.Path]::GetFullPath($run).StartsWith($root+'\FactionVerificationRuns\')){throw 'Unsafe verifier path'}
Copy-Item -LiteralPath $InputWorld -Destination $run -Recurse
Copy-Item -LiteralPath $jar -Destination (Join-Path $run 'mods')
$cfg=Get-Content -LiteralPath (Join-Path $run 'config.json') -Raw|ConvertFrom-Json
$cfg.Mods|Add-Member -NotePropertyName 'FirebeardDev:ContainerFactionBuilder' -NotePropertyValue @{Enabled=$true} -Force
$cfg|ConvertTo-Json -Depth 40|Set-Content -LiteralPath (Join-Path $run 'config.json')
$classes=Join-Path $run 'classes';New-Item -ItemType Directory -Path $classes|Out-Null
& (Join-Path $jdk 'javac.exe') -cp ($server+';'+$jar) -d $classes (Join-Path $PSScriptRoot 'FactionFieldBuilder.java') (Join-Path $PSScriptRoot 'ContainerWorldBuilder.java') (Join-Path $PSScriptRoot 'ContainerWitnessTest.java') (Join-Path $PSScriptRoot 'ContainerProbeTest.java') (Join-Path $PSScriptRoot 'ContainerPacketBridgeTest.java') (Join-Path $PSScriptRoot 'PersistentOwnershipIntegrationTest.java')
if($LASTEXITCODE -ne 0){throw 'Verifier compilation failed'}
$zip=[IO.Compression.ZipFile]::OpenRead($jar)
try{foreach($entry in $zip.Entries){if($entry.FullName -match '^com/local/pickpocket/(ContainerRemovalProbe|OwnedContainerRegistry|ContainerVisualWitness|ContainerCaughtNotification|HyThiefStealthAcquisition|HyThiefGuardAlert|ContainerPacketBridge|PersistentContainerOwnership|ContainerOwnershipPolicy).*\.class$'){
 $dest=Join-Path $classes $entry.FullName;New-Item -ItemType Directory -Path (Split-Path $dest -Parent) -Force|Out-Null
 [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$dest,$true)
}}}finally{$zip.Dispose()}
$manifest=@{Group='FirebeardDev';Name='ContainerFactionBuilder';Version='1.0.0';ServerVersion='=0.6.8';Main='com.local.pickpocket.FactionFieldBuilder';Dependencies=@{'Local:HyThief'='*'}}
$manifest|ConvertTo-Json|Set-Content -LiteralPath (Join-Path $classes 'manifest.json')
& (Join-Path $jdk 'jar.exe') --create --file (Join-Path $run 'mods\FactionFieldBuilder.jar') -C $classes .
if($LASTEXITCODE -ne 0){throw 'Verifier packaging failed'}
Push-Location -LiteralPath $run
try{
 & (Join-Path $jdk 'java.exe') ('-Dfactionfield.author='+$Author.IsPresent) ('-Dfactionfield.response='+$Response) -jar $server --assets 'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip' --auth-mode offline --bind 127.0.0.1:15539 --disable-sentry --disable-file-watcher --disable-cpb-build *> 'native.log'
 if($LASTEXITCODE -ne 0 -or !(Test-Path 'faction-verification.json') -or (Select-String -LiteralPath 'native.log' -SimpleMatch 'FACTION_FIELD_FAILED')){throw ('Native faction validation failed: '+$run+'\native.log')}
 if($Response -and !(Select-String -LiteralPath 'native.log' -SimpleMatch 'FACTION_NATIVE_RESPONSE_PASS')){throw 'Native responder failed'}
 Select-String -LiteralPath 'native.log' -Pattern 'FACTION_STATION_PASS|FACTION_NATIVE_RESPONSE_PASS|FACTION_FIELD_VERIFIED'|ForEach-Object Line
 Write-Host ('RUN='+$run)
}finally{Pop-Location}
