# Zest – Compress-20 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 284736 (valid 59.81%) |
| Cycles | 11 |
| Corpus | 62 |
| Zest branch coverage (total / valid) | 213 / 210 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 62 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 70 / 214 = 32.71% |
| Branch coverage | 34 / 114 = 29.82% |
| Test suite length (statements) | 62 |
| Mutation score | 493 / 587 = 83.99% |
| Fuzz time | 122s |
| Pipeline time | 226s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/20/t12001/Compress-20f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_20/b120_r1/src/org/apache/commons/compress/archivers/cpio/CpioArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_20/b120_r1`
