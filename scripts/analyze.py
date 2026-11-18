"""
analyze.py - tong hop CSV tho, tinh thong ke, ve do thi.

Chay: python scripts/analyze.py

Doc tat ca file CSV trong results/raw/ va:
  1. In bang tom tat (trung vi, trung binh, std, CI95) cho moi (type, N, dist, cutoff)
  2. Ghi tong hop vao results/summary/summary.csv
  3. Ve do thi thoi gian theo CUTOFF, moi (type, dist) mot hinh, mau duong theo N
  4. Danh dau CUTOFF = 7 va 10, va CUTOFF toi uu
  5. Ve do thi chuan hoa so voi CUTOFF = 7
"""

import os
import glob
import numpy as np
import pandas as pd
import matplotlib.pyplot as plt

RAW_DIR = "results/raw"
SUM_DIR = "results/summary"
PLOT_DIR = "results/plots"
os.makedirs(SUM_DIR, exist_ok=True)
os.makedirs(PLOT_DIR, exist_ok=True)


def load_all():
    files = glob.glob(os.path.join(RAW_DIR, "*.csv"))
    if not files:
        raise SystemExit(f"Khong co file CSV nao trong {RAW_DIR}")
    dfs = [pd.read_csv(f) for f in files]
    return pd.concat(dfs, ignore_index=True)


def summarize(df):
    g = df.groupby(["type", "N", "dist", "cutoff"])["nanos"]
    summary = g.agg(
        n="count",
        median="median",
        mean="mean",
        std="std",
        p25=lambda s: s.quantile(0.25),
        p75=lambda s: s.quantile(0.75),
    ).reset_index()
    # Khoang tin cay 95% cho trung binh (xap xi t-student ~ 1.96 voi n lon)
    summary["ci95"] = 1.96 * summary["std"] / np.sqrt(summary["n"])
    return summary


def plot_time_vs_cutoff(summary):
    for (typ, dist), sub in summary.groupby(["type", "dist"]):
        fig, ax = plt.subplots(figsize=(9, 5.5))
        for N, s2 in sub.groupby("N"):
            s2 = s2.sort_values("cutoff")
            ax.plot(s2["cutoff"], s2["median"] / 1e6, marker="o",
                    markersize=3, label=f"N={N:,}")
        ax.axvline(7, color="gray", linestyle="--", alpha=0.6, label="CUTOFF=7 (cu)")
        ax.axvline(10, color="black", linestyle="--", alpha=0.6, label="CUTOFF=10 (moi)")
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel("Thoi gian (ms, trung vi)")
        ax.set_title(f"Mergesort - type={typ}, dist={dist}")
        ax.set_yscale("log")
        ax.grid(True, which="both", alpha=0.3)
        ax.legend(fontsize=8, ncol=2)
        fig.tight_layout()
        out = os.path.join(PLOT_DIR, f"time_vs_cutoff__{typ}__{dist}.png")
        fig.savefig(out, dpi=140)
        plt.close(fig)
        print("saved", out)


def plot_normalized(summary, ref_cutoff=7):
    for (typ, dist), sub in summary.groupby(["type", "dist"]):
        fig, ax = plt.subplots(figsize=(9, 5.5))
        for N, s2 in sub.groupby("N"):
            s2 = s2.sort_values("cutoff")
            ref = s2.loc[s2["cutoff"] == ref_cutoff, "median"]
            if ref.empty:
                continue
            ref_val = ref.iloc[0]
            ax.plot(s2["cutoff"], s2["median"] / ref_val, marker="o",
                    markersize=3, label=f"N={N:,}")
        ax.axhline(1.0, color="gray", linestyle=":", alpha=0.6)
        ax.axvline(7, color="gray", linestyle="--", alpha=0.6)
        ax.axvline(10, color="black", linestyle="--", alpha=0.6)
        ax.set_xlabel("CUTOFF")
        ax.set_ylabel(f"Thoi gian / thoi gian khi CUTOFF={ref_cutoff}")
        ax.set_title(f"Chuan hoa - type={typ}, dist={dist}")
        ax.grid(True, alpha=0.3)
        ax.legend(fontsize=8, ncol=2)
        fig.tight_layout()
        out = os.path.join(PLOT_DIR, f"normalized__{typ}__{dist}.png")
        fig.savefig(out, dpi=140)
        plt.close(fig)
        print("saved", out)


def best_cutoff_table(summary):
    """Voi moi (type, N, dist): CUTOFF cho trung vi be nhat."""
    idx = summary.groupby(["type", "N", "dist"])["median"].idxmin()
    best = summary.loc[idx, ["type", "N", "dist", "cutoff", "median"]]
    best = best.rename(columns={"cutoff": "best_cutoff", "median": "best_median_ns"})
    best.to_csv(os.path.join(SUM_DIR, "best_cutoff.csv"), index=False)
    print("\n=== CUTOFF toi uu theo (type, N, dist) ===")
    print(best.to_string(index=False))
    return best


def main():
    df = load_all()
    print(f"Doc {len(df):,} dong tu {RAW_DIR}")
    summary = summarize(df)
    summary.to_csv(os.path.join(SUM_DIR, "summary.csv"), index=False)
    print(f"Ghi summary.csv ({len(summary):,} dong)")
    plot_time_vs_cutoff(summary)
    plot_normalized(summary, ref_cutoff=7)
    plot_normalized(summary, ref_cutoff=10)
    best_cutoff_table(summary)


if __name__ == "__main__":
    main()
