import shutil
import shlex
from pathlib import Path
"""
Automated AI Test Generation Pipeline for Defect4J
====================================================
รัน AI (Claude / GPT ผ่าน KKU IntelSphere) generate JUnit test suite
ให้ครบทุก bug ของ project ที่กำหนด แล้ววัด coverage + fault detection อัตโนมัติ

วิธีใช้:
    1. ติดตั้ง dependency: pip install openai python-dotenv --break-system-packages
    2. สร้างไฟล์ .env ในโฟลเดอร์เดียวกัน ใส่ KKU_API_KEY=xxxxx
    3. แก้ตัวแปร PROJECT, MODEL_NAME ด้านล่างตามต้องการ
    4. รัน: python3 run_ai_testgen.py
    5. ถ้าโควต้าหมดกลางทาง รันคำสั่งเดิมซ้ำได้เลย จะ resume ต่อจากจุดเดิมอัตโนมัติ

หมายเหตุสำคัญ (แก้ปัญหา coverage / "Fileset of tests to run is empty!"):
    `defects4j test` และ `defects4j coverage` โดย default จะรันจาก
    "developer-written test suite" ของทั้งโปรเจกต์เสมอ (อ้างอิง metadata ภายใน
    ของ Defects4J เอง ไม่ใช่แค่สแกนไฟล์ .java ในโฟลเดอร์ test) การย้าย dev
    tests ออกแล้วใส่ไฟล์ AI แทนที่ทำให้ ant fileset ที่ Defects4J คาดหวังไว้
    ว่างเปล่า -> BUILD FAILED

    วิธีที่ถูกต้องตามที่ Defects4J รองรับคือใช้ flag `-s <archive>` เพื่อรัน
    เฉพาะ "external test suite" ของเราเอง โดยไม่ต้องยุ่งกับ dev tests เลย
    (กลไกเดียวกับที่ใช้ประเมิน EvoSuite/Randoop generated suites)

    รูปแบบชื่อไฟล์ archive ตาม convention ของ Defects4J:
        <PID>-<VID>-<SUITE_SRC>.<SUITE_NUM>.tar.bz2
        เช่น Chart-24b-aitest.1.tar.bz2
    และโครงสร้างภายในไฟล์ tar ต้องเป็น package path ของ .java ตรงๆ
    เช่น org/jfree/chart/renderer/GrayPaintScaleTest.java (ไม่มี wrapper dir)
"""

import subprocess
import os
import re
import csv
import json
import time
import datetime
from openai import OpenAI

# ============================================================
# CONFIGURATION - แก้ตรงนี้ตามต้องการ
# ============================================================
PROJECT = "JacksonXml"                          # ชื่อ project ใน Defect4J
MODEL_NAME = "gemini-3.7-flash"              # หรือ "gpt-mini"
BASE_URL = "https://gen.ai.kku.ac.th/api/v1"

# แยก working directory ตาม Model เพื่อป้องกัน GPT และ Claude ทับกัน
MODEL_SAFE_NAME = re.sub(r"[^A-Za-z0-9_.-]+", "_", MODEL_NAME)
WORK_BASE = os.path.expanduser(
    f"~/work/{PROJECT.lower()}_batch/{MODEL_SAFE_NAME}"
)

# แยกไฟล์ผลลัพธ์ตาม Model
RESULTS_CSV = f"results_{MODEL_SAFE_NAME}_{PROJECT}.csv"
PROGRESS_FILE = f"progress_{MODEL_SAFE_NAME}_{PROJECT}.json"

# Output directory containing ONLY AI-generated JUnit test files.
# Defects4J checkout files remain in WORK_BASE for compile/test/coverage.
OUTPUT_BASE = os.path.abspath(
    os.path.join("results", MODEL_SAFE_NAME, PROJECT)
)

# ใช้เป็น <SUITE_SRC> ในชื่อ archive ต้องไม่มีจุด (.) เพราะจุดถูกใช้แยก SUITE_NUM
SUITE_SRC_NAME = "aitest"

DAILY_TOKEN_LIMIT = 350000
MAX_REFINEMENT_ROUNDS = 1
API_SLEEP_SECONDS = 1
MAX_TOKENS_PER_CALL = 8000
TARGET_LINE_COVERAGE = 70.0
# ============================================================
# SETUP
# ============================================================
api_key = os.environ.get("KKU_API_KEY")
if not api_key:
    # ลองอ่านจากไฟล์ .env ถ้าไม่มีใน environment
    if os.path.exists(".env"):
        with open(".env") as f:
            for line in f:
                if line.startswith("KKU_API_KEY="):
                    api_key = line.strip().split("=", 1)[1]
