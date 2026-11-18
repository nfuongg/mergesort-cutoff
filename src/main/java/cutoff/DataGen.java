package cutoff;

import java.util.Random;

/**
 * Sinh du lieu test theo nhieu phan bo, voi seed co dinh de tai lap.
 * Moi phan bo nhan (n, seed) va tra ve mang moi.
 */
public final class DataGen {

    public enum Dist {
        RANDOM, SORTED, REVERSE, NEARLY, DUPLICATES;

        public static Dist parse(String s) {
            switch (s.toLowerCase()) {
                case "random":    return RANDOM;
                case "sorted":    return SORTED;
                case "reverse":   return REVERSE;
                case "nearly":    return NEARLY;
                case "duplicates":return DUPLICATES;
                default: throw new IllegalArgumentException("Unknown dist: " + s);
            }
        }
    }

    private DataGen() {}

    // --- Comparable (Integer boxed) ---
    public static Integer[] gen(Dist dist, int n, long seed) {
        Random rnd = new Random(seed);
        Integer[] a = new Integer[n];
        switch (dist) {
            case RANDOM:
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt();
                break;
            case SORTED:
                for (int i = 0; i < n; i++) a[i] = i;
                break;
            case REVERSE:
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            case NEARLY:
                // 99% sap xep, 1% swap ngau nhien
                for (int i = 0; i < n; i++) a[i] = i;
                int swaps = Math.max(1, n / 100);
                for (int k = 0; k < swaps; k++) {
                    int i = rnd.nextInt(n), j = rnd.nextInt(n);
                    Integer t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case DUPLICATES:
                // chi co sqrt(n) gia tri khac nhau
                int distinct = Math.max(2, (int) Math.sqrt(n));
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(distinct);
                break;
        }
        return a;
    }

    // --- int[] primitive ---
    public static int[] genInt(Dist dist, int n, long seed) {
        Random rnd = new Random(seed);
        int[] a = new int[n];
        switch (dist) {
            case RANDOM:
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt();
                break;
            case SORTED:
                for (int i = 0; i < n; i++) a[i] = i;
                break;
            case REVERSE:
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            case NEARLY:
                for (int i = 0; i < n; i++) a[i] = i;
                int swaps = Math.max(1, n / 100);
                for (int k = 0; k < swaps; k++) {
                    int i = rnd.nextInt(n), j = rnd.nextInt(n);
                    int t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case DUPLICATES:
                int distinct = Math.max(2, (int) Math.sqrt(n));
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(distinct);
                break;
        }
        return a;
    }
}
