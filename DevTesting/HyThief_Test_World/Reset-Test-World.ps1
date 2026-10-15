[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$master=Join-Path $root 'Master'
$saves='C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves'
$active=[IO.Path]::GetFullPath((Join-Path $saves 'HyThief Test World'))
if($active -ne 'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Test World'){throw 'Unexpected active path'}
function Assert-Closed {
 $running=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'HytaleClient*' -or $_.Name -like 'HytaleServer*' -or ($_.Name -eq 'java.exe' -and $_.CommandLine -match 'HytaleServer|hytale.server') })
 if($running.Count){throw 'Close Hytale and every Hytale server completely, then run this reset again. No save was changed.'}
}
function Check-Hashes($base,$records){foreach($r in $records){$p=Join-Path $base $r.Relative;if(!(Test-Path -LiteralPath $p) -or (Get-FileHash -LiteralPath $p).Hash -ne $r.Hash){throw ('Checksum mismatch: '+$p)}}}
Assert-Closed
$marker=Get-Content -LiteralPath (Join-Path $master 'hythief-test-world.json') -Raw|ConvertFrom-Json

$installedJar='C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Server\HytaleServer.jar'
$archive=[IO.Compression.ZipFile]::OpenRead($installedJar)
try{$reader=[IO.StreamReader]::new($archive.GetEntry('META-INF/MANIFEST.MF').Open());try{$nativeManifest=$reader.ReadToEnd()}finally{$reader.Dispose()}}finally{$archive.Dispose()}
$nativeVersion=[regex]::Match($nativeManifest,'Implementation-Version: ([^\r\n]+)').Groups[1].Value.Trim()
$nativeRevision=[regex]::Match($nativeManifest,'Implementation-Revision-Id: ([^\r\n]+)').Groups[1].Value.Trim()
if(!$nativeVersion -or !$nativeRevision -or $marker.ValidatedHytaleVersion -cne $nativeVersion -or $marker.ValidatedHytaleRevision -cne $nativeRevision){throw 'General HyThief master does not match installed Hytale. Revalidate before reset. No save changed.'}

if($marker.Kind -ne 'Firebeard.HyThief.DevWorld' -or $marker.Name -ne 'HyThief Test World'){throw 'Invalid pristine master marker'}
$records=Get-Content -LiteralPath (Join-Path $root 'master-checksums.json') -Raw|ConvertFrom-Json
Check-Hashes $master $records
if(@(Get-ChildItem -LiteralPath $master -File -Recurse).Count -ne $records.Count){throw 'Master contains unexpected files'}
foreach($p in @($master,$active)){if((Test-Path -LiteralPath $p) -and ((Get-Item -LiteralPath $p).Attributes -band [IO.FileAttributes]::ReparsePoint)){throw 'Refusing a redirected world directory'}}
foreach($required in @('config.json','client_metadata.json','universe\worlds\default\config.json','hythief-test-world.json')){if(!(Test-Path -LiteralPath (Join-Path $master $required))){throw ('Missing master file '+$required)}}
if(!(Test-Path -LiteralPath (Join-Path $master 'universe\worlds\default\chunks'))){throw 'Saved arena chunks missing'}
$lock=$null
try {
 if(Test-Path -LiteralPath $active){
  $activeMarkerPath=Join-Path $active 'hythief-test-world.json'
  if(!(Test-Path -LiteralPath $activeMarkerPath)){throw 'Refusing to replace an unmarked save'}
  $activeMarker=Get-Content -LiteralPath $activeMarkerPath -Raw|ConvertFrom-Json
  if($activeMarker.Kind -ne $marker.Kind -or ($activeMarker.WorldUUID|ConvertTo-Json -Compress) -ne ($marker.WorldUUID|ConvertTo-Json -Compress)){throw 'Active save identity mismatch'}
  $lockPath=Join-Path $active '.world.lock'
  if(Test-Path -LiteralPath $lockPath){$lock=[IO.File]::Open($lockPath,[IO.FileMode]::Open,[IO.FileAccess]::ReadWrite,[IO.FileShare]::None)}
 }
 $stage=Join-Path $saves ('.HyThief-reset-'+[guid]::NewGuid().ToString('N'))
 Copy-Item -LiteralPath $master -Destination $stage -Recurse
 Check-Hashes $stage $records
 Assert-Closed
 $backup=$null
 if(Test-Path -LiteralPath $active){
  $backup=Join-Path $root ('Archives\'+(Get-Date -Format yyyyMMdd-HHmmss)+'-'+[guid]::NewGuid().ToString('N'))
  New-Item -ItemType Directory -Path (Split-Path $backup -Parent) -Force|Out-Null
  if($lock){$lock.Dispose();$lock=$null}
  Copy-Item -LiteralPath $active -Destination $backup -Recurse
  $dirty=@(Get-ChildItem -LiteralPath $active -File -Recurse|ForEach-Object {@{Relative=[IO.Path]::GetRelativePath($active,$_.FullName);Hash=(Get-FileHash -LiteralPath $_.FullName).Hash}})
  Check-Hashes $backup $dirty
  Assert-Closed
  # Exact fixed target, validated above; no wildcard or computed world enumeration.
  if([IO.Path]::GetFullPath($active) -ne 'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Test World'){throw 'Unsafe deletion target'}
  Remove-Item -LiteralPath $active -Recurse
 }
 try{Move-Item -LiteralPath $stage -Destination $active;Check-Hashes $active $records}
 catch{if($backup -and !(Test-Path -LiteralPath $active)){Copy-Item -LiteralPath $backup -Destination $active -Recurse};throw}
 [pscustomobject]@{Ready=$active;DirtyBackup=$backup;VerifiedFiles=$records.Count;MasterUnchanged=$true}|ConvertTo-Json
}finally{if($lock){$lock.Dispose()}}
