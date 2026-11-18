# Ghi chú sử dụng AI

Project này cho phép dùng AI để **thảo luận, viết script chạy thí nghiệm, xây dựng bộ dữ liệu test**. Phần cài đặt mergesort phải tự dựng từ nội dung bài giảng.

## Phần sinh viên tự làm

- `src/main/java/cutoff/Merge.java` — mergesort Comparable[] với CUTOFF, viết theo slide 2.2 trang 7–8 và trang 18.
- `src/main/java/cutoff/Insertion.java` — insertion sort trên [lo, hi], viết theo slide 2.1 trang 26.
- `src/main/java/cutoff/MergeInt.java` — bản int[] của Merge.
- `src/main/java/cutoff/InsertionInt.java` — bản int[] của Insertion.
- Chạy toàn bộ thí nghiệm trên máy thật, thu thập kết quả.
- Viết phần phân tích và kết luận trong báo cáo.

## Phần AI hỗ trợ

- Dựng khung thư mục repo.
- Viết `DataGen.java`, `Verify.java`, `Harness.java` (khung đo thời gian).
- Viết script PowerShell: `sysinfo`, `build`, `run_smoke`, `run_pilot`, `run_full`.
- Viết `analyze.py` (pandas + matplotlib).
- Thiết kế thí nghiệm (chọn dải CUTOFF, N, loại dữ liệu, số lần lặp).
- Review code `Merge.java` / `Insertion.java` do sinh viên viết.
- Soạn nháp báo cáo từ dữ liệu kết quả thực tế.

## Log chat

Nếu nộp, log chat đầy đủ sẽ được xuất riêng cuối project.
