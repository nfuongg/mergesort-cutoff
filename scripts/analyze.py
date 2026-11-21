"""
analyze.py - phan tich ket qua thi nghiem CUTOFF cua mergesort.

Dau vao:
  results/raw/full_comparable.csv, full_int.csv (timing, 30 rep/cell)
  results/raw/count_comparable.csv, count_int.csv (counting, 1 rep/cell, tuy chon)

Dau ra:
  results/summary/
    summary.csv      - median, mean, std, bootstrap CI95 cho median, moi cell
    best_cutoff.csv  - CUTOFF toi uu + VUNG tuong duong (CI chong) theo (type, N, dist)
    comparison.csv   - bang so sanh CUTOFF=1/7/10/best o N lon
    doubling.csv     - T(2N)/T(N) chi lay cap gap doi dung
    power_law.csv    - fit T ~ a*N^b, uoc luong b
    counts.csv       - bang so compares theo cutoff (neu co counting)
  results/plots/
    headline.png
    time_vs_cutoff__{type}__{dist}.png
    normalized_c{7,10}__{type}__{dist}.png
    loglog__{type}__{dist}.png
    compares_vs_cutoff__{type}__{dist}.png
"""

import os
import glob
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

RAW_DIR  = "results/raw"
SUM_DIR  = "results/summary"
PLOT_DIR = "results/plots"
os.makedirs(SUM_DIR, exist_ok=True)
os.makedirs(PLOT_DIR, exist_ok=True)

BOOT_ITERS = 2000
RNG = np.random.default_rng(42)


def bootstrap_median_ci(x, iters=BOOT_ITERS, alpha=0.05):
    x = np.asarray(x)
    if len(x) < 2:
        return (float(np.median(x)), float(np.median(x)))
    idx = RNG.integers(0, len(x), size=(iters, len(x)))
    meds = np.median(x[idx], axis=1)
    lo = float(np.quantile(meds, alpha / 2))
    hi = float(np.quantile(meds, 1 - alpha / 2))
    return (lo, hi)


def load_full():
    files = sorted(glob.glob(os.path.join(RAW_DIR, "full_*.csv")))
    if not files:
        raise SystemExit(f"Khong co file full_*.csv trong {RAW_DIR}")
    dfs = [pd.read_csv(f) for f in files]
    df = pd.concat(dfs, ignore_index=True)
    print(f"Doc {len(df):,} dong timing tu {len(files)} file")
    return df


def load_counts():
    files = sorted(glob.glob(os.path.join(RAW_DIR, "count_*.csv")))
    if not files:
        print("(bo qua) khong co file count_*.csv")
        return None
    dfs = [pd.read_csv(f) for f in files]
    df = pd.concat(dfs, ignore_index=True)
    print(f"Doc {len(df):,} dong counting tu {len(files)} file")
    return df


def summarize(df):
    rows = []
    for (typ, N, dist, cutoff), g in df.groupby(["type", "N", "dist", "cutoff"]):
        xs = g["nanos"].values
        med = float(np.median(xs))
        lo, hi = bootstrap_median_ci(xs)
        rows.append({
            "type": typ, "N": N, "dist": dist, "cutoff": cutoff,
            "n": len(xs),
            "median": med,
            "ci_lo": lo,
            "ci_hi": hi,
            "mean": float(np.mean(xs)),
            "std":  float(np.std(xs, ddof=1)) if len(xs) > 1 else 0.0,
        })
    s = pd.DataFrame(rows)
    s.to_csv(os.path.join(SUM_DIR, "summary.csv"), index=False)
    return s


def best_cutoff_and_region(summary):
    rows = []
    for (typ, N, dist), g in summary.groupby(["type", "N", "dist"]):
        g = g.sort_values("cutoff")
        best = g.loc[g["median"].idxmin()]
        thresh = best["ci_hi"]
        region = g[g["ci_lo"] <= thresh]["cutoff"].tolist()
        rows.append({
            "type": typ, "N": N, "dist": dist,
            "best_cutoff": int(best["cutoff"]),
            "best_median_ns": best["median"],
            "best_ci_lo": best["ci_lo"],
            "best_ci_hi": best["ci_hi"],
            "equiv_region_min": min(region),
            "equiv_region_max": max(region),
            "equiv_region_size": len(region),
            "equiv_region": ",".join(str(x) for x in sorted(region)),
        })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "best_cutoff.csv"), index=False)
    return df


