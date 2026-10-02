# Zest – Compress-36 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 224979 (valid 53.66%) |
| Cycles | 21 |
| Corpus | 21 |
| Zest branch coverage (total / valid) | 154 / 151 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 21 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 14 / 535 = 2.62% |
| Branch coverage | 5 / 305 = 1.64% |
| Test suite length (statements) | 21 |
| Mutation score | 949 / 957 = 99.16% |
| Fuzz time | 122s |
| Pipeline time | 225s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/36/t12001/Compress-36f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_36/b120_r1/src/org/apache/commons/compress/archivers/sevenz/SevenZFile_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_36/b120_r1`
