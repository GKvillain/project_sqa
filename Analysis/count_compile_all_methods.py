import os
import re
import csv
import glob
from collections import defaultdict

BASE = "/mnt/d/AllFinalWork/project_sqa"
GWO_BASE = "/home/user/evosuite-results"

PROJECTS = [
    "Chart", "Cli", "Closure", "Codec", "Collections", "Compress",
    "Csv", "Gson", "JacksonCore", "JacksonDatabind", "JacksonXml",
    "Jsoup", "JxPath", "Lang", "Math", "Mockito", "Time"
]

METHODS = ["GWO", "Zest", "Gemini", "DeepSeek"]


# ============================================================
# COMMON
# ============================================================

def norm_bug(x):
    if x is None:
        return None

    x = str(x).strip()
    x = re.sub(r"[bB]$", "", x)

    try:
        return str(int(float(x)))
    except:
        return x


def key(project, bug):
    return (project, norm_bug(bug))


# ============================================================
# GWO
# ============================================================

def gwo_log_status(path):

    try:
        with open(path, "r", errors="ignore") as f:
            text = f.read()
    except:
        return "UNKNOWN"

    markers = list(
        re.finditer(
            r"\* Compiling and checking tests",
            text
        )
    )

    if not markers:
        return "UNKNOWN"

    # Last compile/check attempt
    pos = markers[-1].start()
    section = text[pos:]

    write_pos = section.find(
        "* Writing tests to file"
    )

    done_pos = section.find(
        "* Done!"
    )

    # Successful EvoSuite compile/check sequence
    if (
        write_pos >= 0
        and done_pos >= 0
        and write_pos < done_pos
    ):
        return "PASS"

    # Only explicit compiler errors count as FAIL.
    # RMI / Broken pipe / ServerError are NOT automatically
    # considered compilation failures.
    before_write = (
        section
        if write_pos < 0
        else section[:write_pos]
    )

    failure_patterns = [
        r"Compilation failed",
        r"failed to compile",
        r"cannot find symbol",
        r"package .* does not exist",
        r"class, interface, enum expected",
        r"incompatible types",
        r"error:\s",
        r"javac.*error",
    ]

    for p in failure_patterns:
        if re.search(
            p,
            before_write,
            re.IGNORECASE
        ):
            return "FAIL"

    return "UNKNOWN"


def get_gwo():

    # Bug -> list of target-class terminal.log statuses
    data = defaultdict(list)

    if not os.path.isdir(GWO_BASE):
        return data

    for project in PROJECTS:

        prefix = project + "-"

        for dirname in os.listdir(GWO_BASE):

            m = re.match(
                rf"^{re.escape(project)}-(\d+)b$",
                dirname
            )

            if not m:
                continue

            bug = m.group(1)

            gwo_dir = os.path.join(
                GWO_BASE,
                dirname,
                "GWO"
            )

            if not os.path.isdir(gwo_dir):
                continue

            logs = []

            for root, dirs, files in os.walk(gwo_dir):

                for f in files:

                    if f == "terminal.log":
                        logs.append(
                            os.path.join(root, f)
                        )

            # IMPORTANT:
            # No terminal.log = this Bug is NOT included.
            # We only count Bugs for which GWO actually has
            # generated-run evidence.
            if not logs:
                continue

            for log in sorted(logs):

                data[
                    key(project, bug)
                ].append(
                    gwo_log_status(log)
                )

    return data


# ============================================================
# AI METHODS
# ============================================================

