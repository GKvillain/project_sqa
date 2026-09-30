#!/usr/bin/env python3

import csv
import re
from pathlib import Path
from statistics import mean

# ============================================================
# CONFIG
# ============================================================

RESULT_ROOT = Path(__file__).resolve().parent / "Result_Round1"
OUTPUT_DIR = RESULT_ROOT / "GWO_summary"

OUTPUT_DIR.mkdir(parents=True, exist_ok=True)


# ============================================================
# HELPERS
# ============================================================

def to_float(value):
    if value is None:
        return None

    value = str(value).strip()
    if not value:
        return None

    value = value.replace("%", "").replace(",", "")

    try:
        return float(value)
    except ValueError:
        return None


def to_int(value):
    if value is None:
        return None

    value = str(value).strip().replace(",", "")

    try:
        return int(float(value))
    except ValueError:
        return None


def avg(values):
    values = [v for v in values if v is not None]

    if not values:
        return None

    return sum(values) / len(values)


def fmt(value, digits=2):
    if value is None:
        return ""

    return round(value, digits)


def find_value(text, patterns, cast=to_float):
    """
    ค้นค่าจากหลายรูปแบบของ log
    """

    for pattern in patterns:
        match = re.search(pattern, text, re.IGNORECASE | re.MULTILINE)

        if match:
            try:
                return cast(match.group(1))
            except Exception:
                pass

    return None


# ============================================================
# PARSE TERMINAL LOG
# ============================================================

