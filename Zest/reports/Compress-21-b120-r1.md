# Zest – Compress-21 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 560148 (valid 0.00%) |
| Cycles | 181 |
| Corpus | 7 |
| Zest branch coverage (total / valid) | 103 / 0 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 7 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 9 / 389 = 2.31% |
| Branch coverage | 0 / 134 = 0.00% |
| Test suite length (statements) | 7 |
| Mutation score | 504 / 509 = 99.02% |
| Fuzz time | 123s |
| Pipeline time | 216s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/21/t12001/Compress-21f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_21/b120_r1/src/org/apache/commons/compress/archivers/sevenz/SevenZOutputFile_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_21/b120_r1`