def comparison_table(summary):
    big = summary[summary["N"] >= 100_000].copy()
    rows = []
    for (typ, N, dist), g in big.groupby(["type", "N", "dist"]):
        c1 = g[g["cutoff"] == 1]
        c7 = g[g["cutoff"] == 7]
        c10 = g[g["cutoff"] == 10]
        if c1.empty or c7.empty or c10.empty:
            continue
        c1v  = c1["median"].iloc[0]
        c7v  = c7["median"].iloc[0]
        c10v = c10["median"].iloc[0]
        best = g.loc[g["median"].idxmin()]
        rows.append({
            "type": typ, "dist": dist, "N": N,
            "c1_ms": c1v / 1e6,
            "c7_ms": c7v / 1e6,
            "c10_ms": c10v / 1e6,
            "best_cutoff": int(best["cutoff"]),
            "best_ms": best["median"] / 1e6,
            "gain_c7_pct":   (c1v - c7v) / c1v * 100,
            "gain_c10_pct":  (c1v - c10v) / c1v * 100,
            "gain_best_pct": (c1v - best["median"]) / c1v * 100,
        })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "comparison.csv"), index=False)
    return df


def doubling_table(summary, ref_cutoff=10):
    rows = []
    for (typ, dist), g in summary[summary.cutoff == ref_cutoff].groupby(["type", "dist"]):
        g = g.sort_values("N")
        Ns = sorted(g["N"].unique())
        for Ni in Ns:
            if 2 * Ni in Ns:
                Ti  = float(g[g.N == Ni]["median"].iloc[0])
                T2i = float(g[g.N == 2 * Ni]["median"].iloc[0])
                exp = 2.0 * np.log2(2 * Ni) / np.log2(Ni)
                rows.append({
                    "type": typ, "dist": dist,
                    "N1": Ni, "N2": 2 * Ni,
                    "T1_ms": Ti / 1e6, "T2_ms": T2i / 1e6,
                    "ratio": T2i / Ti,
                    "expected_NlogN": exp,
                    "cutoff": ref_cutoff,
                })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "doubling.csv"), index=False)
    return df


def power_law_fit(summary, cutoff=10):
    rows = []
    sub = summary[summary.cutoff == cutoff].copy()
    for (typ, dist), g in sub.groupby(["type", "dist"]):
        g = g.sort_values("N")
        if len(g) < 3:
            continue
        logN = np.log10(g["N"].values.astype(float))
        logT = np.log10(g["median"].values / 1e6)
        b, loga = np.polyfit(logN, logT, 1)
        rows.append({
            "type": typ, "dist": dist, "cutoff": cutoff,
            "b_exponent": float(b),
            "a_prefactor": float(10 ** loga),
            "n_points": len(g),
        })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "power_law.csv"), index=False)
    return df


def plot_time_vs_cutoff(summary):
    for (typ, dist), sub in summary.groupby(["type", "dist"]):
        fig, ax = plt.subplots(figsize=(9, 5.5))
        for N, s in sub.groupby("N"):
            if N < 50_000:
                continue
            s = s.sort_values("cutoff")
            ax.errorbar(s["cutoff"], s["median"] / 1e6,
                        yerr=[(s["median"] - s["ci_lo"]) / 1e6,
                              (s["ci_hi"] - s["median"]) / 1e6],
                        marker="o", markersize=3, capsize=2, linewidth=1,
                        label=f"N={N:,}")
        ax.axvline(7, color="gray", linestyle="--", alpha=0.6, label="CUTOFF=7")
        ax.axvline(10, color="black", linestyle="--", alpha=0.6, label="CUTOFF=10")
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel("Thoi gian (ms, trung vi + CI95)")
        ax.set_title(f"Mergesort - type={typ}, dist={dist}")
        ax.set_yscale("log")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend(fontsize=8, ncol=2)
        fig.tight_layout()
        fig.savefig(os.path.join(PLOT_DIR, f"time_vs_cutoff__{typ}__{dist}.png"), dpi=140)
        plt.close(fig)


def plot_normalized(summary, ref_cutoff):
    for (typ, dist), sub in summary.groupby(["type", "dist"]):
        fig, ax = plt.subplots(figsize=(9, 5.5))
        for N, s in sub.groupby("N"):
            if N < 50_000:
                continue
            s = s.sort_values("cutoff")
            ref = s.loc[s["cutoff"] == ref_cutoff, "median"]
            if ref.empty:
                continue
            ax.plot(s["cutoff"], s["median"] / ref.iloc[0], marker="o",
                    markersize=3, label=f"N={N:,}")
        ax.axhline(1.0, color="gray", linestyle=":", alpha=0.6)
        ax.axvline(ref_cutoff, color="red", linestyle="--", alpha=0.6,
                   label=f"ref=CUTOFF={ref_cutoff}")
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel(f"Time / Time(CUTOFF={ref_cutoff})")
        ax.set_title(f"Chuan hoa - type={typ}, dist={dist}")
        ax.grid(True, alpha=0.3)
        ax.legend(fontsize=8, ncol=2)
        fig.tight_layout()
        fig.savefig(os.path.join(PLOT_DIR,
                                 f"normalized_c{ref_cutoff}__{typ}__{dist}.png"), dpi=140)
        plt.close(fig)


