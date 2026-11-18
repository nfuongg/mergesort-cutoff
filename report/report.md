# Thí nghiệm CUTOFF cho Mergesort

---

## 1. Mục tiêu

Giáo trình đề xuất cải tiến **chuyển sang Insertion sort cho mảng con nhỏ**: khi kích thước đoạn còn lại ≤ CUTOFF thì gọi `Insertion.sort(a, lo, hi)` thay vì tiếp tục chia đôi. Giá trị CUTOFF được nhắc đến khác nhau giữa các phiên bản: **7** và **≈ 10**.

Câu hỏi: **giá trị CUTOFF nào là tốt nhất trên cấu hình máy thực tế**, và cải tiến này mang lại bao nhiêu phần trăm tiết kiệm?

## 2. Phương pháp

Áp dụng đúng chu trình **Observe → Hypothesize → Predict → Verify → Validate**:

- **Observe**: đo thời gian chạy `Merge.sort(a)` với nhiều giá trị CUTOFF, nhiều N, nhiều loại dữ liệu.
- **Hypothesize**: thời gian T(N) ~ a · N log N, hệ số a phụ thuộc CUTOFF, kỳ vọng có **vùng CUTOFF tối thiểu hóa a**. Với CUTOFF = 1 (mergesort thuần) a lớn; với CUTOFF rất lớn, insertion sort O(N²) chiếm ưu thế → a cũng lớn. Đồ thị kỳ vọng hình chữ U.
- **Predict / Verify**: so sánh tỷ số T(2N)/T(N) với dự đoán `2 · log(2N)/log(N)`.
- **Validate**: lặp lại trên 5 phân bố dữ liệu và 2 kiểu phần tử.

### 2.1. Cài đặt

- `Insertion.java` / `InsertionInt.java`.
- `Merge.java` / `MergeInt.java`.

Khung đo (`Harness.java`) + sinh dữ liệu (`DataGen.java`) + kiểm tra (`Verify.java`) do AI hỗ trợ.

### 2.2. Thiết kế thí nghiệm

| Biến | Giá trị |
|---|---|
| CUTOFF | 1, 2, 3, …, 32, 40, 48, 64, 96, 128 (37 giá trị) |
| N | 1k, 2k, 5k, 10k, 20k, 50k, 100k, 200k, 500k, 1M (10 giá trị) |
| Phân bố | random, sorted, reverse, nearly-sorted (99% sắp xếp, 1% swap), duplicates (√N giá trị phân biệt) |
| Kiểu dữ liệu | `Comparable[]` (Integer boxed) và `int[]` primitive |
| Số lần lặp | 30 lần/cấu hình |
| Warmup | 5 vòng JIT trước khi đo |

Tổng cộng **2 × 55 500 = 111 000 phép đo**, tất cả 0 lỗi (checksum khớp hoàn toàn).

### 2.3. Quy ước đo để giảm nhiễu

- Ghim JVM vào 4 P-core (`start /b /affinity FF /wait java ...`) vì Raptor Lake-H có 4 P + 4 E-core.
- Cố định heap `-Xms2g -Xmx2g -XX:+AlwaysPreTouch` để GC không co giãn.
- Warmup JIT trước khi đo.
- **Xáo trộn thứ tự chạy** (seeded) các cấu hình để tránh sai lệch do nhiệt độ/turbo CPU.
- Mỗi cấu hình dùng **cùng seed** cho dữ liệu đầu vào - các CUTOFF khác nhau so sánh trên cùng một mảng.
- Clone mảng NGOÀI vùng đo thời gian.
- Chạy trên Power plan "High performance", cắm sạc, đóng app nền.
- Harness tự `Verify.checkSorted(a)` sau mỗi lần sắp xếp + tính checksum FNV-1a.

## 3. Cấu hình máy

| | |
|---|---|
| Laptop | ASUS Vivobook 16X K3605VC |
| CPU | 13th Gen Intel Core i5-13420H (4 P-core + 4 E-core, 12 luồng, base 2.1 GHz) |
| Cache | L1 704 KB, L2 7 MB, L3 12 MB |
| RAM | 16 GB |
| OS | Windows 11 Home build 26200 |
| Java | Oracle JDK 25.0.2 LTS, HotSpot 64-Bit Server VM |

Chi tiết trong `results/sysinfo.txt`.

## 4. Kết quả chính

### 4.1. Random data - trường hợp tiêu biểu

Bảng trung vị (ms) với các CUTOFF chọn lọc, random data, N ≥ 100k:

