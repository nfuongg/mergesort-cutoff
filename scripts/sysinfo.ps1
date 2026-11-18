# sysinfo.ps1 - thu thập cấu hình máy, ghi vào results/sysinfo.txt
# Chạy: powershell -ExecutionPolicy Bypass -File scripts\sysinfo.ps1

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
java -version 2>&1 | Out-File -FilePath $out -Append -Encoding utf8
javac -version 2>&1 | Out-File -FilePath $out -Append -Encoding utf8

Section "Checklist khi đo"
@"
[ ] Cắm sạc
[ ] Power plan = High performance hoặc Best performance
[ ] Đóng trình duyệt, IDE, ứng dụng nền
[ ] Tạm tắt Windows Defender real-time (nếu đồng ý)
[ ] Không bật Turbo biến thiên bất thường (xem Maximum processor state)
[ ] Nhiệt độ CPU khởi đầu < 60 degC (nếu có cảm biến)
"@ | Out-File -FilePath $out -Append -Encoding utf8

Write-Host "Da ghi: $out"
