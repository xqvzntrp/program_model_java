$ErrorActionPreference = 'Stop'
Set-Location (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force 'build/main' | Out-Null
$files = @(Get-ChildItem 'src/main/java' -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac --release 17 -d build/main $files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