if not api_key:
    raise RuntimeError("ไม่พบ KKU_API_KEY กรุณาตั้งค่าใน .env หรือ environment variable ก่อน")

client = OpenAI(base_url=BASE_URL, api_key=api_key)
os.makedirs(WORK_BASE, exist_ok=True)
os.makedirs(OUTPUT_BASE, exist_ok=True)


# ============================================================
# HELPER FUNCTIONS
# ============================================================
def run_cmd(cmd, cwd=None, timeout=300):
    """รันคำสั่ง shell แล้วคืนค่า stdout, stderr"""
    try:
        result = subprocess.run(
            cmd, shell=True, cwd=cwd,
            capture_output=True, text=True, timeout=timeout
        )
        return result.stdout, result.stderr
    except subprocess.TimeoutExpired:
        return "", "TIMEOUT"


def get_bug_ids(project):
    """ดึงรายชื่อ bug ID ทั้งหมดของ project"""
    out, err = run_cmd(f"defects4j bids -p {project}")
    if err and "bids" not in out:
        print(f"[WARN] defects4j bids error: {err}")
    return [b.strip() for b in out.strip().split("\n") if b.strip().isdigit()]


def checkout_bug(project, bug_id, work_dir):
    """Checkout buggy version ของ bug นี้"""
    out, err = run_cmd(
        f"defects4j checkout -p {project} -v {bug_id}b -w {work_dir}"
    )
    return "OK" in out or "FAIL" not in out


def get_modified_classes(work_dir):
    """หา class ที่มี defect จาก property classes.modified"""
    out, err = run_cmd("defects4j export -p classes.modified", cwd=work_dir)
    classes = [c.strip() for c in out.strip().split("\n") if c.strip()]
    return classes


def get_src_dir(work_dir):
    """หา source directory ที่ถูกต้อง (บาง project path ต่างกัน)"""
    out, _ = run_cmd("defects4j export -p dir.src.classes", cwd=work_dir)
    return out.strip()


def get_test_dir(work_dir):
    out, _ = run_cmd("defects4j export -p dir.src.tests", cwd=work_dir)
    return out.strip()


def read_source(work_dir, src_dir, class_name):
    """อ่าน source code เต็มไฟล์ของ class"""
    path = class_name.replace(".", "/") + ".java"
    full_path = os.path.join(work_dir, src_dir, path)
    if not os.path.exists(full_path):
        return None
    with open(full_path, encoding="utf-8", errors="ignore") as f:
        return f.read()


