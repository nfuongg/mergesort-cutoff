
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
(java -version 2>&1) | Out-String | Out-File -FilePath $out -Append -Encoding utf8
(javac -version 2>&1) | Out-String | Out-File -FilePath $out -Append -Encoding utf8

Section "Measurement checklist"
@"
[ ] Laptop plugged in
[ ] Power plan = High performance (powercfg /setactive 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c)
[ ] Browser / IDE / background apps closed
[ ] Windows Defender real-time scan paused (optional)
[ ] Processor power management: Maximum state as desired (Turbo on/off noted)
[ ] CPU temperature < 60 C at start (if sensor available)
[ ] Java pinned to P-cores via: start /b /affinity FF /wait java ...
"@ | Out-File -FilePath $out -Append -Encoding utf8

Write-Host "Written: $out" -ForegroundColor Green
