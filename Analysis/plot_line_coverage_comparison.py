import csv
import glob
import os
import re
from collections import defaultdict

import matplotlib.pyplot as plt
import numpy as np


ROOT = "/mnt/d/AllFinalWork/project_sqa"

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections",
    "Compress", "Csv", "Gson", "JacksonCore", "JacksonDatabind",
    "JacksonXml", "Jsoup", "JxPath", "Lang", "Math",
    "Mockito", "Time"
]

OUT = f"{ROOT}/Analysis/LineCoverage_Diagrams"
os.makedirs(OUT, exist_ok=True)


# ============================================================
# READ GWO
# ============================================================

gwo = defaultdict(dict)

for project in PROJECTS:

    if project == "Math":
        pattern = (
            f"{ROOT}/GreyWolfOptimizer/Result_Round1/"
            f"Math/Math-*b/GWO/terminal.log"
        )
    else:
        pattern = (
            f"{ROOT}/GreyWolfOptimizer/Result_Round1/"
            f"{project}/{project}-*b/GWO/*/terminal.log"
        )

    tmp = defaultdict(lambda: [0, 0])

    for path in glob.glob(pattern):

        m = re.search(
            rf"/{re.escape(project)}-(\d+)b/GWO/",
            path
        )

        if not m:
            continue

        bug = int(m.group(1))

        with open(
            path,
            encoding="utf-8",
            errors="ignore"
        ) as f:
            text = f.read()

        positions = [
            x.start()
            for x in re.finditer(
                r"\* Coverage analysis for criterion LINE",
                text
            )
        ]

        if not positions:
            continue

        block = text[positions[-1]:]

        mt = re.search(
            r"\* Total number of goals:\s*(\d+)",
            block
        )

        mc = re.search(
            r"\* Number of covered goals:\s*(\d+)",
            block
        )

        if mt and mc:

            total = int(mt.group(1))
            covered = int(mc.group(1))

            if total > 0:
                tmp[bug][0] += covered
                tmp[bug][1] += total

    for bug, (covered, total) in tmp.items():

        if total > 0:
            gwo[project][bug] = covered / total * 100


# ============================================================
# READ ZEST
# ============================================================

zest = defaultdict(dict)

for project in PROJECTS:

    for path in glob.glob(
        f"{ROOT}/Zest/reports/{project}-*-b*.md"
    ):

        filename = path.split("/")[-1]

        m = re.search(
            rf"^{re.escape(project)}-(\d+)-b\d+",
            filename
        )

        if not m:
            continue

        bug = int(m.group(1))

        with open(
            path,
            encoding="utf-8",
            errors="ignore"
        ) as f:
            text = f.read()

        m_cov = re.search(
            r"Line coverage\s*\|\s*(\d+)\s*/\s*(\d+)",
            text,
            re.I
        )

        if m_cov:

            covered = int(m_cov.group(1))
            total = int(m_cov.group(2))

            if total > 0:
                zest[project][bug] = covered / total * 100


# ============================================================
# READ GEMINI
# ============================================================

gemini = defaultdict(dict)

for project in PROJECTS:

    path = (
        f"{ROOT}/Gemini-3.7-flash/Coverage/"
        f"results_gemini-3.7-flash_{project}.csv"
    )

    try:

        with open(
            path,
            encoding="utf-8-sig",
            newline=""
        ) as f:

            for row in csv.DictReader(f):

                try:
                    bug = int(row["bug_id"])
                    total = float(row["line_total"])
                    covered = float(row["line_covered"])
                except:
                    continue

                if total > 0:
                    gemini[project][bug] = (
                        covered / total * 100
                    )

    except FileNotFoundError:
        pass


# ============================================================
# READ DEEPSEEK
# ============================================================

deepseek = defaultdict(dict)

for project in PROJECTS:

    path = (
        f"{ROOT}/deepseek-v4-flash/{project}/"
        f"results_deepseek-v4-flash_{project}.csv"
    )

    try:

        with open(
            path,
            encoding="utf-8-sig",
            newline=""
        ) as f:

            for row in csv.DictReader(f):

                try:
                    bug = int(row["bug_id"])
                    total = float(row["line_total"])
                    covered = float(row["line_covered"])
                except:
                    continue

                if total > 0:
                    deepseek[project][bug] = (
                        covered / total * 100
                    )

    except FileNotFoundError:
        pass


METHODS = {
    "GWO": gwo,
    "Zest": zest,
    "Gemini-3.7-flash": gemini,
    "DeepSeek-v4-flash": deepseek
}


# ============================================================
# COMMON BUGS
# ============================================================

