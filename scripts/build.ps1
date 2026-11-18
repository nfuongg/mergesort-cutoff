# build.ps1 - biên dịch toàn bộ Java source
$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "build" | Out-Null
$sources = Get-ChildItem -Path "src\main\java" -Filter "*.java" -Recurse | ForEach-Object { $_.FullName }
javac -d build -encoding UTF-8 $sources
if ($LASTEXITCODE -ne 0) {
    Write-Host "BUILD FAILED (javac exit $LASTEXITCODE)" -ForegroundColor Red
    exit $LASTEXITCODE
}
Write-Host "Build OK -> build\" -ForegroundColor Green
