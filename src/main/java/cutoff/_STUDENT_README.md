# Phần sinh viên tự viết

4 file dưới đây **phải tự viết từ nội dung bài giảng** (slide 2.2 Mergesort + 2.1 Elementary Sorts). Không copy từ nguồn khác.

## 1. `Insertion.java`

Theo slide 2.1 trang 26. API đúng như sau để Harness gọi được:

```java
package cutoff;

public class Insertion {
    // Sap xep a[lo..hi] (bao gom ca hi) bang insertion sort.
    // Dung less() + exch() dung tinh than slide.
    public static void sort(Comparable[] a, int lo, int hi) {
        // TODO: viet theo slide 2.1 trang 26
    }

    private static boolean less(Comparable v, Comparable w) {
        return v.compareTo(w) < 0;
    }

    private static void exch(Object[] a, int i, int j) {
        Object t = a[i]; a[i] = a[j]; a[j] = t;
    }
}
```

## 2. `Merge.java`

Theo slide 2.2 trang 7 (merge), trang 8 (sort), trang 18 (CUTOFF + insertion).
Giữ đúng tên biến `CUTOFF` public static để Harness chỉnh được:

```java
package cutoff;

public class Merge {
    public static int CUTOFF = 7;   // Harness se set lai truoc moi run

    // merge a[lo..mid] voi a[mid+1..hi] dung aux
    private static void merge(Comparable[] a, Comparable[] aux, int lo, int mid, int hi) {
        // TODO: slide 2.2 trang 7
    }

    private static void sort(Comparable[] a, Comparable[] aux, int lo, int hi) {
        // TODO: slide 2.2 trang 18 - cai tien CUTOFF:
        //   if (hi <= lo + CUTOFF - 1) { Insertion.sort(a, lo, hi); return; }
        //   int mid = lo + (hi - lo) / 2;
        //   sort(a, aux, lo, mid);
        //   sort(a, aux, mid + 1, hi);
        //   // (co the them: if (!less(a[mid+1], a[mid])) return;  -- slide trang 17)
        //   merge(a, aux, lo, mid, hi);
    }

    public static void sort(Comparable[] a) {
        Comparable[] aux = new Comparable[a.length];
        sort(a, aux, 0, a.length - 1);
    }

    private static boolean less(Comparable v, Comparable w) {
        return v.compareTo(w) < 0;
    }
}
```

## 3. `InsertionInt.java` và `MergeInt.java`

Phiên bản int[] của hai file trên. Cùng cấu trúc, thay `Comparable` bằng `int`, so sánh bằng `<` thay vì `compareTo`.

```java
package cutoff;

public class InsertionInt {
    public static void sort(int[] a, int lo, int hi) {
        // TODO
    }
}

public class MergeInt {
    public static int CUTOFF = 7;

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi) { /* TODO */ }
    private static void sort(int[] a, int[] aux, int lo, int hi) { /* TODO - voi CUTOFF */ }
    public static void sort(int[] a) {
        int[] aux = new int[a.length];
        sort(a, aux, 0, a.length - 1);
    }
}
```

## Kiểm tra

Sau khi viết xong, chạy:

```powershell
powershell -File scripts\build.ps1
powershell -File scripts\run_smoke.ps1
```

Harness sẽ tự verify (kiểm sắp xếp + so sánh multiset) và ném lỗi nếu sai.
