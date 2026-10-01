#!/usr/bin/env python3

from pathlib import Path
import csv
import re
import statistics


# ============================================================
# CONFIG
# ============================================================

RESULT_ROOT = Path(__file__).resolve().parent / "Result_Round1"
OUTPUT_DIR = RESULT_ROOT / "GWO_summary"

PROJECT_NAMES = {
    "Chart",
    "Cli",
    "Closure",
    "Codec",
    "Collections",
    "Compress",
    "Csv",
    "Gson",
    "JacksonCore",
    "JacksonDatabind",
    "JacksonXml",
    "Jsoup",
    "JxPath",
    "Lang",
    "Math",
    "Mockito",
    "Time",
}


# ============================================================
# UTILITY
# ============================================================

def to_float(value):
    if value is None:
        return None

    try:
        value = str(value).strip()
        value = value.replace("%", "")
        return float(value)
    except (ValueError, TypeError):
        return None


def to_int(value):
    if value is None:
        return None

    try:
        return int(float(str(value).strip()))
    except (ValueError, TypeError):
        return None


def avg(values):
    values = [
        float(v)
        for v in values
        if v is not None
    ]

    if not values:
        return None

    return statistics.mean(values)


def fmt(value, digits=4):
    if value is None:
        return ""

    if isinstance(value, float):
        return f"{value:.{digits}f}"

    return str(value)


def find_value(text, patterns, converter=None):
    """
    Try several regex patterns and return the first match.
    """

    for pattern in patterns:
        match = re.search(
            pattern,
            text,
            re.IGNORECASE | re.MULTILINE
        )

        if match:
            value = match.group(1).strip()

            if converter:
                return converter(value)

            return value

    return None


# ============================================================
# PROJECT / BUG DETECTION
# ============================================================

def detect_project_bug(log_path):
    """
    Detect project and bug number from the path.

    Example paths supported:

        Chart/Chart-1b/...
        Chart/Chart-1f/...
        Cli/Cli-10b/...
        JacksonCore/JacksonCore-5b/...
        Math/Math-12b/...
    """

    parts = log_path.parts

    project = ""

    # --------------------------------------------------------
    # Find project name
    # --------------------------------------------------------

    for part in parts:
        if part in PROJECT_NAMES:
            project = part
            break

    # --------------------------------------------------------
    # Find bug
    # --------------------------------------------------------

    bug = ""

    if project:
        bug_pattern = re.compile(
            rf"^{re.escape(project)}-(\d+)(?:b|f)$",
            re.IGNORECASE
        )

        for part in parts:
            match = bug_pattern.match(part)

            if match:
                bug_number = match.group(1)
                bug = f"{bug_number}b"
                break

    # --------------------------------------------------------
    # Fallback:
    # Search anywhere in the path
    # --------------------------------------------------------

    if not bug:
        for part in parts:
            match = re.search(
                r"-(\d+)(?:b|f)$",
                part,
                re.IGNORECASE
            )

            if match:
                bug = f"{match.group(1)}b"
                break

    return project, bug


# ============================================================
# PARSE TERMINAL.LOG
# ============================================================

