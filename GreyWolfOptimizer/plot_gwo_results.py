
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

PROJECTS = [
    p for p in RESULT_DIR.iterdir() if p.is_dir()
]

# ==================================================
# PARSE TERMINAL LOG
# ==================================================
def extract(pattern, text, cast=float):
    matches = re.findall(pattern, text, re.IGNORECASE)
    if not matches:
        return None
    try:
        return cast(matches[-1])
    except (ValueError, TypeError):
        return None


def parse_log(log_path, project, bug):
    text = log_path.read_text(
        encoding="utf-8", errors="ignore"
    )

    def coverage(name):
        return extract(
            rf"Coverage of criterion {name}:\s*(\d+)%", text, int
        )

    # Final execution metrics
    elapsed = extract(
        r"Search finished after\s*([\d.]+)s", text
    )
    generations = extract(
        r"Search finished after\s*\d+(?:\.\d+)?s and\s*(\d+)\s*generations",
        text, int
    )
    statements = extract(
        r"(\d+)\s+statements,\s*best individual", text, int
    )
    fitness = extract(
        r"best individual has fitness:\s*([\d.]+)", text
    )
    tests = extract(
        r"Generated\s+(\d+)\s+tests", text, int
    )
    length = extract(
        r"Generated\s+\d+\s+tests with total length\s+(\d+)",
        text, int
    )
    mutation = extract(
        r"Resulting test suite's mutation score:\s*(\d+)%", text, int
    )

    # Per-generation progress
    progress = re.findall(
        r"GWO iteration=(\d+)\s+bestFitness=([\d.]+)", text
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


# ==================================================
# READ ALL PROJECTS AND BUGS
# ==================================================
records = []

for project_dir in sorted(PROJECTS):
    project = project_dir.name

    for bug_dir in sorted(project_dir.iterdir()):
        if not bug_dir.is_dir():
            continue
        if not bug_dir.name.startswith(project + "-"):
            continue

        bug = bug_dir.name

        for log in bug_dir.rglob("terminal.log"):
            # Only include logs from GWO result directories
            if "GWO" not in log.parts:
                continue

            try:
                records.append(
                    parse_log(log, project, bug)
                )
            except Exception as e:
                print(f"Error reading {log}: {e}")

if not records:
    print("No terminal.log files found.")
    raise SystemExit(1)

df = pd.DataFrame(records)

# Save detailed raw results
df.drop(columns=["Progress"]).to_csv(
    OUTPUT_DIR / "gwo_all_results.csv",
    index=False
)

# Average multiple class logs for each bug
metric_cols = [
    "Time_sec", "Generations", "Statements", "Fitness",
    "Line", "Branch", "Exception", "Mutation",
    "Output", "Method", "CBranch", "Tests", "Length"
]

bug_df = (
    df.groupby(["Project", "Bug"], as_index=False)[metric_cols]
    .mean(numeric_only=True)
)

# ==================================================
# PLOT PER-PROJECT DIAGRAMS
# ==================================================
def save_plot(fig, path):
    fig.tight_layout()
    fig.savefig(path, dpi=300, bbox_inches="tight")
    plt.close(fig)


for project in sorted(bug_df["Project"].unique()):
    data = bug_df[bug_df["Project"] == project].copy()
    data = data.sort_values("Bug")
    out = OUTPUT_DIR / project
    out.mkdir(parents=True, exist_ok=True)

    # 1. Coverage comparison
    coverage_cols = ["Line", "Branch", "Mutation"]
    available = [c for c in coverage_cols if data[c].notna().any()]

    if available:
        ax = data.set_index("Bug")[available].plot(
            kind="bar", figsize=(14, 6), width=0.8
        )
        ax.set_title(f"{project} - Coverage Comparison")
        ax.set_xlabel("Bug ID")
        ax.set_ylabel("Coverage / Mutation Score (%)")
        ax.set_ylim(0, 105)
        ax.legend(title="Metric")
        ax.tick_params(axis="x", rotation=45)
        save_plot(ax.figure, out / "coverage_comparison.png")

    # 2. Execution performance
    perf_cols = ["Time_sec", "Generations", "Tests", "Length"]
    available = [c for c in perf_cols if data[c].notna().any()]

    if available:
        fig, axes = plt.subplots(
            len(available), 1,
            figsize=(13, 3.2 * len(available)),
            squeeze=False
        )
        for ax, metric in zip(axes.flatten(), available):
            ax.bar(data["Bug"], data[metric])
            ax.set_title(f"{metric} by Bug")
            ax.set_xlabel("Bug ID")
            ax.set_ylabel(metric)
            ax.tick_params(axis="x", rotation=45)

        save_plot(fig, out / "execution_performance.png")

    # 3. Fitness vs GWO iteration (each available log)
    project_logs = df[df["Project"] == project]
    fig, ax = plt.subplots(figsize=(12, 6))
    plotted = 0

    for _, row in project_logs.iterrows():
        progress = row["Progress"]
        if not progress:
            continue

        iterations = [int(p[0]) for p in progress]
        fitness_values = [float(p[1]) for p in progress]
        ax.plot(
            iterations, fitness_values,
            alpha=0.35, linewidth=1
        )
        plotted += 1

    if plotted:
        ax.set_title(f"{project} - GWO Fitness Progress")
        ax.set_xlabel("GWO Iteration")
        ax.set_ylabel("Best Fitness")
        ax.grid(True, alpha=0.3)
        save_plot(fig, out / "fitness_progress.png")
    else:
        plt.close(fig)

    print(f"Created diagrams for {project}")


# ==================================================
# OVERALL PROJECT SUMMARY
# ==================================================
summary = (
    bug_df.groupby("Project")[metric_cols]
    .mean(numeric_only=True)
    .reset_index()
)
summary["Bug_Count"] = (
    bug_df.groupby("Project")["Bug"].nunique().values
)
summary.to_csv(
    OUTPUT_DIR / "gwo_project_summary.csv", index=False
)

# Overall line, branch, mutation comparison
for metrics, filename, title, ylabel in [
    (["Line", "Branch"], "project_coverage.png",
     "Average Coverage by Project", "Coverage (%)"),
    (["Mutation"], "project_mutation.png",
     "Average Mutation Score by Project", "Mutation Score (%)"),
    (["Time_sec"], "project_runtime.png",
     "Average Runtime by Project", "Time (seconds)"),
    (["Tests"], "project_test_count.png",
     "Average Test Cases by Project", "Test Cases")
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
    if ylabel == "Coverage (%)":
        ax.set_ylim(0, 105)
    ax.tick_params(axis="x", rotation=45)
    save_plot(ax.figure, OUTPUT_DIR / filename)

print("\n========== GWO ANALYSIS COMPLETE ==========")
print(f"Logs parsed: {len(df)}")
print(f"Projects: {df['Project'].nunique()}")
print(f"Bug IDs: {df['Bug'].nunique()}")
print(f"Output: {OUTPUT_DIR}")
print(summary.round(2).to_string(index=False))
