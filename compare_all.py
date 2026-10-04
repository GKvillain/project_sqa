"""
สรุปเทียบผลการทดลองทั้ง 4 วิธี (GWO, Zest, Gemini-3.7-flash, DeepSeek-v4-flash)
รันจาก root ของ repo: project_sqa/
    python3 compare_all.py
ต้องมี pandas: pip install pandas --break-system-packages  (ถ้ายังไม่มี)
"""
import pandas as pd
import glob
import os
import re

ROOT = os.path.dirname(os.path.abspath(__file__))
# ถ้าสคริปต์นี้ไม่ได้อยู่ใน root ของ repo ให้แก้ตรงนี้เป็น path จริง:
# ROOT = "/mnt/d/University/Year_3_1/SQA/Project/project_sqa"

rows = []
bad_files = []  # เก็บ (file, n_bad_lines) ที่มีแถวเพี้ยนถูก skip ไป


def safe_read_csv(path):
    """อ่าน CSV แบบทนทาน: ถ้ามีแถวที่ column ไม่ตรง schema (bug เดิมจาก
    make_failed_result เขียน key เกิน) ให้ skip แถวนั้นแล้วแจ้งเตือน แทนที่จะ crash"""
    try:
        return pd.read_csv(path)
    except pd.errors.ParserError:
        before = sum(1 for _ in open(path, encoding="utf-8", errors="replace")) - 1
        df = pd.read_csv(path, on_bad_lines="skip", engine="python")
        skipped = before - len(df)
        bad_files.append((path, skipped))
        return df


# ---------- 1) GWO : ใช้ gwo_project_summary.csv ที่สรุปไว้แล้ว ----------
gwo_path = os.path.join(ROOT, "GreyWolfOptimizer", "GWO_Diagrams", "gwo_project_summary.csv")
if os.path.exists(gwo_path):
    gwo = pd.read_csv(gwo_path)
    for _, r in gwo.iterrows():
        rows.append({
            "Project": r["Project"],
            "Method": "GWO",
            "Bugs": r["Bug_Count"],
            "LineCoverage": r["Line"],
            "BranchCoverage": r["Branch"],
            "TestCount": r["Tests"],
            "TestLength": r["Length"],
            "Time_or_Tokens": r["Time_sec"],
        })
else:
    print("WARN: ไม่พบ", gwo_path)

# ---------- 2) Zest : รวมจากไฟล์ราย Project ใน Zest/results/*.csv ----------
zest_files = glob.glob(os.path.join(ROOT, "Zest", "results", "*.csv"))
if not zest_files:
    print("WARN: ไม่เจอไฟล์ Zest เลย ตรวจ path อีกครั้ง")
for f in zest_files:
    project = os.path.splitext(os.path.basename(f))[0]
    df = safe_read_csv(f)
    rows.append({
        "Project": project,
        "Method": "Zest",
        "Bugs": df["bug_id"].nunique(),
        "LineCoverage": df["line_coverage"].mean(),
        "BranchCoverage": df["branch_coverage"].mean(),
        "TestCount": df["test_cases"].mean(),
        "TestLength": df["test_length"].mean(),
        "Time_or_Tokens": df["time_sec"].mean(),
    })

# ---------- 3) Gemini-3.7-flash : results_gemini-3.7-flash_<Project>.csv ----------
gemini_files = glob.glob(os.path.join(ROOT, "Gemini-3.7-flash", "Coverage", "results_gemini-3.7-flash_*.csv"))
if not gemini_files:
    print("WARN: ไม่เจอไฟล์ Gemini เลย ตรวจ path อีกครั้ง")
for f in gemini_files:
    m = re.search(r"results_gemini-3\.7-flash_(.+)\.csv$", os.path.basename(f))
    project = m.group(1) if m else os.path.basename(f)
    df = safe_read_csv(f)
    df["line_coverage"] = pd.to_numeric(df["line_coverage"], errors="coerce")
    df["condition_coverage"] = pd.to_numeric(df["condition_coverage"], errors="coerce")
    rows.append({
        "Project": project,
        "Method": "Gemini-3.7-flash",
        "Bugs": df["bug_id"].nunique(),
        "LineCoverage": df["line_coverage"].mean(),
        "BranchCoverage": df["condition_coverage"].mean(),  # condition coverage ~ branch
        "TestCount": None,
        "TestLength": None,
        "Time_or_Tokens": df["tokens_used"].mean(),
    })

# ---------- 4) DeepSeek-v4-flash : deepseek-v4-flash/<Project>/results_deepseek-v4-flash_<Project>.csv ----------
deepseek_files = glob.glob(os.path.join(ROOT, "deepseek-v4-flash", "*", "results_deepseek-v4-flash_*.csv"))
if not deepseek_files:
    print("WARN: ไม่เจอไฟล์ DeepSeek เลย ตรวจ path อีกครั้ง")
for f in deepseek_files:
    m = re.search(r"results_deepseek-v4-flash_(.+)\.csv$", os.path.basename(f))
    project = m.group(1) if m else os.path.basename(f)
    df = safe_read_csv(f)
    df["line_coverage"] = pd.to_numeric(df["line_coverage"], errors="coerce")
    df["condition_coverage"] = pd.to_numeric(df["condition_coverage"], errors="coerce")
    rows.append({
        "Project": project,
        "Method": "DeepSeek-v4-flash",
        "Bugs": df["bug_id"].nunique(),
        "LineCoverage": df["line_coverage"].mean(),
        "BranchCoverage": df["condition_coverage"].mean(),
        "TestCount": None,
        "TestLength": None,
        "Time_or_Tokens": df["tokens_used"].mean(),
    })

result = pd.DataFrame(rows)
result = result.round(2)
result = result.sort_values(["Project", "Method"])

out_path = os.path.join(ROOT, "compare_all_methods.csv")
result.to_csv(out_path, index=False, encoding="utf-8-sig")

print(result.to_string(index=False))
print(f"\nบันทึกไฟล์สรุปไว้ที่: {out_path}")

# ---------- สรุปรวมทุก Project ต่อ 1 วิธี ----------
print("\n=== ค่าเฉลี่ยรวมทุก Project ต่อวิธี (ไม่ weight ตามจำนวน bug) ===")
overall = result.groupby("Method")[["LineCoverage", "BranchCoverage", "Time_or_Tokens"]].mean().round(2)
print(overall.to_string())

if bad_files:
    print("\n=== WARNING: ไฟล์ที่มีแถวเพี้ยน (column ไม่ตรง schema) ถูก skip ไปตอนอ่าน ===")
    for path, n in bad_files:
        print(f"  {path}: skip {n} แถว")
    print("สาเหตุคือ bug เดิมใน make_failed_result()/append_result_csv() ที่เคยแก้ไปแล้วบางไฟล์")
    print("ถ้าต้องการ fix แถวเหล่านี้ให้ข้อมูลครบ ให้บอกชื่อไฟล์ แล้วจะให้สคริปต์ซ่อมเหมือนที่ทำกับ Time/JacksonDatabind ก่อนหน้า")
