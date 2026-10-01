#!/usr/bin/env python3

import os
import re
import csv

# ============================================================
# CONFIG
# ============================================================

RESULT_ROOT = "/mnt/d/AllFinalWork/project_sqa/GreyWolfOptimizer/Result_Round1"
OUTPUT_CSV = os.path.join(RESULT_ROOT, "GWO_all_results.csv")


# ============================================================
# HELPERS
# ============================================================

def extract(pattern, text, default=""):
    match = re.search(pattern, text, re.MULTILINE)
    return match.group(1) if match else default


def extract_float(pattern, text, default=""):
    value = extract(pattern, text, default)
    try:
        return float(value)
    except:
        return default


def extract_int(pattern, text, default=""):
    value = extract(pattern, text, default)
    try:
        return int(value)
    except:
        return default


# ============================================================
# PARSE TERMINAL LOG
# ============================================================

def parse_log(log_path):

    with open(log_path, "r", encoding="utf-8", errors="ignore") as f:
        text = f.read()

    result = {}

    # --------------------------------------------------------
    # Path information
    # --------------------------------------------------------

    # .../Chart/Chart-10b/GWO/org_xxx/terminal.log
    parts = os.path.normpath(log_path).split(os.sep)

    result["Project"] = ""
    result["Bug_ID"] = ""
    result["Algorithm"] = "GWO"
    result["Test_Class"] = ""

    for i, part in enumerate(parts):

        # Chart-10b
        if re.match(r"^[A-Za-z]+-\d+[bf]?$", part):
            result["Bug_ID"] = part

            # parent directory = project
            if i > 0:
                result["Project"] = parts[i - 1]

        # GWO
        if part == "GWO":
            result["Algorithm"] = "GWO"

        # class directory
        if part.startswith("org_"):
            result["Test_Class"] = part

    # --------------------------------------------------------
    # EvoSuite information
    # --------------------------------------------------------

    result["EvoSuite_Version"] = extract(
        r"\* EvoSuite\s+(.+)", text
    )

    result["Target_Class"] = extract(
        r"Going to generate test cases for class:\s*(.+)", text
    )

    # --------------------------------------------------------
    # Seed
    # --------------------------------------------------------

    result["Seed"] = extract_int(
        r"Using seed\s+(\d+)", text
    )

    # --------------------------------------------------------
    # Search finished
    # --------------------------------------------------------

    result["Search_Time_Seconds"] = extract_float(
        r"Search finished after\s+([\d.]+)s", text
    )

    result["Generations"] = extract_int(
        r"Search finished after\s+[\d.]+s and\s+(\d+)\s+generations", text
    )

    result["Statements"] = extract_int(
        r"(\d+)\s+statements,\s+best individual", text
    )

    result["Best_Fitness"] = extract_float(
        r"best individual has fitness:\s*([\d.]+)", text
    )

    # --------------------------------------------------------
    # GWO parameters
    # --------------------------------------------------------

    # Initial population
    result["Population_Size"] = extract_int(
        r"GWO EVOLVE iteration=0 population=(\d+)", text
    )

    # Number of iterations
    iterations = re.findall(
        r"GWO iteration=(\d+)", text
    )

    if iterations:
        result["Last_GWO_Iteration"] = max(map(int, iterations))
    else:
        result["Last_GWO_Iteration"] = ""

    # Final a value
    a_values = re.findall(
        r"GWO iteration=\d+ bestFitness=[\d.]+ population=\d+ a=([\d.]+)",
        text
    )

    result["Final_a"] = float(a_values[-1]) if a_values else ""

    # --------------------------------------------------------
    # Generated tests
    # --------------------------------------------------------

    result["Generated_Tests"] = extract_int(
        r"Generated\s+(\d+)\s+tests", text
    )

    result["Total_Test_Length"] = extract_int(
        r"Generated\s+\d+\s+tests with total length\s+(\d+)", text
    )

    # --------------------------------------------------------
    # Overall coverage
    # --------------------------------------------------------

    result["Overall_Coverage"] = extract_float(
        r"Resulting test suite's coverage:\s*([\d.]+)%", text
    )

    result["Mutation_Score"] = extract_float(
        r"Resulting test suite's mutation score:\s*([\d.]+)%", text
    )

    # --------------------------------------------------------
    # Coverage criteria
    # --------------------------------------------------------

    criteria = {
        "LINE": "Line_Coverage",
        "BRANCH": "Branch_Coverage",
        "EXCEPTION": "Exception_Coverage",
        "WEAKMUTATION": "WeakMutation_Coverage",
        "OUTPUT": "Output_Coverage",
        "METHOD": "Method_Coverage",
        "METHODNOEXCEPTION": "MethodNoException_Coverage",
        "CBRANCH": "CBranch_Coverage",
    }

    for criterion, column in criteria.items():

        pattern = (
            rf"Coverage of criterion {criterion}:\s*([\d.]+)%"
        )

        result[column] = extract_float(pattern, text)

    # --------------------------------------------------------
    # Goals
    # --------------------------------------------------------

    goal_info = {
        "LINE": "Line",
        "BRANCH": "Branch",
        "EXCEPTION": "Exception",
        "WEAKMUTATION": "WeakMutation",
        "OUTPUT": "Output",
        "METHOD": "Method",
        "METHODNOEXCEPTION": "MethodNoException",
        "CBRANCH": "CBranch",
    }

    for criterion, prefix in goal_info.items():

        total = extract_int(
            rf"Coverage of criterion {criterion}:.*?"
            rf"Total number of goals:\s*(\d+)",
            text
        )

        covered = extract_int(
            rf"Coverage of criterion {criterion}:.*?"
            rf"Number of covered goals:\s*(\d+)",
            text
        )

        result[f"{prefix}_Total_Goals"] = total
        result[f"{prefix}_Covered_Goals"] = covered

    # --------------------------------------------------------
    # Test generation status
    # --------------------------------------------------------

    result["Compilation_Status"] = (
        "SUCCESS"
        if "Compiling and checking tests" in text
        else ""
    )

    result["Generation_Status"] = (
        "SUCCESS"
        if "Done!" in text
        else "FAILED"
    )

    return result


