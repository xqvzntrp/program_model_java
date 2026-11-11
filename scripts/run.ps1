$ErrorActionPreference = 'Stop'
& "$PSScriptRoot/build.ps1"
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
Set-Location (Join-Path $PSScriptRoot '..')
& java -cp build/main programmodel.ProgramModel @args
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }
