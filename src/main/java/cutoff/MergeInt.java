package cutoff;

// Ban int[] cua Merge.java (so sanh truc tiep bang <).
// Thuat toan giong het Merge.java, chi khac kieu phan tu.
public class MergeInt
{
    public static int CUTOFF = 10;
    public static boolean count = false;
    public static long compares = 0;

    private static void merge(int[] a, int[] aux, int lo, int mid, int hi)
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

    private static void sort(int[] a, int[] aux, int lo, int hi)
    {
        if (hi <= lo + CUTOFF - 1)
        {
            InsertionInt.sort(a, lo, hi);
            return;
        }
        int mid = lo + (hi - lo) / 2;
        sort(a, aux, lo, mid);
        sort(a, aux, mid+1, hi);
        merge(a, aux, lo, mid, hi);
    }

    public static void sort(int[] a)
    {
        int[] aux = new int[a.length];
        sort(a, aux, 0, a.length - 1);
    }

    private static boolean less(int v, int w)
    {
        if (count) compares++;
        return v < w;
    }
}
