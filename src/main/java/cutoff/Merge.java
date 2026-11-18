package cutoff;

// Mergesort — merge(): slide "Merging: Java implementation" (tr.7)
//             sort():  slide "Mergesort: practical improvements" — cutoff to insertion sort (tr.18)
public class Merge
{
    // Slide dùng hằng CUTOFF. Để làm thí nghiệm, CUTOFF để dạng biến (mặc định 10 theo slide).
    // Yêu cầu CUTOFF >= 1 (CUTOFF = 1 tương đương mergesort gốc, không cutoff).
    // public để Harness set lại qua reflection trước mỗi run.
    public static int CUTOFF = 10;

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
        if (hi <= lo + CUTOFF - 1)
        {
            Insertion.sort(a, lo, hi);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort (a, aux, lo, mid);
        sort (a, aux, mid+1, hi);
        merge(a, aux, lo, mid, hi);
    }

    public static void sort(Comparable[] a)
    {
        Comparable[] aux = new Comparable[a.length];
        sort(a, aux, 0, a.length - 1);
    }

    private static boolean less(Comparable v, Comparable w)
    {  return v.compareTo(w) < 0;  }
}
