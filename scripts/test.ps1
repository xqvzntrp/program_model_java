$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/build.ps1"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Set-Location (Join-Path $PSScriptRoot '..')
New-Item -ItemType Directory -Force 'build/test' | Out-Null
$files = @(Get-ChildItem 'src/test/java' -Recurse -Filter '*.java' | ForEach-Object FullName)
& javac --release 17 -cp build/main -d build/test $files
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
& java -cp 'build/main;build/test' programmodel.tests.AllTests
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
