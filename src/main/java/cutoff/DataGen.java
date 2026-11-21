package cutoff;

import java.io.PrintWriter;
import java.util.Random;

public final class DataGen {

    public enum Dist {
        RANDOM, SORTED, REVERSE, NEARLY, DUPLICATES;

        public static Dist parse(String s) {
            switch (s.toLowerCase()) {
                case "random":     return RANDOM;
                case "sorted":     return SORTED;
                case "reverse":    return REVERSE;
                case "nearly":     return NEARLY;
                case "duplicates": return DUPLICATES;
                default: throw new IllegalArgumentException("Unknown dist: " + s);
            }
        }
    }

    private DataGen() {}

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
                for (int i = 0; i < n; i++) a[i] = i;
                int swaps = Math.max(1, n / 100);
                for (int k = 0; k < swaps; k++) {
                    int i = rnd.nextInt(n), j = rnd.nextInt(n);
                    Integer t = a[i]; a[i] = a[j]; a[j] = t;
                }
                break;
            case DUPLICATES:
                int distinct = Math.max(2, (int) Math.sqrt(n));
                for (int i = 0; i < n; i++) a[i] = rnd.nextInt(distinct);
                break;
        }
        return a;
    }

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

    public static void main(String[] args) throws Exception {
        String outDir = "data";
        int[] Ns = {10_000, 100_000, 1_000_000};
        String[] distStrs = {"random", "sorted", "reverse", "nearly", "duplicates"};
        long seed = 42;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--out":  outDir = args[++i]; break;
                case "--N":    Ns = parseInts(args[++i]); break;
                case "--dist": distStrs = args[++i].split(","); break;
                case "--seed": seed = Long.parseLong(args[++i]); break;
            }
        }

        new java.io.File(outDir).mkdirs();
        for (int N : Ns) {
            for (String dStr : distStrs) {
                Dist d = Dist.parse(dStr);
                int[] arr = genInt(d, N, seed);
                String path = String.format("%s/%s_N%d_seed%d.txt", outDir, dStr, N, seed);
                try (PrintWriter w = new PrintWriter(path)) {
                    for (int x : arr) w.println(x);
                }
                System.out.println("wrote " + path + " (" + arr.length + " ints)");
            }
        }
    }

    private static int[] parseInts(String s) {
        String[] parts = s.split(",");
        int[] a = new int[parts.length];
        for (int i = 0; i < parts.length; i++) a[i] = Integer.parseInt(parts[i].trim());
        return a;
    }
}
