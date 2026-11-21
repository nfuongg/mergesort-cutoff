package cutoff;

// Insertion sort cho Comparable[].
// Phien ban dai [lo..hi]: slide 2.1 "Insertion sort implementation" (21ElementarySorts.pdf, tr.26)
// duoc goi tu Merge.sort khi mang con nho.
// less() / exch(): slide 2.1 tr.27 (dung Comparable thay vi Comparator).
public class Insertion
{
    public static boolean count = false;
    public static long compares = 0;

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
    {
        if (count) compares++;
        return v.compareTo(w) < 0;
    }

    private static void exch(Comparable[] a, int i, int j)
    {  Comparable swap = a[i]; a[i] = a[j]; a[j] = swap;  }
}
