# Thí nghiệm CUTOFF cho Mergesort

Tác giả: Phuong · Ngày: 2026-11-21

---

## 1. Mục tiêu

Slide 2.2 "Mergesort: practical improvements" đề xuất cải tiến chuyển sang Insertion sort cho mảng con nhỏ: khi đoạn còn lại có kích thước không quá CUTOFF thì gọi `Insertion.sort(a, lo, hi)` thay vì tiếp tục đệ quy. Giáo trình nhắc hai giá trị: 7 (Java 6 `Arrays.sort`, slide tr.22) và 10 (slide bản mới, tr.18).

Câu hỏi: giá trị CUTOFF nào phù hợp với cấu hình máy thực tế, và cải tiến này mang lại bao nhiêu phần trăm tiết kiệm?

## 2. Phương pháp

Áp dụng khung của slide 1.4: observe → hypothesize → predict → verify → validate.

- Observe: đo thời gian `Merge.sort(a)` và đếm số phép so sánh với nhiều giá trị CUTOFF, nhiều N, nhiều phân bố.
- Hypothesize: T(N) ~ a · N log N, hệ số a phụ thuộc CUTOFF. Kỳ vọng có vùng CUTOFF tối thiểu a vì (i) CUTOFF = 1 gánh toàn bộ overhead đệ quy; (ii) CUTOFF rất lớn thì insertion sort O(k²) chiếm ưu thế.
- Predict/Verify: so sánh tỉ số T(2N)/T(N) với dự đoán của N log N, ước lượng số mũ b trong fit T = a·N^b.
- Validate: lặp lại trên 5 phân bố × 2 kiểu phần tử.

### 2.1. Cài đặt

Toàn bộ code Java 25 thuần, không dùng `algs4.jar`.

Các file thuật toán cốt lõi được viết lại từ slide, không có tối ưu khác ngoài tham số CUTOFF:

- `Insertion.java` / `InsertionInt.java` — slide 2.1 tr.26, mở rộng sang dải `[lo, hi]`.
- `Merge.java` / `MergeInt.java` — hàm `merge` theo slide 2.2 tr.7; hàm `sort` theo tr.18, kiểm tra cutoff bằng
  ```java
  if (hi <= lo + CUTOFF - 1) { Insertion.sort(a, lo, hi); return; }
  ```
  Điều kiện này tương đương `(hi - lo + 1) <= CUTOFF`, nghĩa là mảng con có **nhiều nhất** CUTOFF phần tử sẽ được đưa cho insertion sort. CUTOFF = 1 chuyển về mergesort thuần (mỗi mảng 1 phần tử vẫn gọi insertion nhưng vòng lặp không làm gì). Cách viết `hi <= lo + CUTOFF - 1` chỉ là tối ưu vi mô: tránh phép cộng `hi - lo + 1`.

Bộ đếm `compares` được thêm vào Merge/Insertion để đo cost model (slide 1.4). Khi `count = false` (lúc đo thời gian), chỉ còn một nhánh `if (false) …` mà JIT xử lý gần như miễn phí; benchmark riêng cho thấy overhead so với bản không có flag < 2%. Counting pass chạy riêng với `count = true`, 1 rep/cell (kết quả deterministic nên không cần lặp).

Khung đo (`Harness.java`), sinh dữ liệu (`DataGen.java`), kiểm tra (`Verify.java`) do AI hỗ trợ — xem `docs/ai-usage.md`. Harness kiểm tra tính đúng đắn hai lớp sau mỗi lần sort: (1) mảng đã sắp `Verify.checkSorted(a)`; (2) multiset được bảo toàn qua `Verify.multisetFingerprint(a)` so với fingerprint lúc chưa sort. `analyze.py` còn kiểm tra tính nhất quán của `Arrays.hashCode(a)` qua mọi CUTOFF ở cùng (N, dist, rep) — nếu một CUTOFF nào đó sort sai thì hash sẽ khác.

### 2.2. Thiết kế thí nghiệm

