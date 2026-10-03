import os
import re
import csv
from collections import defaultdict

import numpy as np
import pandas as pd
import matplotlib.pyplot as plt


ROOT = "/mnt/d/AllFinalWork/project_sqa"

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections", "Compress",
    "Csv", "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml",
    "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"
]

OUT = os.path.join(ROOT, "Analysis", "LineCoverage_Diagrams")
os.makedirs(OUT, exist_ok=True)


# ============================================================
# Utility
# ============================================================

def normalize_bug(x):
    """
    Convert:
        1
        1b
        Bug 1
        1B
    into:
        1b
    """
    if x is None:
        return None

    s = str(x).strip()

    m = re.search(r'(\d+)\s*[bB]?', s)
    if not m:
        return None

    return f"{int(m.group(1))}b"


def bug_number(bug):
    m = re.search(r'\d+', bug)
    return int(m.group()) if m else 999999


# ============================================================
# GWO
# ============================================================

def parse_gwo_log(path):
    """
    Read LINE coverage from one terminal.log.

    Uses:
        Coverage of criterion LINE
        Total number of goals
        Number of covered goals
    """

    try:
        text = open(path, encoding="utf-8", errors="ignore").read()
    except Exception:
        return None

    pattern = re.compile(
        r'Coverage analysis for criterion LINE.*?'
        r'Coverage of criterion LINE:\s*[\d.]+%.*?'
        r'Total number of goals:\s*(\d+).*?'
        r'Number of covered goals:\s*(\d+)',
        re.S
    )

    matches = pattern.findall(text)

    if not matches:
        return None

    # Last LINE coverage block in the log
    total, covered = map(int, matches[-1])

    if total <= 0:
        return None

    return covered, total


def read_gwo():
    result = defaultdict(lambda: defaultdict(lambda: [0, 0]))

    base = os.path.join(
        ROOT,
        "GreyWolfOptimizer",
        "Result_Round1"
    )

    for project in PROJECTS:

        project_dir = os.path.join(base, project)

        if not os.path.isdir(project_dir):
            continue

        for bug_dir in os.listdir(project_dir):

            bug_path = os.path.join(project_dir, bug_dir)

            if not os.path.isdir(bug_path):
                continue

            bug = normalize_bug(bug_dir)

            if bug is None:
                continue

            gwo_dir = os.path.join(bug_path, "GWO")

            if not os.path.isdir(gwo_dir):
                continue

            # ------------------------------------------------
            # Math special structure:
            #
            # Math/Math-1b/GWO/terminal.log
            # ------------------------------------------------
            direct_log = os.path.join(gwo_dir, "terminal.log")

            if os.path.isfile(direct_log):

                parsed = parse_gwo_log(direct_log)

                if parsed:
                    covered, total = parsed
                    result[project][bug][0] += covered
                    result[project][bug][1] += total

                continue

            # ------------------------------------------------
            # Normal structure:
            #
            # Project/Project-1b/GWO/<target>/terminal.log
            # ------------------------------------------------
            for root, dirs, files in os.walk(gwo_dir):

                if "terminal.log" not in files:
                    continue

                log = os.path.join(root, "terminal.log")

                parsed = parse_gwo_log(log)

                if parsed:
                    covered, total = parsed
                    result[project][bug][0] += covered
                    result[project][bug][1] += total

    return result


# ============================================================
# ZEST
# ============================================================

def read_zest():
    result = defaultdict(dict)

    base = os.path.join(ROOT, "Zest", "reports")

    if not os.path.isdir(base):
        return result

    for filename in os.listdir(base):

        if not filename.endswith(".md"):
            continue

        path = os.path.join(base, filename)

        # Project-BUG-b...
        m = re.match(r"([A-Za-z]+)-(\d+)b-", filename)

        if not m:
            continue

        project = m.group(1)
        bug = normalize_bug(m.group(2))

        text = open(
            path,
            encoding="utf-8",
            errors="ignore"
        ).read()

        m_cov = re.search(
            r'Line coverage\s*\|\s*(\d+)\s*/\s*(\d+)\s*=\s*([\d.]+)%',
            text,
            re.I
        )

        if not m_cov:
            continue

        covered = int(m_cov.group(1))
        total = int(m_cov.group(2))

        if total > 0:
            result[project][bug] = (covered, total)

    return result


# ============================================================
# GEMINI
# ============================================================