| Type | N | C=1 (thuần) | C=7 | C=10 | Best | Best CUTOFF | Tiết kiệm best |
|---|---:|---:|---:|---:|---:|---:|---:|
| Comparable | 100k | 15.74 | 14.36 | 14.37 | 14.18 | **21** | 9.9% |
| Comparable | 200k | 51.04 | 47.32 | 47.61 | 46.98 | **11** | 8.0% |
| Comparable | 500k | 143.53 | 134.54 | 134.09 | 133.71 | **14** | 6.8% |
| Comparable | 1M | 320.03 | 302.99 | 302.84 | 300.25 | **13** | 6.2% |
| int | 100k | 7.65 | 6.62 | 6.66 | 6.28 | **25** | 18.0% |
| int | 200k | 16.15 | 14.24 | 14.15 | 13.41 | **27** | 17.0% |
| int | 500k | 43.15 | 38.85 | 37.69 | 36.47 | **17** | 15.5% |
| int | 1M | 90.89 | 81.96 | 79.74 | 76.98 | **18** | 15.3% |

**Nhận xét:**

1. **Vùng CUTOFF tối ưu tồn tại**. Hình chữ U được xác nhận trên mọi (type, N) - xem `results/plots/headline.png` và `time_vs_cutoff__*__random.png`.
2. **CUTOFF = 7 và CUTOFF = 10 đều nằm trong vùng tốt**. Với `Comparable[]`, chênh lệch so với best chỉ **~0.5–1%**, trong khoảng nhiễu đo. Có thể nói **cả 7 và 10 đều là lựa chọn hợp lý cho use case chung**.
3. **Vùng tối ưu phụ thuộc kiểu dữ liệu**:
   - `Comparable[]` (có overhead `compareTo()` ảo + autoboxing): optimum ~**11–21**, cùng bậc độ lớn với giáo trình.
   - `int[]` (so sánh trực tiếp `<`): optimum dịch cao hơn, ~**17–27**. Lý giải: khi mỗi phép so sánh rẻ hơn, insertion sort O(k²) có thể "chịu" được k lớn hơn.
4. **Tiết kiệm so với mergesort thuần**:
   - `Comparable[]` random: **5–10%** (với CUTOFF = 7 hoặc 10).
   - `int[]` random: **12–16%** (với CUTOFF = 10).

### 4.2. Các phân bố đặc biệt

| Dist | Best CUTOFF (Comparable, N=1M) | Best CUTOFF (int, N=1M) | Giải thích |
|---|---:|---:|---|
| random | 13 | 18 | Hình chữ U cổ điển |
| sorted | 128 (max) | 128 (max) | Insertion sort O(N) trên dữ liệu đã sắp. Càng giao mảng lớn cho insertion thì càng ít merge → càng nhanh. |
| nearly | 128 (max) | 128 (max) | Tương tự sorted, chỉ 1% swap. Insertion gần như O(N). |
| reverse | 6 | 10 | Insertion sort O(N²) trên reverse, phải tránh - nên CUTOFF nhỏ, chia cho mergesort xử lý. |
| duplicates | 8 | 23 | Trung gian giữa random và sorted |

**Kết luận phần này:**
- Chọn CUTOFF duy nhất cho mọi trường hợp là bất khả thi. Nếu biết trước dữ liệu hầu như đã sắp, CUTOFF rất lớn là tối ưu; nếu lo ngại reverse, CUTOFF nhỏ.
- Với dữ liệu **ngẫu nhiên hoặc không biết trước** (use case điển hình), CUTOFF **≈ 10** là lựa chọn an toàn: gần optimum cho cả 2 kiểu dữ liệu, không bị penalty nặng trên bất kỳ phân bố nào được khảo sát (kể cả reverse: với CUTOFF = 10, int reverse vẫn đạt best; Comparable reverse chỉ chậm ~1% so với best).

### 4.3. Kiểm định doubling hypothesis

Với CUTOFF = 10, random data:

| N | T (ms, Comparable) | T(2N)/T(N) | Dự đoán |
|---:|---:|---:|---:|
| 100k | 14.375 | - | - |
| 200k | 47.608 | 3.31 | 2.12 |
| 500k | 134.091 | 2.82 | 2.69 |
| 1M | 302.840 | 2.26 | 2.11 |

| N | T (ms, int) | T(2N)/T(N) | Dự đoán |
|---:|---:|---:|---:|
| 100k | 6.660 | - | - |
| 200k | 14.151 | 2.12 | 2.12 |
| 500k | 37.692 | 2.66 | 2.69 |
| 1M | 79.740 | 2.12 | 2.11 |

`int[]` khớp dự đoán **gần như hoàn hảo** → xác nhận T(N) ~ N log N. `Comparable[]` lệch ở N = 200k (3.31 vs 2.12 dự đoán) - có thể do hiệu ứng cache (mảng Integer[] 200k phần tử + aux ≈ 3.2 MB, qua giới hạn L2 7 MB khi tính cả header + indirection). Chi tiết trong "Threats to validity".

## 5. Thảo luận

### Vì sao CUTOFF giúp tiết kiệm thời gian?