| Biến | Giá trị |
|---|---|
| CUTOFF | 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 24, 32, 48, 64 (22 giá trị) |
| N | 50k, 100k, 200k, 400k, 500k, 800k, 1M, 2M (8 giá trị) |
| Cặp N gấp đôi | 50k↔100k, 100k↔200k, 200k↔400k, 400k↔800k, 500k↔1M, 1M↔2M (6 cặp) |
| Phân bố | random, sorted, reverse, nearly-sorted (99% sắp, 1% swap), duplicates (√N giá trị phân biệt) |
| Kiểu | `Comparable[]` (Integer boxed), `int[]` primitive |
| Timing reps | 30 lần/cấu hình |
| Counting reps | 1 lần/cấu hình (deterministic), 3 N: 100k, 500k, 1M |
| Warmup | 5 vòng JIT |

Tổng: 2 × 26,400 = 52,800 dòng timing + 660 dòng counting. Checksum nhất quán qua mọi CUTOFF (xác minh trong `analyze.py`), 0 lỗi multiset trong Harness.

### 2.3. Quy ước đo để giảm nhiễu

- Ghim JVM vào 4 P-core (`start /b /affinity FF /wait java ...`). Raptor Lake-H có 4 P-core + 4 E-core; E-core chậm hơn nhiều và dễ làm nhiễu phép đo.
- Cố định heap `-Xms2g -Xmx2g` để GC không co giãn giữa phép đo.
- Warmup JIT trước khi đo (xem `Harness.warmupJIT`).
- Xáo trộn thứ tự chạy các cấu hình (seed cố định) để tránh sai lệch do nhiệt/turbo CPU.
- Mỗi cấu hình dùng cùng seed dữ liệu — các CUTOFF so sánh trên cùng một mảng input.
- Clone mảng ngoài vùng đo thời gian.
- Power plan `High performance`, cắm sạc, đóng app nền — các mục này được `sysinfo.ps1` tự kiểm tra và ghi vào `results/sysinfo.txt`.

## 3. Cấu hình máy

| | |
|---|---|
| Laptop | ASUS Vivobook 16X K3605VC |
| CPU | Intel Core i5-13420H (4 P-core + 4 E-core, 12 luồng, base 2.1 GHz) |
| Cache | L2 7 MB (tổng), L3 12 MB |
| RAM | 16 GB |
| OS | Windows 11 Home build 26200 |
| Java | Oracle JDK 25.0.2 LTS, HotSpot 64-Bit Server VM |

Trạng thái lúc đo (auto-checked trong sysinfo): Power plan = High performance, plugged in = true, no heavy apps.

## 4. Kết quả

Mọi bảng và đồ thị trong báo cáo đều có thể tái lập bằng `python scripts/analyze.py` trên dữ liệu thô trong `results/raw/`.

### 4.1. Random data

Bảng trung vị (ms) với các CUTOFF chọn lọc, random data:

| Type | N | C=1 | C=7 | C=10 | Best CUTOFF | Best (ms) | Gain(C=7) | Gain(C=10) | Gain(best) |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---:|
| Comparable | 100k | 16.80 | 15.44 | 15.35 | 18 | 15.05 | 8.1% | 8.6% | 10.4% |
| Comparable | 500k | 151.45 | 141.62 | 142.35 | 8 | 140.30 | 6.5% | 6.0% | 7.4% |
| Comparable | 1M | 342.27 | 320.60 | 320.82 | 12 | 319.95 | 6.3% | 6.3% | 6.5% |
| Comparable | 2M | 768.14 | 732.63 | 728.04 | 12 | 725.26 | 4.6% | 5.2% | 5.6% |
| int | 100k | 7.86 | 6.90 | 6.86 | 32 | 6.32 | 12.2% | 12.7% | 19.5% |
| int | 500k | 44.37 | 40.03 | 38.85 | 48 | 36.40 | 9.8% | 12.4% | 18.0% |
| int | 1M | 94.00 | 84.63 | 82.76 | 18 | 78.16 | 10.0% | 12.0% | 16.8% |
| int | 2M | 196.23 | 178.30 | 173.46 | 48 | 163.83 | 9.1% | 11.6% | 16.5% |

