import csv
import glob
import os
import re
from collections import defaultdict

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections", "Compress",
    "Csv", "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml",
    "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"
]

METHODS = ["GWO", "Zest", "Gemini", "DeepSeek"]

results = {
    method: defaultdict(dict)
    for method in METHODS
}


# ============================================================
# GWO
#
# IMPORTANT:
# Use only Bug-level:
#   Project/Project-Nb/GWO/terminal.log
#
# Do NOT count:
#   Project/Project-Nb/GWO/<target-class>/terminal.log
# ============================================================

for project in PROJECTS:

    pattern = (
        f"GreyWolfOptimizer/Result_Round1/"
        f"{project}/{project}-*b/GWO/terminal.log"
    )

    for path in glob.glob(pattern):

        match_bug = re.search(
            rf"{re.escape(project)}-(\d+)b/GWO/terminal\.log$",
            path.replace("\\", "/")
        )

        if not match_bug:
            continue

        bug_id = int(match_bug.group(1))

        try:
            with open(path, "r", encoding="utf-8", errors="ignore") as f:
                text = f.read()
        except Exception:
            continue

        matches = re.findall(
            r"Coverage of criterion LINE:\s*(\d+(?:\.\d+)?)\s*%",
            text,
            re.IGNORECASE
        )

        if not matches:
            continue

        try:
            coverage = float(matches[-1])
        except ValueError:
            continue

        if 0 <= coverage <= 100:
            results["GWO"][project][bug_id] = coverage


# ============================================================
# Zest
#
# One report = one Bug
#
# Example:
# Zest/reports/Time-9-b120-r1.md
#
# Line coverage:
# | 230 / 355 = 64.79%
# ============================================================

for path in glob.glob("Zest/reports/*.md"):

    filename = os.path.basename(path)

    match = re.match(
        r"(.+)-(\d+)-b\d+-r\d+\.md$",
        filename
    )

    if not match:
        continue

    project = match.group(1)
    bug_id = int(match.group(2))

    if project not in PROJECTS:
        continue

    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            text = f.read()
    except Exception:
        continue

    match_cov = re.search(
        r"Line coverage\s*\|\s*\d+\s*/\s*\d+\s*=\s*(\d+(?:\.\d+)?)\s*%",
        text,
        re.IGNORECASE
    )

    if not match_cov:
        continue

    coverage = float(match_cov.group(1))

    if 0 <= coverage <= 100:
        results["Zest"][project][bug_id] = coverage


# ============================================================
# Gemini
#
# One CSV row / Bug
# Only rows with valid line_coverage
# ============================================================

for path in glob.glob(
    "Gemini-3.7-flash/Coverage/results_gemini-3.7-flash_*.csv"
):

    try:
        with open(
            path,
            "r",
            encoding="utf-8-sig",
            newline=""
        ) as f:

            reader = csv.DictReader(f)

            for row in reader:

                project = row.get("project", "").strip()

                if project not in PROJECTS:
                    continue

                bug_text = row.get("bug_id", "").strip()
                coverage_text = row.get("line_coverage", "").strip()

                if not bug_text or not coverage_text:
                    continue

                if coverage_text.upper() == "N/A":
                    continue

                try:
                    bug_id = int(float(bug_text))
                    coverage = float(coverage_text)
                except ValueError:
                    continue

                if 0 <= coverage <= 100:
                    results["Gemini"][project][bug_id] = coverage

    except Exception as e:
        print(f"[WARNING] Gemini: {path}: {e}")


# ============================================================
# DeepSeek
#
# One CSV row / Bug
# Only rows with valid line_coverage
# ============================================================

for path in glob.glob(
    "deepseek-v4-flash/**/results_deepseek-v4-flash_*.csv",
    recursive=True
):

    try:
        with open(
            path,
            "r",
            encoding="utf-8-sig",
            newline=""
        ) as f:

            reader = csv.DictReader(f)

            for row in reader:

                project = row.get("project", "").strip()

                if project not in PROJECTS:
                    continue

                bug_text = row.get("bug_id", "").strip()
                coverage_text = row.get("line_coverage", "").strip()

                if not bug_text or not coverage_text:
                    continue

                if coverage_text.upper() == "N/A":
                    continue

                try:
                    bug_id = int(float(bug_text))
                    coverage = float(coverage_text)
                except ValueError:
                    continue

                if 0 <= coverage <= 100:
                    results["DeepSeek"][project][bug_id] = coverage

    except Exception as e:
        print(f"[WARNING] DeepSeek: {path}: {e}")


