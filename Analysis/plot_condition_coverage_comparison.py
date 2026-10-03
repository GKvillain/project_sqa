#!/usr/bin/env python3

import csv
from pathlib import Path
import matplotlib.pyplot as plt
import numpy as np

ROOT = Path("/mnt/d/AllFinalWork/project_sqa")

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections", "Compress",
    "Csv", "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml",
    "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"
]

GEMINI_DIR = ROOT / "Gemini-3.7-flash" / "Coverage"
DEEPSEEK_DIR = ROOT / "deepseek-v4-flash"

OUTPUT_DIR = ROOT / "Analysis" / "ConditionCoverage_Diagrams"
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


def load_results(path):
    """
    Read condition coverage from CSV.

    Returns:
        {
            bug_id: (condition_covered, condition_total)
        }
    """
    data = {}

    if not path.exists():
        return data

    with open(path, "r", encoding="utf-8-sig", newline="") as f:
        reader = csv.DictReader(f)

        for row in reader:
            try:
                bug_id = int(row["bug_id"])
                covered = int(float(row["condition_covered"]))
                total = int(float(row["condition_total"]))
            except (KeyError, ValueError, TypeError):
                continue

            if total > 0:
                data[bug_id] = (covered, total)

    return data


def weighted_coverage(data, bugs=None):
    covered = 0
    total = 0

    if bugs is None:
        bugs = data.keys()

    for bug in bugs:
        if bug in data:
            c, t = data[bug]
            covered += c
            total += t

    if total == 0:
        return None

    return covered / total * 100


# ============================================================
# Load Gemini / DeepSeek
# ============================================================

gemini = {}
deepseek = {}

for project in PROJECTS:

    gemini_file = (
        GEMINI_DIR /
        f"results_gemini-3.7-flash_{project}.csv"
    )

    deepseek_file = (
        DEEPSEEK_DIR /
        project /
        f"results_deepseek-v4-flash_{project}.csv"
    )

    gemini[project] = load_results(gemini_file)
    deepseek[project] = load_results(deepseek_file)


# ============================================================
# Find Common Bugs
# ============================================================

common_bugs = {}

for project in PROJECTS:
    common_bugs[project] = sorted(
        set(gemini[project]) &
        set(deepseek[project])
    )


# ============================================================
# Calculate Condition Coverage by Project
# ============================================================

project_values = {}

for project in PROJECTS:

    bugs = common_bugs[project]

    gemini_cov = weighted_coverage(
        gemini[project],
        bugs
    )

    deepseek_cov = weighted_coverage(
        deepseek[project],
        bugs
    )

    project_values[project] = {
        "common": len(bugs),
        "gemini": gemini_cov,
        "deepseek": deepseek_cov
    }


# ============================================================
# Calculate Overall Condition Coverage
# ============================================================

gemini_total_covered = 0
gemini_total_conditions = 0

deepseek_total_covered = 0
deepseek_total_conditions = 0

total_common = 0

for project in PROJECTS:

    bugs = common_bugs[project]

    total_common += len(bugs)

    for bug in bugs:

        gc, gt = gemini[project][bug]
        dc, dt = deepseek[project][bug]

        gemini_total_covered += gc
        gemini_total_conditions += gt

        deepseek_total_covered += dc
        deepseek_total_conditions += dt


overall_gemini = (
    gemini_total_covered /
    gemini_total_conditions * 100
    if gemini_total_conditions else None
)

overall_deepseek = (
    deepseek_total_covered /
    deepseek_total_conditions * 100
    if deepseek_total_conditions else None
)


# ============================================================
# Print Results
# ============================================================

print("=" * 105)
print("CONDITION COVERAGE — GEMINI vs DEEPSEEK — COMMON BUGS")
print("=" * 105)

print(
    f"{'Project':<20}"
    f"{'Common Bugs':>15}"
    f"{'Gemini':>15}"
    f"{'DeepSeek':>15}"
)

print("-" * 105)

for project in PROJECTS:

    values = project_values[project]

    g = (
        f"{values['gemini']:.2f}%"
        if values["gemini"] is not None
        else "N/A"
    )

    d = (
        f"{values['deepseek']:.2f}%"
        if values["deepseek"] is not None
        else "N/A"
    )

    print(
        f"{project:<20}"
        f"{values['common']:>15}"
        f"{g:>15}"
        f"{d:>15}"
    )

print("-" * 105)

print(
    f"{'TOTAL':<20}"
    f"{total_common:>15}"
    f"{overall_gemini:>14.2f}%"
    f"{overall_deepseek:>14.2f}%"
)

print("=" * 105)


# ============================================================
# Graph 1
# Condition Coverage by Project (Common Bugs)
# ============================================================

projects_with_data = [
    p for p in PROJECTS
    if (
        project_values[p]["gemini"] is not None
        or project_values[p]["deepseek"] is not None
    )
]

x = np.arange(len(projects_with_data))
width = 0.38

gemini_values = [
    project_values[p]["gemini"] or 0
    for p in projects_with_data
]

deepseek_values = [
    project_values[p]["deepseek"] or 0
    for p in projects_with_data
]

fig, ax = plt.subplots(
    figsize=(18, 8)
)

ax.bar(
    x - width / 2,
    gemini_values,
    width,
    label="Gemini-3.7-flash"
)

ax.bar(
    x + width / 2,
    deepseek_values,
    width,
    label="DeepSeek-v4-flash"
)

ax.set_title(
    "Condition Coverage by Defects4J Project (Common Bugs)",
    fontsize=16
)

ax.set_xlabel("Defects4J Project")
ax.set_ylabel("Condition Coverage (%)")

ax.set_xticks(x)
ax.set_xticklabels(
    projects_with_data,
    rotation=45,
    ha="right"
)

ax.set_ylim(0, 100)
ax.grid(
    axis="y",
    linestyle="--",
    alpha=0.3
)

ax.legend()

plt.tight_layout()

output1 = (
    OUTPUT_DIR /
    "01_Condition_Coverage_by_Project_Common_Bugs.png"
)

plt.savefig(
    output1,
    dpi=300,
    bbox_inches="tight"
)

plt.close()


# ============================================================
# Graph 2
# Overall Condition Coverage (Common Bugs)
# ============================================================

methods = [
    "Gemini-3.7-flash",
    "DeepSeek-v4-flash"
]

values = [
    overall_gemini,
    overall_deepseek
]

fig, ax = plt.subplots(
    figsize=(8, 6)
)

bars = ax.bar(
    methods,
    values
)

ax.set_title(
    "Overall Condition Coverage (Common Bugs)",
    fontsize=16
)

ax.set_ylabel("Condition Coverage (%)")
ax.set_ylim(0, 100)

ax.grid(
    axis="y",
    linestyle="--",
    alpha=0.3
)

for bar, value in zip(bars, values):
    ax.text(
        bar.get_x() + bar.get_width() / 2,
        bar.get_height() + 1,
        f"{value:.2f}%",
        ha="center",
        va="bottom",
        fontsize=11
    )

plt.tight_layout()

output2 = (
    OUTPUT_DIR /
    "02_Overall_Condition_Coverage_Common_Bugs.png"
)

plt.savefig(
    output2,
    dpi=300,
    bbox_inches="tight"
)

plt.close()


print()
print("Graphs created:")
print(output1)
print(output2)