def read_gemini():
    result = defaultdict(dict)

    base = os.path.join(
        ROOT,
        "Gemini-3.7-flash",
        "Coverage"
    )

    for project in PROJECTS:

        path = os.path.join(
            base,
            f"results_gemini-3.7-flash_{project}.csv"
        )

        if not os.path.isfile(path):
            continue

        try:
            df = pd.read_csv(path)
        except Exception:
            continue

        required = {
            "bug_id",
            "line_total",
            "line_covered"
        }

        if not required.issubset(df.columns):
            continue

        for _, row in df.iterrows():

            bug = normalize_bug(row["bug_id"])

            if bug is None:
                continue

            try:
                total = float(row["line_total"])
                covered = float(row["line_covered"])
            except Exception:
                continue

            if total <= 0:
                continue

            result[project][bug] = (covered, total)

    return result


# ============================================================
# DEEPSEEK
# ============================================================

def read_deepseek():
    result = defaultdict(dict)

    base = os.path.join(
        ROOT,
        "deepseek-v4-flash"
    )

    for project in PROJECTS:

        path = os.path.join(
            base,
            project,
            f"results_deepseek-v4-flash_{project}.csv"
        )

        if not os.path.isfile(path):
            continue

        try:
            df = pd.read_csv(path)
        except Exception:
            continue

        required = {
            "bug_id",
            "line_total",
            "line_covered"
        }

        if not required.issubset(df.columns):
            continue

        for _, row in df.iterrows():

            bug = normalize_bug(row["bug_id"])

            if bug is None:
                continue

            try:
                total = float(row["line_total"])
                covered = float(row["line_covered"])
            except Exception:
                continue

            if total <= 0:
                continue

            result[project][bug] = (covered, total)

    return result


# ============================================================
# Coverage calculation
# ============================================================

def coverage(data, project, bug):

    try:
        covered, total = data[project][bug]

        if total <= 0:
            return np.nan

        return covered / total * 100

    except Exception:
        return np.nan


def average_project(data, project, bugs):

    values = [
        coverage(data, project, bug)
        for bug in bugs
    ]

    values = [x for x in values if not np.isnan(x)]

    if not values:
        return np.nan

    return np.mean(values)


# ============================================================
# MAIN
# ============================================================

print()
print("=" * 90)
print("READING RESULTS")
print("=" * 90)

gwo = read_gwo()
zest = read_zest()
gemini = read_gemini()
deepseek = read_deepseek()


# ------------------------------------------------------------
# Show available bugs
# ------------------------------------------------------------

print()
print("=" * 90)
print("USABLE LINE-COVERAGE BUGS")
print("=" * 90)

print(
    f"{'Project':<20}"
    f"{'GWO':>8}"
    f"{'Zest':>8}"
    f"{'Gemini':>10}"
    f"{'DeepSeek':>10}"
)

print("-" * 90)

for project in PROJECTS:

    print(
        f"{project:<20}"
        f"{len(gwo[project]):>8}"
        f"{len(zest[project]):>8}"
        f"{len(gemini[project]):>10}"
        f"{len(deepseek[project]):>10}"
    )


# ============================================================
# Common bugs
# ============================================================

common = {}

for project in PROJECTS:

    common[project] = (
        set(gwo[project])
        & set(zest[project])
        & set(gemini[project])
        & set(deepseek[project])
    )


print()
print("=" * 90)
print("COMMON BUGS")
print("=" * 90)

print(
    f"{'Project':<20}"
    f"{'Common Bugs':>15}"
)

print("-" * 90)

for project in PROJECTS:

    bugs = sorted(
        common[project],
        key=bug_number
    )

    print(
        f"{project:<20}"
        f"{len(bugs):>15}"
    )

total_common = sum(
    len(common[p])
    for p in PROJECTS
)

print("-" * 90)
print(f"{'TOTAL':<20}{total_common:>15}")


# ============================================================
# Graph 1
# Average Line Coverage by Project
# ============================================================

project_values = {
    "GWO": [],
    "Zest": [],
    "Gemini-3.7-flash": [],
    "DeepSeek-v4-flash": []
}

for project in PROJECTS:

    # all usable bugs for each method
    project_values["GWO"].append(
        average_project(
            gwo,
            project,
            gwo[project].keys()
        )
    )

    project_values["Zest"].append(
        average_project(
            zest,
            project,
            zest[project].keys()
        )
    )

    project_values["Gemini-3.7-flash"].append(
        average_project(
            gemini,
            project,
            gemini[project].keys()
        )
    )

    project_values["DeepSeek-v4-flash"].append(
        average_project(
            deepseek,
            project,
            deepseek[project].keys()
        )
    )


