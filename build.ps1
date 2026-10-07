[CmdletBinding(PositionalBinding = $false)]
param(
    [string]$JdkHome,
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$GradleArgs
)
$ErrorActionPreference = 'Stop'
$workspace = Split-Path (Split-Path $PSScriptRoot -Parent) -Parent
if (!$JdkHome) { $JdkHome = Join-Path $workspace 'Toolchains\JDK25' }
foreach ($binary in @('java.exe', 'javac.exe')) {
    if (!(Test-Path -LiteralPath (Join-Path $JdkHome "bin\$binary"))) {
        throw "Missing $binary in JDK: $JdkHome"
    }
}
$previousJavaHome = $env:JAVA_HOME
$previousGradleHome = $env:GRADLE_USER_HOME
try {
    $env:JAVA_HOME = $JdkHome
    $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '.build\gradle-user-home'
    $modules = & (Join-Path $JdkHome 'bin\java.exe') --list-modules
    if ($LASTEXITCODE -ne 0 -or !($modules -match '^jdk.compiler@25')) {
        throw 'A complete Java 25 JDK with jdk.compiler is required'
    }
    if (!$GradleArgs) { $GradleArgs = @('clean', 'build') }
    & (Join-Path $PSScriptRoot 'gradlew.bat') --project-dir $PSScriptRoot --no-daemon --console=plain @GradleArgs
    $buildExitCode = $LASTEXITCODE
} finally {
    $env:JAVA_HOME = $previousJavaHome
    $env:GRADLE_USER_HOME = $previousGradleHome
}
exit $buildExitCode