Vùng CUTOFF tương đương (CI95 của trung vị chồng lên trung vị tốt nhất):

| Type | N | Best | Vùng tương đương |
|---|---:|---:|---|
| Comparable | 500k | 8 | 4, 7, 8, 9, 11, 12, 13, 14 |
| Comparable | 1M | 12 | 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 |
| Comparable | 2M | 12 | 4, 7, 8, 9, 10, 11, 12, 13, 14 |
| int | 500k | 48 | 32, 48 |
| int | 1M | 18 | 16, 18, 20, 24 |
| int | 2M | 48 | 32, 48 |

Nhận xét: vùng tương đương cho `Comparable[]` ở N lớn phủ CUTOFF 4 đến 14. Bên trong vùng này, chọn 7 hay 10 hay 12 không có khác biệt thống kê. Cho `int[]`, vùng dịch cao hơn, khoảng 16–48.

Nguyên nhân dịch cao hơn ở `int[]`: mỗi phép so sánh `<` trên `int` rẻ hơn nhiều so với `compareTo()` ảo trên `Integer`, nên insertion sort "chịu được" k lớn hơn trước khi O(k²) trở nên đắt đỏ.

Tiết kiệm tuyệt đối so với mergesort thuần: 5–7% với `Comparable[]`, 10–18% với `int[]`.

### 4.2. Các phân bố khác

| Dist | Best CUTOFF (Comparable 1M) | Best (int 1M) | Giải thích |
|---|---:|---:|---|
| random | 12 | 18 | Hình chữ U cổ điển |
| sorted | 64 | 64 | Insertion O(N) trên sorted — chi tiết ở 4.4 |
| nearly | 64 | 64 | Tương tự sorted, chỉ 1% swap |
| reverse | 4 | 10 | Insertion O(N²) trên reverse, phải tránh |
| duplicates | 9 | 16 | Trung gian |

**Reverse Comparable: 10 chậm hơn 7 có hệ thống.** So sánh trực tiếp T(C=10) với T(C=7) ở N lớn, phân bố reverse:

| N | T(C=7) ms | T(C=10) ms | C=10 chậm hơn C=7 |
|---:|---:|---:|---:|
| 500k | 109.70 | 113.40 | +3.37% |
| 1M | 231.85 | 238.98 | +3.08% |
| 2M | 489.44 | 503.28 | +2.83% |

Vùng tương đương cho reverse Comparable N = 1M là {2, 3, 4, 5, 6}, nghĩa là CUTOFF = 7 và 10 đều **ngoài** vùng tối ưu cho reverse. 7 vẫn gần optimum hơn 10. Khẳng định "CUTOFF = 10 luôn tốt hơn 7" không đúng ở đây.

### 4.3. Doubling hypothesis và power law

Lấy các cặp N đúng gấp đôi, CUTOFF = 10, random:

| Type | N₁ → N₂ | T₂/T₁ | Expected |
|---|---:|---:|---:|
| int | 100k → 200k | 2.13 | 2.12 |
| int | 200k → 400k | 2.13 | 2.11 |
| int | 400k → 800k | 2.11 | 2.11 |
| int | 500k → 1M | 2.13 | 2.11 |
| int | 1M → 2M | 2.10 | 2.10 |
| Comparable | 100k → 200k | **3.21** | 2.12 |
| Comparable | 200k → 400k | 2.22 | 2.11 |
| Comparable | 400k → 800k | 2.25 | 2.11 |
| Comparable | 500k → 1M | 2.25 | 2.11 |
| Comparable | 1M → 2M | 2.27 | 2.10 |

`int[]` khớp N log N gần như hoàn hảo (sai số < 2%). `Comparable[]` có một anomaly rõ ở cặp 100k → 200k (3.21 vs 2.12). Hiện tượng này xuất hiện ở MỌI phân bố của `Comparable[]`: duplicates 3.49, nearly 4.53, random 3.21, reverse 5.06, sorted 5.06.

