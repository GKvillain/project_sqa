from pathlib import Path
import re
import pandas as pd
import matplotlib.pyplot as plt

# ==================================================
# CONFIG
# ==================================================
BASE_DIR = Path(__file__).resolve().parent
RESULT_DIR = BASE_DIR / "Result_Round1"
OUTPUT_DIR = BASE_DIR / "GWO_Diagrams"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

if not RESULT_DIR.exists():
    raise SystemExit(f"Result directory not found: {RESULT_DIR}")

PROJECTS = [p for p in RESULT_DIR.iterdir() if p.is_dir()]

# ==================================================
# PARSE HELPERS
# ==================================================
def extract(pattern, text, cast=float):
    matches = re.findall(pattern, text, re.IGNORECASE | re.MULTILINE)
    if not matches:
        return None
    value = matches[-1]
    if isinstance(value, tuple):
        value = value[0]
    try:
        return cast(value)
    except (ValueError, TypeError):
        return None


def parse_log(log_path, project, bug):
    text = log_path.read_text(encoding="utf-8", errors="ignore")

    def coverage(name):
        # Logs commonly report "Coverage of criterion LINE: 67%"
        return extract(
            rf"Coverage of criterion {re.escape(name)}:\s*([\d.]+)\s*%",
            text, float
        )

    elapsed = extract(r"Search finished after\s*([\d.]+)\s*s", text)
    generations = extract(
        r"Search finished after\s*[\d.]+\s*s\s*and\s*(\d+)\s*generations",
        text, int
    )
    statements = extract(r"(\d+)\s+statements,\s*best individual", text, int)
    fitness = extract(r"best individual has fitness:\s*([\d.]+)", text)
    tests = extract(r"Generated\s+(\d+)\s+tests", text, int)
    length = extract(
        r"Generated\s+\d+\s+tests\s+with\s+total\s+length\s+(\d+)",
        text, int
    )
    mutation = extract(
        r"Resulting test suite's mutation score:\s*([\d.]+)\s*%",
        text, float
    )
    progress = re.findall(
        r"GWO iteration\s*=\s*(\d+)\s+bestFitness\s*=\s*([\d.]+)",
        text, re.IGNORECASE
    )

    return {
        "Project": project,
        "Bug": bug,
        "Class": log_path.parent.name,
        "Time_sec": elapsed,
        "Generations": generations,
        "Statements": statements,
        "Fitness": fitness,
        "Line": coverage("LINE"),
        "Branch": coverage("BRANCH"),
        "Exception": coverage("EXCEPTION"),
        "Mutation": mutation,
        "Output": coverage("OUTPUT"),
        "Method": coverage("METHOD"),
        "CBranch": coverage("CBRANCH"),
        "Tests": tests,
        "Length": length,
        "Log": str(log_path),
        "Progress": progress,
    }


def save_plot(fig, path):
    fig.tight_layout()
    fig.savefig(path, dpi=300, bbox_inches="tight")
    plt.close(fig)


def numeric_mean(frame, columns):
    return frame.groupby(["Project", "Bug"], as_index=False)[columns].mean(
        numeric_only=True
    )


# ==================================================
# READ ALL GWO LOGS
# Expected layout:
# Result_Round1/<Project>/<Project>-<Bug>b/GWO/<class>/terminal.log
# ==================================================
records = []
for project_dir in sorted(PROJECTS):
    project = project_dir.name
    for bug_dir in sorted(project_dir.iterdir()):
        if not bug_dir.is_dir() or not bug_dir.name.startswith(project + "-"):
            continue
        bug = bug_dir.name
        for log_path in bug_dir.rglob("terminal.log"):
            if "GWO" not in log_path.parts:
                continue
            try:
                records.append(parse_log(log_path, project, bug))
            except Exception as exc:
                print(f"Error reading {log_path}: {exc}")

if not records:
    print(f"No GWO terminal.log files found under: {RESULT_DIR}")
    raise SystemExit(1)

df = pd.DataFrame(records)
df.drop(columns=["Progress"]).to_csv(
    OUTPUT_DIR / "gwo_all_results.csv", index=False
)

metric_cols = [
    "Time_sec", "Generations", "Statements", "Fitness",
    "Line", "Branch", "Exception", "Mutation", "Output",
    "Method", "CBranch", "Tests", "Length"
]
bug_df = numeric_mean(df, metric_cols)
bug_df.to_csv(OUTPUT_DIR / "gwo_bug_summary.csv", index=False)