common_bugs = {}

for project in PROJECTS:

    common_bugs[project] = (
        set(gwo[project])
        & set(zest[project])
        & set(gemini[project])
        & set(deepseek[project])
    )


# ============================================================
# GRAPH 1
# AVERAGE LINE COVERAGE BY PROJECT
# ============================================================

project_average = defaultdict(dict)

for project in PROJECTS:

    for method, data in METHODS.items():

        values = list(data[project].values())

        project_average[project][method] = (
            sum(values) / len(values)
            if values
            else np.nan
        )


def plot_project_graph(data, title, filename):

    x = np.arange(len(PROJECTS))
    width = 0.20

    fig, ax = plt.subplots(figsize=(18, 8))

    for i, method in enumerate(METHODS):

        values = [
            data[project][method]
            for project in PROJECTS
        ]

        ax.bar(
            x + (i - 1.5) * width,
            values,
            width,
            label=method
        )

    ax.set_title(title)
    ax.set_xlabel("Defects4J Project")
    ax.set_ylabel("Line Coverage (%)")

    ax.set_xticks(x)
    ax.set_xticklabels(
        PROJECTS,
        rotation=45,
        ha="right"
    )

    ax.set_ylim(0, 100)
    ax.grid(
        axis="y",
        alpha=0.3
    )

    ax.legend()

    fig.tight_layout()

    fig.savefig(
        f"{OUT}/{filename}",
        dpi=300,
        bbox_inches="tight"
    )

    plt.close(fig)


plot_project_graph(
    project_average,
    "Average Line Coverage by Defects4J Project",
    "01_Average_Line_Coverage_by_Defects4J_Project.png"
)


# ============================================================
# GRAPH 2
# LINE COVERAGE ON COMMON BUGS
# ============================================================

common_average = defaultdict(dict)

for project in PROJECTS:

    bugs = common_bugs[project]

    for method, data in METHODS.items():

        values = [
            data[project][bug]
            for bug in bugs
        ]

        common_average[project][method] = (
            sum(values) / len(values)
            if values
            else np.nan
        )


plot_project_graph(
    common_average,
    "Line Coverage on Common Bugs",
    "02_Line_Coverage_on_Common_Bugs.png"
)


# ============================================================
# GRAPH 3
# OVERALL LINE COVERAGE ON COMMON BUGS
# ============================================================

overall_common = {}

for method, data in METHODS.items():

    values = []

    for project in PROJECTS:

        for bug in common_bugs[project]:

            values.append(
                data[project][bug]
            )

    overall_common[method] = (
        sum(values) / len(values)
        if values
        else np.nan
    )


methods = list(METHODS.keys())

values = [
    overall_common[method]
    for method in methods
]


fig, ax = plt.subplots(figsize=(10, 7))

bars = ax.bar(
    methods,
    values
)

ax.set_title(
    "Overall Line Coverage on Common Bugs"
)

ax.set_xlabel(
    "Test Generation Method"
)

ax.set_ylabel(
    "Line Coverage (%)"
)

ax.set_ylim(0, 100)

ax.grid(
    axis="y",
    alpha=0.3
)

for bar, value in zip(bars, values):

    if not np.isnan(value):

        ax.text(
            bar.get_x() + bar.get_width() / 2,
            value + 1,
            f"{value:.2f}%",
            ha="center",
            va="bottom"
        )

fig.tight_layout()

fig.savefig(
    f"{OUT}/03_Overall_Line_Coverage_on_Common_Bugs.png",
    dpi=300,
    bbox_inches="tight"
)

plt.close(fig)


# ============================================================
# PRINT RESULTS
# ============================================================

print()
print("=" * 100)
print("LINE COVERAGE GRAPHS CREATED")
print("=" * 100)

print()
print("Common Bugs by Project")
print("-" * 50)

for project in PROJECTS:

    print(
        f"{project:<20}"
        f"{len(common_bugs[project]):>5}"
    )

total_common = sum(
    len(common_bugs[project])
    for project in PROJECTS
)

print("-" * 50)

print(
    f"{'TOTAL':<20}"
    f"{total_common:>5}"
)

print()
print("Overall Line Coverage on Common Bugs")
print("-" * 50)

for method in METHODS:

    print(
        f"{method:<22}"
        f"{overall_common[method]:.2f}%"
    )

print()
print(f"Output: {OUT}")

print()
print("Created:")
print("  01_Average_Line_Coverage_by_Defects4J_Project.png")
print("  02_Line_Coverage_on_Common_Bugs.png")
print("  03_Overall_Line_Coverage_on_Common_Bugs.png")

print("=" * 100)