**Fit power law T = a · N^b, chỉ dùng N ≥ 200k để loại anomaly:**

| Type | Dist | b (N ≥ 200k) | b (full range) | Lý thuyết |
|---|---|---:|---:|---:|
| int | random | 1.07 | 1.08 | ≈ 1.07 |
| int | sorted | 1.07 | 1.09 | ≈ 1.07 |
| Comparable | random | 1.15 | 1.28 | ≈ 1.07 |
| Comparable | sorted | 1.08 | 1.36 | ≈ 1.07 |
| Comparable | nearly | 1.09 | 1.33 | ≈ 1.07 |

`int[]` khớp lý thuyết. `Comparable[]` ngay cả trong dải N ≥ 200k (đã loại anomaly 100k→200k) vẫn có b ≈ 1.08–1.15, lệch có hệ thống 1–8% so với 1.07. Cặp doubling của Comparable vẫn 2.22–2.27 (expected 2.11) — chênh 5–8%.

Giả thuyết cho cả hai hiện tượng (anomaly + lệch hệ thống): áp lực trên allocator khi `Comparable[]` phải cấp phát `aux = new Comparable[N]` lớn dần theo N. `aux` chỉ là mảng tham chiếu (8N byte), không tạo thêm `Integer` object; nhưng mỗi lần `Merge.sort` cấp phát một mảng tham chiếu mới, và với Comparable ở N lớn, mảng này có thể qua ngưỡng chuyển sang Old generation hoặc kích hoạt GC minor trong vòng đo. **Giả thuyết này chưa được kiểm chứng** — cần chạy lại với `-Xlog:gc:file=gc.log` và thêm các mốc N trong khoảng 100k–200k để khoanh vùng (việc này tốn vài phút, có thể làm trong vòng cải tiến sau).

### 4.4. Cost model theo compare count

Đếm số phép so sánh (deterministic, 1 rep), random N = 1M:

| CUTOFF | compares | / (N log N) |
|---:|---:|---:|
| 1 | 18,674,892 | 0.937 |
| 7 | 18,822,569 | 0.944 |
| 10 | 19,073,077 | 0.957 |
| 14 | 19,073,077 | 0.957 |
| 16 | 20,225,581 | 1.015 |
| 32 | 23,186,972 | 1.163 |
| 64 | 29,901,732 | 1.500 |

Mô hình compare mô tả được **vế phải** của hình chữ U nhưng không mô tả được **vế trái**:

- Khi CUTOFF tăng, compare count tăng rõ (0.94 → 1.50 × N log N). Giải thích tại sao CUTOFF ≥ 20 bắt đầu chậm.
- Nhưng CUTOFF nhỏ nhất (1–3) lại có compare count THẤP NHẤT (0.937). Nếu chỉ nhìn compare, phải kết luận CUTOFF = 1 là tối ưu — trái với timing.
- Hơn nữa, `int[]` và `Comparable[]` có compare count **giống hệt nhau** (cùng thuật toán) nhưng điểm tối ưu timing khác nhau (12 vs 18–48). Nghĩa là compare không phải chi phí duy nhất.

Cost thực tế có hai thành phần:
1. **Compare + data movement** — tăng theo CUTOFF (vế phải U), bắt được bằng counter.
2. **Overhead đệ quy + cấp phát `aux` + lời gọi merge** — giảm khi CUTOFF tăng (vế trái U), KHÔNG bắt được bằng counter.

Vùng CUTOFF tối ưu là nơi hai thành phần cân bằng. Compare count giải thích điểm cắt dưới của vùng; overhead đệ quy giải thích điểm cắt trên.

Trên sorted, compare count ngược lại GIẢM khi CUTOFF tăng (10.07M xuống 7.99M), vì insertion trên sorted chỉ cần N – 1 compare. Giải thích vì sao CUTOFF càng lớn càng tốt trên sorted/nearly.

### 4.5. Lượng tử hóa CUTOFF theo độ sâu đệ quy

Quan sát từ `counts.csv` cho N = 1M, random (`Comparable` và `int` giống nhau vì cùng thuật toán):

