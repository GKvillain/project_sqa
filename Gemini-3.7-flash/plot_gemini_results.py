
from pathlib import Path
import pandas as pd
import matplotlib.pyplot as plt

# ==================================================
# CONFIG
# ==================================================
BASE_DIR = Path(__file__).resolve().parent
CSV_DIR = BASE_DIR / "Coverage"
OUTPUT_DIR = BASE_DIR / "Diagrams"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

CSV_FILES = sorted(CSV_DIR.glob("results_*.csv"))

if not CSV_FILES:
    raise SystemExit(f"No CSV files found in {CSV_DIR}")

# ==================================================
# LOAD ALL CSV FILES
# ==================================================
frames = []

for file in CSV_FILES:
    try:
        data = pd.read_csv(file)
        data["source_file"] = file.name
        frames.append(data)
        print(f"Loaded: {file.name} ({len(data)} rows)")
    except Exception as e:
        print(f"Error reading {file.name}: {e}")

df = pd.concat(frames, ignore_index=True)

# Convert numeric columns
numeric_cols = [
    "bug_id", "line_total", "line_covered", "line_coverage",
    "condition_total", "condition_covered", "condition_coverage",
    "refinement_rounds", "tokens_used"
]

for col in numeric_cols:
    if col in df.columns:
        df[col] = pd.to_numeric(df[col], errors="coerce")

# Normalize booleans
def to_bool(value):
    return str(value).strip().lower() in ("true", "1", "yes")

df["compile_ok"] = df["compile_ok"].apply(to_bool)
df["fault_detected"] = df["fault_detected"].apply(to_bool)

# Exclude N/A or zero-denominator coverage
df.loc[df["line_total"] <= 0, "line_coverage"] = float("nan")
df.loc[df["condition_total"] <= 0, "condition_coverage"] = float("nan")

# ==================================================
# AGGREGATE
# Multiple classes can exist for one Bug.
# Average class-level metrics per Bug to avoid
# treating multiple class records as separate Bugs.
# ==================================================
metric_cols = [
    "line_coverage",
    "condition_coverage",
    "compile_ok",
    "fault_detected",
    "tokens_used",
    "refinement_rounds"
]

bug_df = (
    df.groupby(["project", "bug_id"], as_index=False)[metric_cols]
    .mean(numeric_only=True)
)

bug_df["Bug"] = bug_df["bug_id"].astype("Int64").astype(str)

# Save normalized row-level and bug-level data
df.to_csv(OUTPUT_DIR / "gemini_all_results.csv", index=False)
bug_df.to_csv(OUTPUT_DIR / "gemini_bug_summary.csv", index=False)

# ==================================================
# PLOT HELPERS
# ==================================================
def save_plot(fig, path):
    fig.tight_layout()
    fig.savefig(path, dpi=300, bbox_inches="tight")
    plt.close(fig)


def plot_bug_metrics(data, metrics, title, ylabel, filename,
                     ylim=None):
    available = [
        m for m in metrics if m in data.columns
        and data[m].notna().any()
    ]
    if not available:
        return

    ax = data.set_index("Bug")[available].plot(
        kind="bar", figsize=(14, 6), width=0.8
    )
    ax.set_title(title)
    ax.set_xlabel("Bug ID")
    ax.set_ylabel(ylabel)
    if ylim:
        ax.set_ylim(*ylim)
    ax.tick_params(axis="x", rotation=45)
    ax.legend(title="Metric")
    save_plot(ax.figure, filename)


# ==================================================
# PER-PROJECT DIAGRAMS
# ==================================================
for project in sorted(bug_df["project"].unique()):
    data = bug_df[bug_df["project"] == project].copy()
    data = data.sort_values("bug_id")

    out = OUTPUT_DIR / project
    out.mkdir(parents=True, exist_ok=True)

    # 1. Line and Condition Coverage
    plot_bug_metrics(
        data,
        ["line_coverage", "condition_coverage"],
        f"{project} - Line & Condition Coverage",
        "Coverage (%)",
        out / "coverage_comparison.png",
        ylim=(0, 105)
    )

    # 2. Compile Success and Fault Detection
    plot_bug_metrics(
        data,
        ["compile_ok", "fault_detected"],
        f"{project} - Compile & Fault Detection",
        "Rate (%)",
        out / "compile_fault_detection.png",
        ylim=(0, 1.05)
    )

    # Convert rates to percentages for clearer presentation
    rate_data = data.copy()
    rate_data["compile_ok"] *= 100
    rate_data["fault_detected"] *= 100

    plot_bug_metrics(
        rate_data,
        ["compile_ok", "fault_detected"],
        f"{project} - Compile Success & Fault Detection",
        "Rate (%)",
        out / "compile_fault_detection_percent.png",
        ylim=(0, 105)
    )

    # 3. Token Usage
    plot_bug_metrics(
        data,
        ["tokens_used"],
        f"{project} - Token Usage",
        "Tokens",
        out / "token_usage.png"
    )

    # 4. Refinement Rounds
    plot_bug_metrics(
        data,
        ["refinement_rounds"],
        f"{project} - Refinement Rounds",
        "Rounds",
        out / "refinement_rounds.png"
    )

    print(f"Created diagrams for {project}")

