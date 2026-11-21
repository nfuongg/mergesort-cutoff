# Mergesort CUTOFF Experiment

Thí nghiệm xác định giá trị CUTOFF tối ưu cho Mergesort + Insertion sort, dựa trên nội dung bài giảng (slide 2.2 Mergesort, 2.1 Elementary Sorts, 1.4 Analysis of Algorithms).

## Mục tiêu

Giáo trình nhắc đến CUTOFF = 7 (cũ) và 10 (mới). Thí nghiệm này đo thời gian chạy mergesort với nhiều giá trị CUTOFF khác nhau trên nhiều kích thước dữ liệu và nhiều phân bố đầu vào, để tìm vùng CUTOFF tối ưu trên cấu hình máy thực tế.

## Cấu hình thí nghiệm

- **Ngôn ngữ**: Java 25 (JDK thuần, không dùng thư viện algs4).
- **Máy đo**: Laptop ASUS Vivobook 16X K3605VC, Intel Core i5-13420H (4 P-core + 4 E-core, 12 luồng), 16 GB RAM, Windows 11 Home.
- **Quy ước đo**: ghim affinity vào 4 P-core, heap cố định `-Xms2g -Xmx2g -XX:+AlwaysPreTouch`, khởi động JIT trước khi đo, mỗi cấu hình lặp >= 30 lần, báo trung vị + độ lệch chuẩn + CI 95%.
- **Biến khảo sát**:
  - `CUTOFF`: 1..32, 40, 48, 64, 96, 128 (CUTOFF = 1 là mergesort thuần).
  - `N`: 1_000, 2_000, 5_000, 10_000, 20_000, 50_000, 100_000, 200_000, 500_000, 1_000_000.
  - Loại dữ liệu: random, sorted, reverse, nearly-sorted, many-duplicates.
  - Kiểu dữ liệu: `Comparable[]` (Integer boxed) và `int[]` primitive.

## Cấu trúc thư mục

```
mergesort-cutoff/
├── README.md
├── .gitignore
├── LICENSE
├── src/main/java/cutoff/
│   ├── Merge.java              # SV viết: mergesort Comparable[] có CUTOFF
│   ├── Insertion.java          # SV viết: insertion sort trên [lo, hi]
│   ├── MergeInt.java           # SV viết: mergesort int[] có CUTOFF
│   ├── InsertionInt.java       # SV viết: insertion sort int[] trên [lo, hi]
│   ├── DataGen.java            # sinh dữ liệu theo seed, nhiều phân bố
│   ├── Verify.java             # kiểm sắp xếp + so sánh multiset
│   └── Harness.java            # khung đo thời gian, in CSV
├── scripts/
│   ├── sysinfo.ps1             # ghi cấu hình máy -> results/sysinfo.txt
│   ├── build.ps1               # biên dịch Java
│   ├── run_smoke.ps1           # chạy thử nhanh để kiểm tra
│   ├── run_pilot.ps1           # pilot: N nhỏ, ít lặp, dải CUTOFF rộng
│   ├── run_full.ps1            # chạy đầy đủ (ghim P-core)
│   └── analyze.py              # pandas + matplotlib, tổng hợp + vẽ đồ thị
├── data/                       # (gitignored) bộ dữ liệu, regen bằng DataGen
├── results/
│   ├── sysinfo.txt
│   ├── raw/                    # CSV thô từng lần chạy
│   ├── summary/                # CSV tổng hợp
│   └── plots/                  # PNG đồ thị
└── report/
    └── report.md               # báo cáo
```

## Cách chạy lại (tóm tắt)

```powershell
# 1. Thu thập cấu hình máy
powershell -File scripts\sysinfo.ps1

# 2. Biên dịch
powershell -File scripts\build.ps1

# 3. Chạy thử (vài phút)
powershell -File scripts\run_smoke.ps1

# 4. Chạy đầy đủ (có thể để qua đêm)
powershell -File scripts\run_full.ps1

# 5. Phân tích + vẽ đồ thị
python scripts\analyze.py
```

Chi tiết các thí nghiệm, cấu hình đo và kết quả xem trong `report/report.md`.