| compares | CUTOFF cho cùng số compare (cùng một bin) |
|---:|---|
| 18,674,892 | 1, 2 |
| 18,728,384 | 4, 5, 6 |
| 18,822,569 | 7 |
| **19,073,077** | **8, 9, 10, 11, 12, 13, 14** |
| 19,889,337 | 15 |
| 20,225,581 | 16, 18, 20, 24 |
| 23,186,972 | 32, 48 |
| 29,901,732 | 64 |

Toàn bộ dải CUTOFF ∈ {8, 9, 10, 11, 12, 13, 14} cho ra **cùng một thuật toán** vì mảng con ở tầng lá sâu nhất luôn có cỡ 7–8 khi mảng gốc có N ≈ 10⁶. Điều kiện `hi <= lo + CUTOFF - 1` chỉ "nhìn thấy" các kích thước lá thực tế mà cây đệ quy tạo ra:

- Tầng lá sâu nhất: cỡ 7–8 → CUTOFF 8..14 có cùng tập lá.
- Tầng áp cuối: cỡ 15–16 → CUTOFF 16..24 nhảy sang bin kế.
- Tầng trên: cỡ 31–32 → CUTOFF 32, 48 chung bin.
- CUTOFF = 64 bắt đầu "cắt" sớm hơn một tầng → bin riêng.

**Hệ quả quan trọng cho diễn giải "best CUTOFF":** các giá trị "best" dao động giữa các N (12 ở 1M, 8 ở 500k, 18 ở `int` 1M, 48 ở `int` 2M…) **không phản ánh sự khác biệt về thuật toán** — tất cả đều trong cùng một bin. "Best = 12" không có nghĩa là 12 tốt hơn 10; chỉ có nghĩa là trong rep cụ thể, CUTOFF 12 ngẫu nhiên có trung vị thấp nhất **trong bin 8–14 vốn tương đương**. Vùng tương đương tính qua CI95 đã bắt được điều này (vùng {4..14} ở Comparable 1M bao trùm hết bin 8–14 cộng với vài CUTOFF nhỏ hơn mà bin khác nhưng CI chồng).

Để thực sự phân biệt được CUTOFF nào tốt hơn trong bin, cần thêm các mốc N không gần bội của 2 (ví dụ N = 700k, 1.3M, 1.7M) để cây đệ quy có cấu trúc lá khác, đẩy các CUTOFF trong bin về các bin khác nhau. Đây là hướng cải tiến cho lần đo tiếp theo.

## 5. Thảo luận

### Vì sao CUTOFF giúp tiết kiệm thời gian

Insertion sort có chi phí hằng số thấp hơn mergesort cho mảng nhỏ: không allocate `aux`, không gọi đệ quy, truy cập bộ nhớ liền kề. Mergesort đệ quy xuống tận mảng 1–2 phần tử tốn rất nhiều lời gọi hàm. CUTOFF cắt đệ quy ở ngưỡng mà overhead mergesort vượt O(k²) của insertion.

### Vì sao optimum của `int[]` cao hơn `Comparable[]`

Mỗi phép `compareTo()` trên `Integer` phải (i) kiểm tra null, (ii) unbox 2 Integer về int, (iii) gọi method ảo. Với `int`, phép so sánh biên dịch thành 1 lệnh CMP. Insertion sort cấp k thực hiện ~k²/4 compare trung bình; khi mỗi compare rẻ hơn ~5–10×, CUTOFF tối ưu dịch cao lên tương ứng.

### CUTOFF 7 so với 10

Dữ liệu không ủng hộ khẳng định "10 luôn tốt hơn 7":
- Với `Comparable[]` random/sorted/nearly ở N lớn, cả 7 và 10 nằm trong vùng tương đương; chênh lệch nhỏ hơn CI95.
- Với `Comparable[]` reverse, 7 nhanh hơn 10 khoảng 3% một cách có hệ thống.
- Với `int[]`, 10 nhanh hơn 7 ở mọi phân bố, nhưng vùng tối ưu thực sự ở `int[]` còn cao hơn (16–48).

