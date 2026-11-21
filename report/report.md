# Thí nghiệm CUTOFF cho Mergesort

---

## 1. Mục tiêu

Slide 2.2 "Mergesort: practical improvements" đề xuất cải tiến chuyển sang Insertion sort cho mảng con nhỏ: khi đoạn còn lại có kích thước không quá CUTOFF thì gọi `Insertion.sort(a, lo, hi)` thay vì tiếp tục đệ quy. Giáo trình nhắc hai giá trị CUTOFF: 7 và khoảng 10.

Câu hỏi: giá trị CUTOFF nào phù hợp với cấu hình máy thực tế, và cải tiến này mang lại bao nhiêu phần trăm tiết kiệm?

## 2. Phương pháp

Áp dụng khung "scientific method": observe → hypothesize → predict → verify → validate.

- Observe: đo thời gian `Merge.sort(a)` và đếm số phép so sánh với nhiều giá trị CUTOFF, nhiều N, nhiều phân bố.
- Hypothesize: T(N) ~ a · N log N, hệ số a phụ thuộc CUTOFF. Kỳ vọng có vùng CUTOFF tối thiểu a vì (i) CUTOFF = 1 gánh toàn bộ overhead đệ quy của mergesort; (ii) CUTOFF rất lớn thì insertion sort O(k²) chiếm ưu thế. Giữa hai cực là vùng tốt.
- Predict/Verify: so sánh tỉ số T(2N)/T(N) với dự đoán của N log N (xấp xỉ 2.1 trong vùng N đang đo), ước lượng số mũ b trong fit T = a·N^b.
- Validate: lặp lại trên 5 phân bố × 2 kiểu phần tử.

### 2.1. Cài đặt

- `Insertion.java` / `InsertionInt.java` - slide 2.1 trang 26, mở rộng sang dải `[lo, hi]`.
- `Merge.java` / `MergeInt.java` - hàm `merge` theo slide 2.2 trang 7; hàm `sort` theo trang 18, kiểm tra cutoff bằng
  ```java
  if (hi <= lo + CUTOFF - 1) { Insertion.sort(a, lo, hi); return; }
  ```
  Điều kiện này tương đương với `(hi - lo + 1) <= CUTOFF`, nghĩa là *mảng con có nhiều nhất CUTOFF phần tử sẽ được đưa cho insertion sort*. `CUTOFF = 1` chuyển về mergesort thuần (mỗi mảng 1 phần tử được "sắp" bởi insertion mà không làm gì). Dạng viết `hi <= lo + CUTOFF - 1` tránh phép cộng `hi - lo + 1` so với `<=` trực tiếp.

Bộ đếm `compares` được thêm vào Merge/Insertion để đo cost model (slide 1.4). Khi `count = false` (mặc định trong các lần đo timing) chi phí chỉ là một nhánh `if (count)` mà JIT và branch predictor xử lý gần như miễn phí; counting pass chạy riêng với `count = true`, 1 rep/cell (kết quả deterministic).

Khung đo (`Harness.java`), sinh dữ liệu (`DataGen.java`), kiểm tra (`Verify.java`) do AI hỗ trợ.

### 2.2. Thiết kế thí nghiệm

| Biến | Giá trị |
|---|---|
| CUTOFF | 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 18, 20, 24, 32, 48, 64 (22 giá trị) |
| N | 50k, 100k, 200k, 400k, 500k, 800k, 1M, 2M (8 giá trị; có 4 cặp đúng gấp đôi: 50↔100k, 100↔200k, 200↔400k, 400↔800k, 500↔1M, 1↔2M) |
| Phân bố | random, sorted, reverse, nearly-sorted (99% sắp, 1% swap), duplicates (√N giá trị phân biệt) |
| Kiểu | `Comparable[]` (Integer boxed), `int[]` primitive |
| Timing reps | 30 lần/cấu hình |
| Counting reps | 1 lần/cấu hình (deterministic), 3 N: 100k, 500k, 1M |
| Warmup | 5 vòng JIT |

Tổng: 2 × 26 400 = **52 800** dòng timing + 660 dòng counting. Checksum khớp hoàn toàn, 0 lỗi.

### 2.3. Quy ước đo để giảm nhiễu

- Ghim JVM vào 4 P-core (`start /b /affinity FF /wait java ...`). Raptor Lake-H có 4 P-core + 4 E-core; E-core chậm hơn nhiều và dễ làm nhiễu phép đo.
- Cố định heap `-Xms2g -Xmx2g` để GC không co giãn giữa phép đo.
- Warmup JIT trước khi đo (xem `Harness.warmupJIT`).
- Xáo trộn thứ tự chạy các cấu hình (seed cố định) để tránh sai lệch do nhiệt/turbo CPU.
- Mỗi cấu hình dùng cùng seed dữ liệu - các CUTOFF so sánh trên cùng một mảng input.
- Clone mảng ngoài vùng đo thời gian.
- Power plan `High performance`, cắm sạc, đóng app nền trước khi đo.
- Hash kiểm tra: `Arrays.hashCode(a)` sau mỗi lần sort.