def parse_terminal_log(log_path):

    text = log_path.read_text(
        encoding="utf-8",
        errors="ignore"
    )

    # --------------------------------------------------------
    # Basic information
    # --------------------------------------------------------

    project = ""
    bug = ""
    algorithm = "GWO"

    # ตัวอย่าง path:
    # Result_Round1/Chart/Chart-10b/GWO/terminal.log

    parts = log_path.parts

    try:
        gwo_index = parts.index("GWO")

        bug_dir = parts[gwo_index - 1]
        project = parts[gwo_index - 2]

        bug_match = re.search(
            rf"{re.escape(project)}-(\d+)b",
            bug_dir,
            re.IGNORECASE
        )

        if bug_match:
            bug = f"{bug_match.group(1)}b"

    except Exception:
        pass

    # --------------------------------------------------------
    # Search values
    # --------------------------------------------------------

    result = {
        "Project": project,
        "Bug": bug,
        "Algorithm": algorithm,
        "Log_File": str(log_path),

        # GWO
        "Seed": find_value(
            text,
            [
                r"seed\s*[:=]\s*(\d+)",
                r"random\s+seed\s*[:=]\s*(\d+)"
            ],
            to_int
        ),

        "Population": find_value(
            text,
            [
                r"population\s*[:=]\s*(\d+)",
                r"population\s+size\s*[:=]\s*(\d+)"
            ],
            to_int
        ),

        "GWO_Iterations": find_value(
            text,
            [
                r"iterations?\s*[:=]\s*(\d+)",
                r"gwo\s+iterations?\s*[:=]\s*(\d+)"
            ],
            to_int
        ),

        "GWO_a": find_value(
            text,
            [
                r"\ba\s*[:=]\s*([0-9.]+)"
            ],
            to_float
        ),

        # ----------------------------------------------------
        # Time
        # ----------------------------------------------------

        "Time_s": find_value(
            text,
            [
                r"(?:time|elapsed\s*time|duration)\s*[:=]\s*([0-9.]+)\s*(?:s|sec|seconds)?",
                r"([0-9.]+)\s*seconds"
            ],
            to_float
        ),

        # ----------------------------------------------------
        # Generation
        # ----------------------------------------------------

        "Generation": find_value(
            text,
            [
                r"generation\s*[:=]\s*(\d+)",
                r"generations?\s*[:=]\s*(\d+)"
            ],
            to_int
        ),

        # ----------------------------------------------------
        # Tests
        # ----------------------------------------------------

        "Tests": find_value(
            text,
            [
                r"(?:tests?|test\s+cases?)\s*[:=]\s*(\d+)",
                r"test\s+suite\s+size\s*[:=]\s*(\d+)"
            ],
            to_int
        ),

        # ----------------------------------------------------
        # Statements
        # ----------------------------------------------------

        "Statements": find_value(
            text,
            [
                r"statements?\s*[:=]\s*(\d+)"
            ],
            to_int
        ),

        # ----------------------------------------------------
        # Fitness
        # ----------------------------------------------------

        "Fitness": find_value(
            text,
            [
                r"fitness\s*[:=]\s*([0-9.]+)"
            ],
            to_float
        ),

        # ----------------------------------------------------
        # Coverage
        # ----------------------------------------------------

        "Line_Coverage": find_value(
            text,
            [
                r"line\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"line\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "Branch_Coverage": find_value(
            text,
            [
                r"branch\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"branch\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "Exception_Coverage": find_value(
            text,
            [
                r"exception\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"exception\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "Mutation_Coverage": find_value(
            text,
            [
                r"mutation\s+(?:score|coverage)\s*[:=]\s*([0-9.]+)%?",
                r"mutation\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "Output_Coverage": find_value(
            text,
            [
                r"output\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"output\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "Method_Coverage": find_value(
            text,
            [
                r"method\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"method\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "CBranch_Coverage": find_value(
            text,
            [
                r"cbranch\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"cbranch\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        "Overall_Coverage": find_value(
            text,
            [
                r"overall\s+coverage\s*[:=]\s*([0-9.]+)%?",
                r"overall\s*[:=]\s*([0-9.]+)%"
            ]
        ),

        # ----------------------------------------------------
        # Goals
        # ----------------------------------------------------

        "Line_Goals": find_value(
            text,
            [
                r"(\d+)\s+line\s+goals"
            ],
            to_int
        ),

        "Line_Covered_Goals": find_value(
            text,
            [
                r"line\s+goals.*?(\d+)\s+(?:covered|cover)"
            ],
            to_int
        ),

        "Branch_Goals": find_value(
            text,
            [
                r"(\d+)\s+branch\s+goals"
            ],
            to_int
        ),

        "Branch_Covered_Goals": find_value(
            text,
            [
                r"branch\s+goals.*?(\d+)\s+(?:covered|cover)"
            ],
            to_int
        ),

        "Mutation_Goals": find_value(
            text,
            [
                r"(\d+)\s+(?:weak-)?mutation\s+goals"
            ],
            to_int
        ),

        "Mutation_Covered_Goals": find_value(
            text,
            [
                r"(?:mutation\s+goals).*?(\d+)\s+(?:covered|cover)"
            ],
            to_int
        ),

        # ----------------------------------------------------
        # Status
        # ----------------------------------------------------

        "Compile_OK": (
            "true"
            if re.search(
                r"(compile|compilation).*(success|successful|ok|passed)",
                text,
                re.IGNORECASE
            )
            else ""
        ),

        "Generation_OK": (
            "true"
            if re.search(
                r"(generation|generated).*(success|successful|ok|completed)",
                text,
                re.IGNORECASE
            )
            else ""
        ),
    }

    return result


# ============================================================
# FIND ALL LOGS
# ============================================================

def collect_results():

    results = []

    if not RESULT_ROOT.exists():
        print(f"[ERROR] ไม่พบโฟลเดอร์:")
        print(f"        {RESULT_ROOT}")
        return results

    logs = list(
        RESULT_ROOT.glob("**/GWO/terminal.log")
    )

    print(f"[INFO] พบ terminal.log จำนวน {len(logs)} ไฟล์")

    for log in sorted(logs):

        print(f"[READ] {log}")

        try:
            data = parse_terminal_log(log)
            results.append(data)

        except Exception as e:
            print(f"[ERROR] {log}")
            print(f"        {e}")

    return results


# ============================================================
# WRITE CSV
# ============================================================

def write_csv(filename, rows, columns):

    output = OUTPUT_DIR / filename

    with output.open(
        "w",
        newline="",
        encoding="utf-8-sig"
    ) as f:

        writer = csv.DictWriter(
            f,
            fieldnames=columns,
            extrasaction="ignore"
        )

        writer.writeheader()

        for row in rows:
            writer.writerow(row)

    print(f"[WRITE] {output}")


# ============================================================
# RAW CSV
# ============================================================

def make_raw(results):

    columns = [
        "Project",
        "Bug",
        "Algorithm",
        "Log_File",

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
        "Generation_OK"
    ]

    write_csv(
        "GWO_raw.csv",
        results,
        columns
    )


# ============================================================
# BUG SUMMARY
# ============================================================

def make_bug_summary(results):

    columns = [
        "Project",
        "Bug",

        "Time_s",
        "Generation",
        "Tests",

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
        "Mutation_Covered_Goals"
    ]

    rows = []

    for r in results:

        row = {
            "Project": r["Project"],
            "Bug": r["Bug"],

            "Time_s": r["Time_s"],
            "Generation": r["Generation"],
            "Tests": r["Tests"],

            "Line_Coverage": r["Line_Coverage"],
            "Branch_Coverage": r["Branch_Coverage"],
            "Exception_Coverage": r["Exception_Coverage"],
            "Mutation_Coverage": r["Mutation_Coverage"],
            "Output_Coverage": r["Output_Coverage"],
            "Method_Coverage": r["Method_Coverage"],
            "CBranch_Coverage": r["CBranch_Coverage"],
            "Overall_Coverage": r["Overall_Coverage"],

            "Line_Goals": r["Line_Goals"],
            "Line_Covered_Goals": r["Line_Covered_Goals"],

            "Branch_Goals": r["Branch_Goals"],
            "Branch_Covered_Goals": r["Branch_Covered_Goals"],

            "Mutation_Goals": r["Mutation_Goals"],
            "Mutation_Covered_Goals": r["Mutation_Covered_Goals"],
        }

        rows.append(row)

    rows.sort(
        key=lambda x: (
            x["Project"],
            x["Bug"]
        )
    )

    write_csv(
        "GWO_summary_bug.csv",
        rows,
        columns
    )

    return rows


# ============================================================
# PROJECT SUMMARY
# ============================================================

def make_project_summary(results):

    projects = {}

    for r in results:

        project = r["Project"]

        if project not in projects:
            projects[project] = []

        projects[project].append(r)

    rows = []

    for project, items in sorted(projects.items()):

        row = {
            "Project": project,
            "Bugs": len(items),

            "Avg_Time_s": fmt(
                avg([x["Time_s"] for x in items])
            ),

            "Avg_Generation": fmt(
                avg([x["Generation"] for x in items])
            ),

            "Avg_Tests": fmt(
                avg([x["Tests"] for x in items])
            ),

            "Avg_Line_Coverage": fmt(
                avg([x["Line_Coverage"] for x in items])
            ),

            "Avg_Branch_Coverage": fmt(
                avg([x["Branch_Coverage"] for x in items])
            ),

            "Avg_Exception_Coverage": fmt(
                avg([x["Exception_Coverage"] for x in items])
            ),

            "Avg_Mutation_Coverage": fmt(
                avg([x["Mutation_Coverage"] for x in items])
            ),

            "Avg_Output_Coverage": fmt(
                avg([x["Output_Coverage"] for x in items])
            ),

            "Avg_Method_Coverage": fmt(
                avg([x["Method_Coverage"] for x in items])
            ),

            "Avg_CBranch_Coverage": fmt(
                avg([x["CBranch_Coverage"] for x in items])
            ),

            "Avg_Overall_Coverage": fmt(
                avg([x["Overall_Coverage"] for x in items])
            ),
        }

        rows.append(row)

    columns = [
        "Project",
        "Bugs",

        "Avg_Time_s",
        "Avg_Generation",
        "Avg_Tests",

        "Avg_Line_Coverage",
        "Avg_Branch_Coverage",
        "Avg_Exception_Coverage",
        "Avg_Mutation_Coverage",
        "Avg_Output_Coverage",
        "Avg_Method_Coverage",
        "Avg_CBranch_Coverage",
        "Avg_Overall_Coverage"
    ]

    write_csv(
        "GWO_summary_project.csv",
        rows,
        columns
    )

    return rows


# ============================================================
# ALGORITHM SUMMARY
# ============================================================

def make_algorithm_summary(results):

    algorithms = {}

    for r in results:

        algorithm = r["Algorithm"]

        if algorithm not in algorithms:
            algorithms[algorithm] = []

        algorithms[algorithm].append(r)

    rows = []

    for algorithm, items in sorted(algorithms.items()):

        rows.append({
            "Algorithm": algorithm,
            "Bugs": len(items),

            "Avg_Time_s": fmt(
                avg([x["Time_s"] for x in items])
            ),

            "Avg_Generation": fmt(
                avg([x["Generation"] for x in items])
            ),

            "Avg_Tests": fmt(
                avg([x["Tests"] for x in items])
            ),

            "Avg_Line_Coverage": fmt(
                avg([x["Line_Coverage"] for x in items])
            ),

            "Avg_Branch_Coverage": fmt(
                avg([x["Branch_Coverage"] for x in items])
            ),

            "Avg_Mutation_Coverage": fmt(
                avg([x["Mutation_Coverage"] for x in items])
            ),

            "Avg_Overall_Coverage": fmt(
                avg([x["Overall_Coverage"] for x in items])
            ),
        })

    columns = [
        "Algorithm",
        "Bugs",
        "Avg_Time_s",
        "Avg_Generation",
        "Avg_Tests",
        "Avg_Line_Coverage",
        "Avg_Branch_Coverage",
        "Avg_Mutation_Coverage",
        "Avg_Overall_Coverage"
    ]

    write_csv(
        "GWO_summary_algorithm.csv",
        rows,
        columns
    )


# ============================================================
# MAIN
# ============================================================

def main():

    print("=" * 70)
    print("GWO RESULT SUMMARY")
    print("=" * 70)

    print(f"[INFO] Result root : {RESULT_ROOT}")
    print(f"[INFO] Output dir  : {OUTPUT_DIR}")
    print()

    results = collect_results()

    if not results:
        print()
        print("[ERROR] ไม่พบข้อมูล GWO")
        return

    print()
    print(f"[INFO] Parsed results: {len(results)}")
    print()

    # 1. Raw
    make_raw(results)

    # 2. Per Bug
    make_bug_summary(results)

    # 3. Per Project
    make_project_summary(results)

    # 4. Algorithm
    make_algorithm_summary(results)

    print()
    print("=" * 70)
    print("DONE")
    print("=" * 70)

    print()
    print("สร้างไฟล์:")
    print(f"  {OUTPUT_DIR}/GWO_raw.csv")
    print(f"  {OUTPUT_DIR}/GWO_summary_bug.csv")
    print(f"  {OUTPUT_DIR}/GWO_summary_project.csv")
    print(f"  {OUTPUT_DIR}/GWO_summary_algorithm.csv")


if __name__ == "__main__":
    main()
