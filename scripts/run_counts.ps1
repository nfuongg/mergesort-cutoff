$ErrorActionPreference = "Stop"
New-Item -ItemType Directory -Force -Path "results\raw" | Out-Null

$cutoffList = "1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,18,20,24,32,48,64"
$N          = "100000,500000,1000000"
$distAll    = "random,sorted,reverse,nearly,duplicates"
$seed       = 42
$jvmFlags   = "-Xms2g -Xmx2g"

function Run-Phase($label, $type, $outCsv) {
    Write-Host ">>> $label" -ForegroundColor Cyan
    $jargs = "$jvmFlags -cp build cutoff.Harness " +
             "--mode count --type $type --N $N --cutoff $cutoffList --dist $distAll " +
             "--seed $seed --out $outCsv"
    cmd.exe /c "java $jargs"
    if ($LASTEXITCODE -ne 0) {
        Write-Host "COUNT PHASE FAILED: $label" -ForegroundColor Red
        exit $LASTEXITCODE
    }
}

$t0 = Get-Date
Run-Phase "Counting Comparable[]" "Comparable" "results\raw\count_comparable.csv"
Run-Phase "Counting int[]"        "int"        "results\raw\count_int.csv"
$dt = (Get-Date) - $t0

Write-Host ""
Write-Host ("Counting pass DONE in {0:N1} min" -f $dt.TotalMinutes) -ForegroundColor Green
