import csv
from pathlib import Path

COVERAGE_DIR = Path("Coverage")

for file in COVERAGE_DIR.glob("results_gemini-3.7-flash_*.csv"):
    with file.open("r", newline="", encoding="utf-8-sig") as f:
        rows = list(csv.reader(f))

    if not rows:
        continue

    header = rows[0]
    fixed_rows = [header]
    fixed_count = 0

    for row in rows[1:]:
        if len(row) == 19 and row[4] == "failed_generation":
            # Remove the extra status and error-reason columns,
            # then normalize the row to the standard 17-column schema.
            normalized = (
                row[:4]
                + [
                    row[6],       # compile_ok
                    "0",          # line_total
                    "0",          # line_covered
                    "N/A",        # line_coverage
                    "0",          # condition_total
                    "0",          # condition_covered
                    "N/A",        # condition_coverage
                    "0",          # refinement_rounds
                    row[14],      # tokens_used
                    row[15],      # fault_detected
                    row[16],      # output_path
                    "No valid AI test generated: " + row[5],
                    row[18],      # ai_tests_run
                ]
            )

            fixed_rows.append(normalized)
            fixed_count += 1
        else:
            fixed_rows.append(row)

    with file.open("w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerows(fixed_rows)

    print(f"{file.name}: fixed {fixed_count} rows")
