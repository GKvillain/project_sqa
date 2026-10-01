# EvoSuite GWO Runner

สคริปต์สำหรับรัน **EvoSuite ที่ใช้ GWO (Grey Wolf Optimizer)** กับโปรเจกต์ใน **Defects4J** แบบอัตโนมัติหลาย Bug

## Requirements

ต้องติดตั้งและตั้งค่า:

- Java
- Defects4J
- EvoSuite ที่มี `GWO`
- Defects4J projects ที่ checkout ไว้ใน:

```text
~/d4j-workspace/
```

และ EvoSuite JAR ต้องอยู่ที่:

```text
~/evosuite-gwo/evosuite/master/target/evosuite-master-1.2.1-SNAPSHOT.jar
```

โครงสร้างโปรเจกต์ตัวอย่าง:

```text
~/d4j-workspace/
├── Chart-1b/
├── Chart-2b/
├── Chart-3b/
├── Lang-1b/
└── ...
```

## Installation

สร้างไฟล์:

```bash
nano run_all_gwo.sh
```

วาง script แล้วบันทึก จากนั้นเพิ่ม permission:

```bash
chmod +x run_all_gwo.sh
```

## Usage

```bash
./run_all_gwo.sh <PROJECT> <START_BUG> <END_BUG>
```

ตัวอย่าง:

```bash
./run_all_gwo.sh Chart 1 26
```

```bash
./run_all_gwo.sh Lang 1 65
```

```bash
./run_all_gwo.sh Math 1 106
```

```bash
./run_all_gwo.sh Time 1 27
```

```bash
./run_all_gwo.sh Closure 1 133
```

## Output

ผลลัพธ์จะถูกเก็บไว้ที่:

```text
~/evosuite-results/
```

ตัวอย่าง:

```text
~/evosuite-results/
└── Chart-1b/
    └── GWO/
        └── org_example_ClassName/
            ├── terminal.log
            └── evosuite-tests/
                └── org_example_ClassName_ESTest.java
```

แต่ละ Target class จะมี `terminal.log` สำหรับดูผลการทำงานของ EvoSuite

## Configuration

ค่าหลักใน script:

```bash
WORKSPACE="$HOME/d4j-workspace"
RESULT_BASE="$HOME/evosuite-results"
EVOSUITE_JAR="$HOME/evosuite-gwo/evosuite/master/target/evosuite-master-1.2.1-SNAPSHOT.jar"
```

Search budget ปัจจุบัน:

```bash
-Dsearch_budget=60
```

Algorithm:

```bash
-Dalgorithm=GWO
```

หากต้องการเปลี่ยนเวลา search ให้แก้ค่า `60` ใน script เช่น:

```bash
-Dsearch_budget=120
```

## Notes

- Script จะ compile แต่ละ Bug ก่อนรัน EvoSuite
- ใช้ `classes.modified` เพื่อหา Target classes
- หากพบ test ที่สร้างไว้แล้ว จะข้าม Target นั้น
- หาก compile ไม่ผ่าน จะข้าม Bug นั้น
- สามารถใช้กับ Defects4J project หลายตัวได้โดยไม่ต้องแก้ชื่อ Project ใน script