Xem `results/sysinfo.txt` để đối chiếu cấu hình máy tại thời điểm đo.

## 3. Cấu hình máy

| | |
|---|---|
| Laptop | ASUS Vivobook 16X K3605VC |
| CPU | Intel Core i5-13420H (4 P-core + 4 E-core, 12 luồng, base 2.1 GHz) |
| Cache | L1 704 KB, L2 7 MB, L3 12 MB |
| RAM | 16 GB |
| OS | Windows 11 Home build 26200 |
| Java | Oracle JDK 25.0.2 LTS, HotSpot 64-Bit Server VM |
| Power plan khi đo | High performance |

## 4. Kết quả

Các số liệu dưới đây lấy trực tiếp từ `results/summary/comparison.csv` và `best_cutoff.csv`. Mọi bảng và đồ thị trong báo cáo đều có thể tái lập bằng `python scripts/analyze.py` trên dữ liệu thô trong `results/raw/`.

### 4.1. Random data

Bảng trung vị (ms) cho một số CUTOFF tiêu biểu, phân bố random:

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

Vùng CUTOFF tương đương (các giá trị có CI95 của trung vị chồng lên trung vị tốt nhất) cho random:

| Type | N | Best | Vùng tương đương |
|---|---:|---:|---|
| Comparable | 500k | 8 | 4, 7, 8, 9, 11, 12, 13, 14 |
| Comparable | 1M | 12 | 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14 |
| Comparable | 2M | 12 | 4, 7, 8, 9, 10, 11, 12, 13, 14 |
| int | 500k | 48 | 32, 48 |
| int | 1M | 18 | 16, 18, 20, 24 |
| int | 2M | 48 | 32, 48 |

Nhận xét:

- Vùng tương đương cho `Comparable[]` ở N lớn phủ cả CUTOFF 4 đến 14. Bên trong vùng này, chọn 7 hay 10 hay 12 không có khác biệt thống kê. Lời khuyên 10 của slide bản mới nằm đúng trong vùng tốt.
- Vùng tương đương cho `int[]` nằm cao hơn, khoảng 16–48. Nguyên nhân: mỗi phép so sánh `<` trên `int` rẻ hơn nhiều so với `compareTo()` ảo trên `Integer`, nên insertion sort "chịu được" k lớn hơn trước khi O(k²) trở nên đắt đỏ.
- Tiết kiệm tuyệt đối so với mergesort thuần: 5–7% với `Comparable[]`, 10–18% với `int[]`.

### 4.2. Các phân bố khác

| Dist | Best CUTOFF (Comparable 1M) | Best (int 1M) | Giải thích |
|---|---:|---:|---|
| random | 12 | 18 | Hình chữ U cổ điển |
| sorted | 64 | 64 | Insertion O(N) trên sorted. CUTOFF càng lớn càng ít merge → càng nhanh. |
| nearly | 64 | 64 | Tương tự sorted, chỉ 1% swap. |
| reverse | 4 | 10 | Insertion O(N²) trên reverse, phải tránh. CUTOFF nhỏ, chia cho mergesort xử lý. |
| duplicates | 9 | 16 | Trung gian giữa random và sorted. |

Lưu ý quan trọng về reverse và kết luận "10 tốt hơn 7":

Với `Comparable` reverse, CUTOFF = 10 chậm hơn CUTOFF = 7 ở các N lớn: N = 500k chậm 0.56%, N = 1M chậm 0.42%, N = 2M chậm 0.25% (xem `comparison.csv`). Chênh lệch nhỏ, trong vùng nhiễu, nhưng đủ để thấy "CUTOFF = 10 luôn tốt hơn 7" không phải khẳng định phổ quát: với `Comparable[]` reverse, 7 và 10 gần như tương đương, thậm chí 7 nhỉnh hơn. Với `int[]` thì 10 nhanh hơn 7 ở mọi phân bố.

Vùng tương đương cho reverse Comparable N = 1M: 2, 3, 4, 5, 6 - toàn CUTOFF nhỏ. CUTOFF = 7 và 10 đã nằm ngoài vùng tối ưu cho tình huống này.

### 4.3. Doubling hypothesis

Lấy các cặp N đúng gấp đôi, CUTOFF = 10:

