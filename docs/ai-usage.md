# Ghi chú sử dụng AI

Project cho phép dùng AI để thảo luận, viết script chạy thí nghiệm, xây bộ dữ liệu test. Phần cài đặt mergesort tự dựng từ slide.

## Sinh viên tự làm

- `src/main/java/cutoff/Merge.java` — mergesort Comparable[] với CUTOFF, theo slide 2.2 trang 7 (merge) và trang 18 (sort có CUTOFF).
- `src/main/java/cutoff/Insertion.java` — insertion sort dải `[lo, hi]`, theo slide 2.1 trang 26.
- `src/main/java/cutoff/MergeInt.java`, `InsertionInt.java` — bản `int[]` song song.
- Chạy toàn bộ thí nghiệm trên máy thật, thu thập kết quả.
- Đọc duyệt và chỉnh sửa báo cáo, kiểm tra số liệu.

Các trường `public static boolean count` và `public static long compares` trong 4 file trên được thêm sau để phục vụ counting pass (slide 1.4 cost model). Khi `count = false` (lúc đo thời gian) chi phí chỉ là một nhánh `if (false)` mà JIT loại bỏ gần như hoàn toàn (đã đo: overhead so với bản không có counter < 2%).

## AI hỗ trợ

- Dựng khung thư mục repo, `.gitignore`, `requirements.txt`, `LICENSE`.
- Viết `DataGen.java` (sinh dữ liệu theo seed + `main` để dump CSV), `Verify.java` (kiểm sắp xếp + fingerprint multiset), `Harness.java` (khung đo thời gian + counting, xáo trộn thứ tự, kiểm tra đa tập sau mỗi lần sort).
- Viết script PowerShell: `sysinfo` (có auto-check power plan, AC, app nền), `build`, `run_smoke`, `run_pilot`, `run_full`, `run_counts`.
- Viết `analyze.py` (pandas, matplotlib, bootstrap CI95 cho median, vùng tương đương, fit power law, phân tích quantization theo bin compare).
- Thiết kế thí nghiệm (dải CUTOFF, N, phân bố, số rep).
- Review code `Merge.java` / `Insertion.java` do sinh viên viết.
- Soạn nháp báo cáo từ số liệu thực.

## Quy trình đã làm

1. Dựng khung repo + Harness, SV viết 4 file thuật toán.
2. Smoke test → pilot → chốt dải CUTOFF/N.
3. Full run timing (~90 phút), counting pass (~1 phút).
4. Phân tích bằng `analyze.py`, soạn báo cáo từ bảng/đồ thị thực tế.
5. Review nhiều vòng: sửa các claim không khớp dữ liệu, bổ sung phân tích quantization + cost model.

## Log chat

Log phiên làm việc với AI có thể được xuất và nộp kèm nếu yêu cầu.
