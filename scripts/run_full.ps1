# run_full.ps1 - chay day du. Nen chay tren may da cam sac, power = high performance
# Ghim affinity vao 4 P-core (bits 0..7 = mask FF) bang start /affinity
$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "results\raw" | Out-Null

$jvmArgs = "-Xms2g -Xmx2g -XX:+AlwaysPreTouch -cp build"
$cutoffList = ((1..32) + @(40,48,64,96,128)) -join ","
$N = "1000,2000,5000,10000,20000,50000,100000,200000,500000,1000000"
$distAll = "random,sorted,reverse,nearly,duplicates"

# --- Comparable[] ---
$cmd1 = "java $jvmArgs cutoff.Harness " +
    "--type Comparable --N $N --cutoff $cutoffList --dist $distAll " +
    "--reps 30 --warmup 5 --seed 42 --out results\raw\full_comparable.csv"
Write-Host ">>> Comparable[] run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c start /b /affinity FF /wait $cmd1" -Wait -NoNewWindow

# --- int[] ---
$cmd2 = "java $jvmArgs cutoff.Harness " +
    "--type int --N $N --cutoff $cutoffList --dist $distAll " +
    "--reps 30 --warmup 5 --seed 42 --out results\raw\full_int.csv"
Write-Host ">>> int[] run"
Start-Process -FilePath "cmd.exe" -ArgumentList "/c start /b /affinity FF /wait $cmd2" -Wait -NoNewWindow

Write-Host "Full run xong -> results\raw\full_comparable.csv va full_int.csv"
