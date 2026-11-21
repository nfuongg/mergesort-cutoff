$ErrorActionPreference = "Continue"
$out = "results\sysinfo.txt"
New-Item -ItemType Directory -Force -Path "results" | Out-Null

function Section($title) {
    "`n=== $title ===" | Out-File -FilePath $out -Append -Encoding utf8
}

"Mergesort CUTOFF experiment - system info" | Out-File -FilePath $out -Encoding utf8
"Collected: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss zzz')" | Out-File -FilePath $out -Append -Encoding utf8

Section "OS"
Get-CimInstance Win32_OperatingSystem |
    Select-Object Caption, Version, BuildNumber, OSArchitecture, LastBootUpTime |
    Format-List | Out-File -FilePath $out -Append -Encoding utf8

Section "CPU"
Get-CimInstance Win32_Processor |
    Select-Object Name, Manufacturer, NumberOfCores, NumberOfLogicalProcessors,
                  MaxClockSpeed, L2CacheSize, L3CacheSize |
    Format-List | Out-File -FilePath $out -Append -Encoding utf8

Section "Memory"
$mem = Get-CimInstance Win32_ComputerSystem
"TotalPhysicalMemory: {0:N2} GB" -f ($mem.TotalPhysicalMemory / 1GB) |
    Out-File -FilePath $out -Append -Encoding utf8

Section "Power plan"
powercfg /GETACTIVESCHEME | Out-File -FilePath $out -Append -Encoding utf8

Section "Java"
cmd /c "java -version 2>&1"  | Out-File -FilePath $out -Append -Encoding utf8
cmd /c "javac -version 2>&1" | Out-File -FilePath $out -Append -Encoding utf8

Section "Measurement state (auto-checked)"
$plan = (powercfg /GETACTIVESCHEME) -join ""
$isHigh = $plan -match "8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c"
$batt = Get-CimInstance Win32_Battery -ErrorAction SilentlyContinue
$onAC = if ($batt) { $batt.BatteryStatus -eq 2 } else { $true }
$heavy = Get-Process -ErrorAction SilentlyContinue | Where-Object {
    $_.Name -in @("chrome","msedge","firefox","Teams","ms-teams","Zoom","Slack","Spotify","Code","idea64")
} | Select-Object -ExpandProperty Name -Unique

$mark = { param($ok) if ($ok) { "[x]" } else { "[ ]" } }
"$(& $mark $isHigh) Power plan = High performance"       | Out-File -FilePath $out -Append -Encoding utf8
"$(& $mark $onAC) Laptop plugged in (AC power)"          | Out-File -FilePath $out -Append -Encoding utf8
"$(& $mark (-not $heavy)) No heavy apps running"         | Out-File -FilePath $out -Append -Encoding utf8
if ($heavy) {
    "    (detected: $($heavy -join ', '))" | Out-File -FilePath $out -Append -Encoding utf8
}
"[-] Windows Defender real-time scan (user decides)"     | Out-File -FilePath $out -Append -Encoding utf8
"[-] Turbo state / Max processor power (user decides)"   | Out-File -FilePath $out -Append -Encoding utf8
"[-] CPU temperature (no reliable sensor API)"           | Out-File -FilePath $out -Append -Encoding utf8
"[-] P-core pinning (done inside run_full via start /affinity FF)" | Out-File -FilePath $out -Append -Encoding utf8

Write-Host "Written: $out" -ForegroundColor Green
