package cutoff;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class Harness {

    public static void main(String[] args) throws Exception {
        String type   = "Comparable";
        int[] Ns      = {10_000};
        int[] cutoffs = {1, 7, 10, 16};
        String[] dists = {"random"};
        int reps = 10, warmup = 3;
        long seed = 42;
        String outPath = "results/raw/out.csv";

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--type":   type    = args[++i]; break;
                case "--N":      Ns      = parseInts(args[++i]); break;
                case "--cutoff": cutoffs = parseInts(args[++i]); break;
                case "--dist":   dists   = args[++i].split(","); break;
                case "--reps":   reps    = Integer.parseInt(args[++i]); break;
                case "--warmup": warmup  = Integer.parseInt(args[++i]); break;
                case "--seed":   seed    = Long.parseLong(args[++i]); break;
                case "--out":    outPath = args[++i]; break;
                default: throw new IllegalArgumentException("Unknown arg: " + args[i]);
            }
        }

        requireClass(type);

        new java.io.File(outPath).getParentFile().mkdirs();
        try (BufferedWriter w = new BufferedWriter(new FileWriter(outPath))) {
            w.write("type,N,dist,cutoff,rep,nanos,checksum\n");

            warmupJIT(type, warmup, seed);

            List<Job> jobs = new ArrayList<>();
            for (int N : Ns) {
                for (String dist : dists) {
                    for (int c : cutoffs) {
                        for (int r = 0; r < reps; r++) {
                            jobs.add(new Job(N, dist, c, r));
                        }
                    }
                }
            }
            Collections.shuffle(jobs, new Random(seed ^ 0xDEADBEEFL));

            int done = 0, total = jobs.size();
            long t0 = System.currentTimeMillis();
            for (Job j : jobs) {
                Result rs = runOne(type, j.N, j.dist, j.cutoff, seed + j.rep);
                w.write(String.format("%s,%d,%s,%d,%d,%d,%d%n",
                        type, j.N, j.dist, j.cutoff, j.rep, rs.nanos, rs.checksum));
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
    private static class Result { long nanos, checksum; }

    private static void warmupJIT(String type, int rounds, long seed) throws Exception {
        for (int i = 0; i < Math.max(2, rounds); i++) {
            runOne(type, 20_000, "random", 7, seed + 1000 + i);
            runOne(type, 20_000, "random", 1, seed + 2000 + i);
        }
    }

    private static Result runOne(String type, int N, String distStr, int cutoff, long seed) throws Exception {
        DataGen.Dist dist = DataGen.Dist.parse(distStr);
        Result rs = new Result();

        if (type.equalsIgnoreCase("Comparable")) {
            setCutoff("cutoff.Merge", cutoff);
            Integer[] base = DataGen.gen(dist, N, seed);
            Integer[] a = base.clone();
            long t1 = System.nanoTime();
            Merge.sort(a);
            long t2 = System.nanoTime();
            rs.nanos = t2 - t1;
            Verify.checkSorted(a);
            rs.checksum = Verify.checksum(a);
        } else if (type.equalsIgnoreCase("int")) {
            setCutoff("cutoff.MergeInt", cutoff);
            int[] base = DataGen.genInt(dist, N, seed);
            int[] a = base.clone();
            long t1 = System.nanoTime();
            MergeInt.sort(a);
            long t2 = System.nanoTime();
            rs.nanos = t2 - t1;
            Verify.checkSorted(a);
            rs.checksum = Verify.checksum(a);
        } else {
            throw new IllegalArgumentException("type phai la Comparable hoac int");
        }
        return rs;
    }

    private static void setCutoff(String className, int v) throws Exception {
        Class<?> c = Class.forName(className);
        Field f = c.getField("CUTOFF");
        f.setInt(null, v);
    }

    private static void requireClass(String type) {
        try {
            if (type.equalsIgnoreCase("Comparable")) {
                Class.forName("cutoff.Merge");
                Class.forName("cutoff.Insertion");
            } else {
                Class.forName("cutoff.MergeInt");
                Class.forName("cutoff.InsertionInt");
            }
        } catch (ClassNotFoundException e) {
            System.err.println("THIEU CLASS: " + e.getMessage());
            System.err.println("Sinh vien chua viet xong. Xem src/main/java/cutoff/_STUDENT_README.md");
            System.exit(2);
        }
    }

    private static int[] parseInts(String s) {
        String[] parts = s.split(",");
        int[] a = new int[parts.length];
        for (int i = 0; i < parts.length; i++) a[i] = Integer.parseInt(parts[i].trim());
        return a;
    }
}