def build_prompt(project, bug_id, class_name, source_code):
    """ประกอบ prompt เต็มรูปแบบตาม template ที่ออกแบบไว้"""
    return f"""
คุณคือ Senior Java Test Engineer ที่เชี่ยวชาญด้าน JUnit 4, Unit Testing, White-box Testing และ Code Coverage

=== TASK ===

สร้าง JUnit 4 Test Class สำหรับ Production Class ที่ให้มาเท่านั้น

Production Class ที่ต้องทดสอบ:
{class_name}

Project:
{project}

Defects4J Bug:
{bug_id}b

Source Code:

```java
{source_code}
```

=== OBJECTIVE ===

สร้าง Test Class ที่สามารถนำไปวางใน Defects4J project เดิมและ compile/run ได้ทันที

เป้าหมายหลักเรียงตามลำดับความสำคัญ:

1. ตรวจจับ defect ของ Defects4J ให้ได้
2. ครอบคลุม branch ที่มีความสำคัญต่อพฤติกรรมของ class
3. ได้ line coverage ที่ดี
4. ใช้จำนวน test case เท่าที่จำเป็น
5. ลด test case ที่ซ้ำซ้อนและลดความยาวของ test code

ไม่จำเป็นต้องพยายามเพิ่ม coverage จนถึง 100% หากต้องสร้าง test เพิ่มจำนวนมากโดยไม่ได้เพิ่มความสามารถในการตรวจจับ defect อย่างมีนัยสำคัญ

เป้าหมายโดยประมาณ:

* Line coverage: 60–70% หรือสูงกว่าหากทำได้โดยไม่เพิ่ม test ที่ไม่จำเป็น
* Branch coverage: 50–60% หรือสูงกว่าหากทำได้โดยไม่เพิ่ม test ที่ไม่จำเป็น

=== TEST CASE REQUIREMENTS ===

สร้างประมาณ 10–20 test cases หรือน้อยกว่านั้นหาก source code มีความซับซ้อนไม่มาก

ให้เลือก test cases อย่างมีเหตุผลจาก source code ที่ให้มา

ต้องพิจารณา:

1. Normal cases

   * input ที่ถูกต้อง
   * expected output ที่ถูกต้อง

2. Boundary cases

   * ค่าต่ำสุด
   * ค่าสูงสุด
   * ค่าที่อยู่ก่อน/หลัง boundary
   * off-by-one cases เมื่อมี boundary ที่ชัดเจน

3. Edge / Invalid cases

   * null เมื่อ method รองรับหรือมีโอกาสรับ null
   * empty string
   * zero
   * negative values
   * values outside valid range

   ห้ามสร้าง test สำหรับ input ที่ไม่มีความเกี่ยวข้องกับ source code

4. Branch coverage

   * if / else
   * switch
   * loop
   * conditional expressions

   พยายามครอบคลุมทั้ง true และ false path เมื่อสามารถทำได้อย่างถูกต้อง

5. Exception

   * หาก source code มีการ throw exception หรือมี exception handling
     ให้สร้าง test เพื่อทดสอบ path ดังกล่าวเมื่อเหมาะสม

6. Regression-oriented testing

   * ให้ความสำคัญกับ method และ condition ที่มีโอกาสเกี่ยวข้องกับ defect
   * อย่าสร้าง test จำนวนมากเพียงเพื่อเพิ่ม coverage

=== ASSERTION REQUIREMENTS ===

ทุก test ที่ตรวจสอบ behavior ต้องมี assertion ที่เหมาะสม เช่น:

assertEquals()
assertTrue()
assertFalse()
assertNull()
assertNotNull()

ห้ามใช้ assertNotNull() เพียงอย่างเดียวเพื่ออ้างว่า behavior ถูกต้อง หากสามารถตรวจสอบ expected value ได้

ต้องระบุ expected result อย่างชัดเจนเมื่อสามารถทำได้

=== TEST NAMING ===

ตั้งชื่อ test method ตามรูปแบบ:

test[MethodName]*[Condition]*[ExpectedResult]

ตัวอย่าง:

testCalculate_positiveValue_returnsCorrectResult()

testParse_nullInput_throwsException()

testConvert_zeroValue_returnsZero()

=== JUNIT REQUIREMENTS ===

ใช้ JUnit 4 เท่านั้น

อนุญาต:

import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import static org.junit.Assert.*;

ห้ามใช้ JUnit 5 โดยเด็ดขาด

ห้าม import:

org.junit.jupiter.*

ห้ามใช้:

@Test
assertThrows()

JUnit 5 API ใด ๆ

สำหรับ exception ให้ใช้:

@Test(expected = XxxException.class)

=== SOURCE CODE RESTRICTIONS ===

ต้องอ้างอิงเฉพาะ method, constructor, field และ behavior ที่ปรากฏใน Source Code ที่ให้มา

ห้าม:

* สมมติ method ที่ไม่มีอยู่จริง
* สมมติ field ที่ไม่มีอยู่จริง
* สมมติ constructor ที่ไม่มีอยู่จริง
* สมมติ package ที่ไม่สามารถทราบได้จาก source code
* เรียกใช้ API ที่ไม่มีอยู่ใน source code หรือ dependency ที่เห็นได้ชัดเจน
* แก้ไข Production Code
* สร้าง Production Class ใหม่
* เปลี่ยน implementation ของ Production Class

Test ต้องสามารถ compile ได้โดยใช้ dependency ที่มีอยู่ใน Defects4J project

=== MOCK / STUB ===

ห้ามใช้ mock หรือ stub โดย default

ใช้เฉพาะกรณีที่จำเป็นจริง ๆ เช่น Production Class มี dependency ที่ไม่สามารถสร้างหรือควบคุมได้โดยตรง

หากสามารถทดสอบโดยใช้ real object ได้ ให้ใช้ real object แทน

=== VERY IMPORTANT: OUTPUT MUST BE ONE FILE ===

สร้างเพียง JUnit Test Class เดียวเท่านั้น

ห้ามสร้าง project ใหม่

ห้ามสร้างหลายไฟล์

ห้ามสร้าง:

* pom.xml
* build.gradle
* settings.gradle
* README.md
* configuration files
* source files
* helper classes แยกไฟล์
* directory structure

ห้ามส่ง project structure

ห้ามส่งหลาย test classes

ห้ามสร้าง test class สำหรับ Production Class อื่น

หากจำเป็นต้องมี helper method ให้เขียนเป็น private method ภายใน Test Class เดียวกันเท่านั้น

รูปแบบ:

Production Class:
{class_name}

Generated Test Class:
{class_name.split('.')[-1]}Test.java

ดังนั้น output ต้องเป็นเนื้อหาของ:

{class_name.split('.')[-1]}Test.java

เพียงไฟล์เดียว

=== PACKAGE ===

ใช้ package เดียวกับ Production Class หากสามารถระบุได้อย่างแน่นอนจาก source code

ห้ามเดา package

หากไม่สามารถระบุ package ได้ ให้ไม่ใส่ package declaration แทนการเดา

=== COMMENTS ===

ใส่ comment สั้น ๆ ก่อนแต่ละ test method เพื่อบอกว่ากำลังทดสอบอะไร เช่น:

// Tests positive boundary value
// Tests false branch of condition
// Tests null input
// Tests exception path

ไม่ต้องเขียนคำอธิบายยาว

=== OUTPUT FORMAT — STRICT ===

ตอบเฉพาะ Java source code ของ Test Class เดียวเท่านั้น

ห้ามมีคำอธิบายก่อนหรือหลัง code

ห้ามสร้าง Markdown

ห้ามใช้:

```java
```

ห้ามใส่:

"Here is the test class"

"Here is the generated test"

"Project structure:"

หรือข้อความอื่นใดนอก Java source code

OUTPUT ต้องเริ่มต้นด้วย Java source code และจบด้วย Java source code

ย้ำอีกครั้ง:

ONE PRODUCTION CLASS
→ ONE JUNIT 4 TEST CLASS
→ ONE .JAVA FILE

ห้ามสร้าง Project ใหม่
ห้ามสร้างหลายไฟล์
ห้ามใช้ JUnit 5
ห้ามใช้ org.junit.jupiter
ห้ามแก้ไข Production Code



"""


