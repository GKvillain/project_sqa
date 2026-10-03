import os
import re
import csv
import glob
from collections import defaultdict

ROOT = "/mnt/d/AllFinalWork/project_sqa"

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections",
    "Compress", "Csv", "Gson", "JacksonCore", "JacksonDatabind",
    "JacksonXml", "Jsoup", "JxPath", "Lang", "Math",
    "Mockito", "Time"
]


# ============================================================
# GWO
# ============================================================

def read_gwo():

    # project -> bug -> [covered, total]
    data = defaultdict(lambda: defaultdict(lambda: [0, 0]))

    for project in PROJECTS:

        # Math มีโครงสร้าง path ต่างจาก Project อื่น
        if project == "Math":
            patterns = [
                f"{ROOT}/GreyWolfOptimizer/Result_Round1/"
                f"Math/Math-*b/GWO/terminal.log"
            ]
        else:
            patterns = [
                f"{ROOT}/GreyWolfOptimizer/Result_Round1/"
                f"{project}/{project}-*b/GWO/*/terminal.log"
            ]

        for pattern in patterns:

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
                    "r",
                    encoding="utf-8",
                    errors="ignore"
                ) as f:
                    text = f.read()

                # LINE block
                positions = [
                    m.start()
                    for m in re.finditer(
                        r"\* Coverage analysis for criterion LINE",
                        text
                    )
                ]

                if not positions:
                    continue

                block = text[positions[-1]:]

                m_total = re.search(
                    r"\* Total number of goals:\s*(\d+)",
                    block
                )

                m_covered = re.search(
                    r"\* Number of covered goals:\s*(\d+)",
                    block
                )

                if not m_total or not m_covered:
                    continue

                total = int(m_total.group(1))
                covered = int(m_covered.group(1))

                if total <= 0:
                    continue

                # รวม target classes ของ bug เดียวกัน
                data[project][bug][0] += covered
                data[project][bug][1] += total

    return data


# ============================================================
# ZEST
# ============================================================

def read_zest():

    # project -> bug -> [covered, total]
    data = defaultdict(dict)

    report_dir = f"{ROOT}/Zest/reports"

    for path in glob.glob(f"{report_dir}/*.md"):

        name = os.path.basename(path)

        m = re.match(
            r"([A-Za-z]+)-(\d+)-",
            name
        )

        if not m:
            continue

        project = m.group(1)
        bug = int(m.group(2))

        if project not in PROJECTS:
            continue

        with open(
            path,
            "r",
            encoding="utf-8",
            errors="ignore"
        ) as f:
            text = f.read()

        matches = re.findall(
            r"Line coverage\s*\|\s*"
            r"(\d+)\s*/\s*(\d+)\s*=\s*"
            r"([0-9.]+)%",
            text,
            re.IGNORECASE
        )

        if not matches:
            continue

        covered, total, _ = matches[-1]

        total = int(total)
        covered = int(covered)

        if total > 0:
            data[project][bug] = (
                covered,
                total
            )

    return data


# ============================================================
# GEMINI / DEEPSEEK
# ============================================================

def read_ai_csv(base_dir, filename_template):

    # project -> bug -> [covered, total]
    data = defaultdict(lambda: defaultdict(lambda: [0, 0]))

    for project in PROJECTS:

        path = (
            f"{ROOT}/{base_dir}/"
            f"{filename_template.format(project=project)}"
        )

        if not os.path.isfile(path):
            continue

        with open(
            path,
            "r",
            encoding="utf-8-sig",
            errors="ignore",
            newline=""
        ) as f:

            reader = csv.DictReader(f)

            for row in reader:

                try:
                    p = row["project"].strip()
                    bug = int(row["bug_id"])
                    total = float(row["line_total"])
                    covered = float(row["line_covered"])
                except (
                    KeyError,
                    ValueError,
                    TypeError
                ):
                    continue

                if p not in PROJECTS:
                    continue

                if total <= 0:
                    continue

                # Multiple rows / target classes -> aggregate by bug
                data[p][bug][0] += covered
                data[p][bug][1] += total

    return data


# ============================================================
# CALCULATE
# ============================================================

def calculate(data):

    result = {}

    for project in PROJECTS:

        values = []

        for bug, (covered, total) in data[project].items():

            if total > 0:
                values.append(
                    covered / total * 100
                )

        if values:

            result[project] = {
                "bugs": len(values),
                "average": sum(values) / len(values)
            }

        else:

            result[project] = {
                "bugs": 0,
                "average": None
            }

    return result


# ============================================================
# READ ALL METHODS
# ============================================================

