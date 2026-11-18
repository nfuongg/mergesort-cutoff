# Thí nghiệm CUTOFF cho Mergesort

> Báo cáo sẽ được viết đầy đủ sau khi có dữ liệu thực tế (D5). File này đang là skeleton.

## 1. Mục tiêu

Xác định giá trị CUTOFF phù hợp để chuyển sang Insertion sort trong Mergesort, so với hai mốc tham chiếu CUTOFF = 7 (Java 6, slide 2.2 trang 22) và CUTOFF = 10 (bản mới, slide 2.2 trang 18).

## 2. Phương pháp (áp dụng khung chương 1.4 Analysis of Algorithms)

- Observe: đo thời gian chạy trên máy thực.
- Hypothesize: thời gian T(N) ~ a · N log N với hệ số a phụ thuộc CUTOFF, kỳ vọng có vùng CUTOFF tối thiểu hóa a.
- Predict / Verify: chạy với nhiều N, so sánh tỉ số thời gian với mô hình.
- Validate: lặp lại trên nhiều phân bố, hai kiểu dữ liệu.

## 3. Cấu hình

Xem `results/sysinfo.txt`.

## 4. Thiết kế thí nghiệm

- CUTOFF: 1..32, 40, 48, 64, 96, 128
- N: 1k..1M (thang đôi/mười)
- Phân bố: random, sorted, reverse, nearly, duplicates
- Kiểu: `Comparable[]` (Integer boxed) và `int[]`
- Lặp: 30 lần/cấu hình, warmup JIT, xáo trộn thứ tự chạy
- Ghim P-core, heap cố định 2 GB, cùng seed

## 5. Kết quả

TODO: điền sau khi chạy D4.

### 5.1 Comparable[]
### 5.2 int[]
### 5.3 Ảnh hưởng của phân bố dữ liệu

## 6. Thảo luận

TODO.

## 7. Threats to validity

- Turbo Boost dao động tần số CPU.
- Lịch Windows có thể đẩy tiến trình sang E-core nếu affinity không hiệu lực.
- GC: đã cố định heap + AlwaysPreTouch nhưng không loại trừ hoàn toàn.
- Chỉ đo 1 máy -> kết luận chỉ đúng cho cấu hình này.

## 8. Kết luận

TODO.

## 9. Phụ lục: cách chạy lại

Xem README.md.