def build_refine_prompt(uncovered_info, previous_test_code):
    """Prompt สำหรับรอบ refine เพิ่ม coverage"""
    return f"""ผลการวัด coverage ปัจจุบันยังไม่ครบถ้วน ดังนี้:

=== ส่วนที่ยังไม่ถูกครอบคลุม ===
{uncovered_info}

=== Test Suite ปัจจุบัน ===
```java
{previous_test_code}
```

กรุณาเพิ่ม test case ใหม่เฉพาะสำหรับส่วนที่ยังไม่ถูกครอบคลุมนี้เท่านั้น โดย:
1. ห้ามเขียน test ซ้ำกับที่มีอยู่แล้ว
2. ส่งกลับเป็นไฟล์ .java เต็มไฟล์ (รวม test เดิม + test ใหม่ที่เพิ่ม)
3. ตอบเฉพาะโค้ดในบล็อก ```java ... ``` เท่านั้น
"""


def call_ai(prompt, model=MODEL_NAME):
    """เรียก API ผ่าน KKU IntelSphere (OpenAI-compatible)"""

    response = client.chat.completions.create(
        model=model,
        messages=[{"role": "user", "content": prompt}],
        max_tokens=MAX_TOKENS_PER_CALL,
    )

    choice = response.choices[0]

    finish_reason = getattr(choice, "finish_reason", None)
    content = getattr(choice.message, "content", None)

    usage = response.usage.total_tokens if response.usage else 0

    print(f"[DEBUG] finish_reason: {finish_reason}")
    print(f"[DEBUG] content is None: {content is None}")
    print(f"[DEBUG] content length: {len(content) if content else 0}")
    print(f"[DEBUG] tokens used: {usage}")

    # ป้องกันไม่ให้เขียนไฟล์ว่าง
    if not content or not content.strip():
        raise RuntimeError(
            f"AI returned empty content. finish_reason={finish_reason}"
        )

    # ถ้า output ถูกตัด ให้แจ้ง caller ว่าไม่ควรเอาไปเขียนไฟล์
    if finish_reason == "length":
        raise RuntimeError(
            f"AI output was truncated because max_tokens={MAX_TOKENS_PER_CALL}"
        )

    return content, usage


def extract_java_code(ai_response):
    """ดึงเฉพาะโค้ดในไฟล์ ```java ... ``` ออกมา"""
    match = re.search(r"```java\s*\n(.*?)```", ai_response, re.DOTALL)
    if match:
        return match.group(1).strip()
    return ai_response.strip()


def export_generated_test(java_file_path, project, bug_id, class_name):
    """
    Export ONLY the generated JUnit .java file (for archival / manual
    inspection). This is separate from the tar.bz2 archive used to
    actually run coverage via `defects4j ... -s`.
    """
    class_simple_name = class_name.split(".")[-1]

    output_dir = os.path.join(
        OUTPUT_BASE,
        f"{bug_id}b",
        class_simple_name
    )
    os.makedirs(output_dir, exist_ok=True)

    output_path = os.path.join(
        output_dir,
        os.path.basename(java_file_path)
    )

    with open(java_file_path, "r", encoding="utf-8") as f:
        content = f.read()

    if not content.strip():
        raise RuntimeError(
            f"Refusing to export empty test file: {java_file_path}"
        )

    with open(output_path, "w", encoding="utf-8") as f:
        f.write(content)

    return output_path


