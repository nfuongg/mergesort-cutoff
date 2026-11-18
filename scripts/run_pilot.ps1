# run_pilot.ps1 - pilot: dai CUTOFF rong, N trung binh, de chot dai chay full
$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "results\raw" | Out-Null

$outfile = "results\raw\pilot.csv"
$java = "java"
$jvmArgs = @("-Xms2g", "-Xmx2g", "-XX:+AlwaysPreTouch", "-cp", "build")

$cutoffList = (1..32 + @(40,48,64,96,128)) -join ","

& $java @jvmArgs cutoff.Harness `
    --type Comparable `
    --N 10000,100000,1000000 `
    --cutoff $cutoffList `
    --dist random `
    --reps 10 `
    --warmup 3 `
    --seed 42 `
    --out $outfile

Write-Host "Pilot xong -> $outfile"
