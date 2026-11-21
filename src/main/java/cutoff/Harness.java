package cutoff;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Harness {

    public static void main(String[] args) throws Exception {
        String type = "Comparable";
        int[] Ns = {10_000};
        int[] cutoffs = {7};
        String[] dists = {"random"};
        int reps = 10, warmup = 3;
        long seed = 42;
        String outPath = "results/raw/out.csv";
        String mode = "time";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--type":   type = args[++i]; break;
                case "--N":      Ns = parseInts(args[++i]); break;
                case "--cutoff": cutoffs = parseInts(args[++i]); break;
                case "--dist":   dists = args[++i].split(","); break;
                case "--reps":   reps = Integer.parseInt(args[++i]); break;
                case "--warmup": warmup = Integer.parseInt(args[++i]); break;
                case "--seed":   seed = Long.parseLong(args[++i]); break;
                case "--out":    outPath = args[++i]; break;
                case "--mode":   mode = args[++i]; break;
                default: throw new IllegalArgumentException("Unknown arg: " + args[i]);
            }
        }

        boolean counting = mode.equals("count");

        new java.io.File(outPath).getParentFile().mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(outPath))) {
            if (counting) {
                w.write("type,N,dist,cutoff,compares,checksum\n");
            } else {
                w.write("type,N,dist,cutoff,rep,nanos,checksum\n");
                warmupJIT(type, warmup, seed);
            }

            List<Job> jobs = new ArrayList<>();
            int actualReps = counting ? 1 : reps;
            for (int N : Ns)
                for (String dist : dists)
                    for (int c : cutoffs)
                        for (int r = 0; r < actualReps; r++)
                            jobs.add(new Job(N, dist, c, r));

            if (!counting) Collections.shuffle(jobs, new Random(seed ^ 0xDEADBEEFL));

            int done = 0, total = jobs.size();
            long t0 = System.currentTimeMillis();
            for (Job j : jobs) {
                Result rs = runOne(type, j.N, j.dist, j.cutoff, seed + j.rep, counting);
                if (counting) {
                    w.write(String.format("%s,%d,%s,%d,%d,%d%n",
                            type, j.N, j.dist, j.cutoff, rs.compares, rs.checksum));
                } else {
                    w.write(String.format("%s,%d,%s,%d,%d,%d,%d%n",
                            type, j.N, j.dist, j.cutoff, j.rep, rs.nanos, rs.checksum));
                }
                done++;
                if (done % 50 == 0 || done == total) {
                    long dt = (System.currentTimeMillis() - t0) / 1000;
                    System.err.printf("[%d/%d] %ds elapsed%n", done, total, dt);
                }
            }
        }
        System.out.println("Done -> " + outPath);
    }

    private static class Job {
        final int N, cutoff, rep; final String dist;
        Job(int N, String dist, int cutoff, int rep) {
            this.N = N; this.dist = dist; this.cutoff = cutoff; this.rep = rep;
        }
    }
    private static class Result { long nanos, checksum, compares; }

    private static void warmupJIT(String type, int rounds, long seed) throws Exception {
        for (int i = 0; i < Math.max(2, rounds); i++) {
            runOne(type, 20_000, "random", 7, seed + 1000 + i, false);
            runOne(type, 20_000, "random", 1, seed + 2000 + i, false);
        }
    }

    private static Result runOne(String type, int N, String distStr, int cutoffVal,
                                 long seed, boolean counting) throws Exception {
        DataGen.Dist dist = DataGen.Dist.parse(distStr);
        Result rs = new Result();

        if (type.equalsIgnoreCase("Comparable")) {
            Merge.CUTOFF = cutoffVal;
            if (counting) {
                Merge.count = true; Insertion.count = true;
                Merge.compares = 0; Insertion.compares = 0;
            }
            Integer[] base = DataGen.gen(dist, N, seed);
            long fpBefore = Verify.multisetFingerprint(base);
            Integer[] a = base.clone();
            long t1 = System.nanoTime();
            Merge.sort(a);
            long t2 = System.nanoTime();
            rs.nanos = t2 - t1;
            Verify.checkSorted(a);
            if (Verify.multisetFingerprint(a) != fpBefore)
                throw new AssertionError("Multiset changed (Comparable, N=" + N + ", cutoff=" + cutoffVal + ")");
            rs.checksum = Verify.checksum(a);
            if (counting) {
                rs.compares = Merge.compares + Insertion.compares;
                Merge.count = false; Insertion.count = false;
            }
        } else if (type.equalsIgnoreCase("int")) {
            MergeInt.CUTOFF = cutoffVal;
            if (counting) {
                MergeInt.count = true; InsertionInt.count = true;
                MergeInt.compares = 0; InsertionInt.compares = 0;
            }
            int[] base = DataGen.genInt(dist, N, seed);
            long fpBefore = Verify.multisetFingerprint(base);
            int[] a = base.clone();
            long t1 = System.nanoTime();
            MergeInt.sort(a);
            long t2 = System.nanoTime();
            rs.nanos = t2 - t1;
            Verify.checkSorted(a);
            if (Verify.multisetFingerprint(a) != fpBefore)
                throw new AssertionError("Multiset changed (int, N=" + N + ", cutoff=" + cutoffVal + ")");
            rs.checksum = Verify.checksum(a);
            if (counting) {
                rs.compares = MergeInt.compares + InsertionInt.compares;
                MergeInt.count = false; InsertionInt.count = false;
            }
        } else {
            throw new IllegalArgumentException("type phai la Comparable hoac int");
        }
        return rs;
    }

    private static int[] parseInts(String s) {
        String[] parts = s.split(",");
        int[] a = new int[parts.length];
        for (int i = 0; i < parts.length; i++) a[i] = Integer.parseInt(parts[i].trim());
        return a;
    }
}