# ============================================================
# FIND ALL terminal.log
# ============================================================

def find_logs(root):

    logs = []

    for dirpath, dirnames, filenames in os.walk(root):

        if "terminal.log" in filenames:

            logs.append(
                os.path.join(dirpath, "terminal.log")
            )

    return logs


# ============================================================
# MAIN
# ============================================================

def main():

    print("=" * 70)
    print("GWO RESULT → CSV")
    print("=" * 70)

    logs = find_logs(RESULT_ROOT)

    print(f"Found {len(logs)} terminal.log files")

    results = []

    for i, log in enumerate(logs, 1):

        print(
            f"[{i}/{len(logs)}] "
            f"{log}"
        )

        try:
            data = parse_log(log)
            data["Log_File"] = log

            results.append(data)

        except Exception as e:

            print(f"  ERROR: {e}")

    if not results:

        print("No results found.")
        return

    # --------------------------------------------------------
    # Keep columns in a useful order
    # --------------------------------------------------------

    columns = [
        "Project",
        "Bug_ID",
        "Algorithm",
        "Test_Class",
        "Target_Class",

        "EvoSuite_Version",
        "Seed",

        "Population_Size",
        "Last_GWO_Iteration",
        "Final_a",

        "Search_Time_Seconds",
        "Generations",
        "Statements",
        "Best_Fitness",

        "Generated_Tests",
        "Total_Test_Length",

        "Overall_Coverage",
        "Mutation_Score",

        "Line_Coverage",
        "Branch_Coverage",
        "Exception_Coverage",
        "WeakMutation_Coverage",
        "Output_Coverage",
        "Method_Coverage",
        "MethodNoException_Coverage",
        "CBranch_Coverage",

        "Line_Total_Goals",
        "Line_Covered_Goals",

        "Branch_Total_Goals",
        "Branch_Covered_Goals",

        "Exception_Total_Goals",
        "Exception_Covered_Goals",

        "WeakMutation_Total_Goals",
        "WeakMutation_Covered_Goals",

        "Output_Total_Goals",
        "Output_Covered_Goals",

        "Method_Total_Goals",
        "Method_Covered_Goals",

        "MethodNoException_Total_Goals",
        "MethodNoException_Covered_Goals",

        "CBranch_Total_Goals",
        "CBranch_Covered_Goals",

        "Compilation_Status",
        "Generation_Status",

        "Log_File",
    ]

    # --------------------------------------------------------
    # Write CSV
    # --------------------------------------------------------

    with open(
        OUTPUT_CSV,
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

        for row in results:
            writer.writerow(row)

    print()
    print("=" * 70)
    print("DONE")
    print("=" * 70)
    print(f"Results : {len(results)}")
    print(f"CSV     : {OUTPUT_CSV}")


if __name__ == "__main__":
    main()
