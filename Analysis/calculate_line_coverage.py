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

# ============================================================
# Storage
# ============================================================

results = {
    "GWO": defaultdict(list),
    "Zest": defaultdict(list),
    "Gemini": defaultdict(list),
    "DeepSeek": defaultdict(list),
}


# ============================================================
# GWO
# อ่านจาก terminal.log โดยตรง
# ============================================================

gwo_root = "GreyWolfOptimizer/Result_Round1"

for path in glob.glob(gwo_root + "/**/terminal.log", recursive=True):

    project = None

    for p in PROJECTS:
        if f"/{p}/" in "/" + path.replace("\\", "/"):
            project = p
            break

    if not project:
        continue

    # หา Line Coverage เช่น
    # Line: 72.78%
    # Line Coverage: 72.78%
    # Coverage of criterion LINE: 72.78%
    patterns = [
        r"Line(?:\s+Coverage)?\s*[:=]\s*(\d+(?:\.\d+)?)\s*%",
        r"Coverage of criterion LINE:\s*(\d+(?:\.\d+)?)\s*%",
        r"LINE.*?(\d+(?:\.\d+)?)\s*%"
    ]

    coverage = None

    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            text = f.read()
    except Exception:
        continue

    for pattern in patterns:
        matches = re.findall(pattern, text, re.IGNORECASE)
        if matches:
            try:
                coverage = float(matches[-1])
                break
            except ValueError:
                pass

    if coverage is not None and 0 <= coverage <= 100:
        results["GWO"][project].append(coverage)


# ============================================================
# Zest
# อ่านจาก Zest/reports/*.md
# ============================================================

zest_root = "Zest/reports"

for path in glob.glob(zest_root + "/*.md"):

    filename = os.path.basename(path)

    project = None

    for p in PROJECTS:
        if filename.startswith(p + "-"):
            project = p
            break

    if not project:
        continue

    try:
        with open(path, "r", encoding="utf-8", errors="ignore") as f:
            text = f.read()
    except Exception:
        continue

    # ตัวอย่าง:
    # Line coverage | 230 / 355 = 64.79%
    match = re.search(
        r"Line coverage\s*\|\s*\d+\s*/\s*\d+\s*=\s*(\d+(?:\.\d+)?)\s*%",
        text,
        re.IGNORECASE
    )

    if match:
        coverage = float(match.group(1))

        if 0 <= coverage <= 100:
            results["Zest"][project].append(coverage)


# ============================================================
# Gemini
# ============================================================

gemini_root = "Gemini-3.7-flash/Coverage"

for path in glob.glob(gemini_root + "/results_gemini-3.7-flash_*.csv"):

    try:
        with open(path, "r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)

            for row in reader:

                project = row.get("project", "").strip()

                if project not in PROJECTS:
                    continue

                value = row.get("line_coverage", "").strip()

                if not value or value.upper() == "N/A":
                    continue

                try:
                    coverage = float(value)
                except ValueError:
                    continue

                if 0 <= coverage <= 100:
                    results["Gemini"][project].append(coverage)

    except Exception as e:
        print(f"[WARNING] Gemini: {path}: {e}")


# ============================================================
# DeepSeek
# ============================================================

deepseek_root = "deepseek-v4-flash"

for path in glob.glob(deepseek_root + "/**/results_deepseek-v4-flash_*.csv",
                       recursive=True):

    try:
        with open(path, "r", encoding="utf-8-sig", newline="") as f:
            reader = csv.DictReader(f)

            for row in reader:

                project = row.get("project", "").strip()

                if project not in PROJECTS:
                    continue

                value = row.get("line_coverage", "").strip()

                if not value or value.upper() == "N/A":
                    continue

                try:
                    coverage = float(value)
                except ValueError:
                    continue

                if 0 <= coverage <= 100:
                    results["DeepSeek"][project].append(coverage)

    except Exception as e:
        print(f"[WARNING] DeepSeek: {path}: {e}")


# ============================================================
# Calculate Average
# ============================================================

print()
print("=" * 110)
print("AVERAGE LINE COVERAGE BY PROJECT")
print("=" * 110)

print(
    f"{'Project':<20}"
    f"{'GWO':>15}"
    f"{'Zest':>15}"
    f"{'Gemini':>15}"
    f"{'DeepSeek':>15}"
)

print("-" * 110)

all_values = defaultdict(list)

for project in PROJECTS:

    row = [project]

    for method in ["GWO", "Zest", "Gemini", "DeepSeek"]:

        values = results[method].get(project, [])

        if values:
            avg = sum(values) / len(values)
            row.append(f"{avg:.2f}%")

            all_values[method].extend(values)
        else:
            row.append("N/A")

    print(
        f"{row[0]:<20}"
        f"{row[1]:>15}"
        f"{row[2]:>15}"
        f"{row[3]:>15}"
        f"{row[4]:>15}"
    )

print("-" * 110)

print()
print("NUMBER OF BUGS USED IN AVERAGE")
print("-" * 110)

print(
    f"{'Project':<20}"
    f"{'GWO':>15}"
    f"{'Zest':>15}"
    f"{'Gemini':>15}"
    f"{'DeepSeek':>15}"
)

print("-" * 110)

for project in PROJECTS:

    print(
        f"{project:<20}"
        f"{len(results['GWO'].get(project, [])):>15}"
        f"{len(results['Zest'].get(project, [])):>15}"
        f"{len(results['Gemini'].get(project, [])):>15}"
        f"{len(results['DeepSeek'].get(project, [])):>15}"
    )

print("-" * 110)


# ============================================================
# Save CSV
# ============================================================

os.makedirs("Analysis", exist_ok=True)

output = "Analysis/average_line_coverage_by_project.csv"

with open(output, "w", encoding="utf-8", newline="") as f:

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

        for method in ["GWO", "Zest", "Gemini", "DeepSeek"]:

            values = results[method].get(project, [])

            if values:
                avg = sum(values) / len(values)
                row.extend([f"{avg:.2f}", len(values)])
            else:
                row.extend(["", 0])

        writer.writerow(row)

print()
print(f"Saved: {output}")
print()
