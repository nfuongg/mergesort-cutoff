package cutoff;

// Mergesort voi cai tien CUTOFF chuyen sang Insertion sort cho mang con nho.
// merge(): slide 2.2 "Merging: Java implementation" (22Mergesort.pdf, tr.7)
// sort():  slide 2.2 "Mergesort: practical improvements" (tr.18)
//
// public static:
//   CUTOFF   - duoc Harness gan truc tiep truoc moi run
//   count    - khi true, less() dem vao `compares` (dung cho mode counting)
//   compares - tich luy so phep so sanh trong lan sort hien tai
public class Merge
{
    public static int CUTOFF = 10;
    public static boolean count = false;
    public static long compares = 0;

    private static void merge(Comparable[] a, Comparable[] aux, int lo, int mid, int hi)
    {
        for (int k = lo; k <= hi; k++)
            aux[k] = a[k];

        int i = lo, j = mid+1;
        for (int k = lo; k <= hi; k++)
        {
            if      (i > mid)              a[k] = aux[j++];
            else if (j > hi)               a[k] = aux[i++];
            else if (less(aux[j], aux[i])) a[k] = aux[j++];
            else                           a[k] = aux[i++];
        }
    }

    private static void sort(Comparable[] a, Comparable[] aux, int lo, int hi)
    {
        // Dieu kien cat cho Insertion: mang con co hi - lo + 1 <= CUTOFF phan tu.
        // CUTOFF = 1 tuong duong khong cat (moi la 1 phan tu).
        if (hi <= lo + CUTOFF - 1)
        {
            Insertion.sort(a, lo, hi);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort(a, aux, lo, mid);
        sort(a, aux, mid+1, hi);
        merge(a, aux, lo, mid, hi);
    }

    public static void sort(Comparable[] a)
    {
        Comparable[] aux = new Comparable[a.length];
        sort(a, aux, 0, a.length - 1);
    }

    private static boolean less(Comparable v, Comparable w)
    {
        if (count) compares++;
        return v.compareTo(w) < 0;
    }
}
