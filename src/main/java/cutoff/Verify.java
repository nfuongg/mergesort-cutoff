package cutoff;

import java.util.Arrays;

public final class Verify {
    private Verify() {}

    public static void checkSorted(Comparable[] a) {
        for (int i = 1; i < a.length; i++)
            if (a[i].compareTo(a[i - 1]) < 0)
                throw new AssertionError("Not sorted at i=" + i);
    }

    public static void checkSorted(int[] a) {
        for (int i = 1; i < a.length; i++)
            if (a[i] < a[i - 1])
                throw new AssertionError("Not sorted at i=" + i);
    }

    public static long checksum(Object[] a) {
        return Arrays.hashCode(a);
    }

    public static long checksum(int[] a) {
        return Arrays.hashCode(a);
    }
}
