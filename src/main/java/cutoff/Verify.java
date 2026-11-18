package cutoff;

import java.util.Arrays;

/**
 * Kiem tra ket qua sap xep: (1) khong giam, (2) la hoan vi cua input (so multiset qua checksum).
 */
public final class Verify {
    private Verify() {}

    public static void checkSorted(Comparable[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i].compareTo(a[i - 1]) < 0) {
                throw new AssertionError("Not sorted at i=" + i);
            }
        }
    }

    public static void checkSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i] < a[i - 1]) {
                throw new AssertionError("Not sorted at i=" + i);
            }
        }
    }

    // Checksum don gian theo tong hashCode -- khong manh ve ly thuyet nhung du phat hien bug thuong gap
    public static long checksum(Comparable[] a) {
        long h = 1469598103934665603L; // FNV offset
        for (Comparable x : a) h = h * 1099511628211L + x.hashCode();
        return h;
    }

    public static long checksum(int[] a) {
        long h = 1469598103934665603L;
        for (int x : a) h = h * 1099511628211L + x;
        return h;
    }

    public static void checkSameMultiset(Comparable[] before, Comparable[] after) {
        Comparable[] b = before.clone();
        Comparable[] c = after.clone();
        Arrays.sort(b);
        Arrays.sort(c);
        if (!Arrays.equals(b, c)) {
            throw new AssertionError("Multiset mismatch");
        }
    }
}
