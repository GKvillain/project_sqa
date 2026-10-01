# Zest (JQF) — Semantic Fuzzing บน Defects4J

ส่วนนี้ทดลองใช้อัลกอริทึม **Zest** (Padhye et al., *Semantic Fuzzing with Zest*, ISSTA 2019) ผ่านเครื่องมือ [JQF](https://github.com/rohanpadhye/JQF)
สร้าง unit test อัตโนมัติให้ bug ใน Defects4J แล้ววัด coverage, fault detection และ mutation score

## วิธีการ

1. **สร้าง fuzz driver** สำหรับแต่ละ bug อัตโนมัติ (`auto_bug.sh` + `src/common/DriverGen2.java`)
   - diff เวอร์ชัน buggy/fixed หา method ที่ patch แก้ แล้วให้ driver เรียก method เหล่านั้นก่อน
   - เรียกผ่าน reflection harness (`src/common/Harness.java`) ได้ทั้ง static/instance method และ constructor
   - Lang-1 ใช้ driver + generator ที่เขียนเอง (`src/Lang_1/`)
2. **Fuzz ด้วย Zest** บน fixed version เป็นเวลา **120 วินาที/รัน, 2 รัน/bug** (`jqf-zest`)
3. **Replay corpus** (`jqf-repro`) แล้วบันทึกผลลัพธ์ของ fixed version เป็น expected value → JUnit regression test (`HarnessTestWriter`)
4. ลบ test ที่ fail บน fixed version ด้วย `fix_test_suite.pl` ของ Defects4J
5. **วัดผลด้วย Defects4J**: `run_bug_detection.pl`, `run_coverage.pl`, `run_mutation.pl`

ทั้งหมดรันด้วยคำสั่งเดียว: `PROJECTS="Lang" BUDGET=120 RUNS=2 ./overnight.sh`

## ผลสรุป

| Project | Bugs | Runs | Bug ที่ตรวจพบ | รันที่ตรวจพบ | Line (%) | Branch (%) | Mutation (%) | จำนวนเทสต์ | Statements (executions) |
|---|---|---|---|---|---|---|---|---|---|
| Chart | 26 | 52 | 10 (38.5%) | 38.5% | 32.26 | 16.69 | 82.59 | 49.8 | 117506 |
| Codec | 18 | 36 | 9 (50.0%) | 41.7% | 70.44 | 62.67 | 63.55 | 104.1 | 109470 |
| Lang | 61 | 122 | 23 (37.7%) | 32.8% | 33.52 | 28.25 | 83.76 | 67.8 | 182824 |
| **รวม** | 105 | 210 | 42 (40.0%) | 35.7% | 39.54 | 31.29 | 79.95 | 69.6 | 154075 |

- **Bug ที่ตรวจพบ** = ตรวจพบในอย่างน้อย 1 รัน (test ผ่านบน fixed และ fail บน buggy)
- Line/Branch = coverage ของคลาสที่ถูกแก้ใน bug นั้น (Defects4J `run_coverage`) เฉลี่ยทุกรัน
- Mutation = Major mutation score เฉลี่ยทุกรัน (ไม่นับรันที่ไม่มี mutant)

**Bug ที่ตรวจพบ:** Chart-5 Chart-8 Chart-10 Chart-14 Chart-15 Chart-17 Chart-18 Chart-19 Chart-22 Chart-23 Codec-4 Codec-7 Codec-9 Codec-11 Codec-12 Codec-13 Codec-15 Codec-16 Codec-17 Lang-1 Lang-5 Lang-6 Lang-8 Lang-9 Lang-10 Lang-11 Lang-15 Lang-17 Lang-23 Lang-27 Lang-29 Lang-34 Lang-42 Lang-46 Lang-47 Lang-52 Lang-55 Lang-58 Lang-59 Lang-60 Lang-61 Lang-63 

## โครงสร้างโฟลเดอร์

| path | เนื้อหา |
|---|---|
| `zest_results.csv` | ผลรายรัน (1 แถว = 1 bug × 1 รัน) ในรูปแบบที่ `plot_comparison_diagrams.py` อ่านได้ |
| `overnight.sh` | รันทุก bug ของ project ที่กำหนด (resume ได้, บันทึก bug ที่ข้าม) |
| `auto_bug.sh` | checkout + compile + สร้าง fuzz driver อัตโนมัติ |
| `zest_pipeline.sh` | 1 bug × 1 รัน: fuzz → replay → เขียน JUnit → วัดผลด้วย Defects4J |
| `zest_report.sh`, `split_results.sh` | สรุปผลเป็น CSV/Markdown |
| `src/common/` | `DriverGen2`, `Harness`, `HarnessTestWriter`, `InputLog`, ... |
| `src/<Project>_<Bug>/` | driver ที่สร้างให้แต่ละ bug (`AutoFuzz.java`, `targets.txt`) |
| `bugs/*.env` | ค่าตั้งของแต่ละ bug |
| `suites/` | **test suite ที่ Zest สร้าง** (`<Project>-<Bug>f-zest.<TID>.tar.bz2`) |
| `reports/` | รายงานแยกแต่ละรัน |
| `patches/jqf-driver.patch` | จุดที่แก้ใน JQF (ชื่อ ASM jar ใน `scripts/jqf-driver.sh`) |
| `ENVIRONMENT.txt` | เวอร์ชัน Java / JQF / Defects4J ที่ใช้ |

## รันซ้ำ

```bash
# ต้องมี: Java 11, Defects4J, JQF (build แล้วที่ ~/jqf), TZ=America/Los_Angeles
git clone https://github.com/rohanpadhye/JQF ~/jqf && cd ~/jqf && mvn package -DskipTests
git apply /path/to/Zest/patches/jqf-driver.patch
mkdir -p ~/zest-d4j && cp -r Zest/* ~/zest-d4j/ && cd ~/zest-d4j
export TZ=America/Los_Angeles
PROJECTS="Lang Chart Cli Codec" BUDGET=120 RUNS=2 nohup ./overnight.sh > ~/results/overnight.log 2>&1 &
```

## ข้อจำกัด

- Driver ส่วนใหญ่สร้างอัตโนมัติด้วย reflection บางคลาสต้องการ input ที่มีโครงสร้าง ทำให้ input ที่ valid มีสัดส่วนต่ำ
- Oracle เป็นแบบ regression (ผลของ fixed version) จึงตรวจพบ bug ได้เมื่อพฤติกรรมของ buggy ต่างจาก fixed บน input ที่ Zest สร้างเท่านั้น
- Coverage วัดเฉพาะคลาสที่ถูกแก้ใน bug นั้น
- 120 วินาที × 2 รันต่อ bug ผลจึงมีความสุ่ม บาง bug ตรวจพบเพียง 1 ใน 2 รัน