# ==================================================
# PROJECT SUMMARY
# Average bug-level metrics per project
# ==================================================
summary = (
    bug_df.groupby("project")[metric_cols]
    .mean(numeric_only=True)
    .reset_index()
)

bug_counts = (
    bug_df.groupby("project")["bug_id"]
    .nunique()
    .rename("Bug_Count")
    .reset_index()
)

summary = summary.merge(bug_counts, on="project")
summary.to_csv(
    OUTPUT_DIR / "gemini_project_summary.csv",
    index=False
)

# Percentage for rates
summary["compile_ok"] *= 100
summary["fault_detected"] *= 100

# 5. Overall Project Coverage
for metrics, title, ylabel, filename, ylim in [
    (
        ["line_coverage", "condition_coverage"],
        "Gemini - Average Coverage by Project",
        "Coverage (%)",
        "project_coverage.png",
        (0, 105)
    ),
    (
        ["compile_ok", "fault_detected"],
        "Gemini - Compile & Fault Detection by Project",
        "Rate (%)",
        "project_compile_fault.png",
        (0, 105)
    ),
    (
        ["tokens_used"],
        "Gemini - Average Token Usage by Project",
        "Tokens",
        "project_token_usage.png",
        None
    ),
    (
        ["refinement_rounds"],
        "Gemini - Average Refinement Rounds",
        "Rounds",
        "project_refinement.png",
        None
    )
]:
    available = [
        m for m in metrics if summary[m].notna().any()
    ]
    if not available:
        continue

    ax = summary.set_index("project")[available].plot(
        kind="bar", figsize=(15, 6), width=0.8
    )
    ax.set_title(title)
    ax.set_xlabel("Project")
    ax.set_ylabel(ylabel)
    if ylim:
        ax.set_ylim(*ylim)
    ax.tick_params(axis="x", rotation=45)
    ax.legend(title="Metric")
    save_plot(ax.figure, OUTPUT_DIR / filename)

# ==================================================
# 6. Coverage Distribution (Box Plot)
# ==================================================
for metric, title, filename in [
    ("line_coverage", "Line Coverage Distribution",
     "line_coverage_boxplot.png"),
    ("condition_coverage", "Condition Coverage Distribution",
     "condition_coverage_boxplot.png")
]:
    groups = [
        bug_df.loc[bug_df["project"] == p, metric].dropna()
        for p in sorted(bug_df["project"].unique())
    ]
    labels = sorted(bug_df["project"].unique())
    valid = [(g, l) for g, l in zip(groups, labels) if len(g)]

    if valid:
        fig, ax = plt.subplots(figsize=(15, 7))
        ax.boxplot(
            [g for g, _ in valid],
            tick_labels=[l for _, l in valid],
            showmeans=True
        )
        ax.set_title(f"Gemini - {title} by Project")
        ax.set_xlabel("Project")
        ax.set_ylabel("Coverage (%)")
        ax.set_ylim(0, 105)
        ax.tick_params(axis="x", rotation=45)
        ax.grid(axis="y", alpha=0.3)
        save_plot(fig, OUTPUT_DIR / filename)

# ==================================================
# FINAL REPORT
# ==================================================
print("\n========== GEMINI ANALYSIS COMPLETE ==========")
print(f"CSV files: {len(CSV_FILES)}")
print(f"Records: {len(df)}")
print(f"Projects: {df['project'].nunique()}")
print(f"Unique Bugs: {bug_df['bug_id'].nunique()}")
print(f"Output: {OUTPUT_DIR}")

print("\n========== PROJECT SUMMARY ==========")
print(summary.round(2).to_string(index=False))