def plot_loglog(summary, power_law, cutoff=10):
    sub = summary[summary.cutoff == cutoff]
    for (typ, dist), g in sub.groupby(["type", "dist"]):
        g = g.sort_values("N")
        if len(g) < 3:
            continue
        fig, ax = plt.subplots(figsize=(7, 5))
        ax.loglog(g["N"], g["median"] / 1e6, "o-", markersize=4, label="Do duoc")
        row = power_law[(power_law.type == typ) & (power_law.dist == dist)]
        if not row.empty:
            b = row.iloc[0]["b_exponent"]
            a = row.iloc[0]["a_prefactor"]
            Nfit = np.array(sorted(g["N"].unique()), dtype=float)
            ax.loglog(Nfit, a * Nfit ** b, "r--", alpha=0.7,
                      label=f"Fit: T = {a:.2e} * N^{b:.3f}")
            ax.text(0.05, 0.95, f"b = {b:.3f}",
                    transform=ax.transAxes, va="top", fontsize=9,
                    bbox=dict(facecolor="white", alpha=0.7))
        ax.set_xlabel("N"); ax.set_ylabel("Time (ms)")
        ax.set_title(f"log-log, CUTOFF={cutoff}, type={typ}, dist={dist}")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend(fontsize=8)
        fig.tight_layout()
        fig.savefig(os.path.join(PLOT_DIR, f"loglog__{typ}__{dist}.png"), dpi=140)
        plt.close(fig)


def plot_headline(summary):
    cases = [
        ("Comparable", "random",  "Random Comparable[]"),
        ("int",        "random",  "Random int[]"),
        ("Comparable", "sorted",  "Sorted Comparable[]"),
        ("int",        "reverse", "Reverse int[]"),
    ]
    fig, axes = plt.subplots(2, 2, figsize=(14, 9))
    for (typ, dist, title), ax in zip(cases, axes.flat):
        sub = summary[(summary.type == typ) & (summary.dist == dist)]
        for N, s in sub.groupby("N"):
            if N < 100_000:
                continue
            s = s.sort_values("cutoff")
            c1 = s[s.cutoff == 1]
            if c1.empty:
                continue
            ax.plot(s["cutoff"], s["median"] / c1["median"].iloc[0], marker="o",
                    markersize=3, label=f"N={N // 1000}k")
        ax.axhline(1.0, color="gray", linestyle=":", alpha=0.5)
        ax.axvline(7, color="gray", linestyle="--", alpha=0.5, label="CUTOFF=7")
        ax.axvline(10, color="black", linestyle="--", alpha=0.5, label="CUTOFF=10")
        ax.set_xlabel("CUTOFF"); ax.set_ylabel("T / T(CUTOFF=1)")
        ax.set_title(title); ax.set_xscale("log")
        ax.grid(alpha=0.3); ax.legend(fontsize=8, ncol=2, loc="best")
    fig.suptitle("Mergesort CUTOFF: speedup so voi mergesort thuan", fontsize=13)
    fig.tight_layout()
    fig.savefig(os.path.join(PLOT_DIR, "headline.png"), dpi=140)
    plt.close(fig)


def analyze_counts(counts):
    if counts is None:
        return None
    rows = []
    for (typ, N, dist), g in counts.groupby(["type", "N", "dist"]):
        g = g.sort_values("cutoff")
        theory = N * np.log2(N)
        for _, r in g.iterrows():
            rows.append({
                "type": typ, "N": N, "dist": dist, "cutoff": int(r["cutoff"]),
                "compares": int(r["compares"]),
                "compares_per_NlogN": r["compares"] / theory,
            })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "counts.csv"), index=False)

    for (typ, dist), g in df.groupby(["type", "dist"]):
        fig, ax = plt.subplots(figsize=(8, 5))
        for N, s in g.groupby("N"):
            s = s.sort_values("cutoff")
            ax.plot(s["cutoff"], s["compares_per_NlogN"], marker="o",
                    markersize=3, label=f"N={N:,}")
        ax.axhline(1.0, color="gray", linestyle=":", alpha=0.6, label="N log N")
        ax.axvline(7,  color="gray",  linestyle="--", alpha=0.5)
        ax.axvline(10, color="black", linestyle="--", alpha=0.5)
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel("So compares / (N log N)")
        ax.set_title(f"Cost model (compares) - type={typ}, dist={dist}")
        ax.grid(alpha=0.3); ax.legend(fontsize=8, ncol=2)
        fig.tight_layout()
        fig.savefig(os.path.join(PLOT_DIR, f"compares_vs_cutoff__{typ}__{dist}.png"), dpi=140)
        plt.close(fig)
    return df


