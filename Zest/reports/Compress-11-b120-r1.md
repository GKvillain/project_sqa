# Zest – Compress-11 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 260606 (valid 50.31%) |
| Cycles | 32 |
| Corpus | 14 |
| Zest branch coverage (total / valid) | 129 / 126 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 14 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 9 / 70 = 12.86% |
| Branch coverage | 5 / 48 = 10.42% |
| Test suite length (statements) | 14 |
| Mutation score | 59 / 61 = 96.72% |
| Fuzz time | 122s |
| Pipeline time | 215s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/11/t12001/Compress-11f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_11/b120_r1/src/org/apache/commons/compress/archivers/ArchiveStreamFactory_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_11/b120_r1`
