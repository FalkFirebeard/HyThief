[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$master=Join-Path $root 'Master'
$saves='C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves'
$active=[IO.Path]::GetFullPath((Join-Path $saves 'HyThief Container Theft Test World'))
if($active -cne 'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Container Theft Test World'){throw 'Unexpected active path'}
function Assert-Closed {
 $running=@(Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'HytaleClient*' -or $_.Name -like 'HytaleServer*' -or ($_.Name -eq 'java.exe' -and $_.CommandLine -match 'HytaleServer|hytale.server') })
 if($running.Count){throw 'Close Hytale and all Hytale servers before resetting. No save changed.'}
}
function Check-Hashes($base,$records){foreach($r in $records){$p=Join-Path $base $r.Relative;if(!(Test-Path -LiteralPath $p) -or (Get-FileHash -LiteralPath $p).Hash -ne $r.Hash){throw ('Checksum mismatch: '+$p)}}}
function Check-NoRedirect($base){if(Test-Path -LiteralPath $base){$items=@(Get-Item -LiteralPath $base)+@(Get-ChildItem -LiteralPath $base -Recurse -Force);if(@($items|Where-Object {$_.Attributes -band [IO.FileAttributes]::ReparsePoint}).Count){throw ('Refusing redirected directory: '+$base)}}}
Assert-Closed
Check-NoRedirect $master
Check-NoRedirect $active
$marker=Get-Content -LiteralPath (Join-Path $master 'container-theft-test-world.json') -Raw|ConvertFrom-Json
if($marker.Kind -ne 'Firebeard.HyThief.ContainerDevWorld' -or $marker.Name -ne 'HyThief Container Theft Test World'){throw 'Invalid master marker'}
$liveServer='C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Server\HytaleServer.jar'
$zip=[IO.Compression.ZipFile]::OpenRead($liveServer)
try{$reader=[IO.StreamReader]::new($zip.GetEntry('META-INF/MANIFEST.MF').Open());try{$installed=$reader.ReadToEnd()}finally{$reader.Dispose()}}finally{$zip.Dispose()}
$installedVersion=[regex]::Match($installed,'Implementation-Version: ([^\r\n]+)').Groups[1].Value.Trim()
$installedRevision=[regex]::Match($installed,'Implementation-Revision-Id: ([^\r\n]+)').Groups[1].Value.Trim()
$fixture=Get-Content -LiteralPath (Join-Path $master 'mods\ContainerTheftFixture\manifest.json') -Raw|ConvertFrom-Json
if(!$installedVersion -or !$installedRevision -or $fixture.ServerVersion -cne ('='+$installedVersion) -or $marker.ValidatedHytaleVersion -cne $installedVersion -or $marker.ValidatedHytaleRevision -cne $installedRevision){throw 'Container master target/revision differs from installed Hytale. Audit and revalidate the fixture before resetting. No save changed.'}
$nativeServer=Get-Content -LiteralPath (Join-Path $master 'config.json') -Raw|ConvertFrom-Json
$nativeWorld=Get-Content -LiteralPath (Join-Path $master 'universe\worlds\default\config.json') -Raw|ConvertFrom-Json
if($nativeServer.Version -ne 4 -or $nativeWorld.Version -ne 4){throw 'Native save codec version differs from the verified current codecs. No save changed.'}
foreach($mf in Get-ChildItem -LiteralPath (Join-Path $master 'mods') -Filter manifest.json -Recurse -File){
 $pack=Get-Content -LiteralPath $mf.FullName -Raw|ConvertFrom-Json
 if($pack.ServerVersion -cne ('='+$installedVersion)){throw ('Embedded fixture pack target mismatch: '+$mf.FullName+'. No save changed.')}
}
foreach($file in Get-ChildItem -LiteralPath (Join-Path $master 'mods') -File|Where-Object {$_.Extension -in '.zip','.jar'}){
 $archive=[IO.Compression.ZipFile]::OpenRead($file.FullName)
 try{
  $entry=$archive.GetEntry('manifest.json');if(!$entry){throw ('Fixture archive manifest missing: '+$file.Name)}
  $reader=[IO.StreamReader]::new($entry.Open());try{$pack=$reader.ReadToEnd()|ConvertFrom-Json}finally{$reader.Dispose()}
  if($pack.ServerVersion -cne ('='+$installedVersion)){throw ('Embedded fixture pack target mismatch: '+$file.Name+'. No save changed.')}
 }finally{$archive.Dispose()}
}
$records=@(Get-Content -LiteralPath (Join-Path $root 'master-checksums.json') -Raw|ConvertFrom-Json)
Check-Hashes $master $records
if(@(Get-ChildItem -LiteralPath $master -File -Recurse -Force).Count -ne $records.Count){throw 'Unexpected master files'}
if(!(Test-Path -LiteralPath (Join-Path $master 'universe\worlds\default\chunks'))){throw 'Master saved chunks missing'}
if(Test-Path -LiteralPath $active){
 $mp=Join-Path $active 'container-theft-test-world.json'
 if(!(Test-Path -LiteralPath $mp)){throw 'Refusing to replace an unmarked save'}
 $am=Get-Content -LiteralPath $mp -Raw|ConvertFrom-Json
 if($am.Kind -ne $marker.Kind -or ($am.WorldUUID|ConvertTo-Json -Compress) -ne ($marker.WorldUUID|ConvertTo-Json -Compress)){throw 'Active world identity mismatch'}
}
$stage=Join-Path $saves ('.HyThief-container-reset-'+[guid]::NewGuid().ToString('N'))
Copy-Item -LiteralPath $master -Destination $stage -Recurse
Check-Hashes $stage $records
Assert-Closed
$backup=$null
if(Test-Path -LiteralPath $active){
 $backup=Join-Path $root ('Archives\'+(Get-Date -Format yyyyMMdd-HHmmss)+'-'+[guid]::NewGuid().ToString('N'))
 New-Item -ItemType Directory -Path (Split-Path $backup -Parent) -Force|Out-Null
 Copy-Item -LiteralPath $active -Destination $backup -Recurse
 $dirty=@(Get-ChildItem -LiteralPath $active -File -Recurse -Force|ForEach-Object {@{Relative=[IO.Path]::GetRelativePath($active,$_.FullName);Hash=(Get-FileHash -LiteralPath $_.FullName).Hash}})
 Check-Hashes $backup $dirty
 Assert-Closed
 if([IO.Path]::GetFullPath($active) -cne 'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Container Theft Test World'){throw 'Unsafe removal target'}
 Remove-Item -LiteralPath $active -Recurse
}
try{Move-Item -LiteralPath $stage -Destination $active;Check-Hashes $active $records}
catch{if($backup -and !(Test-Path -LiteralPath $active)){Copy-Item -LiteralPath $backup -Destination $active -Recurse};throw}
Write-Host 'HyThief Container Theft reset: OK'
[pscustomobject]@{Ready=$active;DirtyBackup=$backup;VerifiedFiles=$records.Count;MasterUnchanged=$true}|ConvertTo-Json