def build_test_suite_archive(work_dir, project, bug_id, class_name, java_code, suite_num=1):
    """
    สร้าง external test suite archive ตาม naming convention ของ Defects4J:
        <PID>-<VID>-<SUITE_SRC>.<SUITE_NUM>.tar.bz2
    โครงสร้างภายในไฟล์ tar เป็น package path ของ .java ไฟล์ตรงๆ
    เช่น org/jfree/chart/renderer/GrayPaintScaleTest.java (ไม่มี wrapper dir)

    ใช้ archive นี้กับ `defects4j test -s <archive>` และ
    `defects4j coverage -s <archive>` เพื่อรันเฉพาะ AI-generated test
    suite เท่านั้น โดยไม่ต้องแตะ/ย้าย developer tests เลย

    คืนค่า (archive_path, java_file_path)
    """
    staging_dir = Path(work_dir) / ".ai_suite_src"
    if staging_dir.exists():
        shutil.rmtree(staging_dir)
    staging_dir.mkdir(parents=True, exist_ok=True)

    package_path = "/".join(class_name.split(".")[:-1])
    test_class_name = class_name.split(".")[-1] + "Test"
    target_dir = staging_dir / package_path if package_path else staging_dir
    target_dir.mkdir(parents=True, exist_ok=True)

    java_file_path = target_dir / f"{test_class_name}.java"
    java_file_path.write_text(java_code, encoding="utf-8")

    suite_dir = Path(work_dir).parent / "suites"
    suite_dir.mkdir(parents=True, exist_ok=True)
    archive_name = f"{project}-{bug_id}b-{SUITE_SRC_NAME}.{suite_num}.tar.bz2"
    archive_path = suite_dir / archive_name

    if archive_path.exists():
        archive_path.unlink()

    # tar เนื้อหา *ข้างใน* staging_dir (ไม่ใช่ตัว staging_dir เอง)
    # เพื่อให้ path ในไฟล์ tar ขึ้นต้นด้วย package path ตรงๆ
    tar_cmd = f"tar cjf {shlex.quote(str(archive_path))} -C {shlex.quote(str(staging_dir))} ."
    out, err = run_cmd(tar_cmd)
    if not archive_path.exists():
        raise RuntimeError(f"Failed to create test suite archive: {out} {err}")

    return str(archive_path), str(java_file_path)


def list_runnable_tests(staging_dir):
    """List .java ไฟล์ที่อยู่ใน staging directory ของ AI suite (สำหรับบันทึกลง CSV)"""
    root = Path(staging_dir)
    return sorted(str(p.relative_to(root)) for p in root.rglob("*.java")) if root.exists() else []


def parse_coverage(cov_out):
    """
    Parse ผลลัพธ์ของคำสั่ง `defects4j coverage` โดยตรงจาก stdout

    Defects4J ไม่ได้สร้างไฟล์ coverage.xml เสมอไป — ในหลาย environment/เวอร์ชัน
    มันรายงานผลเป็นข้อความทาง stdout เท่านั้น รูปแบบทั่วไปคือ:

        Lines total: 132
        Lines covered: 100
        Conditions total: 45
        Conditions covered: 30

    ฟังก์ชันนี้จึงรับ stdout ของคำสั่ง coverage มาตรงๆ (ไม่ใช่ work_dir)
    แล้ว regex หาตัวเลขออกมาคำนวณเปอร์เซ็นต์เอง

    คืนค่า:
    - line_total
    - line_covered
    - condition_total
    - condition_covered
    - line_coverage
    - condition_coverage
    """

    if not cov_out:
        print("[WARN] Empty coverage output")
        return {
            "line_total": 0,
            "line_covered": 0,
            "condition_total": 0,
            "condition_covered": 0,
            "line_coverage": "N/A",
            "condition_coverage": "N/A",
        }

    def _grab_int(pattern, text):
        m = re.search(pattern, text, re.IGNORECASE)
        return int(m.group(1)) if m else None

    def _grab_float(pattern, text):
        m = re.search(pattern, text, re.IGNORECASE)
        return float(m.group(1)) if m else None

    # รูปแบบหลัก: "Lines total: N" / "Lines covered: N"
    line_total = _grab_int(r"Lines?\s*total:\s*(\d+)", cov_out)
    line_covered = _grab_int(r"Lines?\s*covered:\s*(\d+)", cov_out)
    condition_total = _grab_int(r"Conditions?\s*total:\s*(\d+)", cov_out)
    condition_covered = _grab_int(r"Conditions?\s*covered:\s*(\d+)", cov_out)

    line_total = line_total or 0
    line_covered = line_covered or 0
    condition_total = condition_total or 0
    condition_covered = condition_covered or 0

    # Fallback: บางเวอร์ชันรายงานเป็น % โดยตรง เช่น "Line coverage: 75.5%"
    line_pct_direct = _grab_float(r"Line coverage:\s*([\d.]+)%?", cov_out)
    condition_pct_direct = _grab_float(r"Condition coverage:\s*([\d.]+)%?", cov_out)

    if line_total == 0 and condition_total == 0 and line_pct_direct is None and condition_pct_direct is None:
        print("[WARN] Could not parse coverage output (unrecognized format)")
        return {
            "line_total": 0,
            "line_covered": 0,
            "condition_total": 0,
            "condition_covered": 0,
            "line_coverage": "N/A",
            "condition_coverage": "N/A",
        }

    if line_total > 0:
        line_coverage = line_covered / line_total * 100
    elif line_pct_direct is not None:
        line_coverage = line_pct_direct
    else:
        line_coverage = 0

    if condition_total > 0:
        condition_coverage = condition_covered / condition_total * 100
    elif condition_pct_direct is not None:
        condition_coverage = condition_pct_direct
    else:
        condition_coverage = 0

    return {
        "line_total": line_total,
        "line_covered": line_covered,
        "condition_total": condition_total,
        "condition_covered": condition_covered,
        "line_coverage": f"{line_coverage:.2f}",
        "condition_coverage": f"{condition_coverage:.2f}",
    }