def check_checksum_consistency(df):
    """Voi moi (type, N, dist, rep): checksum phai dong nhat qua tat ca cutoff.
       Neu khac nhau nghia la co cutoff sort sai (tao ra hoan vi khac)."""
    bad = []
    for (typ, N, dist, rep), g in df.groupby(["type", "N", "dist", "rep"]):
        if g["checksum"].nunique() > 1:
            bad.append((typ, N, dist, rep))
    if bad:
        print(f"CANH BAO: checksum khong nhat quan o {len(bad)} cell:")
        for b in bad[:5]:
            print(f"  {b}")
        return False
    print("OK: checksum nhat quan qua moi cutoff (sort dung + bao toan phan tu)")
    return True


def quantization_bins(counts):
    """Voi moi (type, N, dist): nhom CUTOFF cho cung so compare = cung 'bin'.
       Chi ra viec 'best_cutoff' nhay chi la nhieu giua cac bin tuong duong."""
    if counts is None:
        return None
    rows = []
    for (typ, N, dist), g in counts.groupby(["type", "N", "dist"]):
        g = g.sort_values("cutoff")
        bins = {}
        for _, r in g.iterrows():
            bins.setdefault(int(r["compares"]), []).append(int(r["cutoff"]))
        for cmp, cs in sorted(bins.items()):
            rows.append({
                "type": typ, "N": N, "dist": dist,
                "compares": cmp,
                "cutoff_bin": ",".join(str(x) for x in sorted(cs)),
                "bin_size": len(cs),
            })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "quantization.csv"), index=False)
    return df


def power_law_fit_restricted(summary, cutoff=10, N_min=200_000):
    """Fit chi tren N >= N_min de loai anomaly o N nho."""
    rows = []
    sub = summary[(summary.cutoff == cutoff) & (summary.N >= N_min)].copy()
    for (typ, dist), g in sub.groupby(["type", "dist"]):
        g = g.sort_values("N")
        if len(g) < 3:
            continue
        logN = np.log10(g["N"].values.astype(float))
        logT = np.log10(g["median"].values / 1e6)
        b, loga = np.polyfit(logN, logT, 1)
        rows.append({
            "type": typ, "dist": dist, "cutoff": cutoff,
            "N_min": N_min,
            "b_exponent": float(b),
            "n_points": len(g),
        })
    df = pd.DataFrame(rows)
    df.to_csv(os.path.join(SUM_DIR, "power_law_restricted.csv"), index=False)
    return df


def main():
    df = load_full()

    reps_cells = df.groupby(["type", "N", "dist", "cutoff"]).size()
    print(f"Reps/cell: min={reps_cells.min()}, max={reps_cells.max()}")
    if reps_cells.nunique() != 1:
        print("CANH BAO: so rep/cell khong deu")
    check_checksum_consistency(df)

    summary   = summarize(df)
    best      = best_cutoff_and_region(summary)
    cmp       = comparison_table(summary)
    doub      = doubling_table(summary, ref_cutoff=10)
    power     = power_law_fit(summary, cutoff=10)
    power_r   = power_law_fit_restricted(summary, cutoff=10, N_min=200_000)

    plot_time_vs_cutoff(summary)
    plot_normalized(summary, ref_cutoff=7)
    plot_normalized(summary, ref_cutoff=10)
    plot_loglog(summary, power, cutoff=10)
    plot_headline(summary)

    counts = load_counts()
    analyze_counts(counts)
    bins = quantization_bins(counts)

    print("\n=== comparison.csv (N >= 100k) ===")
    print(cmp.round(3).to_string(index=False))
    print("\n=== best_cutoff.csv (co vung tuong duong) ===")
    print(best.to_string(index=False))
    print("\n=== doubling.csv (cap N dung gap doi, ref_cutoff=10) ===")
    print(doub.round(3).to_string(index=False))
    print("\n=== power_law.csv (fit tren ALL N, cutoff=10) ===")
    print(power.round(4).to_string(index=False))
    print("\n=== power_law_restricted.csv (fit tren N >= 200k, cutoff=10) ===")
    print(power_r.round(4).to_string(index=False))
    if bins is not None:
        print("\n=== quantization.csv (N=1M, random) - cac CUTOFF cung bin ===")
        b1m = bins[(bins.N == 1_000_000) & (bins.dist == "random")]
        print(b1m.to_string(index=False))


if __name__ == "__main__":
    main()
