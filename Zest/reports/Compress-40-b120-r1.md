# Zest – Compress-40 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 229876 (valid 72.86%) |
| Cycles | 29 |
| Corpus | 17 |
| Zest branch coverage (total / valid) | 149 / 146 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 17 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 26 / 55 = 47.27% |
| Branch coverage | 9 / 22 = 40.91% |
| Test suite length (statements) | 17 |
| Mutation score | 86 / 144 = 59.72% |
| Fuzz time | 120s |
| Pipeline time | 216s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/40/t12001/Compress-40f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_40/b120_r1/src/org/apache/commons/compress/utils/BitInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_40/b120_r1`
