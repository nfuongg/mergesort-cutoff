# run_smoke.ps1 - chay nhanh de kiem tra pipeline ca 2 nhanh (Comparable + int)
$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "results\raw" | Out-Null

$java = "java"
$jvmArgs = @("-Xms1g", "-Xmx1g", "-XX:+AlwaysPreTouch", "-cp", "build")

Write-Host ">>> Smoke Comparable[]"
& $java @jvmArgs cutoff.Harness `
    --type Comparable `
    --N 10000,50000 `
    --cutoff 1,2,4,7,10,16,32 `
    --dist random,sorted `
    --reps 5 --warmup 2 --seed 42 `
    --out results\raw\smoke_comparable.csv

Write-Host ">>> Smoke int[]"
& $java @jvmArgs cutoff.Harness `
    --type int `
    --N 10000,50000 `
    --cutoff 1,2,4,7,10,16,32 `
    --dist random,sorted `
    --reps 5 --warmup 2 --seed 42 `
    --out results\raw\smoke_int.csv

# Xoa file smoke cu neu ton tai (truoc khi tach ra 2 file)
if (Test-Path results\raw\smoke.csv) { Remove-Item results\raw\smoke.csv }

Write-Host "Smoke xong -> results\raw\smoke_comparable.csv, smoke_int.csv" -ForegroundColor Green