def compile_and_test(work_dir, archive_path):
    """
    Compile source code เป็น baseline ก่อน แล้วรัน test + coverage โดยใช้
    เฉพาะ AI-generated test suite ผ่าน `-s <archive>` (ไม่แตะ dev tests)
    """

    compile_out, compile_err = run_cmd(
        "defects4j compile",
        cwd=work_dir
    )

    src_compile_ok = (
        "FAIL" not in compile_out
        and "BUILD FAILED" not in compile_out
        and "BUILD FAILED" not in compile_err
    )

    if not src_compile_ok:
        print("[ERROR] Base project compile failed, skipping test/coverage for this round.")
        print(f"[ERROR] compile stdout (tail): {compile_out[-800:]}")
        print(f"[ERROR] compile stderr (tail): {compile_err[-800:]}")
        return {
            "compile_ok": False,
            "compile_log": (compile_out + compile_err)[-1500:],
            "test_log": "",
            "coverage_log": "SKIPPED: base project compile failed",
            "coverage_stdout": "",
        }

    archive_arg = shlex.quote(archive_path)

    test_out, test_err = run_cmd(
        f"defects4j test -s {archive_arg}",
        cwd=work_dir
    )

    suite_compile_failed = (
        "BUILD FAILED" in test_out
        or "BUILD FAILED" in test_err
        or "error:" in test_out.lower()
    )

    if suite_compile_failed:
        print("[ERROR] AI test suite failed to compile via -s archive.")
        print(f"[ERROR] test stdout (tail): {test_out[-800:]}")
        print(f"[ERROR] test stderr (tail): {test_err[-800:]}")
        return {
            "compile_ok": False,
            "compile_log": (compile_out + compile_err)[-1500:],
            "test_log": (test_out + test_err)[-1500:],
            "coverage_log": "SKIPPED: AI test suite compile failed",
            "coverage_stdout": "",
        }

    # รัน coverage โดยใช้ suite เดียวกัน
    cov_out, cov_err = run_cmd(
        f"defects4j coverage -s {archive_arg}",
        cwd=work_dir
    )
    
    print("\n" + "=" * 60)
    print("[defects4j coverage]")
    print("=" * 60)
    if cov_out.strip():
        print(cov_out.rstrip())
    if cov_err.strip():
        print("[stderr]")
        print(cov_err.rstrip())
    print("=" * 60 + "\n")

    if not cov_out or not cov_out.strip():
        print("[WARN] 'defects4j coverage -s' returned empty stdout.")
        if cov_err and cov_err.strip():
            print(f"[WARN] coverage stderr (tail): {cov_err[-800:]}")
        else:
            print("[WARN] coverage stderr is also empty — command may have produced no output at all.")

    return {
        "compile_ok": True,
        "compile_log": (compile_out + compile_err)[-1500:],
        "test_log": (test_out + test_err)[-1500:],
        "coverage_log": (cov_out + cov_err)[-1500:],
        # เก็บ raw stdout ของ coverage ไว้ parse โดยตรง (ไม่มี coverage.xml)
        "coverage_stdout": cov_out + "\n" + cov_err,
    }


