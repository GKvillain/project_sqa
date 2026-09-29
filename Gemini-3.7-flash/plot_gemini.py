from pathlib import Path
import re
import pandas as pd
import matplotlib.pyplot as plt

# ==================================================
# CONFIG
# ==================================================
BASE_DIR = Path(__file__).resolve().parent
COVERAGE_DIR = BASE_DIR / "Coverage"
OUTPUT_DIR = BASE_DIR / "Gemini_Diagrams"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# Finds results_gemini-3.7-flash_<Project>.csv files recursively under Coverage
CSV_FILES = sorted(COVERAGE_DIR.rglob("results_gemini-3.7-flash_*.csv")) if COVERAGE_DIR.exists() else []

if not CSV_FILES:
    raise SystemExit(
        f"No Gemini result CSV files found in {COVERAGE_DIR}\n"
        "Expected: Coverage/results_gemini-3.7-flash_<Project>.csv"
    )

# ==================================================
# HELPERS
# ==================================================
def to_bool(value):
    if pd.isna(value):
        return None
    value = str(value).strip().lower()
    if value in {"true", "1", "yes", "y"}:
        return True
    if value in {"false", "0", "no", "n"}:
        return False
    return None


def project_from_csv(path):
    match = re.search(r"results_gemini-3\.7-flash_(.+)\.csv$", path.name, re.IGNORECASE)
    if match:
        return match.group(1)
    return path.parent.name


def save_plot(fig, path):
    fig.tight_layout()
    fig.savefig(path, dpi=300, bbox_inches="tight")
    plt.close(fig)


def numeric_columns(frame, columns):
    for col in columns:
        if col in frame.columns:
            frame[col] = pd.to_numeric(frame[col], errors="coerce")
        else:
            frame[col] = float("nan")
    return frame


def plot_bug_bars(data, metric, title, ylabel, filename, out, ylim=None):
    valid = data.dropna(subset=[metric])
    if valid.empty:
        return
    fig, ax = plt.subplots(figsize=(13, 5))
    ax.bar(valid["bug_id"].astype(str), valid[metric])
    ax.set_title(title)
    ax.set_xlabel("Bug ID")
    ax.set_ylabel(ylabel)
    if ylim:
        ax.set_ylim(*ylim)
    ax.tick_params(axis="x", rotation=45)
    ax.grid(axis="y", alpha=0.2)
    save_plot(fig, out / filename)


# ==================================================
# LOAD AND NORMALIZE CSV RESULTS
# ==================================================
frames = []
required = {"bug_id"}
numeric = [
    "line_total", "line_covered", "line_coverage",
    "condition_total", "condition_covered", "condition_coverage",
    "refinement_rounds", "tokens_used"
]

for csv_path in CSV_FILES:
    try:
        part = pd.read_csv(csv_path)
    except Exception as exc:
        print(f"Skip unreadable CSV {csv_path}: {exc}")
        continue

    part.columns = [str(c).strip().lower() for c in part.columns]
    if not required.issubset(part.columns):
        print(f"Skip CSV missing bug_id: {csv_path}")
        continue

    part["project"] = (
        part["project"].fillna(project_from_csv(csv_path)).astype(str)
        if "project" in part.columns
        else project_from_csv(csv_path)
    )
    part["bug_id"] = part["bug_id"].astype(str).str.strip()
    part["class_name"] = part["class_name"].astype(str) if "class_name" in part.columns else ""
    part["model"] = part["model"].astype(str) if "model" in part.columns else "gemini-3.7-flash"

    for col in numeric:
        if col not in part.columns:
            part[col] = float("nan")
    part = numeric_columns(part, numeric)

    if "compile_ok" in part.columns:
        part["compile_ok_bool"] = part["compile_ok"].map(to_bool)
    else:
        part["compile_ok_bool"] = None

    if "fault_detected" in part.columns:
        part["fault_detected_bool"] = part["fault_detected"].map(to_bool)
    else:
        part["fault_detected_bool"] = None

    part["source_csv"] = str(csv_path)
    frames.append(part)

if not frames:
    raise SystemExit("No usable Gemini CSV records found.")

df = pd.concat(frames, ignore_index=True)
# Remove duplicate copies of the same class/bug/model if backups are present.
dedupe_cols = [c for c in ["project", "bug_id", "class_name", "model", "output_path"] if c in df.columns]
if dedupe_cols:
    df = df.drop_duplicates(subset=dedupe_cols, keep="last")

df.to_csv(OUTPUT_DIR / "gemini_all_results.csv", index=False)

# ==================================================
# AGGREGATE CLASS-LEVEL ROWS TO BUG-LEVEL
# One bug can have more than one target class.
# Coverage is averaged over available class rows; fault detected if any row is True.
# ==================================================
group_cols = ["project", "bug_id"]
mean_cols = [
    "line_total", "line_covered", "line_coverage",
    "condition_total", "condition_covered", "condition_coverage",
    "refinement_rounds", "tokens_used"
]
bug_df = df.groupby(group_cols, as_index=False)[mean_cols].mean(numeric_only=True)

def aggregate_bool(series, mode="any"):
    values = [v for v in series.tolist() if v is not None and not pd.isna(v)]
    if not values:
        return float("nan")
    if mode == "any":
        return int(any(values))
    return sum(bool(v) for v in values) / len(values) * 100