| Type | Dist | N₁ → N₂ | T₂/T₁ | Expected |
|---|---|---:|---:|---:|
| int | random | 100k → 200k | 2.13 | 2.12 |
| int | random | 200k → 400k | 2.13 | 2.11 |
| int | random | 400k → 800k | 2.11 | 2.11 |
| int | random | 500k → 1M | 2.13 | 2.11 |
| int | random | 1M → 2M | 2.10 | 2.10 |
| Comparable | random | 100k → 200k | 3.21 | 2.12 |
| Comparable | random | 200k → 400k | 2.22 | 2.11 |
| Comparable | random | 400k → 800k | 2.25 | 2.11 |
| Comparable | random | 500k → 1M | 2.25 | 2.11 |
| Comparable | random | 1M → 2M | 2.27 | 2.10 |

`int[]` khớp dự đoán N log N gần như hoàn hảo (sai số < 2%).

`Comparable[]` có một anomaly rõ ở cặp 100k → 200k: tỉ số thực đo là 3.21 thay vì 2.12. Hiện tượng này xuất hiện ở MỌI phân bố của `Comparable[]`:
- duplicates: 3.49
- nearly: 4.53
- random: 3.21
- reverse: 5.06
- sorted: 5.06

và không xuất hiện ở `int[]`. Lý do có khả năng nhất là áp lực GC/allocator khi `Integer` boxing, vì ở N = 200k có tổng cộng 400k Integer object (input + aux), cộng với các vùng young generation khác, có thể vượt ngưỡng kích hoạt minor GC trong vòng đo. Để xác nhận cần chạy lại với `-Xlog:gc:file=gc.log` và thêm các mốc N trong khoảng 100k–200k để khoanh ngưỡng.

Fit power law T = a · N^b với CUTOFF = 10:

| Type | Dist | b |
|---|---|---:|
| Comparable | random | 1.275 |
| Comparable | sorted | 1.364 |
| int | random | 1.083 |
| int | sorted | 1.094 |

`int[]` có b ≈ 1.08, rất sát dự đoán lý thuyết cho N log N trong khoảng N đã đo (log N tăng khoảng 1.3× khi N tăng 40× ⇒ b thực nghiệm ≈ 1 + log(1.3)/log(40) ≈ 1.07). `Comparable[]` có b cao hơn do anomaly 100k → 200k kéo đường fit lên.

### 4.4. Cost model theo compare count

Đếm số phép so sánh (deterministic, 1 rep) cho random N = 1M:

| CUTOFF | compares | compares / (N log N) |
|---:|---:|---:|
| 1 | 18 674 892 | 0.937 |
| 7 | 18 822 569 | 0.944 |
| 10 | 19 073 077 | 0.957 |
| 14 | 19 073 077 | 0.957 |
| 16 | 20 225 581 | 1.015 |
| 32 | 23 186 972 | 1.163 |
| 64 | 29 901 732 | 1.500 |

Trong vùng CUTOFF 7–14, số compare chỉ tăng ~2% so với mergesort thuần (CUTOFF = 1). Từ CUTOFF = 16 trở đi compare bắt đầu nhảy mạnh (+8% rồi +25% rồi +60%). Đây là lời giải vì sao vùng tối ưu về thời gian cũng nằm ở khoảng 7–14: trong vùng này, insertion sort làm thêm ít compare nhưng tiết kiệm được overhead đệ quy, cấp phát `aux`, lời gọi merge; từ CUTOFF ≈ 20 trở đi, compare tăng vượt lợi ích thu được.

Trên sorted, compare count ngược lại: GIẢM khi CUTOFF tăng, vì insertion trên sorted chỉ cần N – 1 compare (một phép so sánh để xác nhận đã đúng chỗ, không swap). Tại CUTOFF = 64, compare giảm còn 7.99M (0.40 × N log N) so với mergesort thuần 10.07M (0.51). Điều này giải thích vì sao trên sorted/nearly, CUTOFF càng lớn càng tốt.

Mô hình compare khớp với mô hình timing: hai chỉ số cùng chỉ cùng một vùng tối ưu, xác nhận rằng thời gian chạy phần lớn bị chi phối bởi số compare + overhead đệ quy, không phải bởi hiệu ứng cache hay phụ thuộc kiến trúc khác.

## 5. Thảo luận

### Vì sao CUTOFF giúp tiết kiệm thời gian

Insertion sort có chi phí hằng số thấp hơn mergesort cho mảng nhỏ: không allocate `aux`, không gọi đệ quy, truy cập bộ nhớ liền kề thân thiện với cache. Mergesort đệ quy xuống tận mảng 1–2 phần tử tốn rất nhiều lời gọi hàm so với công việc thực sự. CUTOFF cắt đệ quy ở ngưỡng mà "overhead/công việc" của mergesort vượt "O(k²)/công việc" của insertion sort.