def load_progress():
    if os.path.exists(PROGRESS_FILE):
        with open(PROGRESS_FILE) as f:
            return json.load(f)

    return {
        "completed": [],
        "tokens_used_today": 0,
        "date": ""
    }

def validate_java_test(code):
    code = code.strip()

    if not code:
        return False, "Empty test code"

    if code.startswith("```") or "```" in code:
        return False, "Contains markdown code fence"

    if "public class" not in code:
        return False, "No test class found"

    if "@Test" not in code:
        return False, "No JUnit @Test found"

    if "org.junit.Test" not in code:
        return False, "Not JUnit 4"

    if "org.junit.jupiter" in code:
        return False, "JUnit 5 import detected"

    if code.count("{") != code.count("}"):
        return False, "Unbalanced braces"

    if code.count("(") != code.count(")"):
        return False, "Unbalanced parentheses"

    if not re.search(r"class\s+\w+Test\b", code):
        return False, "Test class name does not end with Test"

    return True, "OK"

def save_progress(progress):
    with open(PROGRESS_FILE, "w") as f:
        json.dump(progress, f, indent=2)


def check_quota(progress, estimated_tokens=35000):
    today = str(datetime.date.today())
    if progress.get("date") != today:
        progress["date"] = today
        progress["tokens_used_today"] = 0
    return (progress["tokens_used_today"] + estimated_tokens) < DAILY_TOKEN_LIMIT


def append_result_csv(row):
    file_exists = os.path.exists(RESULTS_CSV)
    with open(RESULTS_CSV, "a", newline="", encoding="utf-8") as f:
        writer = csv.DictWriter(f, fieldnames=row.keys())
        if not file_exists:
            writer.writeheader()
        writer.writerow(row)


# ============================================================
# MAIN PIPELINE
# ============================================================
def make_failed_result(
    project,
    bug_id,
    class_name,
    reason,
    tokens_used=0,
):
    return {
        "project": project,
        "bug_id": bug_id,
        "class_name": class_name,
        "model": MODEL_NAME,
        "status": "failed_generation",
        "failure_reason": reason,
        "compile_ok": False,
        "line_total": "",
        "line_covered": "",
        "line_coverage": "N/A",
        "condition_total": "",
        "condition_covered": "",
        "condition_coverage": "N/A",
        "refinement_rounds": 0,
        "tokens_used": tokens_used,
        "fault_detected": False,
        "output_path": "",
        "coverage_source": "No valid AI test generated",
        "ai_tests_run": "",
    }

