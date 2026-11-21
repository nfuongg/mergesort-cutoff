package cutoff;

// Ban int[] cua Insertion.java: less(v, w) -> v < w thay vi v.compareTo(w) < 0.
public class InsertionInt
{
    public static boolean count = false;
    public static long compares = 0;

    public static void sort(int[] a)
    {
        int N = a.length;
        for (int i = 0; i < N; i++)
            for (int j = i; j > 0 && less(a[j], a[j-1]); j--)
                exch(a, j, j-1);
    }

    public static void sort(int[] a, int lo, int hi)
    {
        for (int i = lo; i <= hi; i++)
            for (int j = i; j > lo && less(a[j], a[j-1]); j--)
                exch(a, j, j-1);
    }

    private static boolean less(int v, int w)
    {
        if (count) compares++;
        return v < w;
    }

    private static void exch(int[] a, int i, int j)
    {  int swap = a[i]; a[i] = a[j]; a[j] = swap;  }
}
