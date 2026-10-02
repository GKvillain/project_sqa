import re
import csv
from pathlib import Path
from collections import defaultdict

REPORT_DIR = Path(".")
OUTPUT = Path("../zest_project_summary.csv")

# Project -> run-level records
projects = defaultdict(list)

for file in REPORT_DIR.glob("*.md"):
    name = file.stem

    # Example:
    # JacksonDatabind-37-b120-r1
    m = re.match(r"(.+)-(\d+)-b(\d+)-r(\d+)$", name)

    if not m:
        continue

    project = m.group(1)
    bug_id = int(m.group(2))
    budget = int(m.group(3))
    run_id = int(m.group(4))

    text = file.read_text(errors="ignore")

    def find(pattern):
        m = re.search(pattern, text, re.MULTILINE)
        return m.group(1) if m else None

    executions = find(r"\|\s*Executions\s*\|\s*([\d,]+)")
    junit_tests = find(r"\|\s*JUnit tests\s*\|\s*(\d+)")
    fault_detected = find(r"\*\*Fault detected\*\*\s*\|\s*\*\*(yes|no)\*\*")

    line = find(r"\|\s*Line coverage\s*\|\s*[\d,]+\s*/\s*[\d,]+\s*=\s*([\d.]+)%")
    branch = find(r"\|\s*Branch coverage\s*\|\s*[\d,]+\s*/\s*[\d,]+\s*=\s*([\d.]+)%")
    mutation = find(r"\|\s*Mutation score\s*\|\s*[\d,]+\s*/\s*[\d,]+\s*=\s*([\d.]+)%")

    suite_length = find(
        r"\|\s*Test suite length \(statements\)\s*\|\s*(\d+)"
    )

    if executions:
        executions = int(executions.replace(",", ""))

    if junit_tests:
        junit_tests = int(junit_tests)

    if line:
        line = float(line)

    if branch:
        branch = float(branch)

    if mutation:
        mutation = float(mutation)

    if suite_length:
        suite_length = int(suite_length)

    projects[project].append({
        "bug": bug_id,
        "budget": budget,
        "run": run_id,
        "executions": executions,
        "junit_tests": junit_tests,
        "fault_detected": fault_detected == "yes",
        "line": line,
        "branch": branch,
        "mutation": mutation,
        "suite_length": suite_length,
    })


rows = []

for project, records in sorted(projects.items()):

    bugs = len(set(r["bug"] for r in records))
    runs = len(records)

    detected_bugs = len(
        set(r["bug"] for r in records if r["fault_detected"])
    )

    detected_runs = sum(
        1 for r in records if r["fault_detected"]
    )

    def avg(field):
        values = [
            r[field]
            for r in records
            if r[field] is not None
        ]
        return sum(values) / len(values) if values else None

    rows.append([
        project,
        bugs,
        runs,
        detected_bugs,
        detected_runs,
        avg("line"),
        avg("branch"),
        avg("mutation"),
        avg("junit_tests"),
        avg("executions"),
    ])


with OUTPUT.open("w", newline="", encoding="utf-8") as f:
    writer = csv.writer(f)

    writer.writerow([
        "Project",
        "Bugs",
        "Runs",
        "Bug ที่ตรวจพบ",
        "รันที่ตรวจพบ",
        "Line (%)",
        "Branch (%)",
        "Mutation (%)",
        "จำนวนเทสต์",
        "Statements (executions)",
    ])

    for row in rows:
        writer.writerow([
            row[0],
            row[1],
            row[2],
            row[3],
            row[4],
            f"{row[5]:.2f}" if row[5] is not None else "",
            f"{row[6]:.2f}" if row[6] is not None else "",
            f"{row[7]:.2f}" if row[7] is not None else "",
            f"{row[8]:.2f}" if row[8] is not None else "",
            f"{row[9]:.2f}" if row[9] is not None else "",
        ])

print(f"Created: {OUTPUT}")
print(f"Projects: {len(rows)}")
