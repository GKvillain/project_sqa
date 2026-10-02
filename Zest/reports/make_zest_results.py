import re
import csv
from pathlib import Path
from collections import defaultdict

REPORT_DIR = Path(".")
OUTPUT_DIR = Path("../results")
OUTPUT_DIR.mkdir(parents=True, exist_ok=True)

# ใช้เฉพาะ budget 120 วินาที
BUDGET = 120

projects = defaultdict(list)

for file in REPORT_DIR.glob("*.md"):

    # เช่น:
    # Chart-1-b120-r1.md
    # JacksonDatabind-37-b120-r1.md
    m = re.match(r"(.+)-(\d+)-b(\d+)-r(\d+)$", file.stem)

    if not m:
        continue

    project = m.group(1)
    bug_id = int(m.group(2))
    budget = int(m.group(3))
    run_id = int(m.group(4))

    # เอาเฉพาะ b120
    if budget != BUDGET:
        continue

    text = file.read_text(errors="ignore")

    def find(pattern):
        m = re.search(pattern, text, re.MULTILINE)
        return m.group(1) if m else None

    # -------------------------
    # Metrics
    # -------------------------

    line = find(
        r"\|\s*Line coverage\s*\|\s*[\d,]+\s*/\s*[\d,]+\s*=\s*([\d.]+)%"
    )

    branch = find(
        r"\|\s*Branch coverage\s*\|\s*[\d,]+\s*/\s*[\d,]+\s*=\s*([\d.]+)%"
    )

    mutation = find(
        r"\|\s*Mutation score\s*\|\s*[\d,]+\s*/\s*[\d,]+\s*=\s*([\d.]+)%"
    )

    executions = find(
        r"\|\s*Executions\s*\|\s*([\d,]+)"
    )

    junit_tests = find(
        r"\|\s*JUnit tests\s*\|\s*(\d+)"
    )

    test_length = find(
        r"\|\s*Test suite length \(statements\)\s*\|\s*(\d+)"
    )

    fuzz_time = find(
        r"\|\s*Fuzz time\s*\|\s*(\d+)s"
    )

    fault_detected = find(
        r"\*\*Fault detected\*\*\s*\|\s*\*\*(yes|no)\*\*"
    )

    # -------------------------
    # Convert values
    # -------------------------

    line = float(line) if line else None
    branch = float(branch) if branch else None
    mutation = float(mutation) if mutation else None

    executions = (
        int(executions.replace(",", ""))
        if executions else None
    )

    junit_tests = int(junit_tests) if junit_tests else None
    test_length = int(test_length) if test_length else None
    fuzz_time = int(fuzz_time) if fuzz_time else None

    projects[project].append({
        "project": project,
        "bug_id": bug_id,
        "method": "Zest",
        "run": run_id,
        "line_coverage": line,
        "branch_coverage": branch,
        "fault_detected": fault_detected if fault_detected else "no",
        "time_sec": fuzz_time,
        "test_cases": junit_tests,
        "test_length": test_length,
        "mutation_score": mutation,
        "statements": executions,
    })


# -------------------------
# Write one CSV per project
# -------------------------

columns = [
    "project",
    "bug_id",
    "method",
    "run",
    "line_coverage",
    "branch_coverage",
    "fault_detected",
    "time_sec",
    "test_cases",
    "test_length",
    "mutation_score",
    "statements",
]

total_rows = 0

for project, records in sorted(projects.items()):

    records.sort(
        key=lambda x: (x["bug_id"], x["run"])
    )

    output_file = OUTPUT_DIR / f"{project}.csv"

    with output_file.open(
        "w",
        newline="",
        encoding="utf-8"
    ) as f:

        writer = csv.DictWriter(
            f,
            fieldnames=columns
        )

        writer.writeheader()

        for record in records:
            writer.writerow(record)

    total_rows += len(records)

    print(
        f"{project:20s} "
        f"{len(set(r['bug_id'] for r in records)):4d} bugs "
        f"{len(records):4d} runs -> {output_file}"
    )

print()
print("==========================================")
print(f"Projects : {len(projects)}")
print(f"Rows     : {total_rows}")
print(f"Budget   : {BUDGET}s")
print("==========================================")
