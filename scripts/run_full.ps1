$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "results\raw" | Out-Null

$cutoffList = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,20,24,32,48,64"
$N          = "50000,100000,200000,400000,500000,800000,1000000,2000000"
$distAll    = "random,sorted,reverse,nearly,duplicates"
$reps       = 30
$warmup     = 5
$seed       = 42
$jvmFlags   = "-Xms2g -Xmx2g"

function Run-Phase($label, $type, $outCsv) {
    Write-Host ">>> $label" -ForegroundColor Cyan
    $jargs = "$jvmFlags -cp build cutoff.Harness " +
             "--mode time --type $type --N $N --cutoff $cutoffList --dist $distAll " +
             "--reps $reps --warmup $warmup --seed $seed --out $outCsv"
    cmd.exe /c "start /b /affinity FF /wait java $jargs"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "PHASE FAILED: $label (exit $LASTEXITCODE)" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

$t0 = Get-Date
Run-Phase "Phase 1/2: Comparable[]" "Comparable" "results\raw\full_comparable.csv"
Run-Phase "Phase 2/2: int[]"        "int"        "results\raw\full_int.csv"
$dt = (Get-Date) - $t0

Write-Host ""
Write-Host ("Full run DONE in {0:N1} min" -f $dt.TotalMinutes) -ForegroundColor Green
Write-Host "  -> results\raw\full_comparable.csv"
Write-Host "  -> results\raw\full_int.csv"
