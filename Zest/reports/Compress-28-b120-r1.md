# Zest – Compress-28 (budget 120s, run 1)

| Metric | Value |
|---|---|
| Driver | `zestd4j.AutoFuzz#fuzz` |
| Executions | 220206 (valid 83.37%) |
| Cycles | 17 |
| Corpus | 39 |
| Zest branch coverage (total / valid) | 197 / 194 |
| Zest failures (on fixed) | 0 |
| JUnit tests | 39 |
| Failing on fixed | 0 |
| Failing on buggy | 0 |
| Bug detection | Pass (triggering: 0) |
| **Fault detected** | **no** |
| Line coverage | 44 / 215 = 20.47% |
| Branch coverage | 13 / 124 = 10.48% |
| Test suite length (statements) | 39 |
| Mutation score | 296 / 332 = 89.16% |
| Fuzz time | 122s |
| Pipeline time | 209s |

## Failing tests on buggy version

(none)

- Suite: `/home/user/suites/Compress/zest/28/t12001/Compress-28f-zest.12001.tar.bz2`
- Tests: `/home/user/zest-d4j-main/zest-d4j-main/gen/Compress_28/b120_r1/src/org/apache/commons/compress/archivers/tar/TarArchiveInputStream_Zest_Test.java`
- Corpus: `/home/user/zest-d4j-main/zest-d4j-main/out/Compress_28/b120_r1`
