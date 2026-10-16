param([ValidateSet('Goblin_Scrapper','Trork_Warrior','Outlander_Brute','Skeleton_Fighter','Zombie')][string]$Role='Trork_Warrior')
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$master=Join-Path $root ('Masters\'+$Role)
$saves='C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves'
$active=Join-Path $saves 'HyThief Passive Witness Test'
function Closed {
 if(@(Get-CimInstance Win32_Process|Where-Object {$_.Name -like 'HytaleClient*' -or $_.Name -like 'HytaleServer*' -or ($_.Name -eq 'java.exe' -and $_.CommandLine -match 'HytaleServer|hytale.server')}).Count){throw 'Close Hytale and native test servers before resetting. No save changed.'}
}
function NoRedirect($path){if(Test-Path -LiteralPath $path){if(@(@(Get-Item -LiteralPath $path)+@(Get-ChildItem -LiteralPath $path -Recurse -Force)|Where-Object {$_.Attributes -band [IO.FileAttributes]::ReparsePoint}).Count){throw 'Refusing redirected test path'}}}
function Verify($path,$records){$files=@(Get-ChildItem -LiteralPath $path -File -Recurse -Force);if($files.Count -ne @($records.PSObject.Properties).Count){throw 'Fixture file count mismatch'};foreach($p in $records.PSObject.Properties){if((Get-FileHash -LiteralPath (Join-Path $path $p.Name)).Hash.ToLower() -cne $p.Value){throw ('Fixture checksum mismatch '+$p.Name)}}}
Closed
NoRedirect $master
NoRedirect $active
$live='C:\Users\askin\AppData\Roaming\Hytale\install\release\package\game\latest\Server\HytaleServer.jar'
if((Get-FileHash -LiteralPath $live).Hash.ToLower() -cne '23f71f03763422ce7c04df191db49cd0e7d87d43b12baad75c43fdc202410aff'){throw 'Hytale changed. Revalidate fixture before reset.'}
$candidate=Get-Content -LiteralPath (Join-Path $root 'candidate.json') -Raw|ConvertFrom-Json
$mod=Join-Path 'C:\Users\askin\AppData\Roaming\Hytale\UserData\Mods' ('HyThief-'+$candidate.Version+'.jar')
if(!(Test-Path -LiteralPath $mod) -or (Get-FileHash -LiteralPath $mod).Hash.ToLower() -cne $candidate.SHA256){throw 'Install the validated container.12 candidate before using this fixture.'}
$records=Get-Content -LiteralPath (Join-Path $root ('master-checksums-'+$Role+'.json')) -Raw|ConvertFrom-Json
Verify $master $records
$marker=Get-Content -LiteralPath (Join-Path $master 'container-theft-test-world.json') -Raw|ConvertFrom-Json
if($marker.ProofRole -cne $Role -or $marker.ValidatedHytaleVersion -cne '0.7.2' -or $marker.ValidatedHytaleRevision -cne 'd769dc3d57881261a62858e1394f0e4de9a21eca'){throw 'Wrong proof master/revision'}
if(Test-Path -LiteralPath $active){$old=Get-Content -LiteralPath (Join-Path $active 'container-theft-test-world.json') -Raw|ConvertFrom-Json;if(!$old.ProofRole -or $old.WorldUUID.'$binary' -cne $marker.WorldUUID.'$binary'){throw 'Refusing to replace another world'}}
$stage=Join-Path $saves ('.HyThief-passive-stage-'+[guid]::NewGuid().ToString('N'))
Copy-Item -LiteralPath $master -Destination $stage -Recurse
Verify $stage $records
Closed
$backup=$null
if(Test-Path -LiteralPath $active){$backup=Join-Path $root ('Archives\'+[guid]::NewGuid().ToString('N'));New-Item -ItemType Directory -Path (Split-Path $backup -Parent) -Force|Out-Null;if([IO.Path]::GetFullPath($active) -cne 'C:\Users\askin\AppData\Roaming\Hytale\UserData\Saves\HyThief Passive Witness Test'){throw 'Unsafe reset path'};Move-Item -LiteralPath $active -Destination $backup}
try{Move-Item -LiteralPath $stage -Destination $active;Verify $active $records}catch{if($backup -and !(Test-Path -LiteralPath $active)){Move-Item -LiteralPath $backup -Destination $active};throw}
Write-Host ('PASSIVE WITNESS TEST READY: '+$Role)
[pscustomobject]@{Role=$Role;Active=$active;Backup=$backup;MatchesMaster=$true;OtherWorldsUnchanged=$true}|ConvertTo-Json