print("Reading GWO...")
gwo = calculate(
    read_gwo()
)

print("Reading Zest...")
zest = calculate(
    read_zest()
)

print("Reading Gemini...")
gemini = calculate(
    read_ai_csv(
        "Gemini-3.7-flash/Coverage",
        "results_gemini-3.7-flash_{project}.csv"
    )
)

print("Reading DeepSeek...")
deepseek = calculate(
    read_ai_csv(
        "deepseek-v4-flash",
        "{project}/results_deepseek-v4-flash_{project}.csv"
    )
)


METHODS = {
    "GWO": gwo,
    "Zest": zest,
    "Gemini": gemini,
    "DeepSeek": deepseek
}


# ============================================================
# PROJECT TABLE
# ============================================================

print()
print("=" * 105)
print("AVERAGE LINE COVERAGE BY PROJECT — ALL METHODS")
print("=" * 105)

print(
    f"{'Project':<20}"
    f"{'GWO':>15}"
    f"{'Zest':>15}"
    f"{'Gemini':>15}"
    f"{'DeepSeek':>15}"
)

print("-" * 105)

for project in PROJECTS:

    row = [project]

    for method in METHODS:

        avg = METHODS[method][project]["average"]

        if avg is None:
            value = "N/A"
        else:
            value = f"{avg:.2f}%"

        row.append(value)

    print(
        f"{row[0]:<20}"
        f"{row[1]:>15}"
        f"{row[2]:>15}"
        f"{row[3]:>15}"
        f"{row[4]:>15}"
    )


# ============================================================
# OVERALL
# ============================================================

print()
print("=" * 105)
print("OVERALL LINE COVERAGE")
print("=" * 105)

overall = {}

for method in METHODS:

    result = METHODS[method]

    values = [
        x["average"]
        for x in result.values()
        if x["average"] is not None
    ]

    bugs = sum(
        x["bugs"]
        for x in result.values()
    )

    if values:
        avg = sum(values) / len(values)
    else:
        avg = None

    overall[method] = {
        "bugs": bugs,
        "projects": len(values),
        "average": avg
    }

    if avg is None:
        print(
            f"{method:<12}"
            f"bugs={bugs:>4}  "
            f"projects={len(values):>2}  "
            f"average=N/A"
        )
    else:
        print(
            f"{method:<12}"
            f"bugs={bugs:>4}  "
            f"projects={len(values):>2}  "
            f"average={avg:.2f}%"
        )


# ============================================================
# BUG COUNTS
# ============================================================

print()
print("=" * 105)
print("BUG COUNTS WITH USABLE LINE COVERAGE")
print("=" * 105)

print(
    f"{'Project':<20}"
    f"{'GWO':>10}"
    f"{'Zest':>10}"
    f"{'Gemini':>10}"
    f"{'DeepSeek':>10}"
)

print("-" * 65)

for project in PROJECTS:

    print(
        f"{project:<20}"
        f"{gwo[project]['bugs']:>10}"
        f"{zest[project]['bugs']:>10}"
        f"{gemini[project]['bugs']:>10}"
        f"{deepseek[project]['bugs']:>10}"
    )

print("-" * 65)

print(
    f"{'TOTAL':<20}"
    f"{overall['GWO']['bugs']:>10}"
    f"{overall['Zest']['bugs']:>10}"
    f"{overall['Gemini']['bugs']:>10}"
    f"{overall['DeepSeek']['bugs']:>10}"
)


# ============================================================
# SAVE CSV
# ============================================================

output = (
    f"{ROOT}/Analysis/"
    "line_coverage_all_methods.csv"
)

with open(
    output,
    "w",
    encoding="utf-8",
    newline=""
) as f:

    writer = csv.writer(f)

    writer.writerow([
        "Project",
        "GWO_Bugs",
        "GWO_Line_Coverage",
        "Zest_Bugs",
        "Zest_Line_Coverage",
        "Gemini_Bugs",
        "Gemini_Line_Coverage",
        "DeepSeek_Bugs",
        "DeepSeek_Line_Coverage"
    ])

    for project in PROJECTS:

        def avg(method):
            x = METHODS[method][project]["average"]
            return "" if x is None else f"{x:.4f}"

        writer.writerow([
            project,
            gwo[project]["bugs"],
            avg("GWO"),
            zest[project]["bugs"],
            avg("Zest"),
            gemini[project]["bugs"],
            avg("Gemini"),
            deepseek[project]["bugs"],
            avg("DeepSeek")
        ])

print()
print(f"CSV saved to:")
print(output)