Về suy đoán "cập nhật 7 → 10 là do xu hướng phần cứng mới": dữ liệu một máy không đủ để kết luận. Cần so sánh cùng thí nghiệm trên nhiều CPU/JDK.

## 6. Hạn chế của thí nghiệm

1. Chỉ chạy trên 1 máy. Kết luận về vị trí optimum chỉ đúng cho Intel Raptor Lake-H + JDK 25 HotSpot.
2. Turbo Boost không tắt chủ động (base 2.1 GHz, boost tối đa 4.6 GHz). Bù bằng 30 rep + xáo trộn + trung vị + CI95 bootstrap.
3. E-core. Đã ghim `/affinity FF`; chưa dùng công cụ như Process Explorer xác minh từng rep.
4. Anomaly 100k → 200k và độ lệch 5–8% ở Comparable chưa được xác nhận bằng `-Xlog:gc`.
5. Chỉ đếm compare, không đếm array accesses. Mô hình cost model của slide 1.4 có cả hai, nhưng với mergesort array accesses hầu hết đi theo compare cộng với copy `aux`.
6. Dải N chủ yếu là bội của 10⁵; không đủ các mốc N "lẻ" để phân biệt các CUTOFF trong cùng một bin lượng tử hóa (xem 4.5).
7. Không kiểm soát nhiệt CPU. Dữ liệu không cho thấy xu hướng suy giảm theo thời gian (do xáo trộn), nhưng chưa đo trực tiếp.

## 7. Kết luận

- Giá trị CUTOFF phù hợp với cấu hình máy thí nghiệm, dữ liệu ngẫu nhiên:
  - `Comparable[]`: vùng tương đương rộng, CUTOFF 4–14 (gần như là cùng một bin lượng tử hóa cộng vài giá trị kế). CUTOFF = 10 là lựa chọn an toàn trong vùng này.
  - `int[]`: vùng dịch cao hơn, CUTOFF 16–48. Nếu chỉ sắp xếp `int`, CUTOFF 20–32 nhanh hơn 10 khoảng 3–6%.
- Hiệu quả so với mergesort thuần: tiết kiệm 5–7% (`Comparable[]` random) và 10–18% (`int[]` random); lên tới 30–50% trên sorted/nearly.
- Khẳng định "CUTOFF 10 tốt hơn 7" không phổ quát. Trên `Comparable[]` reverse, 10 chậm hơn 7 có hệ thống (~3%). Cả hai giá trị đều nằm gần vùng tối ưu cho các trường hợp thường gặp.
- Compare count giải thích vế phải của hình chữ U (CUTOFF lớn → nhiều compare), nhưng không giải thích vế trái. Vế trái là overhead đệ quy + cấp phát `aux`, không bắt được bằng counter. Hai thành phần cân bằng nhau xác định vùng CUTOFF tối ưu.
- Phát hiện lượng tử hóa: nhiều CUTOFF liền nhau là cùng một thuật toán khi xét đến cấu trúc cây đệ quy. "Best CUTOFF" dao động giữa các rep thực chất là nhiễu giữa các CUTOFF tương đương.
- Dữ liệu một máy không chứng minh được giả thuyết "cập nhật 7 → 10 do xu hướng phần cứng mới".

## 8. Phụ lục: cách chạy lại

Xem `README.md`. Tóm tắt:

```powershell
powercfg /setactive 8c5e7fda-e8bf-4a96-9a85-a6e23a8c635c
powershell -ExecutionPolicy Bypass -File scripts\sysinfo.ps1
powershell -ExecutionPolicy Bypass -File scripts\build.ps1
powershell -ExecutionPolicy Bypass -File scripts\run_full.ps1    # ~90 phut
powershell -ExecutionPolicy Bypass -File scripts\run_counts.ps1  # ~1 phut
pip install -r requirements.txt
python scripts\analyze.py
```

Dữ liệu random tái lập được nhờ seed cố định (42). Toàn bộ raw CSV, summary CSV và plot đều được version-control trong repo.