x = np.arange(len(PROJECTS))
width = 0.2

plt.figure(figsize=(16, 9))

for i, (method, values) in enumerate(project_values.items()):

    values = np.array(values, dtype=float)

    plt.bar(
        x + (i - 1.5) * width,
        values,
        width,
        label=method
    )

plt.xticks(x, PROJECTS, rotation=45, ha="right")
plt.ylabel("Average Line Coverage (%)")
plt.xlabel("Defects4J Project")
plt.title("Average Line Coverage by Defects4J Project")
plt.ylim(0, 100)
plt.legend()
plt.grid(axis="y", alpha=0.3)
plt.tight_layout()

plt.savefig(
    os.path.join(
        OUT,
        "01_Average_Line_Coverage_by_Defects4J_Project.png"
    ),
    dpi=300
)

plt.close()


# ============================================================
# Graph 2
# Line Coverage on Common Bugs
# ============================================================

common_values = {
    "GWO": [],
    "Zest": [],
    "Gemini-3.7-flash": [],
    "DeepSeek-v4-flash": []
}

for project in PROJECTS:

    bugs = common[project]

    common_values["GWO"].append(
        average_project(gwo, project, bugs)
    )

    common_values["Zest"].append(
        average_project(zest, project, bugs)
    )

    common_values["Gemini-3.7-flash"].append(
        average_project(gemini, project, bugs)
    )

    common_values["DeepSeek-v4-flash"].append(
        average_project(deepseek, project, bugs)
    )


plt.figure(figsize=(16, 9))

for i, (method, values) in enumerate(common_values.items()):

    values = np.array(values, dtype=float)

    plt.bar(
        x + (i - 1.5) * width,
        values,
        width,
        label=method
    )

plt.xticks(x, PROJECTS, rotation=45, ha="right")
plt.ylabel("Average Line Coverage (%)")
plt.xlabel("Defects4J Project")
plt.title("Line Coverage on Common Bugs")
plt.ylim(0, 100)
plt.legend()
plt.grid(axis="y", alpha=0.3)
plt.tight_layout()

plt.savefig(
    os.path.join(
        OUT,
        "02_Line_Coverage_on_Common_Bugs.png"
    ),
    dpi=300
)

plt.close()


# ============================================================
# Graph 3
# Overall Line Coverage on Common Bugs
# ============================================================

overall = {}

for method, data in [
    ("GWO", gwo),
    ("Zest", zest),
    ("Gemini-3.7-flash", gemini),
    ("DeepSeek-v4-flash", deepseek)
]:

    values = []

    for project in PROJECTS:

        for bug in common[project]:

            value = coverage(
                data,
                project,
                bug
            )

            if not np.isnan(value):
                values.append(value)

    overall[method] = (
        np.mean(values)
        if values
        else np.nan
    )


print()
print("=" * 90)
print("OVERALL LINE COVERAGE ON COMMON BUGS")
print("=" * 90)

for method, value in overall.items():

    if np.isnan(value):
        print(f"{method:<22} N/A")
    else:
        print(f"{method:<22} {value:.2f}%")


methods = list(overall.keys())
values = list(overall.values())

plt.figure(figsize=(10, 6))

bars = plt.bar(
    methods,
    values
)

plt.ylabel("Average Line Coverage (%)")
plt.title("Overall Line Coverage on Common Bugs")
plt.ylim(0, 100)
plt.grid(axis="y", alpha=0.3)

for bar, value in zip(bars, values):

    if not np.isnan(value):

        plt.text(
            bar.get_x() + bar.get_width() / 2,
            value + 1,
            f"{value:.2f}%",
            ha="center"
        )

plt.tight_layout()

plt.savefig(
    os.path.join(
        OUT,
        "03_Overall_Line_Coverage_on_Common_Bugs.png"
    ),
    dpi=300
)

plt.close()


print()
print("=" * 90)
print("DONE")
print("=" * 90)

print(f"Total common bugs : {total_common}")

print()
print("Output:")
print(OUT)

print()
print("Files:")
print("  01_Average_Line_Coverage_by_Defects4J_Project.png")
print("  02_Line_Coverage_on_Common_Bugs.png")
print("  03_Overall_Line_Coverage_on_Common_Bugs.png")
print()
