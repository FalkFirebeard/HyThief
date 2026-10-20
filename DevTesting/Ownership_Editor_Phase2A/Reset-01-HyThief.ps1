param()
$ErrorActionPreference='Stop'
$root=$PSScriptRoot
$master=Join-Path $root 'Master'
$active=Join-Path $env:APPDATA 'Hytale\UserData\Saves\01 HyThief'
$mods=Join-Path $env:APPDATA 'Hytale\UserData\Mods'
$log=Join-Path $root 'last-reset.log'
function Read-SHA256([string]$file){
  $stream=[IO.File]::OpenRead($file)
  $algorithm=[Security.Cryptography.SHA256]::Create()
  try { return [BitConverter]::ToString($algorithm.ComputeHash($stream)).Replace('-','') }
  finally { $algorithm.Dispose(); $stream.Dispose() }
}
try {
  $running=Get-CimInstance Win32_Process | Where-Object { $_.Name -like 'HytaleClient*' -or $_.Name -like 'HytaleServer*' -or ($_.Name -like 'java*' -and $_.CommandLine -match 'HytaleServer|com\.hypixel\.hytale') }
  if($running){throw 'Close Hytale and all Hytale servers before resetting this world.'}
  $pin=Get-Content -LiteralPath (Join-Path $root 'candidate-pin.json') -Raw | ConvertFrom-Json
  foreach($entry in $pin.Mods){
    $file=Join-Path $mods $entry.File
    if(!(Test-Path -LiteralPath $file) -or (Read-SHA256 $file) -ne $entry.SHA256){throw "Installed candidate mismatch: $($entry.File)"}
  }
  $server=Join-Path $env:APPDATA 'Hytale\install\release\package\game\latest\Server\HytaleServer.jar'
  if((Read-SHA256 $server) -ne $pin.ServerSHA256){throw 'Hytale changed. Revalidate this fixture before resetting.'}
  $expected=Get-Content -LiteralPath (Join-Path $root 'master-checksums.json') -Raw | ConvertFrom-Json
  function Verify-Tree([string]$directory){
    $files=@(Get-ChildItem -LiteralPath $directory -File -Recurse)
    if($files.Count -ne @($expected.PSObject.Properties).Count){throw "Unexpected files in $directory"}
    foreach($e in $expected.PSObject.Properties){
      $file=Join-Path $directory $e.Name
      if(!(Test-Path -LiteralPath $file) -or (Read-SHA256 $file) -ne $e.Value){throw "Fixture differs: $file"}
    }
  }
  Verify-Tree $master
  $resolved=[IO.Path]::GetFullPath($active)
  $saves=[IO.Path]::GetFullPath((Join-Path $env:APPDATA 'Hytale\UserData\Saves'))
  if((Split-Path $resolved -Parent) -ne $saves -or (Split-Path $resolved -Leaf) -ne '01 HyThief'){throw 'Unsafe reset target.'}
  if(Test-Path -LiteralPath $active){
    $config=Get-Content -LiteralPath (Join-Path $active 'config.json') -Raw | ConvertFrom-Json
    if($config.ServerName -ne '01 HyThief'){throw 'Existing save is not the ownership-editor fixture.'}
    $backup=Join-Path $root ('ResetBackups\'+[guid]::NewGuid().ToString())
    New-Item -ItemType Directory -Path $backup -Force | Out-Null
    Copy-Item -LiteralPath $active -Destination (Join-Path $backup '01 HyThief') -Recurse
    # Target is checked immediately above; only this isolated fixture is replaced.
    Remove-Item -LiteralPath $resolved -Recurse -Force
  }
  Copy-Item -LiteralPath $master -Destination $active -Recurse
  Verify-Tree $active
  "PASS $(Get-Date -Format o): reset 01 HyThief; all active files equal master. No other world was reset." | Set-Content -LiteralPath $log
  Write-Host (Get-Content -LiteralPath $log)
} catch {
  "FAIL $(Get-Date -Format o): $_" | Set-Content -LiteralPath $log
  Write-Error $_
  exit 1
}