Insertion sort có **chi phí hằng số thấp** hơn mergesort cho mảng nhỏ: không cần allocate `aux`, không gọi đệ quy, truy cập bộ nhớ liền kề thân thiện với cache. Mergesort đệ quy xuống tận các mảng 1-2 phần tử tốn rất nhiều lời gọi hàm so với công việc thực sự. CUTOFF ngắt đệ quy ở một ngưỡng mà "overhead/công việc" của mergesort vượt qua "O(k²)/công việc" của insertion sort.

### Vì sao optimum của `int[]` cao hơn `Comparable[]`?

Mỗi phép `compareTo()` trên `Integer` phải: (1) kiểm tra null, (2) unbox 2 Integer về int, (3) gọi method ảo (virtual dispatch). Với `int`, phép so sánh biên dịch thành 1 lệnh CMP. Insertion sort cấp k thực hiện ~k²/4 phép so sánh; khi mỗi phép rẻ hơn ~5–10 lần thì CUTOFF tối ưu dịch cao lên tương ứng.

### Về lựa chọn Java 6 (CUTOFF = 7) vs bản mới (CUTOFF = 10)

Kết quả **ủng hộ việc chọn 10 hơn 7** trong phần lớn tình huống đã khảo sát. Với `Comparable[]` random, 10 nhanh hơn 7 ở N = 500k và 1M (±0.3%, trong sai số). Với `int[]`, 10 nhanh hơn rõ (~2–3% ở N lớn). Trên sorted/nearly, 10 cũng nhỉnh hơn 7. Chênh lệch nhỏ, nhưng xu hướng nhất quán - phù hợp với việc giáo trình cập nhật từ 7 lên 10.

## 6. Threats to validity

1. **Chỉ chạy trên 1 máy**. Mọi kết luận về vị trí optimum (10 vs 15 vs 25) chỉ đúng cho Intel Raptor Lake-H + JDK 25 HotSpot. Trên ARM, AMD, JDK khác - số liệu có thể dịch.
2. **Turbo Boost dao động tần số** 2.1–4.6 GHz. Không tắt chủ động (chỉ High performance plan). Bù bằng: 30 rep + xáo trộn thứ tự + báo trung vị thay vì trung bình.
3. **E-core**. Đã ghim `/affinity FF` (4 P-core); giả định Windows scheduler tôn trọng. Chưa xác minh từng rep có thực sự chạy trên P-core không.
4. **GC**. Đã cố định heap 2 GB + AlwaysPreTouch. Nhưng với `Comparable[]`, allocate `aux = new Comparable[N]` mỗi lần `Merge.sort` → GC có thể chạy. Chưa đo tần suất GC riêng.
5. **Hiệu ứng cache** gây lệch doubling hypothesis cho Comparable ở N = 200k. Lý tưởng là chạy nhiều N quanh ngưỡng này để khoanh vùng.
6. **Nhiễu đo với N nhỏ** (N ≤ 20k, median ≤ 1 ms): phép đo quanh 100 μs chịu sai số lớn từ OS scheduling. Đã bỏ khỏi phân tích chính.

## 7. Kết luận

- **Giá trị CUTOFF phù hợp nhất với cấu hình máy thí nghiệm**:
  - Use case chung (random data, không biết trước): **CUTOFF = 10** là lựa chọn an toàn và gần tối ưu.
  - Nếu tối ưu cho `int[]` nói riêng: **15–25** nhanh hơn một chút.
  - Nếu biết dữ liệu gần sắp xếp: CUTOFF rất lớn (≥ 64) là tốt nhất.
- **Hiệu quả của cải tiến CUTOFF so với mergesort thuần**:
  - `Comparable[]`: tiết kiệm 5–10% trên dữ liệu ngẫu nhiên, lên tới 35–43% trên sorted.
  - `int[]`: tiết kiệm 12–16% trên dữ liệu ngẫu nhiên, lên tới 50%+ trên sorted.
- **Lý do giáo trình cập nhật 7 → 10**: dữ liệu thí nghiệm xác nhận 10 nhỉnh hơn 7 trên cả 2 kiểu dữ liệu, đúng với xu hướng phần cứng mới.
- **Phương pháp khoa học của slide 1.4** đã được áp dụng đầy đủ: quan sát → giả thiết chữ U → dự đoán doubling N log N → kiểm chứng trên 5 phân bố × 2 kiểu dữ liệu.

## 8. Phụ lục: cách chạy lại

Xem `README.md` ở gốc repo. Tóm tắt:

```powershell
powershell -ExecutionPolicy Bypass -File scripts\sysinfo.ps1
powershell -ExecutionPolicy Bypass -File scripts\build.ps1
powershell -ExecutionPolicy Bypass -File scripts\run_full.ps1   # ~100 phut
python scripts\analyze.py
```

Toàn bộ dữ liệu đo + summary + plots nằm trong `results/`. Dữ liệu random tái lập được nhờ seed cố định (42).
