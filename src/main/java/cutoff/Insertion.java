package cutoff;

// Insertion sort — theo slide "Stability: insertion sort" (22Mergesort, tr.52).
// less()/exch(): theo slide "insertion sort using a Comparator" (tr.46), đổi Comparator -> Comparable.
// sort(a, lo, hi): bản sắp xếp đoạn [lo..hi] mà slide 18 gọi tới (Insertion.sort(a, lo, hi)).
public class Insertion
{
    public static void sort(Comparable[] a)
    {
        int N = a.length;
        for (int i = 0; i < N; i++)
            for (int j = i; j > 0 && less(a[j], a[j-1]); j--)
                exch(a, j, j-1);
    }

    public static void sort(Comparable[] a, int lo, int hi)
    {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && less(a[j], a[j-1]); j--)
                exch(a, j, j-1);
    }

    private static boolean less(Comparable v, Comparable w)
    {  return v.compareTo(w) < 0;  }

    private static void exch(Comparable[] a, int i, int j)
    {  Comparable swap = a[i]; a[i] = a[j]; a[j] = swap;  }
}