# ============================================================
# Display Average Line Coverage
# ============================================================

print()
print("=" * 120)
print("AVERAGE LINE COVERAGE BY PROJECT")
print("=" * 120)

print(
    f"{'Project':<20}"
    f"{'GWO':>15}"
    f"{'Zest':>15}"
    f"{'Gemini':>15}"
    f"{'DeepSeek':>15}"
)

print("-" * 120)

for project in PROJECTS:

    row = []

    for method in METHODS:

        values = list(results[method][project].values())

        if values:
            avg = sum(values) / len(values)
            row.append(f"{avg:.2f}%")
        else:
            row.append("N/A")

    print(
        f"{project:<20}"
        f"{row[0]:>15}"
        f"{row[1]:>15}"
        f"{row[2]:>15}"
        f"{row[3]:>15}"
    )

print("-" * 120)


# ============================================================
# Number of Bugs Used
# ============================================================

print()
print("=" * 120)
print("NUMBER OF BUGS USED FOR LINE COVERAGE AVERAGE")
print("=" * 120)

print(
    f"{'Project':<20}"
    f"{'GWO':>15}"
    f"{'Zest':>15}"
    f"{'Gemini':>15}"
    f"{'DeepSeek':>15}"
)

print("-" * 120)

for project in PROJECTS:

    print(
        f"{project:<20}"
        f"{len(results['GWO'][project]):>15}"
        f"{len(results['Zest'][project]):>15}"
        f"{len(results['Gemini'][project]):>15}"
        f"{len(results['DeepSeek'][project]):>15}"
    )

print("-" * 120)


# ============================================================
# Overall Average
#
# This is the average of Bug-level coverage values.
# It is NOT an average of project averages.
# ============================================================

print()
print("=" * 120)
print("OVERALL LINE COVERAGE")
print("=" * 120)

for method in METHODS:

    values = []

    for project in PROJECTS:
        values.extend(results[method][project].values())

    if values:
        avg = sum(values) / len(values)

        print(
            f"{method:<12}"
            f"Average = {avg:.2f}%    "
            f"Bug count = {len(values)}"
        )
    else:
        print(
            f"{method:<12}"
            f"Average = N/A    Bug count = 0"
        )


# ============================================================
# Save CSV
# ============================================================

os.makedirs("Analysis", exist_ok=True)

output = "Analysis/line_coverage_all_methods.csv"

with open(
    output,
    "w",
    encoding="utf-8",
    newline=""
) as f:

    writer = csv.writer(f)

    writer.writerow([
        "Project",

        "GWO_Average_Line_Coverage",
        "GWO_Bug_Count",

        "Zest_Average_Line_Coverage",
        "Zest_Bug_Count",

        "Gemini_Average_Line_Coverage",
        "Gemini_Bug_Count",

        "DeepSeek_Average_Line_Coverage",
        "DeepSeek_Bug_Count"
    ])

    for project in PROJECTS:

        row = [project]

        for method in METHODS:

            values = list(results[method][project].values())

            if values:
                avg = sum(values) / len(values)
                row.extend([
                    f"{avg:.2f}",
                    len(values)
                ])
            else:
                row.extend([
                    "",
                    0
                ])

        writer.writerow(row)


# ============================================================
# Save Bug-level raw data
# ============================================================

raw_output = "Analysis/line_coverage_bug_level.csv"

with open(
    raw_output,
    "w",
    encoding="utf-8",
    newline=""
) as f:

    writer = csv.writer(f)

    writer.writerow([
        "Project",
        "Bug_ID",
        "Method",
        "Line_Coverage"
    ])

    for method in METHODS:

        for project in PROJECTS:

            for bug_id, coverage in sorted(
                results[method][project].items()
            ):

                writer.writerow([
                    project,
                    bug_id,
                    method,
                    f"{coverage:.2f}"
                ])


print()
print("=" * 120)
print("OUTPUT FILES")
print("=" * 120)
print(output)
print(raw_output)
print("=" * 120)
