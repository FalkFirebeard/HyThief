[CmdletBinding()]
param([string]$DiagnosticJar)
$ErrorActionPreference='Stop'
$project=Split-Path (Split-Path (Split-Path $PSScriptRoot -Parent) -Parent) -Parent
$root=Join-Path $project 'DevTesting\Container_Theft_Test_World'
$jdk=Join-Path (Split-Path (Split-Path $project -Parent) -Parent) 'Toolchains\JDK25\bin'
$server=Join-Path $project '.build\dependencies\HytaleServer-0.6.8.jar'
if(!$DiagnosticJar){$DiagnosticJar=Join-Path $project 'build\libs\HyThief-1.3.1-container.2.jar'}
$run=Join-Path $root ('VerificationRuns\'+(Get-Date -Format yyyyMMdd-HHmmss)+'-'+[guid]::NewGuid().ToString('N'))
if(![IO.Path]::GetFullPath($run).StartsWith($root+'\VerificationRuns\')){throw 'Unsafe verification path'}
New-Item -ItemType Directory -Path (Split-Path $run -Parent) -Force|Out-Null
Copy-Item -LiteralPath (Join-Path $root 'Master') -Destination $run -Recurse
Copy-Item -LiteralPath (Join-Path $root 'Prefabs') -Destination (Join-Path $run 'prefabs') -Recurse
Copy-Item -LiteralPath $DiagnosticJar -Destination (Join-Path $run 'mods')
$cfg=Get-Content (Join-Path $run 'config.json') -Raw|ConvertFrom-Json
$cfg.Mods.'FirebeardDev:ContainerWorldBuilder'.Enabled=$true
$cfg|ConvertTo-Json -Depth 35|Set-Content (Join-Path $run 'config.json')
$classes=Join-Path $run 'verification-classes'
New-Item -ItemType Directory -Path $classes|Out-Null
& (Join-Path $jdk 'javac.exe') -cp ($server+';'+$DiagnosticJar) -d $classes (Join-Path $PSScriptRoot 'ContainerWorldBuilder.java') (Join-Path $PSScriptRoot 'ContainerProbeTest.java')
if($LASTEXITCODE -ne 0){throw 'Verifier compilation failed'}
$zip=[IO.Compression.ZipFile]::OpenRead($DiagnosticJar)
try{foreach($entry in $zip.Entries){
 if($entry.FullName -match '^com/local/pickpocket/(ContainerRemovalProbe|OwnedContainerRegistry).*\.class$'){
  $dest=Join-Path $classes $entry.FullName
  New-Item -ItemType Directory -Path (Split-Path $dest -Parent) -Force|Out-Null
  [IO.Compression.ZipFileExtensions]::ExtractToFile($entry,$dest,$true)
 }
}}finally{$zip.Dispose()}
& (Join-Path $jdk 'jar.exe') --create --file (Join-Path $run 'mods\ContainerWorldBuilder.jar') -C $classes . -C $PSScriptRoot manifest.json
if($LASTEXITCODE -ne 0){throw 'Verifier packaging failed'}
Push-Location -LiteralPath $run
try{
 & (Join-Path $jdk 'java.exe') '-Dcontainerworld.reload=true' '-Dcontainerworld.pristine=true' -jar $server --assets 'C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Assets.zip' --auth-mode offline --bind 127.0.0.1:15539 --disable-sentry --disable-file-watcher --disable-cpb-build *> 'native.log'
 if($LASTEXITCODE -ne 0 -or !(Test-Path 'reload-verification.json')){throw ('Native verification failed; inspect '+$run+'\native.log')}
 if(!(Select-String -LiteralPath 'native.log' -SimpleMatch 'Action=AUTO_REGISTER Owner=Kweebec')){throw 'No real plugin automatic registration'}
 if(Select-String -LiteralPath 'native.log' -Pattern 'AUTO_REGISTER_ERROR|CONTAINER_WORLD_FAILED'){throw 'Native validation error'}
 Select-String -LiteralPath 'native.log' -Pattern 'AUTO_REGISTER|REAL_CHEST_PROBE_PASS|REAL_CONTROL_TRANSFER_PASS|CONTAINER_WORLD_VERIFIED'|ForEach-Object Line
 Write-Host ('Verified disposable fresh-master load: '+$run)
}finally{Pop-Location}