def get_ai(method):

    data = defaultdict(list)

    if method == "Gemini":

        patterns = [
            os.path.join(
                BASE,
                "Gemini-3.7-flash",
                "Coverage",
                "results_gemini-3.7-flash_*.csv"
            )
        ]

    elif method == "DeepSeek":

        patterns = [
            os.path.join(
                BASE,
                "deepseek-v4-flash",
                "*",
                "results_deepseek-v4-flash_*.csv"
            )
        ]

    else:
        return data

    files = []

    for p in patterns:
        files.extend(glob.glob(p))

    for file in sorted(set(files)):

        try:

            with open(
                file,
                "r",
                encoding="utf-8-sig",
                errors="ignore",
                newline=""
            ) as f:

                reader = csv.DictReader(f)

                for row in reader:

                    project = str(
                        row.get("project", "")
                    ).strip()

                    if project not in PROJECTS:
                        continue

                    bug = norm_bug(
                        row.get("bug_id")
                    )

                    if not bug:
                        continue

                    value = str(
                        row.get("compile_ok", "")
                    ).strip().lower()

                    if value in {
                        "true", "1", "yes",
                        "pass", "passed", "ok"
                    }:
                        status = "PASS"

                    elif value in {
                        "false", "0", "no",
                        "fail", "failed"
                    }:
                        status = "FAIL"

                    else:
                        status = "UNKNOWN"

                    data[
                        key(project, bug)
                    ].append(status)

        except Exception as e:

            print(
                f"[WARNING] {file}: {e}"
            )

    return data


# ============================================================
# ZEST
# ============================================================

def parse_zest_reports():

    report_dir = os.path.join(
        BASE,
        "Zest",
        "reports"
    )

    data = defaultdict(list)

    if not os.path.isdir(report_dir):
        return data

    for report in glob.glob(
        os.path.join(report_dir, "*.md")
    ):

        name = os.path.basename(report)

        m = re.match(
            r"^(.+?)-(\d+)-b\d+-r\d+\.md$",
            name,
            re.IGNORECASE
        )

        if not m:
            continue

        project_raw = m.group(1)
        bug = m.group(2)

        project = next(
            (
                p for p in PROJECTS
                if p.lower() == project_raw.lower()
            ),
            None
        )

        if project is None:
            continue

        try:

            with open(
                report,
                "r",
                errors="ignore"
            ) as f:
                text = f.read()

        except:
            continue

        # Zest report gives actual generated-test path.
        mtest = re.search(
            r"^- Tests:\s*`([^`]+)`",
            text,
            re.MULTILINE
        )

        if not mtest:
            # We know the Bug exists in Zest reports,
            # but no generated test path was reported.
            data[
                key(project, bug)
            ].append("UNKNOWN")
            continue

        test_path = mtest.group(1).strip()

        if not os.path.isfile(test_path):

            data[
                key(project, bug)
            ].append("UNKNOWN")

            continue

        # IMPORTANT:
        # Do NOT use "Bug detection" or "Fault detected"
        # as compile status.
        #
        # Actual Zest compile evidence must be obtained
        # separately from the Zest build/pipeline.
        #
        # For now preserve it as UNKNOWN rather than
        # falsely reporting FAIL.
        data[
            key(project, bug)
        ].append("UNKNOWN")

    return data


# ============================================================
# BUG-LEVEL COLLAPSE
# ============================================================

def collapse(statuses):

    if not statuses:
        return "UNKNOWN"

    # All target classes / generated test rows must compile.
    if all(x == "PASS" for x in statuses):
        return "PASS"

    # Any explicit compilation failure makes Bug FAIL.
    if any(x == "FAIL" for x in statuses):
        return "FAIL"

    return "UNKNOWN"


# ============================================================
# BUILD ALL RESULTS
# ============================================================

method_data = {
    "GWO": get_gwo(),
    "Zest": parse_zest_reports(),
    "Gemini": get_ai("Gemini"),
    "DeepSeek": get_ai("DeepSeek"),
}


detail = []

for method in METHODS:

    for (project, bug), statuses in method_data[method].items():

        detail.append({
            "method": method,
            "project": project,
            "bug_id": bug,
            "status": collapse(statuses),
            "evidence_count": len(statuses),
        })


# ============================================================
# SUMMARY
# ============================================================

print()
print("=" * 120)
print("COMPILE STATUS — BUG LEVEL — ALL METHODS")
print("=" * 120)

summary = []

