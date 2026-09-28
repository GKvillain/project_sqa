# Automated AI Test Generation for Defects4J

โปรแกรมสำหรับสร้าง **JUnit 4 Test ด้วย AI** และทดสอบบน [Defects4J](https://github.com/rjust/defects4j) โดยใช้ KKU IntelSphere API

ระบบจะ:

- Checkout Bug ของ Defects4J
- ให้ AI สร้าง JUnit 4 Test
- Compile และ Run Test
- วัด Line / Condition Coverage
- ตรวจสอบ Fault Detection
- บันทึกผลเป็น CSV
- Resume การทำงานจากจุดเดิมได้

## 1. Requirements

ต้องติดตั้ง:

- Python 3
- Java / JDK
- Defects4J
- Apache Ant

ติดตั้ง Python dependencies:

```bash
python3 -m venv .venv
source .venv/bin/activate

pip install openai python-dotenv
```

## 2. ตั้งค่า API Key

สร้างไฟล์ `.env`:

```env
KKU_API_KEY=YOUR_API_KEY
```

**ห้าม commit `.env` ขึ้น GitHub**

## 3. ตั้งค่า Project และ Model

เปิด `run_ai_testgen.py` แล้วแก้:

```python
PROJECT = "Chart"
MODEL_NAME = "gemini-3.7-flash"
BASE_URL = "https://gen.ai.kku.ac.th/api/v1"
```

`PROJECT` ต้องเป็น Project ที่มีอยู่ใน Defects4J และ `MODEL_NAME` ต้องเป็น Model ที่สามารถใช้งานผ่าน KKU IntelSphere ได้

## 4. Run

เปิด Virtual Environment:

```bash
source .venv/bin/activate
```

จากนั้น:

```bash
python3 run_ai_testgen.py
```

## 5. ผลลัพธ์

Generated Tests:

```text
results/<model>/<project>/
```

ผลการทดลอง:

```text
results_<model>_<project>.csv
```

Progress:

```text
progress_<model>_<project>.json
```

หากโปรแกรมหยุดระหว่างทาง สามารถรันคำสั่งเดิมอีกครั้งเพื่อทำต่อจาก Bug ที่ยังไม่เสร็จได้

## 6. เปลี่ยนจำนวน Bug

แก้ใน `run_ai_testgen.py`:

```python
MAX_BUGS = 10
```

เช่น `10` หมายถึงประมวลผล 10 Bugs แรก