compile_stats = df.groupby(group_cols)["compile_ok_bool"].apply(
    lambda s: aggregate_bool(s, "rate")
).reset_index(name="compile_rate")
fault_stats = df.groupby(group_cols)["fault_detected_bool"].apply(
    lambda s: aggregate_bool(s, "any")
).reset_index(name="fault_detected")
bug_df = bug_df.merge(compile_stats, on=group_cols, how="left")
bug_df = bug_df.merge(fault_stats, on=group_cols, how="left")
bug_df["compile_rate"] = pd.to_numeric(bug_df["compile_rate"], errors="coerce")
bug_df["fault_detected"] = pd.to_numeric(bug_df["fault_detected"], errors="coerce")
bug_df.to_csv(OUTPUT_DIR / "gemini_bug_summary.csv", index=False)

# ==================================================
# PER-PROJECT DIAGRAMS
# ==================================================
for project in sorted(bug_df["project"].unique()):
    data = bug_df[bug_df["project"] == project].copy()
    data = data.sort_values("bug_id", key=lambda s: s.astype(str))
    out = OUTPUT_DIR / str(project)
    out.mkdir(parents=True, exist_ok=True)
    data.to_csv(out / "gemini_bug_results.csv", index=False)

    # 1) Line and condition coverage by bug
    cov_cols = [
        c for c in ["line_coverage", "condition_coverage"]
        if data[c].notna().any()
    ]
    if cov_cols:
        ax = data.set_index("bug_id")[cov_cols].plot(
            kind="bar", figsize=(14, 6), width=0.8
        )
        ax.set_title(f"{project} - Coverage by Bug")
        ax.set_xlabel("Bug ID")
        ax.set_ylabel("Coverage (%)")
        ax.set_ylim(0, 105)
        ax.tick_params(axis="x", rotation=45)
        ax.legend(["Line Coverage" if c == "line_coverage" else "Condition Coverage" for c in cov_cols])
        ax.grid(axis="y", alpha=0.2)
        save_plot(ax.figure, out / "coverage_comparison.png")

    # 2) Coverage distributions
    for metric, title, filename in [
        ("line_coverage", "Line Coverage Distribution", "line_coverage_boxplot.png"),
        ("condition_coverage", "Condition Coverage Distribution", "condition_coverage_boxplot.png"),
    ]:
        vals = data[metric].dropna()
        if vals.empty:
            continue
        fig, ax = plt.subplots(figsize=(7, 5))
        ax.boxplot(vals, tick_labels=[title], patch_artist=True)
        ax.set_title(f"{project} - {title}")
        ax.set_ylabel("Coverage (%)")
        ax.set_ylim(0, 105)
        ax.grid(axis="y", alpha=0.25)
        save_plot(fig, out / filename)

    # 3) Fault detection rate per bug
    plot_bug_bars(
        data, "fault_detected",
        f"{project} - Fault Detection by Bug",
        "Fault detected (1 = yes, 0 = no)",
        "fault_detection.png", out, (0, 1.05)
    )

    # 4) Compilation rate per bug
    plot_bug_bars(
        data, "compile_rate",
        f"{project} - Compilation Success Rate",
        "Compilation success (%)",
        "compile_success.png", out, (0, 105)
    )

    # 5) Token use and refinement rounds
    plot_bug_bars(
        data, "tokens_used",
        f"{project} - Average Tokens Used by Bug",
        "Tokens", "tokens_used.png", out
    )
    plot_bug_bars(
        data, "refinement_rounds",
        f"{project} - Refinement Rounds by Bug",
        "Refinement rounds", "refinement_rounds.png", out
    )
    print(f"Created Gemini diagrams for {project} ({len(data)} bugs)")

# ==================================================
# OVERALL PROJECT SUMMARY
# ==================================================
summary = bug_df.groupby("project", as_index=False)[
    ["line_coverage", "condition_coverage", "compile_rate",
     "fault_detected", "tokens_used", "refinement_rounds"]
].mean(numeric_only=True)
summary["bug_count"] = bug_df.groupby("project")["bug_id"].nunique().reindex(
    summary["project"]
).to_numpy()
summary.to_csv(OUTPUT_DIR / "gemini_project_summary.csv", index=False)

for metric, filename, title, ylabel, ylim in [
    ("line_coverage", "project_line_coverage.png", "Average Line Coverage by Project", "Coverage (%)", (0, 105)),
    ("condition_coverage", "project_condition_coverage.png", "Average Condition Coverage by Project", "Coverage (%)", (0, 105)),
    ("compile_rate", "project_compile_success.png", "Compilation Success Rate by Project", "Success (%)", (0, 105)),
    ("fault_detected", "project_fault_detection.png", "Fault Detection Rate by Project", "Rate (0-1)", (0, 1.05)),
    ("tokens_used", "project_tokens.png", "Average Token Usage by Project", "Tokens", None),
]:
    valid = summary.dropna(subset=[metric])
    if valid.empty:
        continue
    fig, ax = plt.subplots(figsize=(15, 6))
    ax.bar(valid["project"], valid[metric])
    ax.set_title(title)
    ax.set_xlabel("Project")
    ax.set_ylabel(ylabel)
    if ylim:
        ax.set_ylim(*ylim)
    ax.tick_params(axis="x", rotation=45)
    ax.grid(axis="y", alpha=0.2)
    save_plot(fig, OUTPUT_DIR / filename)

print("\n========== GEMINI ANALYSIS COMPLETE ==========")
print(f"CSV files read: {len(CSV_FILES)}")
print(f"Rows: {len(df)}")
print(f"Projects: {df['project'].nunique()}")
print(f"Unique bugs: {bug_df['bug_id'].nunique()}")
print(f"Output: {OUTPUT_DIR}")
print(summary.round(2).to_string(index=False))