# ==================================================
# PER-PROJECT DIAGRAMS
# ==================================================
for project in sorted(bug_df["Project"].unique()):
    data = bug_df[bug_df["Project"] == project].copy().sort_values("Bug")
    out = OUTPUT_DIR / project
    out.mkdir(parents=True, exist_ok=True)

    # 1) Coverage and mutation by bug
    cols = [c for c in ["Line", "Branch", "Mutation"] if data[c].notna().any()]
    if cols:
        ax = data.set_index("Bug")[cols].plot(
            kind="bar", figsize=(14, 6), width=0.8
        )
        ax.set_title(f"{project} - Coverage and Mutation Score")
        ax.set_xlabel("Bug ID")
        ax.set_ylabel("Percentage (%)")
        ax.set_ylim(0, 105)
        ax.tick_params(axis="x", rotation=45)
        ax.legend(title="Metric")
        save_plot(ax.figure, out / "coverage_comparison.png")

    # 2) Line coverage distribution across bugs
    line = data["Line"].dropna()
    if not line.empty:
        fig, ax = plt.subplots(figsize=(7, 5))
        ax.boxplot(line, tick_labels=["Line Coverage"], patch_artist=True)
        ax.set_title(f"{project} - Line Coverage Distribution")
        ax.set_ylabel("Coverage (%)")
        ax.set_ylim(0, 105)
        ax.grid(axis="y", alpha=0.25)
        save_plot(fig, out / "line_coverage_boxplot.png")

    # 3) Runtime, test count, and total test length
    for metric, ylabel, filename in [
        ("Time_sec", "Time (seconds)", "runtime.png"),
        ("Tests", "Test cases", "test_cases.png"),
        ("Length", "Total test length", "test_length.png"),
        ("Generations", "Generations", "generations.png"),
    ]:
        valid = data.dropna(subset=[metric])
        if valid.empty:
            continue
        fig, ax = plt.subplots(figsize=(13, 5))
        ax.bar(valid["Bug"], valid[metric])
        ax.set_title(f"{project} - {metric} by Bug")
        ax.set_xlabel("Bug ID")
        ax.set_ylabel(ylabel)
        ax.tick_params(axis="x", rotation=45)
        ax.grid(axis="y", alpha=0.2)
        save_plot(fig, out / filename)

    # 4) GWO fitness progress (one curve per available class log)
    logs = df[df["Project"] == project]
    fig, ax = plt.subplots(figsize=(12, 6))
    plotted = 0
    for _, row in logs.iterrows():
        progress = row["Progress"]
        if not isinstance(progress, list) or not progress:
            continue
        iterations = [int(item[0]) for item in progress]
        fitness_values = [float(item[1]) for item in progress]
        ax.plot(iterations, fitness_values, alpha=0.4, linewidth=1)
        plotted += 1
    if plotted:
        ax.set_title(f"{project} - GWO Fitness Progress")
        ax.set_xlabel("GWO Iteration")
        ax.set_ylabel("Best Fitness")
        ax.grid(True, alpha=0.3)
        save_plot(fig, out / "fitness_progress.png")
    else:
        plt.close(fig)

    # Per-project bug-level data
    data.to_csv(out / "gwo_bug_results.csv", index=False)
    print(f"Created diagrams for {project} ({len(data)} bugs)")

# ==================================================
# OVERALL PROJECT SUMMARY AND DIAGRAMS
# ==================================================
summary = bug_df.groupby("Project", as_index=False)[metric_cols].mean(numeric_only=True)
summary["Bug_Count"] = bug_df.groupby("Project")["Bug"].nunique().reindex(
    summary["Project"]
).to_numpy()
summary.to_csv(OUTPUT_DIR / "gwo_project_summary.csv", index=False)

for metrics, filename, title, ylabel, ylim in [
    (["Line", "Branch"], "project_coverage.png", "Average Coverage by Project", "Coverage (%)", (0, 105)),
    (["Mutation"], "project_mutation.png", "Average Mutation Score by Project", "Mutation Score (%)", (0, 105)),
    (["Time_sec"], "project_runtime.png", "Average Runtime by Project", "Time (seconds)", None),
    (["Tests"], "project_test_count.png", "Average Test Cases by Project", "Test cases", None),
    (["Length"], "project_test_length.png", "Average Total Test Length by Project", "Total test length", None),
]:
    available = [m for m in metrics if summary[m].notna().any()]
    if not available:
        continue
    ax = summary.set_index("Project")[available].plot(
        kind="bar", figsize=(15, 6), width=0.8
    )
    ax.set_title(title)
    ax.set_xlabel("Project")
    ax.set_ylabel(ylabel)
    if ylim:
        ax.set_ylim(*ylim)
    ax.tick_params(axis="x", rotation=45)
    ax.grid(axis="y", alpha=0.2)
    save_plot(ax.figure, OUTPUT_DIR / filename)

print("\n========== GWO ANALYSIS COMPLETE ==========")
print(f"Logs parsed: {len(df)}")
print(f"Projects: {df['Project'].nunique()}")
print(f"Unique bugs: {bug_df['Bug'].nunique()}")
print(f"Output: {OUTPUT_DIR}")
print(summary.round(2).to_string(index=False))