### Vì sao optimum của `int[]` cao hơn `Comparable[]`

Mỗi phép `compareTo()` trên `Integer` phải (i) kiểm tra null, (ii) unbox 2 Integer về int, (iii) gọi method ảo. Với `int`, phép so sánh biên dịch thành 1 lệnh CMP. Insertion sort cấp k thực hiện ~k²/4 compare trung bình; khi mỗi compare rẻ hơn ~5–10×, CUTOFF tối ưu dịch cao lên tương ứng. Số liệu compare count ở 4.4 cho thấy CUTOFF = 32 trên random có compare = 1.163 × N log N (tăng 23% so với mergesort thuần) - chi phí đó đắt với `Comparable` nhưng rẻ với `int`, nên optimum của `int` dịch sang 32–48.

### Về giáo trình: CUTOFF 7 so với 10

Dữ liệu không đủ để khẳng định bên nào "tốt hơn" một cách tuyệt đối. Với `Comparable[]` random/sorted/nearly ở N lớn, cả 7 và 10 đều nằm trong vùng tương đương; chênh lệch giữa chúng nhỏ hơn CI95. Với `Comparable[]` reverse, 7 nhỉnh hơn 10 một chút. Với `int[]`, 10 nhanh hơn 7 ở mọi phân bố. Nếu chọn một số duy nhất cho use case chung trên máy này, 10 là lựa chọn hợp lý: không thua 7 ở hầu hết tình huống và nhanh hơn rõ với `int[]`.

Về suy đoán "cập nhật 7 → 10 là do xu hướng phần cứng mới": dữ liệu một máy không đủ để kết luận. Cần so sánh cùng thí nghiệm trên nhiều CPU/JDK để kiểm chứng. Báo cáo này không khẳng định điều đó.

## 6. Hạn chế của thí nghiệm

1. Chỉ chạy trên 1 máy. Kết luận về vị trí optimum chỉ đúng cho Intel Raptor Lake-H + JDK 25 HotSpot. CPU khác, JDK khác có thể cho optimum khác.
2. Turbo Boost dao động tần số (base 2.1 GHz, boost tối đa 4.6 GHz) không tắt chủ động. Bù bằng 30 rep + xáo trộn + báo trung vị. CI95 từ bootstrap đã phản ánh phần dao động này.
3. E-core. Đã ghim `/affinity FF` (bits 0..7 = 4 P-core); giả định Windows scheduler tôn trọng. Chưa dùng công cụ như Process Explorer để xác minh từng rep.
4. GC. Heap cố định 2 GB nhưng vẫn có young generation. Anomaly ở Comparable 100k → 200k nghi do GC - cần `-Xlog:gc` để xác nhận.
5. Chỉ đếm compares, không đếm array accesses. Mô hình cost của slide 1.4 gồm cả hai; array accesses ít thông tin hơn trong bài này vì cả merge và insertion đều đọc/ghi mảng tuyến tính.
6. Không kiểm soát nhiệt CPU. Laptop có thể throttle khi chạy liên tục 90+ phút. Dữ liệu không cho thấy xu hướng suy giảm theo thời gian (do xáo trộn thứ tự), nhưng chưa đo trực tiếp nhiệt độ.

## 7. Kết luận

- Giá trị CUTOFF phù hợp với cấu hình máy thí nghiệm, dữ liệu ngẫu nhiên:
  - `Comparable[]`: vùng tương đương rộng, khoảng 4–14. CUTOFF = 10 là lựa chọn an toàn.
  - `int[]`: vùng tương đương dịch cao hơn, khoảng 16–48. Nếu chỉ sắp xếp int, CUTOFF 20–32 nhanh hơn 10 khoảng 3–6%.
- Hiệu quả so với mergesort thuần: tiết kiệm 5–7% (`Comparable[]` random) và 10–18% (`int[]` random); lên tới 30–50% trên sorted/nearly.
- CUTOFF = 7 vs 10: khác biệt nhỏ, phụ thuộc phân bố. Không khẳng định được bên nào tốt hơn tuyệt đối; cả hai đều nằm trong vùng tương đương cho các trường hợp thường gặp. Dữ liệu này không chứng minh được giả thuyết "cập nhật 7 → 10 do xu hướng phần cứng".
- Mô hình chi phí theo số phép so sánh khớp với mô hình thời gian: cả hai cùng xác định vùng CUTOFF tối ưu - bằng chứng rằng thời gian chạy chủ yếu được quyết định bởi số compare cộng overhead đệ quy.

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