for project in PROJECTS:

    print()
    print(project)

    for method in METHODS:

        rows = [
            x for x in detail
            if x["project"] == project
            and x["method"] == method
        ]

        if not rows:

            print(
                f"  {method:<9} : N/A"
            )
            continue

        passed = sum(
            x["status"] == "PASS"
            for x in rows
        )

        failed = sum(
            x["status"] == "FAIL"
            for x in rows
        )

        unknown = sum(
            x["status"] == "UNKNOWN"
            for x in rows
        )

        total = len(rows)

        # IMPORTANT:
        # Compile rate is calculated only from Bugs
        # with an actual PASS/FAIL determination.
        determined = passed + failed

        rate = (
            passed / determined * 100
            if determined
            else 0
        )

        print(
            f"  {method:<9} "
            f"PASS={passed:3d}  "
            f"FAIL={failed:3d}  "
            f"UNKNOWN={unknown:3d}  "
            f"TOTAL={total:3d}  "
            f"DETERMINED={determined:3d}  "
            f"RATE={rate:6.2f}%"
        )

        summary.append({
            "project": project,
            "method": method,
            "compile_pass": passed,
            "compile_fail": failed,
            "unknown": unknown,
            "total_bugs": total,
            "determined_bugs": determined,
            "compile_rate": round(rate, 2),
        })


# ============================================================
# TOTAL
# ============================================================

print()
print("=" * 120)
print("TOTAL — BUG LEVEL")
print("=" * 120)

for method in METHODS:

    rows = [
        x for x in detail
        if x["method"] == method
    ]

    if not rows:

        print(
            f"{method:<10}: N/A"
        )
        continue

    passed = sum(
        x["status"] == "PASS"
        for x in rows
    )

    failed = sum(
        x["status"] == "FAIL"
        for x in rows
    )

    unknown = sum(
        x["status"] == "UNKNOWN"
        for x in rows
    )

    total = len(rows)

    determined = passed + failed

    rate = (
        passed / determined * 100
        if determined
        else 0
    )

    print(
        f"{method:<10}: "
        f"PASS={passed:4d}  "
        f"FAIL={failed:4d}  "
        f"UNKNOWN={unknown:4d}  "
        f"TOTAL={total:4d}  "
        f"DETERMINED={determined:4d}  "
        f"RATE={rate:6.2f}%"
    )


# ============================================================
# PRINT BUG IDs FOR AUDIT
# ============================================================

print()
print("=" * 120)
print("BUG-LEVEL AUDIT")
print("=" * 120)

for method in METHODS:

    print()
    print(f"[{method}]")

    for project in PROJECTS:

        rows = [
            x for x in detail
            if x["method"] == method
            and x["project"] == project
        ]

        if not rows:
            continue

        def ids(status):
            return [
                int(x["bug_id"])
                if str(x["bug_id"]).isdigit()
                else x["bug_id"]
                for x in rows
                if x["status"] == status
            ]

        print(
            f"{project:<18} "
            f"PASS={ids('PASS')} "
            f"FAIL={ids('FAIL')} "
            f"UNKNOWN={ids('UNKNOWN')}"
        )


# ============================================================
# SAVE CSV
# ============================================================

analysis_dir = os.path.join(
    BASE,
    "Analysis"
)

os.makedirs(
    analysis_dir,
    exist_ok=True
)

detail_file = os.path.join(
    analysis_dir,
    "compile_status_all_methods_bug_level.csv"
)

summary_file = os.path.join(
    analysis_dir,
    "compile_summary_all_methods_bug_level.csv"
)

with open(
    detail_file,
    "w",
    newline="",
    encoding="utf-8"
) as f:

    writer = csv.DictWriter(
        f,
        fieldnames=[
            "method",
            "project",
            "bug_id",
            "status",
            "evidence_count",
        ]
    )

    writer.writeheader()

    for row in sorted(
        detail,
        key=lambda x: (
            METHODS.index(x["method"]),
            PROJECTS.index(x["project"]),
            int(x["bug_id"])
            if str(x["bug_id"]).isdigit()
            else 999999,
        )
    ):
        writer.writerow(row)


with open(
    summary_file,
    "w",
    newline="",
    encoding="utf-8"
) as f:

    writer = csv.DictWriter(
        f,
        fieldnames=[
            "project",
            "method",
            "compile_pass",
            "compile_fail",
            "unknown",
            "total_bugs",
            "determined_bugs",
            "compile_rate",
        ]
    )

    writer.writeheader()

    writer.writerows(summary)


print()
print("=" * 120)
print("OUTPUT")
print("=" * 120)
print(detail_file)
print(summary_file)
print()