def process_bug(project, bug_id, progress):
    """ประมวลผล 1 bug ให้ครบวงจร (checkout -> AI generate -> test -> coverage)"""
    work_dir = os.path.join(WORK_BASE, f"bug_{bug_id}")
    print(f"\n{'='*60}\nProcessing {project}-{bug_id}b\n{'='*60}")

    # 1. Checkout
    if not os.path.exists(work_dir):
        ok = checkout_bug(project, bug_id, work_dir)
        if not ok:
            print(f"[SKIP] Checkout failed for bug {bug_id}")
            return None
    else:
        print(f"[INFO] Working dir already exists, reusing: {work_dir}")

    # 2. หา class ที่มี defect
    classes = get_modified_classes(work_dir)
    if not classes:
        print(f"[SKIP] No modified classes found for bug {bug_id}")
        return None

    src_dir = get_src_dir(work_dir)

    total_tokens_used = 0
    class_results = []

    for class_name in classes:
        print(f"\n[INFO] Processing modified class: {class_name}")
        source = read_source(work_dir, src_dir, class_name)
        if source is None:
            print(f"[WARN] Source not found for {class_name}, skip")
            continue

        # เช็ค quota ก่อนยิง
        if not check_quota(progress):
            print("[STOP] ใกล้ครบโควต้าวันนี้แล้ว หยุดไว้ก่อน รันสคริปต์นี้ใหม่พรุ่งนี้")
            return "QUOTA_EXCEEDED"

        # 3. เรียก AI รอบแรก
        print(f"[INFO] Sending generation request for: {class_name}")
        prompt = build_prompt(project, bug_id, class_name, source)
        try:
            ai_response, tokens = call_ai(prompt)
        except Exception as e:
            print(f"[ERROR] API call failed: {e}")
            continue
        total_tokens_used += tokens
        progress["tokens_used_today"] += tokens
        time.sleep(API_SLEEP_SECONDS)

        java_code = extract_java_code(ai_response)

        valid, reason = validate_java_test(java_code)

        if not valid:
            print(f"[ERROR] Invalid AI test: {reason}")
            print("[ERROR] No valid AI test was produced for this class.")

            failed_row = make_failed_result(
                project=project,
                bug_id=bug_id,
                class_name=class_name,
                reason=reason,
                tokens_used=total_tokens_used,
            )

            append_result_csv(failed_row)
            save_progress(progress)

            class_results.append(failed_row)

            # ไม่ใช้ test เดิม และไม่เข้าสู่ coverage
            continue

        # Build the external test suite archive (-s) — does NOT touch dev tests
        archive_path, java_file_path = build_test_suite_archive(
            work_dir, project, bug_id, class_name, java_code
        )

        # Export ONLY the generated JUnit file (for record-keeping).
        output_path = export_generated_test(
            java_file_path, project, bug_id, class_name
        )
        print(f"[OUTPUT] Generated test exported to: {output_path}")
        print(f"[OUTPUT] Test suite archive: {archive_path}")

        # 4. Compile + test + coverage รอบแรก (ใช้ -s archive)
        result = compile_and_test(work_dir, archive_path)
        cov = parse_coverage(result["coverage_stdout"])
        ai_tests_run = list_runnable_tests(Path(work_dir) / ".ai_suite_src")

        # 5. Refinement loop (ถ้า compile fail หรือ coverage ยังต่ำ)
        refine_round = 0
        while refine_round < MAX_REFINEMENT_ROUNDS:
            if result["compile_ok"] and cov["line_coverage"] != "N/A":
                try:
                    if float(cov["line_coverage"]) >= TARGET_LINE_COVERAGE:
                        break  # coverage ดีพอแล้ว ไม่ต้อง refine ต่อ
                except ValueError:
                    pass

            if not check_quota(progress):
                print("[STOP] โควต้าหมดระหว่าง refinement")
                break

            refine_round += 1
            print(f"  -> Refinement round {refine_round} for {class_name}")

            uncovered_info = result["coverage_log"] if not result["compile_ok"] else result["compile_log"]
            refine_prompt = build_refine_prompt(uncovered_info, java_code)
            try:
                ai_response, tokens = call_ai(refine_prompt)
            except Exception as e:
                print(f"[ERROR] Refinement API call failed: {e}")
                break
            total_tokens_used += tokens
            progress["tokens_used_today"] += tokens
            time.sleep(API_SLEEP_SECONDS)

            java_code = extract_java_code(ai_response)

            # สร้าง archive ใหม่ทับของเดิมด้วย test code ที่ refine แล้ว
            archive_path, java_file_path = build_test_suite_archive(
                work_dir, project, bug_id, class_name, java_code
            )

            # Export ONLY the refined JUnit file.
            output_path = export_generated_test(
                java_file_path, project, bug_id, class_name
            )
            print(f"[OUTPUT] Refined test exported to: {output_path}")

            result = compile_and_test(work_dir, archive_path)
            cov = parse_coverage(result["coverage_stdout"])
            ai_tests_run = list_runnable_tests(Path(work_dir) / ".ai_suite_src")

        row = {
            "project": project,
            "bug_id": bug_id,
            "class_name": class_name,
            "model": MODEL_NAME,

            "compile_ok": result["compile_ok"],

            "line_total": cov["line_total"],
            "line_covered": cov["line_covered"],
            "line_coverage": cov["line_coverage"],

            "condition_total": cov["condition_total"],
            "condition_covered": cov["condition_covered"],
            "condition_coverage": cov["condition_coverage"],

            "refinement_rounds": refine_round,
            "tokens_used": total_tokens_used,

            "fault_detected": "Failing tests" in result["test_log"],

            "output_path": output_path if "output_path" in locals() else "",
            "coverage_source": "AI-generated test suite via defects4j -s archive (dev tests untouched)",
            "ai_tests_run": ";".join(ai_tests_run),
        }
        class_results.append(row)
        append_result_csv(row)   # เขียนทันที กัน Ctrl+C แล้วข้อมูลหาย
        save_progress(progress)

    return class_results


def main():
    progress = load_progress()
    bug_ids = get_bug_ids(PROJECT)

    # จำกัดจำนวน Bug ที่ต้องการทดสอบ
    MAX_BUGS = 176
    bug_ids = bug_ids[:MAX_BUGS]

    print(f"Project: {PROJECT}")
    print(f"Testing {len(bug_ids)} bugs: {bug_ids}")

    for bug_id in bug_ids:
        if bug_id in progress.get("completed", []):
            print(f"Bug {bug_id} already completed, skipping...")
            continue

        try:
            result = process_bug(PROJECT, bug_id, progress)

            progress.setdefault("completed", []).append(bug_id)
            save_progress(progress)

        except Exception as e:
            print(f"Error processing Bug {bug_id}: {e}")


if __name__ == "__main__":
    main()