def parse_terminal_log(log_path):

    try:
        text = log_path.read_text(
            encoding="utf-8",
            errors="ignore"
        )
    except Exception as e:
        print(f"[WARN] อ่านไม่ได้: {log_path}")
        print(f"       {e}")
        return None

    project, bug = detect_project_bug(log_path)

    # --------------------------------------------------------
    # Basic information
    # --------------------------------------------------------

    algorithm = find_value(
        text,
        [
            r"Algorithm\s*[:=]\s*(.+)",
            r"algorithm\s*[:=]\s*(.+)",
        ]
    )

    if not algorithm:
        algorithm = "GWO"

    seed = find_value(
        text,
        [
            r"Seed\s*[:=]\s*(\d+)",
            r"seed\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    population = find_value(
        text,
        [
            r"Population\s*[:=]\s*(\d+)",
            r"population\s*[:=]\s*(\d+)",
            r"pop(?:ulation)?\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    iterations = find_value(
        text,
        [
            r"GWO[_ ]?Iterations?\s*[:=]\s*(\d+)",
            r"Iterations?\s*[:=]\s*(\d+)",
            r"iterations?\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    gwo_a = find_value(
        text,
        [
            r"GWO[_ ]?a\s*[:=]\s*([0-9.eE+-]+)",
            r"\ba\s*[:=]\s*([0-9.eE+-]+)",
        ],
        to_float
    )

    # --------------------------------------------------------
    # Time / generation / tests
    # --------------------------------------------------------

    time_s = find_value(
        text,
        [
            r"Time\s*[:=]\s*([0-9.]+)\s*s",
            r"Time[_ ]?s\s*[:=]\s*([0-9.]+)",
            r"Elapsed\s*[:=]\s*([0-9.]+)\s*s",
            r"Elapsed[_ ]?time\s*[:=]\s*([0-9.]+)",
            r"Runtime\s*[:=]\s*([0-9.]+)\s*s",
        ],
        to_float
    )

    generation = find_value(
        text,
        [
            r"Generation\s*[:=]\s*(\d+)",
            r"Generations?\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    tests = find_value(
        text,
        [
            r"Tests?\s*[:=]\s*(\d+)",
            r"Test\s*Cases?\s*[:=]\s*(\d+)",
            r"TestSuite\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    statements = find_value(
        text,
        [
            r"Statements?\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    fitness = find_value(
        text,
        [
            r"Fitness\s*[:=]\s*([0-9.eE+-]+)",
        ],
        to_float
    )

    # --------------------------------------------------------
    # Coverage
    # --------------------------------------------------------

    line_coverage = find_value(
        text,
        [
            r"Line[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    branch_coverage = find_value(
        text,
        [
            r"Branch[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    exception_coverage = find_value(
        text,
        [
            r"Exception[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    mutation_coverage = find_value(
        text,
        [
            r"Mutation[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    output_coverage = find_value(
        text,
        [
            r"Output[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    method_coverage = find_value(
        text,
        [
            r"Method[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    cbranch_coverage = find_value(
        text,
        [
            r"CBranch[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
            r"Condition[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    overall_coverage = find_value(
        text,
        [
            r"Overall[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
            r"Total[_ ]?Coverage\s*[:=]\s*([0-9.]+)%?",
        ],
        to_float
    )

    # --------------------------------------------------------
    # Goals
    # --------------------------------------------------------

    line_goals = find_value(
        text,
        [
            r"Line[_ ]?Goals\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    line_covered_goals = find_value(
        text,
        [
            r"Line[_ ]?Covered[_ ]?Goals\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    branch_goals = find_value(
        text,
        [
            r"Branch[_ ]?Goals\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    branch_covered_goals = find_value(
        text,
        [
            r"Branch[_ ]?Covered[_ ]?Goals\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    mutation_goals = find_value(
        text,
        [
            r"Mutation[_ ]?Goals\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    mutation_covered_goals = find_value(
        text,
        [
            r"Mutation[_ ]?Covered[_ ]?Goals\s*[:=]\s*(\d+)",
        ],
        to_int
    )

    # --------------------------------------------------------
    # Status
    # --------------------------------------------------------

    compile_ok = ""

    if re.search(
        r"compile.*(?:success|ok|passed|successful)",
        text,
        re.IGNORECASE
    ):
        compile_ok = "YES"
    elif re.search(
        r"compile.*(?:fail|error)",
        text,
        re.IGNORECASE
    ):
        compile_ok = "NO"

    generation_ok = ""

    if re.search(
        r"(?:generation|generate).*(?:success|ok|passed|successful)",
        text,
        re.IGNORECASE
    ):
        generation_ok = "YES"
    elif re.search(
        r"(?:generation|generate).*(?:fail|error)",
        text,
        re.IGNORECASE
    ):
        generation_ok = "NO"

    # --------------------------------------------------------
    # Return
    # --------------------------------------------------------

    return {
        "Project": project,
        "Bug": bug,
        "Algorithm": algorithm,
        "Seed": seed,
        "Population": population,
        "GWO_Iterations": iterations,
        "GWO_a": gwo_a,

        "Time_s": time_s,
        "Generation": generation,
        "Tests": tests,
        "Statements": statements,
        "Fitness": fitness,

        "Line_Coverage": line_coverage,
        "Branch_Coverage": branch_coverage,
        "Exception_Coverage": exception_coverage,
        "Mutation_Coverage": mutation_coverage,
        "Output_Coverage": output_coverage,
        "Method_Coverage": method_coverage,
        "CBranch_Coverage": cbranch_coverage,
        "Overall_Coverage": overall_coverage,

        "Line_Goals": line_goals,
        "Line_Covered_Goals": line_covered_goals,
        "Branch_Goals": branch_goals,
        "Branch_Covered_Goals": branch_covered_goals,
        "Mutation_Goals": mutation_goals,
        "Mutation_Covered_Goals": mutation_covered_goals,

        "Compile_OK": compile_ok,
        "Generation_OK": generation_ok,

        "Log_File": str(log_path.relative_to(RESULT_ROOT)),
    }


# ============================================================
# COLLECT ALL LOGS
# ============================================================

def collect_results():

    logs = list(
        RESULT_ROOT.glob("**/terminal.log")
    )

    # Do not parse generated summary files
    logs = [
        log for log in logs
        if "GWO_summary" not in log.parts
    ]

    print(f"พบ terminal.log ทั้งหมด: {len(logs)}")

    rows = []

    for index, log_path in enumerate(
        sorted(logs),
        start=1
    ):

        row = parse_terminal_log(log_path)

        if row:
            rows.append(row)

        if index % 100 == 0:
            print(
                f"  parsed {index}/{len(logs)}"
            )

    return rows


# ============================================================
# CSV WRITER
# ============================================================

def write_csv(path, rows, fieldnames):

    with path.open(
        "w",
        newline="",
        encoding="utf-8-sig"
    ) as f:

        writer = csv.DictWriter(
            f,
            fieldnames=fieldnames
        )

        writer.writeheader()

        for row in rows:
            writer.writerow({
                field: row.get(field, "")
                for field in fieldnames
            })


# ============================================================
# RAW
# ============================================================

RAW_FIELDS = [
    "Project",
    "Bug",
    "Algorithm",
    "Seed",
    "Population",
    "GWO_Iterations",
    "GWO_a",

    "Time_s",
    "Generation",
    "Tests",
    "Statements",
    "Fitness",

    "Line_Coverage",
    "Branch_Coverage",
    "Exception_Coverage",
    "Mutation_Coverage",
    "Output_Coverage",
    "Method_Coverage",
    "CBranch_Coverage",
    "Overall_Coverage",

    "Line_Goals",
    "Line_Covered_Goals",
    "Branch_Goals",
    "Branch_Covered_Goals",
    "Mutation_Goals",
    "Mutation_Covered_Goals",

    "Compile_OK",
    "Generation_OK",

    "Log_File",
]


def make_raw(rows):

    output = OUTPUT_DIR / "GWO_raw.csv"

    write_csv(
        output,
        rows,
        RAW_FIELDS
    )

    return output


# ============================================================
# BUG SUMMARY
# ============================================================

def make_bug_summary(rows):

    groups = {}

    for row in rows:

        key = (
            row["Project"],
            row["Bug"]
        )

        groups.setdefault(
            key,
            []
        ).append(row)

    output_rows = []

    for (project, bug), group in sorted(groups.items()):

        output_rows.append({
            "Project": project,
            "Bug": bug,
            "Runs": len(group),

            "Avg_Time_s": avg(
                [r["Time_s"] for r in group]
            ),

            "Avg_Tests": avg(
                [r["Tests"] for r in group]
            ),

            "Avg_Statements": avg(
                [r["Statements"] for r in group]
            ),

            "Avg_Fitness": avg(
                [r["Fitness"] for r in group]
            ),

            "Avg_Line_Coverage": avg(
                [r["Line_Coverage"] for r in group]
            ),

            "Avg_Branch_Coverage": avg(
                [r["Branch_Coverage"] for r in group]
            ),

            "Avg_Exception_Coverage": avg(
                [r["Exception_Coverage"] for r in group]
            ),

            "Avg_Mutation_Coverage": avg(
                [r["Mutation_Coverage"] for r in group]
            ),

            "Avg_Output_Coverage": avg(
                [r["Output_Coverage"] for r in group]
            ),

            "Avg_Method_Coverage": avg(
                [r["Method_Coverage"] for r in group]
            ),

            "Avg_CBranch_Coverage": avg(
                [r["CBranch_Coverage"] for r in group]
            ),

            "Avg_Overall_Coverage": avg(
                [r["Overall_Coverage"] for r in group]
            ),

            "Avg_Generation": avg(
                [r["Generation"] for r in group]
            ),
        })

    fields = [
        "Project",
        "Bug",
        "Runs",

        "Avg_Time_s",
        "Avg_Tests",
        "Avg_Statements",
        "Avg_Fitness",

        "Avg_Line_Coverage",
        "Avg_Branch_Coverage",
        "Avg_Exception_Coverage",
        "Avg_Mutation_Coverage",
        "Avg_Output_Coverage",
        "Avg_Method_Coverage",
        "Avg_CBranch_Coverage",
        "Avg_Overall_Coverage",

        "Avg_Generation",
    ]

    output = OUTPUT_DIR / "GWO_summary_bug.csv"

    write_csv(
        output,
        output_rows,
        fields
    )

    return output


# ============================================================
# PROJECT SUMMARY
# ============================================================

def make_project_summary(rows):

    groups = {}

    for row in rows:

        project = row["Project"]

        if not project:
            continue

        groups.setdefault(
            project,
            []
        ).append(row)

    output_rows = []

    for project, group in sorted(groups.items()):

        bugs = sorted({
            r["Bug"]
            for r in group
            if r["Bug"]
        })

        output_rows.append({
            "Project": project,
            "Bugs": len(bugs),
            "Runs": len(group),

            "Avg_Time_s": avg(
                [r["Time_s"] for r in group]
            ),

            "Avg_Tests": avg(
                [r["Tests"] for r in group]
            ),

            "Avg_Statements": avg(
                [r["Statements"] for r in group]
            ),

            "Avg_Fitness": avg(
                [r["Fitness"] for r in group]
            ),

            "Avg_Line_Coverage": avg(
                [r["Line_Coverage"] for r in group]
            ),

            "Avg_Branch_Coverage": avg(
                [r["Branch_Coverage"] for r in group]
            ),

            "Avg_Exception_Coverage": avg(
                [r["Exception_Coverage"] for r in group]
            ),

            "Avg_Mutation_Coverage": avg(
                [r["Mutation_Coverage"] for r in group]
            ),

            "Avg_Overall_Coverage": avg(
                [r["Overall_Coverage"] for r in group]
            ),

            "Avg_Generation": avg(
                [r["Generation"] for r in group]
            ),
        })

    fields = [
        "Project",
        "Bugs",
        "Runs",

        "Avg_Time_s",
        "Avg_Tests",
        "Avg_Statements",
        "Avg_Fitness",

        "Avg_Line_Coverage",
        "Avg_Branch_Coverage",
        "Avg_Exception_Coverage",
        "Avg_Mutation_Coverage",
        "Avg_Overall_Coverage",

        "Avg_Generation",
    ]

    output = OUTPUT_DIR / "GWO_summary_project.csv"

    write_csv(
        output,
        output_rows,
        fields
    )

    return output


# ============================================================
# ALGORITHM SUMMARY
# ============================================================

def make_algorithm_summary(rows):

    groups = {}

    for row in rows:

        algorithm = row["Algorithm"]

        groups.setdefault(
            algorithm,
            []
        ).append(row)

    output_rows = []

    for algorithm, group in sorted(groups.items()):

        projects = sorted({
            r["Project"]
            for r in group
            if r["Project"]
        })

        bugs = sorted({
            f'{r["Project"]}-{r["Bug"]}'
            for r in group
            if r["Project"] and r["Bug"]
        })

        output_rows.append({
            "Algorithm": algorithm,
            "Projects": len(projects),
            "Bugs": len(bugs),
            "Runs": len(group),

            "Avg_Time_s": avg(
                [r["Time_s"] for r in group]
            ),

            "Avg_Tests": avg(
                [r["Tests"] for r in group]
            ),

            "Avg_Statements": avg(
                [r["Statements"] for r in group]
            ),

            "Avg_Fitness": avg(
                [r["Fitness"] for r in group]
            ),

            "Avg_Line_Coverage": avg(
                [r["Line_Coverage"] for r in group]
            ),

            "Avg_Branch_Coverage": avg(
                [r["Branch_Coverage"] for r in group]
            ),

            "Avg_Exception_Coverage": avg(
                [r["Exception_Coverage"] for r in group]
            ),

            "Avg_Mutation_Coverage": avg(
                [r["Mutation_Coverage"] for r in group]
            ),

            "Avg_Overall_Coverage": avg(
                [r["Overall_Coverage"] for r in group]
            ),
        })

    fields = [
        "Algorithm",
        "Projects",
        "Bugs",
        "Runs",

        "Avg_Time_s",
        "Avg_Tests",
        "Avg_Statements",
        "Avg_Fitness",

        "Avg_Line_Coverage",
        "Avg_Branch_Coverage",
        "Avg_Exception_Coverage",
        "Avg_Mutation_Coverage",
        "Avg_Overall_Coverage",
    ]

    output = OUTPUT_DIR / "GWO_summary_algorithm.csv"

    write_csv(
        output,
        output_rows,
        fields
    )

    return output


# ============================================================
# MAIN
# ============================================================

def main():

    print("=" * 70)
    print("GWO RESULT SUMMARY")
    print("=" * 70)

    print(f"RESULT_ROOT : {RESULT_ROOT}")
    print(f"OUTPUT_DIR  : {OUTPUT_DIR}")
    print()

    if not RESULT_ROOT.exists():
        print("[ERROR] ไม่พบ Result_Round1")
        return

    OUTPUT_DIR.mkdir(
        parents=True,
        exist_ok=True
    )

    rows = collect_results()

    print()
    print(f"อ่านผลสำเร็จ: {len(rows)} logs")
    print()

    if not rows:
        print("[ERROR] ไม่พบข้อมูล")
        return

    # --------------------------------------------------------
    # Show project count
    # --------------------------------------------------------

    project_counts = {}

    for row in rows:

        project = row["Project"]

        if project:
            project_counts[project] = (
                project_counts.get(project, 0) + 1
            )

    print("จำนวน log แยกตาม Project")
    print("-" * 40)

    for project in sorted(project_counts):
        print(
            f"{project:<20} "
            f"{project_counts[project]}"
        )

    print()

    # --------------------------------------------------------
    # Create CSVs
    # --------------------------------------------------------

    raw_file = make_raw(rows)
    bug_file = make_bug_summary(rows)
    project_file = make_project_summary(rows)
    algorithm_file = make_algorithm_summary(rows)

    print("=" * 70)
    print("DONE")
    print("=" * 70)

    print("สร้างไฟล์:")

    print(f"  {raw_file}")
    print(f"  {bug_file}")
    print(f"  {project_file}")
    print(f"  {algorithm_file}")

    print("=" * 70)


if __name__ == "__main__":
    main()
