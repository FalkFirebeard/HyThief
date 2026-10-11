[CmdletBinding()]
param()
$ErrorActionPreference='Stop'
$root=Split-Path $PSScriptRoot -Parent
$master=Join-Path $root 'Master'
$authorInput=Join-Path $root ('FactionAuthoringInputs\'+[guid]::NewGuid().ToString('N'))
if(![IO.Path]::GetFullPath($authorInput).StartsWith($root+'\FactionAuthoringInputs\')){throw 'Unsafe authoring input path'}
New-Item -ItemType Directory -Path (Split-Path $authorInput -Parent) -Force|Out-Null
Copy-Item -LiteralPath $master -Destination $authorInput -Recurse
$playerFiles=@(Get-ChildItem -LiteralPath (Join-Path $master 'universe\players') -Filter '*.json' -File)
if($playerFiles.Count -ne 1){throw 'Master must contain exactly one starting-player seed'}; $playerId=$playerFiles[0].Name
$seed=Join-Path $master ('universe\players\'+$playerId)
if(!(Test-Path -LiteralPath $seed)){$seed=Join-Path $PSScriptRoot 'StartingPlayer.json'}
if(!(Test-Path -LiteralPath $seed)){throw 'Starting-player seed missing'}
$player=Get-Content -LiteralPath $seed -Raw
$world=Get-Content -LiteralPath (Join-Path $authorInput 'universe\worlds\default\config.json') -Raw
$universe=[IO.Path]::GetFullPath((Join-Path $authorInput 'universe'))
if(!$universe.StartsWith($authorInput+'\')){throw 'Unsafe copied universe path'}
if(@(Get-ChildItem -LiteralPath $authorInput -Recurse -Force|Where-Object {$_.Attributes -band [IO.FileAttributes]::ReparsePoint}).Count){throw 'Refusing redirected copied save'}
Remove-Item -LiteralPath $universe -Recurse
New-Item -ItemType Directory -Path (Join-Path $universe 'worlds\default\chunks') -Force|Out-Null
New-Item -ItemType Directory -Path (Join-Path $universe 'players') -Force|Out-Null
$player|Set-Content -LiteralPath (Join-Path $universe ('players\'+$playerId))
$world|Set-Content -LiteralPath (Join-Path $universe 'worlds\default\config.json')
$assets=Join-Path $root 'FactionFieldAssets'
$fixture=Join-Path $authorInput 'mods\HyThief_Faction_Field.zip'
if(Test-Path -LiteralPath $fixture){Remove-Item -LiteralPath $fixture}
$zip=[IO.Compression.ZipFile]::Open($fixture,[IO.Compression.ZipArchiveMode]::Create)
try{[IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,(Join-Path $assets 'manifest.json'),'manifest.json')|Out-Null
 foreach($file in Get-ChildItem -LiteralPath $assets -Filter '*.json' -File){if($file.Name -ne 'manifest.json'){
  [IO.Compression.ZipFileExtensions]::CreateEntryFromFile($zip,$file.FullName,('Server/NPC/Roles/DevTesting/'+$file.Name))|Out-Null
 }}
}finally{$zip.Dispose()}
$marker=Get-Content -LiteralPath (Join-Path $authorInput 'container-theft-test-world.json') -Raw|ConvertFrom-Json
$marker|Add-Member -NotePropertyName OwnershipField -NotePropertyValue $true -Force
$marker|Add-Member -NotePropertyName WitnessRoles -NotePropertyValue @('Kweebec_Sapling_HyThiefWitness','Feran_Civilian_HyThiefWitness') -Force
$marker.PSObject.Properties.Remove('AutoContainerDiagnostics')
$marker|ConvertTo-Json -Depth 40|Set-Content -LiteralPath (Join-Path $authorInput 'container-theft-test-world.json')
Write-Host ('Prepared disposable authoring input: '+$authorInput)
Write-Host 'Next: Verify-Faction-Field.ps1 -Author -InputWorld <printed input>. Validate the authored result without -Author before staging it as Master. Never stage a combat-response run.'
