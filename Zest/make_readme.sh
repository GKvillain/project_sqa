#!/usr/bin/env bash
# Writes Zest/README.md with a results table computed from Zest/zest_results.csv
cd "$(dirname "$0")"
CSV=zest_results.csv
summ() { awk -F, -v only="$1" 'NR>1 && (only=="" || $1==only) { k=$1 SUBSEP $2; r++; if($7=="yes"){dr++; db[k]=1}; b[k]=1; l+=$5; br+=$6; t+=$9; s+=$12; if($11 ~ /^[0-9.]+$/){m+=$11; mn++} }
END{ for(k in b){nb++; if(k in db) nd++}
  printf "| %s | %d | %d | %d (%.1f%%) | %.1f%% | %.2f | %.2f | %.2f | %.1f | %.0f |\n", (only==""?"**รวม**":only), nb, r, nd, 100*nd/nb, 100*dr/r, l/r, br/r, (mn?m/mn:0), t/r, s/r }' "$CSV"; }
DETECTED=$(awk -F, 'NR>1 && $7=="yes" {print $1"-"$2}' "$CSV" | sort -t- -k1,1 -k2,2n -u | tr '\n' ' ')
{
cat <<'EOF'
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

EOF
echo "| Project | Bugs | Runs | Bug ที่ตรวจพบ | รันที่ตรวจพบ | Line (%) | Branch (%) | Mutation (%) | จำนวนเทสต์ | Statements (executions) |"
echo "|---|---|---|---|---|---|---|---|---|---|"
for p in $(tail -n +2 "$CSV" | cut -d, -f1 | sort -u); do summ "$p"; done; summ ""
echo
echo "- **Bug ที่ตรวจพบ** = ตรวจพบในอย่างน้อย 1 รัน (test ผ่านบน fixed และ fail บน buggy)"
echo "- Line/Branch = coverage ของคลาสที่ถูกแก้ใน bug นั้น (Defects4J \`run_coverage\`) เฉลี่ยทุกรัน"
echo "- Mutation = Major mutation score เฉลี่ยทุกรัน (ไม่นับรันที่ไม่มี mutant)"
echo
echo "**Bug ที่ตรวจพบ:** $DETECTED"
cat <<'EOF'

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
EOF
} > README.md
echo "wrote $(pwd)/README.md"
