import csv
import glob
import os
import subprocess
from collections import defaultdict

# Project ที่ต้องการสรุป
PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections",
    "Compress", "Csv", "Gson", "JacksonCore",
    "JacksonDatabind", "JacksonXml", "Jsoup",
    "JxPath", "Lang", "Math", "Mockito", "Time"
]

def get_expected_bugs(project):
    """ดึง Bug IDs ที่มีจริงจาก Defects4J"""
    try:
        result = subprocess.run(
            ["defects4j", "bids", "-p", project],
            capture_output=True,
            text=True,
            check=True
        )
        return set(
            int(line.strip())
            for line in result.stdout.splitlines()
            if line.strip().isdigit()
        )
    except Exception as e:
        print(f"WARNING: ดึง Bug IDs ของ {project} ไม่สำเร็จ: {e}")
        return None

def read_csv(project):
    pattern = f"results_gemini-3.7-flash_{project}.csv"
    files = glob.glob(pattern)

    if not files:
        return []

    rows = []
    for filename in files:
        with open(filename, newline="", encoding="utf-8-sig") as f:
            rows.extend(csv.DictReader(f))
    return rows

summary = []

print("=" * 120)
print("GEMINI-3.7-FLASH: SUMMARY BY PROJECT")
print("=" * 120)

headers = [
    "Project", "Expected", "Recorded", "Missing",
    "Compile Pass", "Compile Fail", "Partial",
    "Class Rows", "No CSV"
]
print("{:<18} {:>9} {:>9} {:>9} {:>13} {:>12} {:>9} {:>11} {:>8}".format(*headers))
print("-" * 120)

for project in PROJECTS:
    expected = get_expected_bugs(project)
    rows = read_csv(project)

    bugs = defaultdict(list)
    for row in rows:
        try:
            bug_id = int(row["bug_id"])
            bugs[bug_id].append(row)
        except (ValueError, KeyError):
            continue

    recorded = set(bugs.keys())

    pass_bugs = 0
    fail_bugs = 0
    partial_bugs = 0

    for bug_id, bug_rows in bugs.items():
        statuses = [
            str(r.get("compile_ok", "")).strip().lower() in ("true", "1", "yes")
            for r in bug_rows
        ]

        if all(statuses):
            pass_bugs += 1
        elif any(statuses):
            partial_bugs += 1
        else:
            fail_bugs += 1

    if expected is not None:
        missing = len(expected - recorded)
        expected_count = len(expected)
        no_csv = "No" if rows else "Yes"
    else:
        missing = -1
        expected_count = -1
        no_csv = "Yes" if not rows else "Unknown"

    summary.append({
        "Project": project,
        "Expected": expected_count,
        "Recorded": len(recorded),
        "Missing": missing,
        "Compile Pass": pass_bugs,
        "Compile Fail": fail_bugs,
        "Partial": partial_bugs,
        "Class Rows": len(rows),
        "No CSV": no_csv
    })

    print(
        f"{project:<18} "
        f"{expected_count:>9} "
        f"{len(recorded):>9} "
        f"{missing if missing >= 0 else 'N/A':>9} "
        f"{pass_bugs:>13} "
        f"{fail_bugs:>12} "
        f"{partial_bugs:>9} "
        f"{len(rows):>11} "
        f"{no_csv:>8}"
    )

print("-" * 120)

# รวมยอด โดยไม่นับ project ที่ดึงจำนวน expected ไม่สำเร็จ
valid = [s for s in summary if s["Expected"] >= 0]

print(
    f"{'TOTAL':<18} "
    f"{sum(s['Expected'] for s in valid):>9} "
    f"{sum(s['Recorded'] for s in summary):>9} "
    f"{sum(s['Missing'] for s in valid):>9} "
    f"{sum(s['Compile Pass'] for s in summary):>13} "
    f"{sum(s['Compile Fail'] for s in summary):>12} "
    f"{sum(s['Partial'] for s in summary):>9} "
    f"{sum(s['Class Rows'] for s in summary):>11}"
)

# บันทึกตารางสรุปเป็น CSV
with open("gemini_project_summary.csv", "w", newline="", encoding="utf-8-sig") as f:
    writer = csv.DictWriter(f, fieldnames=headers)
    writer.writeheader()
    writer.writerows(summary)

print("\nSaved: gemini_project_summary.csv")

# สร้างรายการ Bug ID ที่ไม่มีข้อมูล
with open("gemini_missing_bugs.csv", "w", newline="", encoding="utf-8-sig") as f:
    writer = csv.writer(f)
    writer.writerow(["Project", "Missing Bug ID"])

    for s in summary:
        if s["Expected"] < 0:
            continue

        project = s["Project"]
        expected = get_expected_bugs(project)
        recorded = set()

        for row in read_csv(project):
            try:
                recorded.add(int(row["bug_id"]))
            except (ValueError, KeyError):
                pass

        if expected is not None:
            for bug_id in sorted(expected - recorded):
                writer.writerow([project, bug_id])

print("Saved: gemini_missing_bugs.csv")
